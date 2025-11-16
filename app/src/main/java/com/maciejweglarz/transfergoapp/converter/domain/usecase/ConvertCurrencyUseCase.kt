package com.maciejweglarz.transfergoapp.converter.domain.usecase

import com.maciejweglarz.transfergoapp.converter.domain.model.FxQuote
import com.maciejweglarz.transfergoapp.converter.domain.repository.CurrencyRepository
import javax.inject.Inject

class ConvertCurrencyUseCase @Inject constructor(
    private val repository: CurrencyRepository
) {
    suspend operator fun invoke(
        from: String,
        to: String,
        amount: Double
    ): FxQuote {
        return repository.getQuote(from, to, amount)
    }
}