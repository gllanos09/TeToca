package com.tetocaApp.tetoca.data.repository

import com.tetocaApp.tetoca.data.remote.RetrofitClient
import com.tetocaApp.tetoca.data.remote.TipoCambioApi

class TipoCambioRepository(private val api: TipoCambioApi = RetrofitClient.api) {
    suspend fun obtenerTasa(base: String = "USD", destino: String = "PEN"): Double {
        val respuesta = api.obtenerTasas(base = base, symbols = destino)
        return respuesta.rates[destino]
            ?: throw IllegalStateException("No se encontró la tasa de $base a $destino")
    }
}