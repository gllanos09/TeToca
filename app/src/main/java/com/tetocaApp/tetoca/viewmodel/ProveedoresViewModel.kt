package com.tetocaApp.tetoca.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tetocaApp.tetoca.data.local.Proveedor
import com.tetocaApp.tetoca.data.local.TeTocaDatabase
import com.tetocaApp.tetoca.data.repository.ProveedorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import android.database.sqlite.SQLiteConstraintException

/** Estado que la vista observa. Todo lo que la pantalla necesita pintar vive aquí. */
data class ProveedoresUiState(
    val proveedores: List<Proveedor> = emptyList(),
    val cargando: Boolean = true,
    val error: String? = null,
    // Controla si el formulario (bottom sheet) está visible, y si está
    // editando un proveedor existente o creando uno nuevo.
    val mostrarFormulario: Boolean = false,
    val proveedorEnEdicion: Proveedor? = null,
    val guardando: Boolean = false
)

class ProveedoresViewModel(private val repo: ProveedorRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ProveedoresUiState())
    val uiState: StateFlow<ProveedoresUiState> = _uiState.asStateFlow()

    init {
        observarProveedores()
    }

    private fun observarProveedores() {
        viewModelScope.launch {
            repo.obtenerTodos()
                .onStart { _uiState.value = _uiState.value.copy(cargando = true, error = null) }
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        error = e.message ?: "Error al cargar los proveedores"
                    )
                }
                .collect { lista ->
                    _uiState.value = _uiState.value.copy(
                        proveedores = lista,
                        cargando = false,
                        error = null
                    )
                }
        }
    }

    /** Abre el bottom sheet en modo "nuevo proveedor". */
    fun onNuevoProveedor() {
        _uiState.value = _uiState.value.copy(mostrarFormulario = true, proveedorEnEdicion = null)
    }

    /** Abre el bottom sheet en modo "editar" con los datos ya cargados. */
    fun onEditarProveedor(proveedor: Proveedor) {
        _uiState.value = _uiState.value.copy(mostrarFormulario = true, proveedorEnEdicion = proveedor)
    }

    fun onCerrarFormulario() {
        _uiState.value = _uiState.value.copy(mostrarFormulario = false, proveedorEnEdicion = null)
    }

    fun guardar(nombre: String, telefono: String, notas: String?) {
        val nombreLimpio = nombre.trim()
        val telefonoLimpio = telefono.trim()
        if (nombreLimpio.isBlank() || telefonoLimpio.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Nombre y teléfono son obligatorios")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(guardando = true, error = null)
            try {
                val existente = _uiState.value.proveedorEnEdicion
                val proveedor = Proveedor(
                    id = existente?.id ?: 0L,
                    nombre = nombreLimpio,
                    telefono = telefonoLimpio,
                    notas = notas?.trim()?.ifBlank { null },
                    fechaRegistro = existente?.fechaRegistro ?: System.currentTimeMillis()
                )
                repo.guardar(proveedor)
                _uiState.value = _uiState.value.copy(
                    guardando = false,
                    mostrarFormulario = false,
                    proveedorEnEdicion = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    guardando = false,
                    error = "No se pudo guardar el proveedor"
                )
            }
        }
    }

    /**
     * Intenta eliminar un proveedor. Si Room lo rechaza porque todavía
     * tiene productos asociados (clave foránea con onDelete = RESTRICT),
     * se captura SQLiteConstraintException y se muestra un mensaje claro,
     * en vez de que la app se caiga.
     */
    fun eliminar(proveedor: Proveedor) {
        viewModelScope.launch {
            try {
                repo.eliminar(proveedor)
            } catch (e: SQLiteConstraintException) {
                _uiState.value = _uiState.value.copy(
                    error = "No se puede eliminar \"${proveedor.nombre}\": todavía tiene productos asociados. Reasigna o elimina esos productos primero."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "No se pudo eliminar el proveedor"
                )
            }
        }
    }

    fun limpiarError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    companion object {
        /** Crea el ViewModel inyectando el Repository desde la base de datos. */
        fun Factory(context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val dao = TeTocaDatabase.getInstance(context).proveedorDao()
                ProveedoresViewModel(ProveedorRepository(dao))
            }
        }
    }
}