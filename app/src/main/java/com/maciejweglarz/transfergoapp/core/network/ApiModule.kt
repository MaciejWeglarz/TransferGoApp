package com.maciejweglarz.transfergoapp.core.network

import com.maciejweglarz.transfergoapp.converter.data.remote.TransferGoApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    @Provides
    @Singleton
    fun provideTransferGoApiService(retrofit: Retrofit): TransferGoApiService =
        retrofit.create(TransferGoApiService::class.java)

}