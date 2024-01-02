import com.binance.connector.futures.client.impl.UMFuturesClientImpl
import data.configs.Config
import data.dto.GetQuantityOrderArgs
import data.dto.SetProspectsArgs
import data.models.FuturesAccountBalance
import data.singletons.UMFutures
import kotlinx.serialization.json.Json

fun getQuantityOrder(client: UMFuturesClientImpl, getQuantityOrderArgs: GetQuantityOrderArgs) {
    val availableCoinBalance = Json.decodeFromString<List<FuturesAccountBalance>>(
        client.account().futuresAccountBalance(LinkedHashMap())
    ).find { it.asset == getQuantityOrderArgs.futuresAccountBalanceParam["asset"] }

    println(availableCoinBalance)
}

fun main(args: Array<String>) {
    val client = UMFutures.client

    setProspects(client, SetProspectsArgs(Config.leverage, Config.margin))
    getQuantityOrder(client, GetQuantityOrderArgs(Config.futuresAccountBalance))
}