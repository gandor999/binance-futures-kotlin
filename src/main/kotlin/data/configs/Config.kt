package data.configs

object Config {
    val leverage = LinkedHashMap<String, Any>().also { it["symbol"] = "BTCUSDT"; it["leverage"] = 2 }
    val margin = LinkedHashMap<String, Any>().also { it["symbol"] = "BTCUSDT"; it["marginType"] = "ISOLATED" }
    val futuresAccountBalance = LinkedHashMap<String, Any>().also { it["asset"] = "USDT" }
}