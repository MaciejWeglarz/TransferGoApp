package com.maciejweglarz.transfergoapp.converter.data.repository

import com.maciejweglarz.transfergoapp.converter.data.remote.TransferGoApiService
import com.maciejweglarz.transfergoapp.converter.domain.model.FxQuote
import com.maciejweglarz.transfergoapp.converter.domain.repository.CurrencyRepository
import javax.inject.Inject

class CurrencyRepositoryImpl @Inject constructor(
    private val api: TransferGoApiService
): CurrencyRepository {
    override suspend fun getQuote(
        fromCurrency: String,
        toCurrency: String,
        amount: Double
    ): FxQuote {

        val response = api.getFxRates(fromCurrency, toCurrency, amount)

        val rate = response.rate
        val toAmount = response.toAmount

        return FxQuote(
            fromCurrency,
            toCurrency = toCurrency,
            amountFrom = amount,
            amountTo = toAmount,
            rate = rate
        )
    }
}