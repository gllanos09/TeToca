package com.authfirebaseappjulon.tetoca.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProveedorDao {

    @Insert
    suspend fun insertar(proveedor: Proveedor): Long

    @Update
    suspend fun actualizar(proveedor: Proveedor)

    @Delete
    suspend fun eliminar(proveedor: Proveedor)

    @Query("SELECT * FROM proveedores ORDER BY nombre ASC")
    fun obtenerTodos(): Flow<List<Proveedor>>

    @Query("SELECT * FROM proveedores WHERE id = :id")
    suspend fun obtenerPorId(id: Long): Proveedor?
}