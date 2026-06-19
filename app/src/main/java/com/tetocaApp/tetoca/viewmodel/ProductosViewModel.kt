package com.tetocaApp.tetoca.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tetocaApp.tetoca.data.local.Producto
import com.tetocaApp.tetoca.data.local.TeTocaDatabase
import com.tetocaApp.tetoca.data.repository.ProductoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

enum class FiltroStock { TODOS, CRITICO, BAJO, OK }

data class ProductosUiState(
    val productos: List<Producto> = emptyList(),
    val productosFiltrados: List<Producto> = emptyList(),
    val query: String = "",
    val filtro: FiltroStock = FiltroStock.TODOS,
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
                    _uiState.value = _uiState.value.copy(
                        productos = lista,
                        productosFiltrados = filtrar(lista, _uiState.value.query, _uiState.value.filtro),
                        cargando = false
                    )
                }
        }
    }

    fun onQueryChange(newQuery: String) {
        _uiState.value = _uiState.value.copy(
            query = newQuery,
            productosFiltrados = filtrar(_uiState.value.productos, newQuery, _uiState.value.filtro)
        )
    }

    fun onFiltroChange(nuevoFiltro: FiltroStock) {
        _uiState.value = _uiState.value.copy(
            filtro = nuevoFiltro,
            productosFiltrados = filtrar(_uiState.value.productos, _uiState.value.query, nuevoFiltro)
        )
    }

    private fun filtrar(lista: List<Producto>, query: String, filtro: FiltroStock): List<Producto> {
        return lista.filter { p ->
            val coincideTexto = p.nombre.contains(query, ignoreCase = true) || 
                               p.categoria.contains(query, ignoreCase = true)
            
            val coincideFiltro = when (filtro) {
                FiltroStock.TODOS -> true
                FiltroStock.CRITICO -> p.stockActual <= p.stockMinimo
                FiltroStock.BAJO -> p.stockActual > p.stockMinimo && p.stockActual <= p.stockMinimo * 2
                FiltroStock.OK -> p.stockActual > p.stockMinimo * 2
            }
            
            coincideTexto && coincideFiltro
        }
    }

    companion object {
        fun Factory(context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val dao = TeTocaDatabase.getInstance(context).productoDao()
                ProductosViewModel(ProductoRepository(dao))
            }
        }
    }
}
