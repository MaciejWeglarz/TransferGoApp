package com.maciejweglarz.transfergoapp.core.model

import com.maciejweglarz.transfergoapp.R

object Currencies {

    val list: List<Currency> = listOf(
        Currency(
            country = "Poland",
            code = "PLN",
            name = "Polish zloty",
            maxAmount = 20000.0,
            flagRes = R.drawable.flag_pol
        ),
        Currency(
            country = "Germany",
            code = "EUR",
            name = "Euro",
            maxAmount = 5000.0,
            flagRes = R.drawable.flag_ger
        ),
        Currency(
            country = "Great Britain",
            code = "GBP",
            name = "British Pound",
            maxAmount = 1000.0,
            flagRes = R.drawable.flag_eng
        ),
        Currency(
            country = "Ukraine",
            code = "UAH",
            name = "Hrivna",
            maxAmount = 50000.0,
            flagRes = R.drawable.flag_uah
        )
    )

    fun getByCode(code: String): Currency =
        list.first { it.code == code }
}
