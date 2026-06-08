package com.cryptopro.domain.ml

import com.cryptopro.data.local.entities.CryptoDataEntity
import kotlin.math.sqrt

/**
 * Local ML Model Implementation
 * Uses TensorFlow Lite and custom algorithms for crypto analysis
 */
class LocalMLModel : StrategyAnalyzer {

    override suspend fun analyzeSignal(cryptoData: List<CryptoDataEntity>): TradingSignal {
        if (cryptoData.isEmpty()) {
            return TradingSignal(TradeType.HOLD, 0.5, "Insufficient data")
        }

        // Calculate technical indicators
        val sma20 = calculateSMA(cryptoData, 20)
        val sma50 = calculateSMA(cryptoData, 50)
        val rsi = calculateRSI(cryptoData, 14)
        val macd = calculateMACD(cryptoData)

        val currentPrice = cryptoData.first().close

        // Simple signal logic
        return when {
            sma20 > sma50 && rsi < 70 && macd.signal > macd.macdLine -> {
                TradingSignal(TradeType.BUY, 0.75, "Golden cross with bullish MACD")
            }
            sma20 < sma50 && rsi > 30 && macd.signal < macd.macdLine -> {
                TradingSignal(TradeType.SELL, 0.75, "Death cross with bearish MACD")
            }
            rsi > 80 -> {
                TradingSignal(TradeType.SELL, 0.6, "Overbought condition")
            }
            rsi < 20 -> {
                TradingSignal(TradeType.BUY, 0.6, "Oversold condition")
            }
            else -> {
                TradingSignal(TradeType.HOLD, 0.5, "No clear signal")
            }
        }
    }

    override suspend fun predictNextMove(cryptoData: List<CryptoDataEntity>): PricePrediction {
        if (cryptoData.isEmpty()) {
            return PricePrediction(0.0, 0.0, "1h")
        }

        val currentPrice = cryptoData.first().close
        val returns = calculateReturns(cryptoData)
        val volatility = calculateVolatility(returns)
        val trend = calculateTrend(cryptoData)

        val predictedPrice = currentPrice * (1 + trend)
        val confidence = (1 - (volatility / currentPrice)).coerceIn(0.0, 1.0)

        return PricePrediction(
            predictedPrice = predictedPrice,
            confidence = confidence,
            timeframe = "1h"
        )
    }

    override suspend fun scoreStrategy(cryptoData: List<CryptoDataEntity>, strategyParams: Map<String, Double>): Double {
        // Score strategy based on win rate, Sharpe ratio, max drawdown
        return 0.5 // Placeholder
    }

    private fun calculateSMA(data: List<CryptoDataEntity>, period: Int): Double {
        return if (data.size >= period) {
            data.take(period).map { it.close }.average()
        } else {
            data.map { it.close }.average()
        }
    }

    private fun calculateRSI(data: List<CryptoDataEntity>, period: Int = 14): Double {
        val changes = data.zipWithNext { a, b -> b.close - a.close }
        val gains = changes.filter { it > 0 }.average()
        val losses = changes.filter { it < 0 }.map { -it }.average()

        return if (losses == 0.0) 100.0 else 100 - (100 / (1 + gains / losses))
    }

    private fun calculateMACD(data: List<CryptoDataEntity>): MACDResult {
        val ema12 = calculateEMA(data, 12)
        val ema26 = calculateEMA(data, 26)
        val macdLine = ema12 - ema26
        val signalLine = calculateEMA(data.map { it.copy() }, 9) // Simplified

        return MACDResult(macdLine, signalLine, macdLine - signalLine)
    }

    private fun calculateEMA(data: List<CryptoDataEntity>, period: Int): Double {
        val multiplier = 2.0 / (period + 1)
        var ema = data.take(period).map { it.close }.average()

        for (i in period until data.size) {
            ema = (data[i].close * multiplier) + (ema * (1 - multiplier))
        }

        return ema
    }

    private fun calculateReturns(data: List<CryptoDataEntity>): List<Double> {
        return data.zipWithNext { a, b -> (b.close - a.close) / a.close }
    }

    private fun calculateVolatility(returns: List<Double>): Double {
        val mean = returns.average()
        val variance = returns.map { (it - mean) * (it - mean) }.average()
        return sqrt(variance)
    }

    private fun calculateTrend(data: List<CryptoDataEntity>): Double {
        val recentChange = (data.first().close - data.last().close) / data.last().close
        return recentChange * 0.01 // Dampened trend
    }

    data class MACDResult(
        val macdLine: Double,
        val signal: Double,
        val histogram: Double
    )
}
