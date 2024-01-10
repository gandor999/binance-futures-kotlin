package data.schemas

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

@Serializable
data class MarkPrice(
    val symbol: String,
    val markPrice: String,
    val indexPrice: String,
    val estimatedSettlePrice: String,
    val lastFundingRate: String,
    val interestRate: String,
    val nextFundingTime: Long,
    val time: Long
)

@Serializable
data class OrderBookTicker(
    val symbol: String,
    val bidPrice: String,
    val bidQty: String,
    val askPrice: String,
    val askQty: String,
    val time: Long,
    val lastUpdateId: Long
)

@Serializable
data class CommissionRates(
    val symbol: String,
    val makerCommissionRate: String,
    val takerCommissionRate: String
)

@Serializable
data class OrderDetails(
    val orderId: Long,
    val symbol: String,
    val status: String,
    val clientOrderId: String,
    val price: String,
    val avgPrice: String,
    val origQty: String,
    val executedQty: String,
    val cumQty: String,
    val cumQuote: String,
    val timeInForce: String,
    val type: String,
    val reduceOnly: Boolean,
    val closePosition: Boolean,
    val side: String,
    val positionSide: String,
    val stopPrice: String,
    val workingType: String,
    val priceProtect: Boolean,
    val origType: String,
    val priceMatch: String,
    val selfTradePreventionMode: String,
    val goodTillDate: Long,
    val updateTime: Long
)

@Serializable
data class PositionDetails(
    val symbol: String,
    val positionAmt: String,
    val entryPrice: String,
    val breakEvenPrice: String,
    val markPrice: String,
    val unRealizedProfit: String,
    val liquidationPrice: String,
    val leverage: String,
    val maxNotionalValue: String,
    val marginType: String,
    val isolatedMargin: String,
    val isAutoAddMargin: String,
    val positionSide: String,
    val notional: String,
    val isolatedWallet: String,
    val updateTime: Long,
    val isolated: Boolean,
    val adlQuantile: Int
)


@Serializable
data class PositionInformation(
    val symbol: String,
    val positionAmt: String,
    val entryPrice: String,
    val breakEvenPrice: String,
    val markPrice: String,
    val unRealizedProfit: String,
    val liquidationPrice: String,
    val leverage: String,
    val maxNotionalValue: String,
    val marginType: String,
    val isolatedMargin: String,
    val isAutoAddMargin: String,
    val positionSide: String,
    val notional: String,
    val isolatedWallet: String,
    val updateTime: Long,
    val isolated: Boolean,
    val adlQuantile: Int
)