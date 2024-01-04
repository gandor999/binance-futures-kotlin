package data.dto

data class GetQuantityOrderArgs(
    val futuresAccountBalanceParam: LinkedHashMap<String, Any>,
    val leverageParam: LinkedHashMap<String, Any>,
    val regularFee: Double
)