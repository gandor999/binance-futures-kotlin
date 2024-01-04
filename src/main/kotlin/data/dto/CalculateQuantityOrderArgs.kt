package data.dto

data class CalculateQuantityOrderArgs(
    val leverage: Int,
    val commissionTake: Double,
    val percentage: Double,
    val markPrice: Double,
    val tickerHighPrice: Double,
    val availableCoinBalance: Double?
)