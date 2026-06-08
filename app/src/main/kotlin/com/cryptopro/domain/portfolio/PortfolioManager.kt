package com.cryptopro.domain.portfolio

import com.cryptopro.data.local.entities.CryptoDataEntity

/**
 * Portfolio Management & Risk Management
 */
data class Portfolio(
    val totalBalance: Double,
    val holdings: Map<String, Double>, // symbol -> quantity
    val currentValues: Map<String, Double> // symbol -> current value
) {
    val totalValue: Double get() = currentValues.values.sum() + (totalBalance - currentValues.values.sum())
    val allocation: Map<String, Double> get() = currentValues.mapValues { (it.value / totalValue).coerceIn(0.0, 1.0) }
}

data class RiskMetrics(
    val maxDrawdown: Double,
    val volatility: Double,
    val sharpeRatio: Double,
    val riskLevel: RiskLevel
)

enum class RiskLevel {
    LOW, MEDIUM, HIGH, CRITICAL
}

class PortfolioManager {

    /**
     * Calculate portfolio value from holdings and current prices
     */
    fun calculatePortfolioValue(
        balance: Double,
        holdings: Map<String, Double>,
        prices: Map<String, Double>
    ): Double {
        var value = balance
        for ((symbol, qty) in holdings) {
            value += qty * (prices[symbol] ?: 0.0)
        }
        return value
    }

    /**
     * Calculate risk metrics for a portfolio
     */
    fun calculateRiskMetrics(
        equityCurve: List<Double>,
        riskFreeRate: Double = 0.02
    ): RiskMetrics {
        val maxValue = equityCurve.maxOrNull() ?: 1.0
        val minValue = equityCurve.minOrNull() ?: 1.0
        val maxDrawdown = ((minValue - maxValue) / maxValue) * 100

        val returns = equityCurve.zipWithNext { a, b -> (b - a) / a }
        val avgReturn = returns.average()
        val variance = returns.map { (it - avgReturn) * (it - avgReturn) }.average()
        val volatility = kotlin.math.sqrt(variance) * 100
        val sharpeRatio = if (volatility > 0) (avgReturn - riskFreeRate) / volatility else 0.0

        val riskLevel = when {
            volatility > 10 -> RiskLevel.CRITICAL
            volatility > 5 -> RiskLevel.HIGH
            volatility > 2 -> RiskLevel.MEDIUM
            else -> RiskLevel.LOW
        }

        return RiskMetrics(
            maxDrawdown = maxDrawdown,
            volatility = volatility,
            sharpeRatio = sharpeRatio,
            riskLevel = riskLevel
        )
    }

    /**
     * Calculate position sizing based on risk management rules
     */
    fun calculatePositionSize(
        accountBalance: Double,
        riskPercentage: Double = 2.0,
        stopLossPercent: Double = 2.0
    ): Double {
        val riskAmount = accountBalance * (riskPercentage / 100)
        val positionSize = riskAmount / (stopLossPercent / 100)
        return positionSize.coerceAtMost(accountBalance)
    }

    /**
     * Calculate stop loss and take profit levels
     */
    fun calculateStopLossTakeProfit(
        entryPrice: Double,
        riskRewardRatio: Double = 2.0,
        stopLossPercent: Double = 2.0
    ): Pair<Double, Double> {
        val stopLoss = entryPrice * (1 - (stopLossPercent / 100))
        val takeProfit = entryPrice * (1 + (stopLossPercent * riskRewardRatio / 100))
        return Pair(stopLoss, takeProfit)
    }
}
