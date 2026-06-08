package com.cryptopro.domain.ml

import com.cryptopro.data.local.entities.CryptoDataEntity

/**
 * Python-based Strategy Analyzer
 * Integrates all technical analysis strategies from the Python script
 */
class PythonStrategyAnalyzer : StrategyAnalyzer {
    private val indicators = TechnicalIndicators()
    private val strategies = StrategySignals()

    override suspend fun analyzeSignal(cryptoData: List<CryptoDataEntity>): TradingSignal {
        if (cryptoData.isEmpty()) {
            return TradingSignal(TradeType.HOLD, 0.5, "Insufficient data")
        }

        val sortedData = cryptoData.sortedBy { it.timestamp }
        val closes = sortedData.map { it.close }

        // Calculate all indicators
        val ma100 = indicators.movingAverage(closes, 100)
        val rsiVals = indicators.rsi(closes, 14)
        val (macdLine, sigLine, _) = indicators.macd(closes, 12, 26, 9)
        val (_, bbUpper, bbLower) = indicators.bollingerBands(closes, 20, 2.0)
        val (support, resistance) = indicators.findSupportResistance(sortedData, 20)

        // Generate signals from each strategy
        val sigMA100 = strategies.strategyMA100(closes, ma100)
        val sigRSI = strategies.strategyRSI(rsiVals)
        val sigMACD = strategies.strategyMACDCrossover(macdLine, sigLine)
        val sigBB = strategies.strategyBollinger(closes, bbUpper, bbLower)
        val sigSR = strategies.strategySupportResistance(closes, support, resistance)

        // Get consensus signal
        val consensus = strategies.consensusSignal(sigMA100, sigRSI, sigMACD, sigBB, sigSR)
        val lastSignal = consensus.lastOrNull() ?: "HOLD"

        // Calculate confidence based on agreement
        val lastSignals = listOf(sigMA100, sigRSI, sigMACD, sigBB, sigSR).map { it.lastOrNull() ?: "HOLD" }
        val agreement = lastSignals.count { it == lastSignal }.toDouble() / lastSignals.size
        val confidence = (0.5 + (agreement * 0.5)).coerceIn(0.0, 1.0)

        val reason = when (lastSignal) {
            "BUY" -> "Consensus buy signal from $agreement% of strategies"
            "SELL" -> "Consensus sell signal from $agreement% of strategies"
            else -> "No clear consensus across strategies"
        }

        return TradingSignal(TradeType.valueOf(lastSignal), confidence, reason)
    }

    override suspend fun predictNextMove(cryptoData: List<CryptoDataEntity>): PricePrediction {
        if (cryptoData.isEmpty()) {
            return PricePrediction(0.0, 0.0, "1h")
        }

        val sortedData = cryptoData.sortedBy { it.timestamp }
        val closes = sortedData.map { it.close }
        val currentPrice = closes.last()

        // Calculate trend based on MA crossover
        val ma20 = indicators.movingAverage(closes, 20)
        val ma50 = indicators.movingAverage(closes, 50)
        val lastMA20 = ma20.lastOrNull() ?: currentPrice
        val lastMA50 = ma50.lastOrNull() ?: currentPrice

        val trend = (lastMA20 - lastMA50) / lastMA50
        val predictedPrice = currentPrice * (1 + trend * 0.01)

        // Confidence based on indicator alignment
        val rsiVals = indicators.rsi(closes, 14)
        val lastRSI = rsiVals.lastOrNull()
        val confidence = when {
            lastRSI == null -> 0.5
            lastRSI in 40.0..60.0 -> 0.6 // RSI in neutral zone
            lastRSI in 20.0..80.0 -> 0.7  // RSI in valid range
            else -> 0.5
        }

        return PricePrediction(
            predictedPrice = predictedPrice.round(2),
            confidence = confidence,
            timeframe = "1h"
        )
    }

    override suspend fun scoreStrategy(cryptoData: List<CryptoDataEntity>, strategyParams: Map<String, Double>): Double {
        // Score based on current signal agreement
        if (cryptoData.isEmpty()) return 0.5

        val signal = analyzeSignal(cryptoData)
        return signal.confidence
    }

    private fun Double.round(decimals: Int): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return kotlin.math.round(this * multiplier) / multiplier
    }
}
