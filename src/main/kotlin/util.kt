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

fun getQuantityOrder(client: UMFuturesClientImpl, getQuantityOrderArgs: GetQuantityOrderArgs): Double? {
    val availableCoinBalance = Json.decodeFromString<List<FuturesAccountBalance>>(
        client.account().futuresAccountBalance(LinkedHashMap())
    ).find { it.asset == getQuantityOrderArgs.futuresAccountBalanceParam["asset"] }?.availableBalance?.toDouble()

    return availableCoinBalance?.times(getQuantityOrderArgs.leverageParam["leverage"] as Int)
        ?.times(getQuantityOrderArgs.regularFee)
}