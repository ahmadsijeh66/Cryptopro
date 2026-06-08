package com.cryptopro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cryptopro.domain.ml.TradeType
import com.cryptopro.ui.viewmodel.TradingViewModel
import com.cryptopro.ui.viewmodel.TradingScreenState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradingScreen(
    viewModel: TradingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchAndAnalyzeData("BTCUSDT")
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Trading Signals") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                state.error != null -> {
                    ErrorCard(
                        message = state.error ?: "Unknown error",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            SymbolSelector(
                                selectedSymbol = state.selectedSymbol,
                                onSymbolSelected = { viewModel.selectSymbol(it) }
                            )
                        }

                        item {
                            if (state.currentSignal != null) {
                                SignalCard(signal = state.currentSignal!!)
                            }
                        }

                        item {
                            Text(
                                "Market Data",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        items(state.cryptoData.takeLast(10)) { data ->
                            CandleDataCard(data = data)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SymbolSelector(
    selectedSymbol: String,
    onSymbolSelected: (String) -> Unit
) {
    val symbols = listOf("BTCUSDT", "ETHUSDT", "BNBUSDT", "ADAUSDT", "DOGEUSDT")

    Column {
        Text(
            "Select Trading Pair",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(symbols) { symbol ->
                Button(
                    onClick = { onSymbolSelected(symbol) },
                    modifier = Modifier.height(40.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (symbol == selectedSymbol)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Text(symbol, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun SignalCard(signal: com.cryptopro.domain.ml.TradingSignal) {
    val backgroundColor = when (signal.signal) {
        TradeType.BUY -> Color(0xFFE8F5E9)
        TradeType.SELL -> Color(0xFFFFEBEE)
        TradeType.HOLD -> Color(0xFFFFF3E0)
    }

    val textColor = when (signal.signal) {
        TradeType.BUY -> Color(0xFF2E7D32)
        TradeType.SELL -> Color(0xFFC62828)
        TradeType.HOLD -> Color(0xFFE65100)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Current Signal",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = when (signal.signal) {
                        TradeType.BUY -> Icons.Filled.TrendingUp
                        TradeType.SELL -> Icons.Filled.TrendingDown
                        else -> Icons.Filled.TrendingUp
                    },
                    contentDescription = signal.signal.name,
                    tint = textColor,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                signal.signal.name,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Confidence", fontSize = 12.sp)
                    Text(
                        "${(signal.confidence * 100).toInt()}%",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }
                Column {
                    Text("Reason", fontSize = 12.sp)
                    Text(
                        signal.reason,
                        fontSize = 12.sp,
                        color = textColor
                    )
                }
            }
        }
    }
}

@Composable
fun CandleDataCard(data: com.cryptopro.data.local.entities.CryptoDataEntity) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    "Close: $${String.format("%.2f", data.close)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "High: $${String.format("%.2f", data.high)} / Low: $${String.format("%.2f", data.low)}",
                    fontSize = 12.sp
                )
            }
            Text(
                "Vol: ${String.format("%.0f", data.volume)}",
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun ErrorCard(message: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFFFEBEE))
    ) {
        Text(
            message,
            modifier = Modifier.padding(16.dp),
            color = Color(0xFFC62828)
        )
    }
}
