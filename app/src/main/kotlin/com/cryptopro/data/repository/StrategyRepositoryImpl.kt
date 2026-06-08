package com.cryptopro.domain.repository

import com.cryptopro.data.local.entities.StrategyEntity
import com.cryptopro.data.local.dao.StrategyDao
import kotlinx.coroutines.flow.Flow

interface StrategyRepository {
    suspend fun createStrategy(strategy: StrategyEntity)
    suspend fun updateStrategy(strategy: StrategyEntity)
    suspend fun deleteStrategy(strategy: StrategyEntity)
    fun getAllStrategies(): Flow<List<StrategyEntity>>
    suspend fun getStrategyById(strategyId: Long): StrategyEntity?
}

class StrategyRepositoryImpl(private val strategyDao: StrategyDao) : StrategyRepository {

    override suspend fun createStrategy(strategy: StrategyEntity) {
        strategyDao.insertStrategy(strategy)
    }

    override suspend fun updateStrategy(strategy: StrategyEntity) {
        strategyDao.updateStrategy(strategy)
    }

    override suspend fun deleteStrategy(strategy: StrategyEntity) {
        strategyDao.deleteStrategy(strategy)
    }

    override fun getAllStrategies(): Flow<List<StrategyEntity>> {
        return strategyDao.getAllStrategies()
    }

    override suspend fun getStrategyById(strategyId: Long): StrategyEntity? {
        return strategyDao.getStrategyById(strategyId)
    }
}
