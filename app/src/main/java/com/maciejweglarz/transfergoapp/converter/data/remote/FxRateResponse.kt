package com.maciejweglarz.transfergoapp.converter.data.remote

import com.squareup.moshi.Json

data class FxRateResponse(
    @Json(name = "rate") val rate: Double,
    @Json(name = "fromAmount") val fromAmount: Double?,
    @Json(name = "toAmount") val toAmount: Double?
)
