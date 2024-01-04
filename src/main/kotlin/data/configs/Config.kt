package data.configs

object Config {
    val leverage = LinkedHashMap<String, Any>().also { it["symbol"] = "BTCUSDT"; it["leverage"] = 20 }
    val margin = LinkedHashMap<String, Any>().also { it["symbol"] = "BTCUSDT"; it["marginType"] = "ISOLATED" }
    val futuresAccountBalance = LinkedHashMap<String, Any>().also { it["asset"] = "USDT" }
    const val regularFee = 0.95
}