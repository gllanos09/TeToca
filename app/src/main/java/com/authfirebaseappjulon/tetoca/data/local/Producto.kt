package com.authfirebaseappjulon.tetoca.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity de Room que representa la tabla "productos".
 *
 * Es la entidad principal del CRUD de la Parte 1. Cada producto pertenece
 * obligatoriamente a un Proveedor (proveedorId no es nullable).
 *
 * Si se intenta eliminar un Proveedor que todavía tiene productos asociados,
 * Room lanzará un error en tiempo de ejecución (onDelete = RESTRICT): primero
 * hay que reasignar o eliminar esos productos.
 */
@Entity(
    tableName = "productos",
    foreignKeys = [
        ForeignKey(
            entity = Proveedor::class,
            parentColumns = ["id"],
            childColumns = ["proveedorId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("proveedorId")]
)
data class Producto(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val nombre: String,

    val categoria: String,

    val stockActual: Int,

    val stockMinimo: Int,

    val precio: Double? = null,

    val proveedorId: Long
)