package com.cryptopro.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.cryptopro.data.local.entities.BacktestResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BacktestResultDao {
    @Insert
    suspend fun insertBacktestResult(result: BacktestResultEntity)

    @Query("SELECT * FROM backtest_results WHERE strategyId = :strategyId ORDER BY createdAt DESC")
    fun getBacktestResultsByStrategy(strategyId: Long): Flow<List<BacktestResultEntity>>

    @Query("SELECT * FROM backtest_results WHERE id = :resultId")
    suspend fun getBacktestResultById(resultId: Long): BacktestResultEntity?

    @Query("SELECT * FROM backtest_results ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentBacktestResults(limit: Int): Flow<List<BacktestResultEntity>>
}
