package com.maciejweglarz.transfergoapp.converter.domain.usecase

import com.maciejweglarz.transfergoapp.converter.domain.repository.CurrencyRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideConvertCurrencyUseCase(
        repository: CurrencyRepository
    ): ConvertCurrencyUseCase = ConvertCurrencyUseCase(repository)

}