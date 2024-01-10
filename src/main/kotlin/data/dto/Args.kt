package data.dto

data class CalculateQuantityOrderArgs(
    val leverage: Int,
    val commissionTake: Double,
    val percentage: Double,
    val markPrice: Double,
    val tickerHighPrice: Double,
    val availableCoinBalance: Double?
)

data class DepsForQuantityCalculationArgs (
    val assetToWorkWith: String,
    val symbol: String
)

data class MakeNewOrderArgs (
    val symbol: String,
    val side: String,
    val type: String,
    val quantity: Double,
    val closePosition: Boolean = false
)

data class QuantityDeps(
    val markPrice: Double,
    val tickerHighPrice: Double,
    val availableCoinBalance: Double?,
    val commissionTake: Double
)

data class SetRiskVariablesArgs(val leverage: Int, val marginType: String, val symbol: String)