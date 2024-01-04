import com.binance.connector.futures.client.impl.UMFuturesClientImpl
import data.client.UMFutures
import data.configs.Config
import data.dto.CalculateQuantityOrderArgs
import data.dto.DepsForQuantityCalculationArgs
import data.dto.SetRiskVariablesArgs

fun makeNewOrder(client: UMFuturesClientImpl, quantity: Double) {

}

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

    print(qtyCost)
}