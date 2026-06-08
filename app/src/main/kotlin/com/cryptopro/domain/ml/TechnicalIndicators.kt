package com.cryptopro.domain.ml

import com.cryptopro.data.local.entities.CryptoDataEntity
import kotlin.math.sqrt

/**
 * Technical Indicator Calculations
 * Ported from Python script:
 * - Moving Average (SMA)
 * - Exponential Moving Average (EMA)
 * - RSI (Relative Strength Index)
 * - MACD (Moving Average Convergence Divergence)
 * - Bollinger Bands
 * - Support & Resistance Detection
 */
class TechnicalIndicators {

    fun movingAverage(closes: List<Double>, period: Int): List<Double?> {
        val result = MutableList<Double?>(period - 1) { null }
        for (i in period - 1 until closes.size) {
            val avg = closes.subList(i - period + 1, i + 1).average()
            result.add(avg.round(2))
        }
        return result
    }

    fun ema(closes: List<Double>, period: Int): List<Double?> {
        val result = MutableList<Double?>(period - 1) { null }
        val k = 2.0 / (period + 1)
        var seed = closes.take(period).average()
        result.add(seed.round(2))

        for (price in closes.drop(period)) {
            seed = price * k + seed * (1 - k)
            result.add(seed.round(2))
        }
        return result
    }

    fun rsi(closes: List<Double>, period: Int = 14): List<Double?> {
        val result = MutableList<Double?>(period) { null }
        val gains = mutableListOf<Double>()
        val losses = mutableListOf<Double>()

        for (i in 1..period) {
            val diff = closes[i] - closes[i - 1]
            gains.add(maxOf(diff, 0.0))
            losses.add(maxOf(-diff, 0.0))
        }

        var avgGain = gains.average()
        var avgLoss = losses.average()

        for (i in period until closes.size - 1) {
            val diff = closes[i + 1] - closes[i]
            avgGain = (avgGain * (period - 1) + maxOf(diff, 0.0)) / period
            avgLoss = (avgLoss * (period - 1) + maxOf(-diff, 0.0)) / period
            val rs = if (avgLoss != 0.0) avgGain / avgLoss else Double.POSITIVE_INFINITY
            result.add((100 - 100 / (1 + rs)).round(2))
        }
        return result
    }

    fun macd(
        closes: List<Double>,
        fast: Int = 12,
        slow: Int = 26,
        signal: Int = 9
    ): Triple<List<Double?>, List<Double?>, List<Double?>> {
        val emaFast = ema(closes, fast)
        val emaSlow = ema(closes, slow)
        val macdLine = emaFast.zip(emaSlow) { f, s ->
            if (f != null && s != null) (f - s).round(2) else null
        }
        val valid = macdLine.filterNotNull()
        val sigRaw = ema(valid, signal)
        val offset = macdLine.size - valid.size
        val sigLine = MutableList<Double?>(offset + signal - 1) { null }
        sigLine.addAll(sigRaw.drop(signal - 1))

        val histogram = macdLine.zip(sigLine) { m, s ->
            if (m != null && s != null) (m - s).round(2) else null
        }
        return Triple(macdLine, sigLine, histogram)
    }

    fun bollingerBands(
        closes: List<Double>,
        period: Int = 20,
        stdMult: Double = 2.0
    ): Triple<List<Double?>, List<Double?>, List<Double?>> {
        val ma = movingAverage(closes, period)
        val upper = mutableListOf<Double?>()
        val lower = mutableListOf<Double?>()

        for (i in ma.indices) {
            if (ma[i] == null) {
                upper.add(null)
                lower.add(null)
            } else {
                val avg = ma[i]!!
                val window = closes.subList(i - period + 1, i + 1)
                val variance = window.map { (it - avg) * (it - avg) }.average()
                val sd = sqrt(variance)
                upper.add((avg + stdMult * sd).round(2))
                lower.add((avg - stdMult * sd).round(2))
            }
        }
        return Triple(ma, upper, lower)
    }

    fun findSupportResistance(
        candles: List<CryptoDataEntity>,
        lookback: Int = 20,
        tolerance: Double = 0.02
    ): Pair<List<Double>, List<Double>> {
        val highs = candles.map { it.high }
        val lows = candles.map { it.low }
        val n = candles.size

        val pivotHighs = mutableListOf<Double>()
        val pivotLows = mutableListOf<Double>()
        val half = lookback / 2

        for (i in half until n - half) {
            val windowH = highs.subList(i - half, i + half + 1)
            val windowL = lows.subList(i - half, i + half + 1)
            if (highs[i] == windowH.maxOrNull()) pivotHighs.add(highs[i])
            if (lows[i] == windowL.minOrNull()) pivotLows.add(lows[i])
        }

        return Pair(cluster(pivotLows, tolerance), cluster(pivotHighs, tolerance))
    }

    private fun cluster(levels: List<Double>, tolerance: Double): List<Double> {
        if (levels.isEmpty()) return emptyList()
        val sorted = levels.sorted()
        val clusters = mutableListOf<Double>()
        var group = mutableListOf(sorted[0])

        for (lvl in sorted.drop(1)) {
            if ((lvl - group[0]) / group[0] <= tolerance) {
                group.add(lvl)
            } else {
                clusters.add((group.average()).round(2))
                group = mutableListOf(lvl)
            }
        }
        clusters.add((group.average()).round(2))
        return clusters
    }

    private fun Double.round(decimals: Int): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return kotlin.math.round(this * multiplier) / multiplier
    }
}
