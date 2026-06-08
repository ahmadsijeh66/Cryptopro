package com.cryptopro.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "backtest_results")
data class BacktestResultEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val strategyId: Long,
    val symbol: String,
    val startDate: Long,
    val endDate: Long,
    val initialBalance: Double,
    val finalBalance: Double,
    val totalReturn: Double,
    val winRate: Double,
    val maxDrawdown: Double,
    val totalTrades: Int,
    val createdAt: Long
)
