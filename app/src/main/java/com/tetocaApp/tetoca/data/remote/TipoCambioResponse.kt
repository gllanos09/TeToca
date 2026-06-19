package com.tetocaApp.tetoca.data.remote

import com.google.gson.annotations.SerializedName

data class TipoCambioResponse(
    val result: String,
    @SerializedName("base_code") val baseCode: String,
    val rates: Map<String, Double>
)