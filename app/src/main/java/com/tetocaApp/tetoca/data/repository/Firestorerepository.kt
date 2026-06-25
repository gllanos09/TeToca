package com.tetocaApp.tetoca.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tetocaApp.tetoca.data.local.Producto
import com.tetocaApp.tetoca.data.local.Proveedor
import kotlinx.coroutines.tasks.await

/**
 * Sincroniza productos y proveedores con Cloud Firestore,
 * segmentados por UID del usuario autenticado.
 *
 * Estructura en Firestore:
 *   usuarios/{uid}/productos/{productoId}
 *   usuarios/{uid}/proveedores/{proveedorId}
 *
 * Room sigue siendo la fuente de verdad local.
 * Firestore actúa como respaldo en la nube.
 */
class FirestoreRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    /** UID del usuario autenticado actual */
    private val uid: String?
        get() = auth.currentUser?.uid

    /** Colección de productos del usuario actual */
    private fun productosCol() = uid?.let {
        db.collection("usuarios").document(it).collection("productos")
    }

    /** Colección de proveedores del usuario actual */
    private fun proveedoresCol() = uid?.let {
        db.collection("usuarios").document(it).collection("proveedores")
    }

    // ── Productos ──────────────────────────────────────────────────────

    suspend fun guardarProducto(producto: Producto) {
        productosCol()?.document(producto.id.toString())?.set(
            mapOf(
                "id"           to producto.id,
                "nombre"       to producto.nombre,
                "categoria"    to producto.categoria,
                "stockActual"  to producto.stockActual,
                "stockMinimo"  to producto.stockMinimo,
                "precio"       to producto.precio,
                "proveedorId"  to producto.proveedorId
            )
        )?.await()
    }

    suspend fun eliminarProducto(productoId: Long) {
        productosCol()?.document(productoId.toString())?.delete()?.await()
    }

    // ── Proveedores ────────────────────────────────────────────────────

    suspend fun guardarProveedor(proveedor: Proveedor) {
        proveedoresCol()?.document(proveedor.id.toString())?.set(
            mapOf(
                "id"            to proveedor.id,
                "nombre"        to proveedor.nombre,
                "telefono"      to proveedor.telefono,
                "notas"         to proveedor.notas,
                "fechaRegistro" to proveedor.fechaRegistro
            )
        )?.await()
    }

    suspend fun eliminarProveedor(proveedorId: Long) {
        proveedoresCol()?.document(proveedorId.toString())?.delete()?.await()
    }
}