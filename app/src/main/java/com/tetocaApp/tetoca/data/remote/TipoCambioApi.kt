package com.tetocaApp.tetoca.data.remote

import retrofit2.http.GET
import retrofit2.http.Query
interface TipoCambioApi {
    @GET("v1/latest")
    suspend fun obtenerTasas(
        @Query("base") base: String,
        @Query("symbols") symbols: String
    ): TipoCambioResponse
}