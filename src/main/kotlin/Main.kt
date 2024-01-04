import data.configs.Config
import data.dto.GetQuantityOrderArgs
import data.dto.SetProspectsArgs
import data.singletons.UMFutures

fun main(args: Array<String>) {
    val client = UMFutures.client

    setProspects(client, SetProspectsArgs(Config.leverage, Config.margin))
    val quantity = getQuantityOrder(client, GetQuantityOrderArgs(Config.futuresAccountBalance, Config.leverage, Config.regularFee))


}