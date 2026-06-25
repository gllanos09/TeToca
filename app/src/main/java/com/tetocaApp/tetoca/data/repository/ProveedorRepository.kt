package com.tetocaApp.tetoca.data.repository

import com.tetocaApp.tetoca.data.local.Proveedor
import com.tetocaApp.tetoca.data.local.ProveedorDao
import kotlinx.coroutines.flow.Flow

/**
 * Única fuente de datos de proveedores para los ViewModel.
 * Escribe en Room (local) y sincroniza con Firestore (nube) en cada operación.
 */
class ProveedorRepository(
    private val dao: ProveedorDao,
    private val firestore: FirestoreRepository = FirestoreRepository()
) {

    fun obtenerTodos(): Flow<List<Proveedor>> = dao.obtenerTodos()

    suspend fun obtenerPorId(id: Long): Proveedor? = dao.obtenerPorId(id)

    /** Guarda en Room y sincroniza con Firestore. */
    suspend fun guardar(proveedor: Proveedor): Long {
        val id = if (proveedor.id == 0L) {
            dao.insertar(proveedor)
        } else {
            dao.actualizar(proveedor)
            proveedor.id
        }
        val proveedorConId = if (proveedor.id == 0L) proveedor.copy(id = id) else proveedor
        runCatching { firestore.guardarProveedor(proveedorConId) }
        return id
    }

    /** Elimina de Room y sincroniza con Firestore. */
    suspend fun eliminar(proveedor: Proveedor) {
        dao.eliminar(proveedor)
        runCatching { firestore.eliminarProveedor(proveedor.id) }
    }
}