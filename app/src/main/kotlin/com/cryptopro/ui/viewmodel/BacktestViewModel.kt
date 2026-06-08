package com.cryptopro.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cryptopro.data.local.entities.BacktestResultEntity
import com.cryptopro.domain.repository.BacktestRepository
import com.cryptopro.domain.usecase.BacktestUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BacktestScreenState(
    val isRunning: Boolean = false,
    val backtestResults: List<BacktestResultEntity> = emptyList(),
    val selectedResult: BacktestResultEntity? = null,
    val error: String? = null
)

@HiltViewModel
class BacktestViewModel @Inject constructor(
    private val backtestRepository: BacktestRepository,
    private val backtestUseCase: BacktestUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(BacktestScreenState())
    val state: StateFlow<BacktestScreenState> = _state.asStateFlow()

    init {
        loadRecentResults()
    }

    fun runBacktest(
        strategyId: Long,
        symbol: String,
        startDate: Long,
        endDate: Long,
        initialBalance: Double = 10000.0
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isRunning = true, error = null)
            try {
                val result = backtestUseCase.backtestStrategy(
                    strategyId,
                    symbol,
                    startDate,
                    endDate,
                    initialBalance
                )
                _state.value = _state.value.copy(
                    isRunning = false,
                    selectedResult = result
                )
                loadRecentResults()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isRunning = false,
                    error = e.message ?: "Backtest failed"
                )
            }
        }
    }

    fun loadRecentResults() {
        viewModelScope.launch {
            backtestRepository.getRecentResults(10).collect { results ->
                _state.value = _state.value.copy(backtestResults = results)
            }
        }
    }

    fun selectResult(result: BacktestResultEntity) {
        _state.value = _state.value.copy(selectedResult = result)
    }
}
