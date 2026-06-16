package com.authfirebaseappjulon.tetoca.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
@Dao
interface ProductoDao {

    @Insert
    suspend fun insertar(producto: Producto): Long

    @Update
    suspend fun actualizar(producto: Producto)

    @Delete
    suspend fun eliminar(producto: Producto)

    @Query("SELECT * FROM productos ORDER BY nombre ASC")
    fun obtenerTodos(): Flow<List<Producto>>

    @Query("SELECT * FROM productos WHERE id = :id")
    suspend fun obtenerPorId(id: Long): Producto?

    @Query("SELECT * FROM productos WHERE proveedorId = :proveedorId")
    fun obtenerPorProveedor(proveedorId: Long): Flow<List<Producto>>

    // Usada en la Parte 2 para la pantalla "Productos por reponer":
    // devuelve los productos cuyo stock actual ya cayó por debajo del mínimo.
    @Query("SELECT * FROM productos WHERE stockActual < stockMinimo ORDER BY nombre ASC")
    fun obtenerConStockBajo(): Flow<List<Producto>>
}