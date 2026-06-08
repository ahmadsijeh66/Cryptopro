package com.cryptopro.domain.repository

import com.cryptopro.data.local.entities.StrategyEntity
import kotlinx.coroutines.flow.Flow

interface StrategyRepository {
    suspend fun createStrategy(strategy: StrategyEntity)
    suspend fun updateStrategy(strategy: StrategyEntity)
    suspend fun deleteStrategy(strategy: StrategyEntity)
    fun getAllStrategies(): Flow<List<StrategyEntity>>
    suspend fun getStrategyById(strategyId: Long): StrategyEntity?
}
