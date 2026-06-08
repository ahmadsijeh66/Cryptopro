package com.cryptopro.data.remote.api

import com.cryptopro.data.remote.dto.KlineDto
import retrofit2.http.GET
import retrofit2.http.Query

interface BinanceApi {
    @GET("https://api.binance.com/api/v3/klines")
    suspend fun getKlines(
        @Query("symbol") symbol: String,
        @Query("interval") interval: String, // "1m", "5m", "1h", "1d", etc.
        @Query("limit") limit: Int = 500,
        @Query("startTime") startTime: Long? = null,
        @Query("endTime") endTime: Long? = null
    ): List<List<Any>>

    @GET("https://api.binance.com/api/v3/ticker/price")
    suspend fun getTickerPrice(
        @Query("symbol") symbol: String
    ): TickerPriceDto

    @GET("https://api.binance.com/api/v3/exchangeInfo")
    suspend fun getExchangeInfo(): ExchangeInfoDto
}

data class TickerPriceDto(
    val symbol: String,
    val price: String
)

data class ExchangeInfoDto(
    val symbols: List<SymbolInfo>
)

data class SymbolInfo(
    val symbol: String,
    val status: String,
    val baseAsset: String,
    val quoteAsset: String
)

data class KlineDto(
    val openTime: Long,
    val open: String,
    val high: String,
    val low: String,
    val close: String,
    val volume: String,
    val closeTime: Long,
    val quoteAssetVolume: String,
    val numberOfTrades: Int,
    val takerBuyBaseAssetVolume: String,
    val takerBuyQuoteAssetVolume: String
)
