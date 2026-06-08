package com.cryptopro.domain.ml

import com.cryptopro.data.local.entities.CryptoDataEntity

/**
 * Local ML Model for analyzing crypto strategies and generating signals
 */
interface StrategyAnalyzer {
    /**
     * Analyzes historical crypto data and returns a buy/sell signal
     */
    suspend fun analyzeSignal(cryptoData: List<CryptoDataEntity>): TradingSignal

    /**
     * Predicts next price movement
     */
    suspend fun predictNextMove(cryptoData: List<CryptoDataEntity>): PricePrediction

    /**
     * Scores a trading strategy based on historical performance
     */
    suspend fun scoreStrategy(cryptoData: List<CryptoDataEntity>, strategyParams: Map<String, Double>): Double
}

data class TradingSignal(
    val signal: TradeType, // BUY, SELL, HOLD
    val confidence: Double, // 0-1
    val reason: String
)

enum class TradeType {
    BUY, SELL, HOLD
}

data class PricePrediction(
    val predictedPrice: Double,
    val confidence: Double,
    val timeframe: String
)
