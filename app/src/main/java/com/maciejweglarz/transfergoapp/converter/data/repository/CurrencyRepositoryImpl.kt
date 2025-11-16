package com.maciejweglarz.transfergoapp.converter.data.repository

import com.maciejweglarz.transfergoapp.converter.data.remote.TransferGoApiService
import com.maciejweglarz.transfergoapp.converter.domain.model.FxQuote
import com.maciejweglarz.transfergoapp.converter.domain.repository.CurrencyRepository
import javax.inject.Inject

class CurrencyRepositoryImpl @Inject constructor(
    private val api: TransferGoApiService
): CurrencyRepository {
    override suspend fun getQuote(
        from: String,
        to: String,
        amount: Double
    ): FxQuote {

        val response = api.getFxRates(from, to, amount)

        val rate = response.rate
        val fromAmount = response.fromAmount ?: amount
        val toAmount = response.toAmount ?: (rate * amount)

        return FxQuote(
            from,
            toCurrency = to,
            amountFrom = fromAmount,
            amountTo = toAmount,
            rate = rate
        )
    }
}