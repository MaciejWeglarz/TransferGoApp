package com.maciejweglarz.transfergoapp.core.model

import androidx.annotation.DrawableRes

data class Currency(
    val country: String,
    val code: String,
    val name: String,
    val maxAmount: Double,
    @DrawableRes val flagRes: Int
)
