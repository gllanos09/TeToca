package com.tetocaApp.tetoca.data.remote
data class TipoCambioResponse(
    val amount: Double,
    val base: String,
    val date: String,
    val rates: Map<String, Double>
)