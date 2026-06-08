package com.cryptopro.domain.repository

import com.cryptopro.data.local.entities.BacktestResultEntity
import kotlinx.coroutines.flow.Flow

interface BacktestRepository {
    suspend fun saveBacktestResult(result: BacktestResultEntity)
    fun getBacktestResultsByStrategy(strategyId: Long): Flow<List<BacktestResultEntity>>
    suspend fun getBacktestResultById(resultId: Long): BacktestResultEntity?
    fun getRecentResults(limit: Int = 10): Flow<List<BacktestResultEntity>>
}
