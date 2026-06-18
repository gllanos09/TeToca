package com.tetocaApp.tetoca.data.repository

import com.tetocaApp.tetoca.data.local.Proveedor
import com.tetocaApp.tetoca.data.local.ProveedorDao
import kotlinx.coroutines.flow.Flow

class ProveedorRepository(private val dao: ProveedorDao) {

    fun obtenerTodos(): Flow<List<Proveedor>> = dao.obtenerTodos()

    suspend fun obtenerPorId(id: Long): Proveedor? = dao.obtenerPorId(id)

    /** Si id == 0 inserta uno nuevo; si no, actualiza el existente. */
    suspend fun guardar(proveedor: Proveedor): Long =
        if (proveedor.id == 0L) {
            dao.insertar(proveedor)
        } else {
            dao.actualizar(proveedor)
            proveedor.id
        }

    suspend fun eliminar(proveedor: Proveedor) = dao.eliminar(proveedor)
}