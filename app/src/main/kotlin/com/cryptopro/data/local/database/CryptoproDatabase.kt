package com.cryptopro.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.cryptopro.data.local.dao.CryptoDataDao
import com.cryptopro.data.local.dao.StrategyDao
import com.cryptopro.data.local.dao.BacktestResultDao
import com.cryptopro.data.local.entities.CryptoDataEntity
import com.cryptopro.data.local.entities.StrategyEntity
import com.cryptopro.data.local.entities.BacktestResultEntity

@Database(
    entities = [
        CryptoDataEntity::class,
        StrategyEntity::class,
        BacktestResultEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class CryptoproDatabase : RoomDatabase() {
    abstract fun cryptoDataDao(): CryptoDataDao
    abstract fun strategyDao(): StrategyDao
    abstract fun backtestResultDao(): BacktestResultDao

    companion object {
        @Volatile
        private var INSTANCE: CryptoproDatabase? = null

        fun getInstance(context: Context): CryptoproDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CryptoproDatabase::class.java,
                    "cryptopro_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
