package com.tetocaApp.tetoca.data.remote

import retrofit2.http.GET
import retrofit2.http.Path

interface TipoCambioApi {
    // Ej: GET https://open.er-api.com/v6/latest/USD
    @GET("v6/latest/{base}")
    suspend fun obtenerTasas(@Path("base") base: String): TipoCambioResponse
}
