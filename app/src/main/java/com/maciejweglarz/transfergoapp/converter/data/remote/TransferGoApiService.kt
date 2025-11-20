package com.maciejweglarz.transfergoapp.converter.data.remote

import retrofit2.http.Query
import retrofit2.http.GET

interface TransferGoApiService {

    @GET("api/fx-rates")
    suspend fun getFxRates(
        @Query("from") fromCurrency: String,
        @Query("to") toCurrency: String,
        @Query("amount") amount: Double
    ): FxRateResponse

}