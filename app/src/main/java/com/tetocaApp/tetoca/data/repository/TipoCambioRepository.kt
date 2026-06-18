package com.tetocaApp.tetoca.data.repository

import com.tetocaApp.tetoca.data.remote.RetrofitClient
import com.tetocaApp.tetoca.data.remote.TipoCambioApi

/** Fuente de datos del tipo de cambio. Aísla a Retrofit del resto de la app. */
class TipoCambioRepository(private val api: TipoCambioApi = RetrofitClient.api) {

    /** Devuelve la tasa de [base] a [destino] (por defecto USD -> PEN). Lanza excepción si falla. */
    suspend fun obtenerTasa(base: String = "USD", destino: String = "PEN"): Double {
        val respuesta = api.obtenerTasas(base)
        if (respuesta.result != "success") {
            throw IllegalStateException("La API de tipo de cambio respondió con error")
        }
        return respuesta.rates[destino]
            ?: throw IllegalStateException("No se encontró la tasa de $base a $destino")
    }
}