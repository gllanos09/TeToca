package com.tetocaApp.tetoca.data.local

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

    @Query("SELECT * FROM productos WHERE id = :id")
    fun observarPorId(id: Long): Flow<Producto?>

    @Query("SELECT * FROM productos WHERE proveedorId = :proveedorId")
    fun obtenerPorProveedor(proveedorId: Long): Flow<List<Producto>>

    @Query("SELECT * FROM productos WHERE stockActual < stockMinimo ORDER BY nombre ASC")
    fun obtenerConStockBajo(): Flow<List<Producto>>

    /** Para WorkManager — lectura única sin Flow. */
    @Query("SELECT * FROM productos WHERE stockActual < stockMinimo ORDER BY nombre ASC")
    suspend fun obtenerConStockBajoUnaVez(): List<Producto>

    // ── Estadísticas ───────────────────────────────────────────────────

    /** Total de SKUs activos (productos distintos). */
    @Query("SELECT COUNT(*) FROM productos")
    fun contarSkus(): Flow<Int>

    /** Valor total del inventario (precio * stockActual). */
    @Query("SELECT SUM(precio * stockActual) FROM productos WHERE precio IS NOT NULL")
    fun calcularValorTotal(): Flow<Double?>

    /** Categoría con más unidades en stock. */
    @Query("""
        SELECT categoria FROM productos
        GROUP BY categoria
        ORDER BY SUM(stockActual) DESC
        LIMIT 1
    """)
    fun categoriaMasStock(): Flow<String?>

    /** Total de unidades en stock (para mostrar en estadísticas). */
    @Query("SELECT SUM(stockActual) FROM productos")
    fun totalUnidades(): Flow<Int?>
}