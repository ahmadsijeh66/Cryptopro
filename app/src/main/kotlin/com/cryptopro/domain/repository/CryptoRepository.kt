package com.cryptopro.domain.repository

import com.cryptopro.data.local.entities.CryptoDataEntity
import kotlinx.coroutines.flow.Flow

interface CryptoRepository {
    suspend fun fetchAndSaveCryptoData(symbol: String, interval: String)
    fun getCryptoDataBySymbol(symbol: String): Flow<List<CryptoDataEntity>>
    suspend fun getCryptoDataRange(symbol: String, startTime: Long, endTime: Long): List<CryptoDataEntity>
}
