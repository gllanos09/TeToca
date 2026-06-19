package com.tetocaApp.tetoca.data.repository

import com.tetocaApp.tetoca.data.local.Producto
import com.tetocaApp.tetoca.data.local.ProductoDao
import kotlinx.coroutines.flow.Flow

/**
 * Única fuente de datos de productos para los ViewModel.
 * Encapsula el ProductoDao: las vistas y los ViewModel nunca tocan Room directamente.
 */
class ProductoRepository(private val dao: ProductoDao) {

    fun obtenerTodos(): Flow<List<Producto>> = dao.obtenerTodos()

    suspend fun obtenerPorId(id: Long): Producto? = dao.obtenerPorId(id)

    fun observarPorId(id: Long): Flow<Producto?> = dao.observarPorId(id)

    /** Si id == 0 inserta uno nuevo; si no, actualiza el existente. */
    suspend fun guardar(producto: Producto): Long =
        if (producto.id == 0L) {
            dao.insertar(producto)
        } else {
            dao.actualizar(producto)
            producto.id
        }

    suspend fun eliminar(producto: Producto) = dao.eliminar(producto)
}
