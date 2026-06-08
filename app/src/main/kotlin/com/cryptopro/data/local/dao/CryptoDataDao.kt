package com.cryptopro.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.cryptopro.data.local.entities.CryptoDataEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CryptoDataDao {
    @Insert
    suspend fun insertCryptoData(cryptoData: CryptoDataEntity)

    @Insert
    suspend fun insertMultipleCryptoData(cryptoDataList: List<CryptoDataEntity>)

    @Query("SELECT * FROM crypto_data WHERE symbol = :symbol ORDER BY timestamp DESC")
    fun getCryptoDataBySymbol(symbol: String): Flow<List<CryptoDataEntity>>

    @Query("SELECT * FROM crypto_data WHERE symbol = :symbol AND timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp ASC")
    suspend fun getCryptoDataRange(symbol: String, startTime: Long, endTime: Long): List<CryptoDataEntity>

    @Query("DELETE FROM crypto_data WHERE symbol = :symbol")
    suspend fun deleteCryptoData(symbol: String)
}
