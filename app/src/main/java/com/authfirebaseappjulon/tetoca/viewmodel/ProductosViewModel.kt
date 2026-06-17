package com.authfirebaseappjulon.tetoca.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.authfirebaseappjulon.tetoca.data.local.Producto
import com.authfirebaseappjulon.tetoca.data.local.TeTocaDatabase
import com.authfirebaseappjulon.tetoca.data.repository.ProductoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

/** Estado que la vista observa. Todo lo que la pantalla necesita pintar vive aquí. */
data class ProductosUiState(
    val productos: List<Producto> = emptyList(),
    val cargando: Boolean = true,
    val error: String? = null
)

class ProductosViewModel(private val repo: ProductoRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductosUiState())
    val uiState: StateFlow<ProductosUiState> = _uiState.asStateFlow()

    init {
        observarProductos()
    }

    private fun observarProductos() {
        viewModelScope.launch {
            repo.obtenerTodos()
                .onStart { _uiState.value = _uiState.value.copy(cargando = true, error = null) }
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        error = e.message ?: "Error al cargar los productos"
                    )
                }
                .collect { lista ->
                    _uiState.value = ProductosUiState(productos = lista, cargando = false, error = null)
                }
        }
    }

    companion object {
        /** Crea el ViewModel inyectando el Repository desde la base de datos. */
        fun Factory(context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val dao = TeTocaDatabase.getInstance(context).productoDao()
                ProductosViewModel(ProductoRepository(dao))
            }
        }
    }
}
