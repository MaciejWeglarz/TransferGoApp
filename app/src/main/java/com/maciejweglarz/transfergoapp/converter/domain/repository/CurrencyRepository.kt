package com.maciejweglarz.transfergoapp.converter.domain.repository

import com.maciejweglarz.transfergoapp.converter.domain.model.FxQuote

interface CurrencyRepository {
    suspend fun getQuote(
        from: String,
        to: String,
        amount: Double
    ): FxQuote
}