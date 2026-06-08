package com.cryptopro.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cryptopro.data.local.entities.CryptoDataEntity
import com.cryptopro.domain.portfolio.Portfolio
import com.cryptopro.domain.portfolio.PortfolioManager
import com.cryptopro.domain.portfolio.RiskMetrics
import com.cryptopro.domain.repository.CryptoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PortfolioScreenState(
    val isLoading: Boolean = false,
    val portfolio: Portfolio? = null,
    val riskMetrics: RiskMetrics? = null,
    val equityCurve: List<Double> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class PortfolioViewModel @Inject constructor(
    private val cryptoRepository: CryptoRepository,
    private val portfolioManager: PortfolioManager
) : ViewModel() {

    private val _state = MutableStateFlow(PortfolioScreenState())
    val state: StateFlow<PortfolioScreenState> = _state.asStateFlow()

    fun updatePortfolio(
        balance: Double,
        holdings: Map<String, Double>,
        prices: Map<String, Double>
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val portfolioValue = portfolioManager.calculatePortfolioValue(balance, holdings, prices)
                val portfolio = Portfolio(
                    totalBalance = balance,
                    holdings = holdings,
                    currentValues = holdings.mapValues { (symbol, qty) ->
                        qty * (prices[symbol] ?: 0.0)
                    }
                )

                _state.value = _state.value.copy(
                    isLoading = false,
                    portfolio = portfolio
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to update portfolio"
                )
            }
        }
    }

    fun calculateRiskMetrics(equityCurve: List<Double>) {
        try {
            val riskMetrics = portfolioManager.calculateRiskMetrics(equityCurve)
            _state.value = _state.value.copy(
                riskMetrics = riskMetrics,
                equityCurve = equityCurve
            )
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                error = e.message ?: "Failed to calculate risk metrics"
            )
        }
    }

    fun getPositionSize(
        accountBalance: Double,
        riskPercentage: Double = 2.0,
        stopLossPercent: Double = 2.0
    ): Double {
        return portfolioManager.calculatePositionSize(accountBalance, riskPercentage, stopLossPercent)
    }

    fun getStopLossTakeProfit(
        entryPrice: Double,
        riskRewardRatio: Double = 2.0,
        stopLossPercent: Double = 2.0
    ): Pair<Double, Double> {
        return portfolioManager.calculateStopLossTakeProfit(entryPrice, riskRewardRatio, stopLossPercent)
    }
}
