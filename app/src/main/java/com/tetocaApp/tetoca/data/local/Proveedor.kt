package com.tetocaApp.tetoca.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "proveedores")
data class Proveedor(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val nombre: String,

    val telefono: String,

    val notas: String? = null,

    val fechaRegistro: Long = System.currentTimeMillis()
)