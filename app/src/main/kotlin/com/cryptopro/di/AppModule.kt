package com.cryptopro.di

import android.content.Context
import androidx.room.Room
import com.cryptopro.data.local.database.CryptoproDatabase
import com.cryptopro.data.remote.api.BinanceApi
import com.cryptopro.domain.ml.LocalMLModel
import com.cryptopro.domain.ml.StrategyAnalyzer
import com.cryptopro.domain.repository.BacktestRepository
import com.cryptopro.domain.repository.CryptoRepository
import com.cryptopro.domain.repository.StrategyRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideBinanceApi(): BinanceApi {
        return Retrofit.Builder()
            .baseUrl("https://api.binance.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BinanceApi::class.java)
    }

    @Singleton
    @Provides
    fun provideCryptoproDatabase(@ApplicationContext context: Context): CryptoproDatabase {
        return Room.databaseBuilder(
            context,
            CryptoproDatabase::class.java,
            "cryptopro_database"
        ).build()
    }

    @Singleton
    @Provides
    fun provideStrategyAnalyzer(): StrategyAnalyzer {
        return LocalMLModel()
    }
}
