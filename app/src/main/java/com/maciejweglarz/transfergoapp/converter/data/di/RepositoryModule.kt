package com.maciejweglarz.transfergoapp.converter.data.di

import com.maciejweglarz.transfergoapp.converter.data.remote.TransferGoApiService
import com.maciejweglarz.transfergoapp.converter.data.repository.CurrencyRepositoryImpl
import com.maciejweglarz.transfergoapp.converter.domain.repository.CurrencyRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideCurrencyRepository(
        api: TransferGoApiService
    ): CurrencyRepository = CurrencyRepositoryImpl(api)
}