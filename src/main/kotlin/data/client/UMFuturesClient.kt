package data.client

import com.binance.connector.futures.client.impl.UMFuturesClientImpl
import data.secrets.BinanceSecrets

object UMFutures {
    val client = UMFuturesClientImpl(
        BinanceSecrets.getTestAPIKey(),
        BinanceSecrets.getTestAPISecret(),
        "https://testnet.binancefuture.com"
    )
}