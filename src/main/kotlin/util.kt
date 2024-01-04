import com.binance.connector.futures.client.impl.UMFuturesClientImpl
import data.dto.CalculateQuantityOrderArgs
import data.dto.DepsForQuantityCalculationArgs
import data.dto.QuantityDeps
import data.dto.SetRiskVariablesArgs
import data.models.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import java.math.RoundingMode
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

fun filter(key: String, value: Any): LinkedHashMap<String?, Any?> {
    return LinkedHashMap<String?, Any?>().also {
        it[key] = value
    }
}

fun filter(filterLambda: (LinkedHashMap<String?, Any?>) -> Unit): LinkedHashMap<String?, Any?> {
    return LinkedHashMap<String?, Any?>().also(filterLambda)
}

fun setRiskVariables(client: UMFuturesClientImpl, setProspectsArgs: SetRiskVariablesArgs) = runBlocking {
    val tasks = listOf(
        async {
            runCatching {
                client.account().changeMarginType(filter {
                    it["symbol"] = setProspectsArgs.symbol
                    it["marginType"] = setProspectsArgs.marginType
                })
            }.also { handleBinanceError(it) }
        },

        async {
            client.account().changeInitialLeverage(filter {
                it["symbol"] = setProspectsArgs.symbol
                it["leverage"] = setProspectsArgs.leverage
            })
        }
    )

    tasks.awaitAll()
}

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