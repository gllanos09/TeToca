package com.tetocaApp.tetoca.data.remote

import retrofit2.http.GET
import retrofit2.http.Path

interface TipoCambioApi {
    @GET("v6/latest/{base}")
    suspend fun obtenerTasas(@Path("base") base: String): TipoCambioResponse
}