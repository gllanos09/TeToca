package com.authfirebaseappjulon.tetoca.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.authfirebaseappjulon.tetoca.data.local.Producto
import com.authfirebaseappjulon.tetoca.data.local.Proveedor
import com.authfirebaseappjulon.tetoca.data.local.ProveedorDao
import com.authfirebaseappjulon.tetoca.data.local.TeTocaDatabase
import com.authfirebaseappjulon.tetoca.data.repository.ProductoRepository
import com.authfirebaseappjulon.tetoca.data.repository.TipoCambioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FormularioUiState(
    val nombre: String = "",
    val categoria: String = "",
    val stockActual: String = "",
    val stockMinimo: String = "",
    val precio: String = "",
    val proveedorId: Long? = null,
    val proveedores: List<Proveedor> = emptyList(),
    val editando: Boolean = false,
    val guardando: Boolean = false,
    val guardado: Boolean = false,
    val error: String? = null,
    // Tipo de cambio (consumo de API con Retrofit)
    val tasaCargando: Boolean = true,
    val tasa: Double? = null,
    val tasaError: String? = null
)

class FormularioViewModel(
    private val productoRepo: ProductoRepository,
    // Lectura de proveedores para el selector. Se puede cambiar por ProveedorRepository
    // (el de Gabriel) sin tocar la vista.
    private val proveedorDao: ProveedorDao,
    private val tipoCambioRepo: TipoCambioRepository,
    private val productoId: Long?
) : ViewModel() {

    private val _uiState = MutableStateFlow(FormularioUiState(editando = productoId != null))
    val uiState: StateFlow<FormularioUiState> = _uiState.asStateFlow()

    init {
        observarProveedores()
        if (productoId != null) cargarProducto(productoId)
        cargarTipoCambio()
    }

    private fun observarProveedores() {
        viewModelScope.launch {
            proveedorDao.obtenerTodos().collect { lista ->
                _uiState.value = _uiState.value.copy(proveedores = lista)
            }
        }
    }

    private fun cargarProducto(id: Long) {
        viewModelScope.launch {
            val p = productoRepo.obtenerPorId(id) ?: return@launch
            _uiState.value = _uiState.value.copy(
                nombre = p.nombre,
                categoria = p.categoria,
                stockActual = p.stockActual.toString(),
                stockMinimo = p.stockMinimo.toString(),
                precio = p.precio?.toString() ?: "",
                proveedorId = p.proveedorId
            )
        }
    }

    fun cargarTipoCambio() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(tasaCargando = true, tasaError = null)
            try {
                val tasa = tipoCambioRepo.obtenerTasa("USD", "PEN")
                _uiState.value = _uiState.value.copy(tasa = tasa, tasaCargando = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    tasaCargando = false,
                    tasaError = "No se pudo obtener el tipo de cambio. Revisa tu conexión."
                )
            }
        }
    }

    fun onNombreChange(v: String) { _uiState.value = _uiState.value.copy(nombre = v, error = null) }
    fun onCategoriaChange(v: String) { _uiState.value = _uiState.value.copy(categoria = v, error = null) }
    fun onStockActualChange(v: String) {
        if (v.all { it.isDigit() }) _uiState.value = _uiState.value.copy(stockActual = v, error = null)
    }
    fun onStockMinimoChange(v: String) {
        if (v.all { it.isDigit() }) _uiState.value = _uiState.value.copy(stockMinimo = v, error = null)
    }
    fun onPrecioChange(v: String) { _uiState.value = _uiState.value.copy(precio = v, error = null) }
    fun onProveedorSeleccionado(id: Long) { _uiState.value = _uiState.value.copy(proveedorId = id, error = null) }

    fun guardar() {
        val s = _uiState.value
        val error = validar(s)
        if (error != null) {
            _uiState.value = s.copy(error = error)
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(guardando = true, error = null)
            try {
                val producto = Producto(
                    id = productoId ?: 0L,
                    nombre = s.nombre.trim(),
                    categoria = s.categoria.trim(),
                    stockActual = s.stockActual.toInt(),
                    stockMinimo = s.stockMinimo.toInt(),
                    precio = s.precio.trim().ifBlank { null }?.toDoubleOrNull(),
                    proveedorId = s.proveedorId!!
                )
                productoRepo.guardar(producto)
                _uiState.value = _uiState.value.copy(guardando = false, guardado = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    guardando = false,
                    error = e.message ?: "No se pudo guardar el producto"
                )
            }
        }
    }

    private fun validar(s: FormularioUiState): String? = when {
        s.nombre.isBlank() -> "El nombre es obligatorio"
        s.categoria.isBlank() -> "La categoría es obligatoria"
        s.stockActual.toIntOrNull() == null -> "El stock actual debe ser un número"
        s.stockMinimo.toIntOrNull() == null -> "El stock mínimo debe ser un número"
        s.precio.isNotBlank() && s.precio.toDoubleOrNull() == null -> "El precio no es un número válido"
        s.proveedorId == null -> "Debes seleccionar un proveedor"
        else -> null
    }

    companion object {
        fun Factory(context: Context, productoId: Long?): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val db = TeTocaDatabase.getInstance(context)
                FormularioViewModel(
                    productoRepo = ProductoRepository(db.productoDao()),
                    proveedorDao = db.proveedorDao(),
                    tipoCambioRepo = TipoCambioRepository(),
                    productoId = productoId
                )
            }
        }
    }
}
