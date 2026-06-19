package com.tetocaApp.tetoca.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tetocaApp.tetoca.data.local.Producto
import com.tetocaApp.tetoca.data.local.ProveedorDao
import com.tetocaApp.tetoca.data.local.TeTocaDatabase
import com.tetocaApp.tetoca.data.repository.ProductoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class DetalleUiState(
    val producto: Producto? = null,
    val proveedorNombre: String? = null,
    val cargando: Boolean = true,
    val error: String? = null
)

class DetalleViewModel(
    private val repo: ProductoRepository,
    private val proveedorDao: ProveedorDao,
    private val productoId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetalleUiState())
    val uiState: StateFlow<DetalleUiState> = _uiState.asStateFlow()

    init {
        observarProducto()
    }

    private fun observarProducto() {
        viewModelScope.launch {
            repo.observarPorId(productoId)
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        error = e.message ?: "Error al observar el producto"
                    )
                }
                .collect { producto ->
                    if (producto == null) {
                        _uiState.value = DetalleUiState(cargando = false, error = "El producto no existe")
                    } else {
                        val nombreProveedor = proveedorDao.obtenerPorId(producto.proveedorId)?.nombre
                        _uiState.value = DetalleUiState(
                            producto = producto,
                            proveedorNombre = nombreProveedor,
                            cargando = false
                        )
                    }
                }
        }
    }

    fun eliminar(onEliminado: () -> Unit) {
        val producto = _uiState.value.producto ?: return
        viewModelScope.launch {
            try {
                repo.eliminar(producto)
                onEliminado()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message ?: "No se pudo eliminar el producto")
            }
        }
    }

    companion object {
        fun Factory(context: Context, productoId: Long): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val db = TeTocaDatabase.getInstance(context)
                DetalleViewModel(
                    repo = ProductoRepository(db.productoDao()),
                    proveedorDao = db.proveedorDao(),
                    productoId = productoId
                )
            }
        }
    }
}
