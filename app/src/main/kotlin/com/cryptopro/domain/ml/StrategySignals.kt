package com.cryptopro.domain.ml

/**
 * Strategy Signal Generation
 * Ported from Python script
 */
class StrategySignals {

    /**
     * MA-100 Strategy: BUY when price crosses above MA-100, SELL when crosses below
     */
    fun strategyMA100(closes: List<Double>, ma100: List<Double?>): List<String> {
        val signals = MutableList(closes.size) { "HOLD" }
        for (i in 1 until closes.size) {
            if (ma100[i] == null || ma100[i - 1] == null) continue
            if (closes[i - 1] < ma100[i - 1]!! && closes[i] >= ma100[i]!!) {
                signals[i] = "BUY"
            } else if (closes[i - 1] > ma100[i - 1]!! && closes[i] <= ma100[i]!!) {
                signals[i] = "SELL"
            }
        }
        return signals
    }

    /**
     * RSI Strategy: BUY when RSI crosses above oversold (30), SELL when crosses below overbought (70)
     */
    fun strategyRSI(rsiVals: List<Double?>, oversold: Double = 30.0, overbought: Double = 70.0): List<String> {
        val signals = MutableList(rsiVals.size) { "HOLD" }
        for (i in 1 until rsiVals.size) {
            val r = rsiVals[i] ?: continue
            val rPrev = rsiVals[i - 1] ?: continue
            if (rPrev <= oversold && r > oversold) {
                signals[i] = "BUY"
            } else if (rPrev >= overbought && r < overbought) {
                signals[i] = "SELL"
            }
        }
        return signals
    }

    /**
     * MACD Strategy: BUY when MACD crosses above signal line, SELL when crosses below
     */
    fun strategyMACDCrossover(macdLine: List<Double?>, sigLine: List<Double?>): List<String> {
        val signals = MutableList(macdLine.size) { "HOLD" }
        for (i in 1 until macdLine.size) {
            val m = macdLine[i] ?: continue
            val s = sigLine[i] ?: continue
            val mp = macdLine[i - 1] ?: continue
            val sp = sigLine[i - 1] ?: continue
            if (mp < sp && m >= s) {
                signals[i] = "BUY"
            } else if (mp > sp && m <= s) {
                signals[i] = "SELL"
            }
        }
        return signals
    }

    /**
     * Bollinger Bands Strategy: BUY at lower band (oversold), SELL at upper band (overbought)
     */
    fun strategyBollinger(closes: List<Double>, upper: List<Double?>, lower: List<Double?>): List<String> {
        val signals = MutableList(closes.size) { "HOLD" }
        for (i in closes.indices) {
            if (upper[i] == null || lower[i] == null) continue
            if (closes[i] <= lower[i]!!) {
                signals[i] = "BUY"
            } else if (closes[i] >= upper[i]!!) {
                signals[i] = "SELL"
            }
        }
        return signals
    }

    /**
     * Support & Resistance Strategy: BUY at support, SELL at resistance
     */
    fun strategySupportResistance(
        closes: List<Double>,
        supportLevels: List<Double>,
        resistanceLevels: List<Double>,
        tolerance: Double = 0.015
    ): List<String> {
        val signals = MutableList(closes.size) { "HOLD" }
        for (i in closes.indices) {
            val price = closes[i]
            var signalFound = false
            for (s in supportLevels) {
                if (kotlin.math.abs(price - s) / s <= tolerance) {
                    signals[i] = "BUY"
                    signalFound = true
                    break
                }
            }
            if (!signalFound) {
                for (r in resistanceLevels) {
                    if (kotlin.math.abs(price - r) / r <= tolerance) {
                        signals[i] = "SELL"
                        break
                    }
                }
            }
        }
        return signals
    }

    /**
     * Consensus Signal: Majority-vote across all strategies
     * Requires > half strategies to agree for a non-HOLD
     */
    fun consensusSignal(vararg signalLists: List<String>): List<String> {
        val minLength = signalLists.minOf { it.size }
        val result = mutableListOf<String>()
        for (i in 0 until minLength) {
            val votes = signalLists.map { it[i] }
            val buys = votes.count { it == "BUY" }
            val sells = votes.count { it == "SELL" }
            val threshold = signalLists.size / 2.0
            result.add(
                when {
                    buys > threshold -> "BUY"
                    sells > threshold -> "SELL"
                    else -> "HOLD"
                }
            )
        }
        return result
    }
}
