
import data.client.UMFutures
import data.configs.Config
import data.dto.CalculateQuantityOrderArgs
import data.dto.DepsForQuantityCalculationArgs
import data.dto.MakeNewOrderArgs
import data.dto.SetRiskVariablesArgs


fun main() {
    val client = UMFutures.client

    setRiskVariables(client, SetRiskVariablesArgs(Config.leverage, Config.marginType, Config.symbol))

    val (
        markPrice,
        tickerHighPrice,
        availableCoinBalance,
        commissionTake
    ) = getDepsForQuantityCalculation(client, DepsForQuantityCalculationArgs(Config.assetToWorkWith, Config.symbol))

    val qtyCost = calculateQuantityOrder(
        CalculateQuantityOrderArgs(
            Config.leverage,
            commissionTake,
            100.00,
            markPrice,
            tickerHighPrice,
            availableCoinBalance
        )
    )

    // TODO: make strategy for when to buy or sell

    val mostRecentOrderMade = makeNewOrder(
        client,
        MakeNewOrderArgs(Config.symbol, "SELL", "MARKET", qtyCost, closePosition = true)
    )

    saveToCache("mostRecentOrderMade.json", mostRecentOrderMade)
}