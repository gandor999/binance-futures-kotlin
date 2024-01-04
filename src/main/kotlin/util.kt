import com.binance.connector.futures.client.impl.UMFuturesClientImpl
import data.dto.GetQuantityOrderArgs
import data.dto.SetProspectsArgs
import data.models.BinanceException
import data.models.FuturesAccountBalance
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

fun handleError(runCatchObject: Result<String>) {
    runCatchObject.onFailure { error ->
        val deserializedError = Json.decodeFromString<BinanceException>(error.message.toString())

        when (deserializedError.code) {
            -4046 -> println(deserializedError.msg)

            else -> throw Exception(error.message)
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

fun setRiskVariables(client: UMFuturesClientImpl, setProspectsArgs: SetProspectsArgs) = runBlocking {
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
    val (
        leverage,
        commissionTake,
        percentage,
        markPrice,
        tickerHighPrice,
        availableCoinBalance
    ) = calculateQuantityOrder

    if (availableCoinBalance != null) return ((availableCoinBalance * (percentage / 100) * .9996) / ((tickerHighPrice * (1 + commissionTake) / leverage) + (abs(
        min(
            0.00,
            1 * (markPrice - tickerHighPrice)
        )
    )))).toBigDecimal().setScale(2, RoundingMode.DOWN).toDouble()

    return 0.00
}