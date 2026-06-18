package com.tetocaApp.tetoca.ui.proveedores

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tetocaApp.tetoca.data.local.Proveedor
import com.tetocaApp.tetoca.viewmodel.ProveedoresViewModel

/**
 * Pantalla de Proveedores: lista + CRUD completo.
 *
 * El formulario de crear/editar se muestra como ModalBottomSheet (no
 * navega a otra pantalla), tal como se decidió para esta vertical.
 *
 * El ViewModel se observa con collectAsState(), siguiendo el mismo
 * patrón MVVM que ListaProductosScreen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProveedoresScreen() {
    val context = LocalContext.current
    val viewModel: ProveedoresViewModel = viewModel(
        factory = ProveedoresViewModel.Factory(context)
    )
    val uiState by viewModel.uiState.collectAsState()

    // Proveedor que el usuario intenta eliminar; se usa para el diálogo de confirmación.
    var proveedorAEliminar by rememberSaveable { mutableStateOf<Proveedor?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Proveedores") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onNuevoProveedor() },
                containerColor = MaterialTheme.colorScheme.secondary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Nuevo proveedor")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.cargando -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                uiState.proveedores.isEmpty() -> {
                    Text(
                        "Todavía no tienes proveedores registrados.\nUsa el botón + para agregar el primero.",
                        modifier = Modifier.align(Alignment.Center).padding(24.dp)
                    )
                }

                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                        items(uiState.proveedores) { proveedor ->
                            ProveedorItem(
                                proveedor = proveedor,
                                onEditar = { viewModel.onEditarProveedor(proveedor) },
                                onEliminar = { proveedorAEliminar = proveedor }
                            )
                        }
                    }
                }
            }
        }
    }

    // Bottom sheet con el formulario de crear/editar.
    if (uiState.mostrarFormulario) {
        FormularioProveedorBottomSheet(
            proveedorEnEdicion = uiState.proveedorEnEdicion,
            guardando = uiState.guardando,
            onGuardar = { nombre, telefono, notas -> viewModel.guardar(nombre, telefono, notas) },
            onCerrar = { viewModel.onCerrarFormulario() }
        )
    }

    // Diálogo de confirmación antes de eliminar.
    proveedorAEliminar?.let { proveedor ->
        AlertDialog(
            onDismissRequest = { proveedorAEliminar = null },
            title = { Text("Eliminar proveedor") },
            text = { Text("¿Seguro que quieres eliminar a \"${proveedor.nombre}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminar(proveedor)
                    proveedorAEliminar = null
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { proveedorAEliminar = null }) { Text("Cancelar") }
            }
        )
    }

    // Mensaje de error (incluye el caso de RESTRICT: proveedor con productos asociados).
    uiState.error?.let { mensaje ->
        AlertDialog(
            onDismissRequest = { viewModel.limpiarError() },
            title = { Text("No se pudo completar la acción") },
            text = { Text(mensaje) },
            confirmButton = {
                TextButton(onClick = { viewModel.limpiarError() }) { Text("Entendido") }
            }
        )
    }
}

@Composable
private fun ProveedorItem(
    proveedor: Proveedor,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(proveedor.nombre, style = MaterialTheme.typography.titleMedium)
                Text(proveedor.telefono, style = MaterialTheme.typography.bodyMedium)
                proveedor.notas?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall)
                }
            }
            Row {
                IconButton(onClick = onEditar) {
                    Icon(Icons.Filled.Edit, contentDescription = "Editar proveedor")
                }
                IconButton(onClick = onEliminar) {
                    Icon(Icons.Filled.Delete, contentDescription = "Eliminar proveedor")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormularioProveedorBottomSheet(
    proveedorEnEdicion: Proveedor?,
    guardando: Boolean,
    onGuardar: (nombre: String, telefono: String, notas: String?) -> Unit,
    onCerrar: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    var nombre by rememberSaveable { mutableStateOf(proveedorEnEdicion?.nombre ?: "") }
    var telefono by rememberSaveable { mutableStateOf(proveedorEnEdicion?.telefono ?: "") }
    var notas by rememberSaveable { mutableStateOf(proveedorEnEdicion?.notas ?: "") }

    ModalBottomSheet(
        onDismissRequest = onCerrar,
        sheetState = sheetState
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
            Text(
                if (proveedorEnEdicion == null) "Nuevo proveedor" else "Editar proveedor",
                style = MaterialTheme.typography.titleLarge
            )

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            )

            OutlinedTextField(
                value = telefono,
                onValueChange = { telefono = it },
                label = { Text("Teléfono") },
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
            )

            OutlinedTextField(
                value = notas,
                onValueChange = { notas = it },
                label = { Text("Notas (opcional)") },
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
            )

            Button(
                onClick = { onGuardar(nombre, telefono, notas) },
                enabled = !guardando,
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp)
            ) {
                Text(if (guardando) "Guardando..." else "Guardar")
            }
        }
    }
}