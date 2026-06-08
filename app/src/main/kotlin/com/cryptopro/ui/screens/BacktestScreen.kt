package com.cryptopro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cryptopro.ui.viewmodel.BacktestViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BacktestScreen(
    viewModel: BacktestViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Backtesting") },
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
                state.isRunning -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                state.selectedResult != null -> {
                    BacktestResultDetail(
                        result = state.selectedResult!!,
                        onBack = { viewModel.selectResult(state.selectedResult!!) }
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                "Recent Results",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        items(state.backtestResults) { result ->
                            BacktestResultCard(
                                result = result,
                                onClick = { viewModel.selectResult(result) }
                            )
                        }

                        if (state.backtestResults.isEmpty()) {
                            item {
                                Text(
                                    "No backtest results yet. Create a new strategy to get started!",
                                    modifier = Modifier.padding(16.dp),
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BacktestResultCard(
    result: com.cryptopro.data.local.entities.BacktestResultEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        result.symbol,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Total Trades: ${result.totalTrades}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                Text(
                    "${String.format("%+.2f", result.totalReturn)}%",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (result.totalReturn >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Initial", fontSize = 10.sp, color = Color.Gray)
                    Text(
                        "$${String.format("%.0f", result.initialBalance)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Column {
                    Text("Final", fontSize = 10.sp, color = Color.Gray)
                    Text(
                        "$${String.format("%.0f", result.finalBalance)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Column {
                    Text("Win Rate", fontSize = 10.sp, color = Color.Gray)
                    Text(
                        "${String.format("%.1f", result.winRate)}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Column {
                    Text("Max DD", fontSize = 10.sp, color = Color.Gray)
                    Text(
                        "${String.format("%.1f", result.maxDrawdown)}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun BacktestResultDetail(
    result: com.cryptopro.data.local.entities.BacktestResultEntity,
    onBack: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                result.symbol,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    MetricRow("Initial Balance", "$${String.format("%.2f", result.initialBalance)}")
                    Spacer(modifier = Modifier.height(8.dp))
                    MetricRow("Final Balance", "$${String.format("%.2f", result.finalBalance)}")
                    Spacer(modifier = Modifier.height(8.dp))
                    MetricRow(
                        "Total Return",
                        "${String.format("%+.2f", result.totalReturn)}%",
                        color = if (result.totalReturn >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
                    )
                }
            }
        }

        item {
            Card {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text("Performance Metrics", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    MetricRow("Total Trades", result.totalTrades.toString())
                    Spacer(modifier = Modifier.height(8.dp))
                    MetricRow("Win Rate", "${String.format("%.2f", result.winRate)}%")
                    Spacer(modifier = Modifier.height(8.dp))
                    MetricRow("Max Drawdown", "${String.format("%.2f", result.maxDrawdown)}%")
                }
            }
        }
    }
}

@Composable
fun MetricRow(
    label: String,
    value: String,
    color: Color = Color.Black
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 14.sp, color = Color.Gray)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
    }
}
