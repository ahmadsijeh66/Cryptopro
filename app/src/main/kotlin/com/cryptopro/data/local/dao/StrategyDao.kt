package com.cryptopro.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Update
import androidx.room.Delete
import androidx.room.Query
import com.cryptopro.data.local.entities.StrategyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StrategyDao {
    @Insert
    suspend fun insertStrategy(strategy: StrategyEntity)

    @Update
    suspend fun updateStrategy(strategy: StrategyEntity)

    @Delete
    suspend fun deleteStrategy(strategy: StrategyEntity)

    @Query("SELECT * FROM strategies ORDER BY createdAt DESC")
    fun getAllStrategies(): Flow<List<StrategyEntity>>

    @Query("SELECT * FROM strategies WHERE id = :strategyId")
    suspend fun getStrategyById(strategyId: Long): StrategyEntity?
}
