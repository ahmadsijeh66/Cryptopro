package com.cryptopro.domain.repository

import com.cryptopro.data.local.entities.CryptoDataEntity
import kotlinx.coroutines.flow.Flow

interface CryptoRepository {
    suspend fun fetchAndSaveCryptoData(symbol: String, interval: String)
    fun getCryptoDataBySymbol(symbol: String): Flow<List<CryptoDataEntity>>
    suspend fun getCryptoDataRange(symbol: String, startTime: Long, endTime: Long): List<CryptoDataEntity>
}

class CryptoRepositoryImpl(
    private val cryptoDataDao: com.cryptopro.data.local.dao.CryptoDataDao,
    private val binanceApi: com.cryptopro.data.remote.api.BinanceApi
) : CryptoRepository {

    override suspend fun fetchAndSaveCryptoData(symbol: String, interval: String) {
        try {
            val klines = binanceApi.getKlines(symbol, interval)
            val cryptoDataList = klines.map { kline ->
                CryptoDataEntity(
                    symbol = symbol,
                    timestamp = (kline[0] as Number).toLong(),
                    open = (kline[1] as String).toDouble(),
                    high = (kline[2] as String).toDouble(),
                    low = (kline[3] as String).toDouble(),
                    close = (kline[4] as String).toDouble(),
                    volume = (kline[7] as String).toDouble(),
                    quoteAssetVolume = (kline[8] as String).toDouble()
                )
            }
            cryptoDataDao.insertMultipleCryptoData(cryptoDataList)
        } catch (e: Exception) {
            throw Exception("Failed to fetch crypto data: ${e.message}")
        }
    }

    override fun getCryptoDataBySymbol(symbol: String): Flow<List<CryptoDataEntity>> {
        return cryptoDataDao.getCryptoDataBySymbol(symbol)
    }

    override suspend fun getCryptoDataRange(
        symbol: String,
        startTime: Long,
        endTime: Long
    ): List<CryptoDataEntity> {
        return cryptoDataDao.getCryptoDataRange(symbol, startTime, endTime)
    }
}
