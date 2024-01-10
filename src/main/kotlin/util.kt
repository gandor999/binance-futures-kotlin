import com.binance.connector.futures.client.impl.UMFuturesClientImpl
import data.dto.*
import data.schemas.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import java.io.File
import java.math.RoundingMode
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.math.abs
import kotlin.math.min

fun handleBinanceError(runCatchObject: Result<String>) {
    runCatchObject.onFailure { error ->
        val deserializedError = Json.decodeFromString<BinanceException>(error.message.toString())

        when (deserializedError.code) {
            -4046 -> println(deserializedError.msg)
        }
    }
}

fun saveToCache(filename: String, content: String) {
    val cachePath = Paths.get("src/main/kotlin/data/cache")
    if (!Files.isDirectory(cachePath)) Files.createDirectory(cachePath)

    val cacheFile = File("src/main/kotlin/data/cache/$filename")

    if (!cacheFile.exists()) {
        cacheFile.createNewFile()
        cacheFile.writeText(content)
    } else cacheFile.writeText(content)
}

fun readCache(filename: String): String {
    return File(filename).readText(Charsets.UTF_8)
}

fun filter(key: String, value: Any): LinkedHashMap<String?, Any?> {
    return LinkedHashMap<String?, Any?>().also {
        it[key] = value
    }
}

fun filter(filterLambda: (LinkedHashMap<String?, Any?>) -> Unit): LinkedHashMap<String?, Any?> {
    return LinkedHashMap<String?, Any?>().also(filterLambda)
}

/**
 *  Sets the leverage level and the margin type that you would like to work on
 *
 *  @param  client the USD-M Futures client
 *  @param  setRiskVariablesArgs the args to be passed to this function as a data class object
 *
 *  @author Geodor A. Ruales
 */
fun setRiskVariables(client: UMFuturesClientImpl, setRiskVariablesArgs: SetRiskVariablesArgs) = runBlocking {
    val tasks = listOf(
        async {
            runCatching {
                client.account().changeMarginType(filter {
                    it["symbol"] = setRiskVariablesArgs.symbol
                    it["marginType"] = setRiskVariablesArgs.marginType
                })
            }.also { handleBinanceError(it) }
        },

        async {
            client.account().changeInitialLeverage(filter {
                it["symbol"] = setRiskVariablesArgs.symbol
                it["leverage"] = setRiskVariablesArgs.leverage
            })
        }
    )

    tasks.awaitAll()
}

/**
 *  Gets the dependencies needed for the calculation of the quantity that we can open a futures order
 *
 *  @param  client the USD-M Futures client
 *  @param  depsForQuantityCalculationArgs the args to be passed to this function as a data class object
 *
 *  @author Geodor A. Ruales
 */
fun getDepsForQuantityCalculation(
    client: UMFuturesClientImpl,
    depsForQuantityCalculationArgs: DepsForQuantityCalculationArgs
): QuantityDeps = runBlocking {
    val (
        assetToWorkWith,
        symbol
    ) = depsForQuantityCalculationArgs

    var availableCoinBalance: Double? = 0.00
    var markPrice = 0.00
    var tickerHighPrice = 0.00
    var commissionTake = 0.00

    val tasks = listOf(
        async {
            availableCoinBalance = Json.decodeFromString<List<FuturesAccountBalance>>(
                client.account().futuresAccountBalance(LinkedHashMap())
            ).find { it.asset == assetToWorkWith }?.availableBalance?.toDouble() // can't seem to filter using the api
        },

        async {
            markPrice =
                Json.decodeFromString<MarkPrice>(
                    client.market().markPrice(filter("symbol", symbol))
                ).markPrice.toDouble()
        },

        async {
            tickerHighPrice =
                Json.decodeFromString<Ticker24H>(
                    client.market().ticker24H(filter("symbol", symbol))
                ).highPrice.toDouble()
        },

        async {
            commissionTake = Json.decodeFromString<CommissionRates>(
                client.account().getCommissionRate(filter("symbol", symbol))
            ).takerCommissionRate.toDouble()
        }
    )

    tasks.awaitAll()

    return@runBlocking QuantityDeps(markPrice, tickerHighPrice, availableCoinBalance, commissionTake)
}

/**
 *  Calculates the quantity of how much we can open a futures order
 *
 *  @param  calculateQuantityOrder the variables that we get from getDepsForQuantityCalculation()
 *
 *  @author Geodor A. Ruales
 */
fun calculateQuantityOrder(calculateQuantityOrder: CalculateQuantityOrderArgs): Double {
    // Reminder: commissionTake will be commissionMake if Sell open order
    // How we got this formula: https://www.binance.com/en/support/faq/how-to-calculate-cost-required-to-open-a-position-in-perpetual-futures-contracts-87fa7ee33b574f7084d42bd2ce2e463b
    // Derive the formula in such a way that we want to find the "number of contracts" instead of the "cost" to get the closest amount of btc that we can use to open future trade

    // Take note that the formula below was tweaked a little so that it will only approach the maximum cost a little distance, the value of that distance is not very consistent, but it should be enough to make an open trade without much of any difference to the amount that a person would like to buy or sell with
    // Particularly, instead of using the order book "askPrice" we use the "tickerHighPrice" to get a little bit lower than the actual maximum cost in binance

    val (
        leverage,
        commissionTake,
        percentage,
        markPrice,
        tickerHighPrice,
        availableCoinBalance
    ) = calculateQuantityOrder

    if (availableCoinBalance != null) return ((availableCoinBalance * (percentage / 100) * (1 - commissionTake)) / ((tickerHighPrice * (1 + commissionTake) / leverage) + (abs(
        min(
            0.00,
            1 * (markPrice - tickerHighPrice)
        )
    )))).toBigDecimal().setScale(2, RoundingMode.DOWN).toDouble()

    return 0.00
}

/**
 *  Makes a new futures order
 *
 *  @param  makeNewOrderArgs the args to be passed to this function as a data class object
 *
 *  @author Geodor A. Ruales
 */
fun makeNewOrder(client: UMFuturesClientImpl, makeNewOrderArgs: MakeNewOrderArgs): String {
    val (
        symbol,
        side,
        type,
        quantity,
        closePosition
    ) = makeNewOrderArgs

    return client.account().newOrder(filter {
        val positionInformation = Json.decodeFromString<List<PositionInformation>>(
            client.account().positionInformation(filter { param ->
                param["symbol"] = symbol
            })
        )

        it["symbol"] = symbol
        it["side"] = side
        it["type"] = type
        it["quantity"] = quantity

        if (closePosition && positionInformation.isNotEmpty()) {
            it["quantity"] =
                positionInformation[0].positionAmt.toDouble() // TODO: add functionality to choose which position to close
            it["newOrderRespType"] = "RESULT"
            it["placeType"] = "position"
            it["positionSide"] = "BOTH"
            it["reduceOnly"] = true
        }
    })
}