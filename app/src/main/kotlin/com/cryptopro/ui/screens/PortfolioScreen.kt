package com.cryptopro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cryptopro.domain.portfolio.RiskLevel
import com.cryptopro.ui.viewmodel.PortfolioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioScreen(
    viewModel: PortfolioViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        // Initialize with sample data
        viewModel.updatePortfolio(
            balance = 5000.0,
            holdings = mapOf("BTCUSDT" to 0.5, "ETHUSDT" to 5.0),
            prices = mapOf("BTCUSDT" to 45000.0, "ETHUSDT" to 2500.0)
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Portfolio") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                if (state.portfolio != null) {
                    PortfolioValueCard(portfolio = state.portfolio!!)
                }
            }

            item {
                if (state.riskMetrics != null) {
                    RiskMetricsCard(riskMetrics = state.riskMetrics!!)
                }
            }

            item {
                if (state.portfolio != null) {
                    AllocationCard(portfolio = state.portfolio!!)
                }
            }

            item {
                PositionSizingCard(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun PortfolioValueCard(portfolio: com.cryptopro.domain.portfolio.Portfolio) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Portfolio Value", fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "$${String.format("%.2f", portfolio.totalValue)}",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Balance", fontSize = 12.sp, color = Color.Gray)
                    Text(
                        "$${String.format("%.2f", portfolio.totalBalance)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Column {
                    Text("Holdings Value", fontSize = 12.sp, color = Color.Gray)
                    Text(
                        "$${String.format("%.2f", portfolio.currentValues.values.sum())}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun RiskMetricsCard(riskMetrics: com.cryptopro.domain.portfolio.RiskMetrics) {
    val riskColor = when (riskMetrics.riskLevel) {
        RiskLevel.LOW -> Color(0xFF2E7D32)
        RiskLevel.MEDIUM -> Color(0xFFF57C00)
        RiskLevel.HIGH -> Color(0xFFE65100)
        RiskLevel.CRITICAL -> Color(0xFFC62828)
    }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Risk Metrics", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Surface(
                    color = riskColor,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.padding(4.dp)
                ) {
                    Text(
                        riskMetrics.riskLevel.name,
                        modifier = Modifier.padding(8.dp, 4.dp),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            MetricRow(
                "Max Drawdown",
                "${String.format("%.2f", riskMetrics.maxDrawdown)}%"
            )
            Spacer(modifier = Modifier.height(8.dp))
            MetricRow(
                "Volatility",
                "${String.format("%.2f", riskMetrics.volatility)}%"
            )
            Spacer(modifier = Modifier.height(8.dp))
            MetricRow(
                "Sharpe Ratio",
                "${String.format("%.4f", riskMetrics.sharpeRatio)}"
            )
        }
    }
}

@Composable
fun AllocationCard(portfolio: com.cryptopro.domain.portfolio.Portfolio) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Asset Allocation", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            portfolio.allocation.forEach { (symbol, allocation) ->
                AllocationRow(
                    symbol = symbol,
                    percentage = allocation * 100,
                    value = portfolio.currentValues[symbol] ?: 0.0
                )
            }
        }
    }
}

@Composable
fun AllocationRow(symbol: String, percentage: Double, value: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(symbol, fontWeight = FontWeight.Bold)
            Text("${String.format("%.1f", percentage)}%", fontSize = 12.sp, color = Color.Gray)
        }
        Box(
            modifier = Modifier
                .weight(2f)
                .height(8.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        )
        Text(
            "$${String.format("%.0f", value)}",
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun PositionSizingCard(viewModel: PortfolioViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Risk Management", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            val positionSize = viewModel.getPositionSize(
                accountBalance = 10000.0,
                riskPercentage = 2.0,
                stopLossPercent = 2.0
            )

            val (stopLoss, takeProfit) = viewModel.getStopLossTakeProfit(
                entryPrice = 45000.0,
                riskRewardRatio = 2.0,
                stopLossPercent = 2.0
            )

            MetricRow(
                "Recommended Position Size",
                "$${String.format("%.0f", positionSize)}"
            )
            Spacer(modifier = Modifier.height(8.dp))
            MetricRow(
                "Entry Price",
                "$${String.format("%.2f", 45000.0)}"
            )
            Spacer(modifier = Modifier.height(8.dp))
            MetricRow(
                "Stop Loss",
                "$${String.format("%.2f", stopLoss)}",
                color = Color(0xFFC62828)
            )
            Spacer(modifier = Modifier.height(8.dp))
            MetricRow(
                "Take Profit",
                "$${String.format("%.2f", takeProfit)}",
                color = Color(0xFF2E7D32)
            )
        }
    }
}
