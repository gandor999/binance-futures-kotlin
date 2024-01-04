package data.dto

data class QuantityDeps(
    val markPrice: Double,
    val tickerHighPrice: Double,
    val availableCoinBalance: Double?,
    val commissionTake: Double
)