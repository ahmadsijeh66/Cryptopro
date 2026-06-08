package com.cryptopro.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cryptopro.data.local.entities.CryptoDataEntity
import com.cryptopro.domain.ml.StrategyAnalyzer
import com.cryptopro.domain.ml.TradingSignal
import com.cryptopro.domain.repository.CryptoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TradingScreenState(
    val isLoading: Boolean = false,
    val cryptoData: List<CryptoDataEntity> = emptyList(),
    val currentSignal: TradingSignal? = null,
    val selectedSymbol: String = "BTCUSDT",
    val error: String? = null
)

@HiltViewModel
class TradingViewModel @Inject constructor(
    private val cryptoRepository: CryptoRepository,
    private val strategyAnalyzer: StrategyAnalyzer
) : ViewModel() {

    private val _state = MutableStateFlow(TradingScreenState())
    val state: StateFlow<TradingScreenState> = _state.asStateFlow()

    fun fetchAndAnalyzeData(symbol: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                // Fetch data from Binance
                cryptoRepository.fetchAndSaveCryptoData(symbol, "1h")

                // Get latest data
                val data = cryptoRepository.getCryptoDataRange(
                    symbol,
                    System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000), // Last 7 days
                    System.currentTimeMillis()
                )

                // Analyze with strategy
                val signal = strategyAnalyzer.analyzeSignal(data.sortedBy { it.timestamp })

                _state.value = _state.value.copy(
                    isLoading = false,
                    cryptoData = data,
                    currentSignal = signal,
                    selectedSymbol = symbol
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }

    fun selectSymbol(symbol: String) {
        _state.value = _state.value.copy(selectedSymbol = symbol)
        fetchAndAnalyzeData(symbol)
    }
}
