import com.binance.connector.futures.client.impl.UMFuturesClientImpl
import secrets.BinanceSecrets
import java.io.File
import java.util.LinkedHashMap

fun main(args: Array<String>) {

    val parameter = LinkedHashMap<String, Any>()
    parameter["symbol"] = "BTCUSDT"

    val client = UMFuturesClientImpl(BinanceSecrets.getAPIKey(), BinanceSecrets.getAPISecret())
    val exchangeInfo = client.market().exchangeInfo()
    val ticker24H = client.market().ticker24H(parameter)

    val exchangeInfoLog = File("logs/exchange-info.json")
    val ticker24HLog = File("logs/ticker-24H.json")

    exchangeInfoLog.writeText(exchangeInfo)
    ticker24HLog.writeText(ticker24H)
}