package data.models

import kotlinx.serialization.Serializable

@Serializable
data class BinanceException(
    val code: Int,
    val msg: String
)

@Serializable
data class FuturesAccountBalance(
    val accountAlias: String,
    val asset: String,
    val balance: String,
    val crossWalletBalance: String,
    val crossUnPnl: String,
    val availableBalance: String,
    val maxWithdrawAmount: String,
    val marginAvailable: Boolean,
    val updateTime: Long
)