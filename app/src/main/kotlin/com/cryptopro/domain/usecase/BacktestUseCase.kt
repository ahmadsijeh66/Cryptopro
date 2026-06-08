package com.cryptopro.domain.usecase

import com.cryptopro.data.local.entities.BacktestResultEntity
import com.cryptopro.data.local.entities.CryptoDataEntity
import com.cryptopro.domain.ml.StrategyAnalyzer
import com.cryptopro.domain.repository.CryptoRepository
import com.cryptopro.domain.repository.BacktestRepository

/**
 * Backtesting Engine
 * Simulates trading strategy on historical data
 */
class BacktestUseCase(
    private val cryptoRepository: CryptoRepository,
    private val backtestRepository: BacktestRepository,
    private val strategyAnalyzer: StrategyAnalyzer
) {

    suspend fun backtestStrategy(
        strategyId: Long,
        symbol: String,
        startDate: Long,
        endDate: Long,
        initialBalance: Double = 10000.0
    ): BacktestResultEntity {
        // Fetch historical crypto data
        val historicalData = cryptoRepository.getCryptoDataRange(symbol, startDate, endDate)
        
        if (historicalData.isEmpty()) {
            throw IllegalArgumentException("No crypto data available for backtesting")
        }

        // Run simulation
        val result = simulateTrading(historicalData, initialBalance)

        // Save result to database
        val backtestResult = BacktestResultEntity(
            strategyId = strategyId,
            symbol = symbol,
            startDate = startDate,
            endDate = endDate,
            initialBalance = initialBalance,
            finalBalance = result.finalBalance,
            totalReturn = result.returnPercentage,
            winRate = result.winRate,
            maxDrawdown = result.maxDrawdown,
            totalTrades = result.totalTrades,
            createdAt = System.currentTimeMillis()
        )

        backtestRepository.saveBacktestResult(backtestResult)
        return backtestResult
    }

    private suspend fun simulateTrading(
        historicalData: List<CryptoDataEntity>,
        initialBalance: Double
    ): SimulationResult {
        var balance = initialBalance
        var holdings = 0.0
        var totalTrades = 0
        var winningTrades = 0
        var maxBalance = initialBalance
        var minBalance = initialBalance

        // Sort data by timestamp ascending
        val sortedData = historicalData.sortedBy { it.timestamp }

        for (i in sortedData.indices) {
            val currentCandle = sortedData[i]
            val window = sortedData.subList(0, i + 1)

            // Get trading signal from AI
            val signal = strategyAnalyzer.analyzeSignal(window.reversed())

            when (signal.signal.name) {
                "BUY" -> {
                    if (balance > 0 && holdings == 0.0) {
                        holdings = balance / currentCandle.close
                        balance = 0.0
                        totalTrades++
                    }
                }
                "SELL" -> {
                    if (holdings > 0) {
                        balance = holdings * currentCandle.close
                        holdings = 0.0
                        totalTrades++
                        if (balance > maxBalance) winningTrades++
                    }
                }
                else -> {} // HOLD
            }

            // Update balance tracking
            val currentBalance = balance + (holdings * currentCandle.close)
            if (currentBalance > maxBalance) maxBalance = currentBalance
            if (currentBalance < minBalance) minBalance = currentBalance
        }

        // Close final position
        if (holdings > 0) {
            balance = holdings * sortedData.last().close
        }

        val finalBalance = balance
        val returnPercentage = ((finalBalance - initialBalance) / initialBalance) * 100
        val winRate = if (totalTrades > 0) (winningTrades.toDouble() / totalTrades) * 100 else 0.0
        val maxDrawdown = ((minBalance - maxBalance) / maxBalance) * 100

        return SimulationResult(
            finalBalance = finalBalance,
            returnPercentage = returnPercentage,
            winRate = winRate,
            maxDrawdown = maxDrawdown,
            totalTrades = totalTrades
        )
    }

    data class SimulationResult(
        val finalBalance: Double,
        val returnPercentage: Double,
        val winRate: Double,
        val maxDrawdown: Double,
        val totalTrades: Int
    )
}
