package com.cryptopro.domain.repository

import com.cryptopro.data.local.entities.BacktestResultEntity
import com.cryptopro.data.local.dao.BacktestResultDao
import kotlinx.coroutines.flow.Flow

interface BacktestRepository {
    suspend fun saveBacktestResult(result: BacktestResultEntity)
    fun getBacktestResultsByStrategy(strategyId: Long): Flow<List<BacktestResultEntity>>
    suspend fun getBacktestResultById(resultId: Long): BacktestResultEntity?
    fun getRecentResults(limit: Int = 10): Flow<List<BacktestResultEntity>>
}

class BacktestRepositoryImpl(private val backtestResultDao: BacktestResultDao) : BacktestRepository {

    override suspend fun saveBacktestResult(result: BacktestResultEntity) {
        backtestResultDao.insertBacktestResult(result)
    }

    override fun getBacktestResultsByStrategy(strategyId: Long): Flow<List<BacktestResultEntity>> {
        return backtestResultDao.getBacktestResultsByStrategy(strategyId)
    }

    override suspend fun getBacktestResultById(resultId: Long): BacktestResultEntity? {
        return backtestResultDao.getBacktestResultById(resultId)
    }

    override fun getRecentResults(limit: Int): Flow<List<BacktestResultEntity>> {
        return backtestResultDao.getRecentBacktestResults(limit)
    }
}
