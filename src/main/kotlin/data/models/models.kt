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

@Serializable
data class TickerSymbol(
    val symbol: String,
    val price: String,
    val time: Long,
)

@Serializable
data class Ticker24H(
    val symbol: String,
    val priceChange: String,
    val priceChangePercent: String,
    val weightedAvgPrice: String,
    val lastPrice: String,
    val lastQty: String,
    val openPrice: String,
    val highPrice: String,
    val lowPrice: String,
    val volume: String,
    val quoteVolume: String,
    val openTime: Long,
    val closeTime: Long,
    val firstId: Long,
    val lastId: Long,
    val count: Int
)
