package com.tetocaApp.tetoca.data.repository

import com.tetocaApp.tetoca.data.local.Producto
import com.tetocaApp.tetoca.data.local.ProductoDao
import kotlinx.coroutines.flow.Flow

/**
 * Única fuente de datos de productos para los ViewModel.
 * Escribe en Room (local) y sincroniza con Firestore (nube) en cada operación.
 */
class ProductoRepository(
    private val dao: ProductoDao,
    private val firestore: FirestoreRepository = FirestoreRepository()
) {

    fun obtenerTodos(): Flow<List<Producto>> = dao.obtenerTodos()

    suspend fun obtenerPorId(id: Long): Producto? = dao.obtenerPorId(id)

    fun observarPorId(id: Long): Flow<Producto?> = dao.observarPorId(id)

    /** Guarda en Room y sincroniza con Firestore. */
    suspend fun guardar(producto: Producto): Long {
        val id = if (producto.id == 0L) {
            dao.insertar(producto)
        } else {
            dao.actualizar(producto)
            producto.id
        }
        // Sincronizar con Firestore usando el id real asignado por Room
        val productoConId = if (producto.id == 0L) producto.copy(id = id) else producto
        runCatching { firestore.guardarProducto(productoConId) }
        return id
    }

    /** Elimina de Room y sincroniza con Firestore. */
    suspend fun eliminar(producto: Producto) {
        dao.eliminar(producto)
        runCatching { firestore.eliminarProducto(producto.id) }
    }
}