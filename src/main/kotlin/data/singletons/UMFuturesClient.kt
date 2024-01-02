package data.singletons

import com.binance.connector.futures.client.impl.UMFuturesClientImpl
import data.secrets.BinanceSecrets

object UMFutures {
    val client = UMFuturesClientImpl(BinanceSecrets.getAPIKey(), BinanceSecrets.getAPISecret())
}