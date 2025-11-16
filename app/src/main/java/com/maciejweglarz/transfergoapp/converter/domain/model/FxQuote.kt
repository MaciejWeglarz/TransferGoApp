package com.maciejweglarz.transfergoapp.converter.domain.model

data class FxQuote(
    val fromCurrency: String,
    val toCurrency: String,
    val amountFrom: Double,
    val amountTo: Double,
    val rate: Double
)
