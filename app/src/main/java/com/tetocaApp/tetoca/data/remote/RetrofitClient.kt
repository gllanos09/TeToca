package com.tetocaApp.tetoca.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
object RetrofitClient {
    private const val BASE_URL = "https://api.frankfurter.dev/"

    val api: TipoCambioApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TipoCambioApi::class.java)
    }
}