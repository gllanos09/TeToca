package com.tetocaApp.tetoca.ui.proveedores

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tetocaApp.tetoca.data.local.Proveedor
import com.tetocaApp.tetoca.viewmodel.ProveedoresViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProveedoresScreen(onVolver: () -> Unit) {
    val context = LocalContext.current
    val viewModel: ProveedoresViewModel = viewModel(factory = ProveedoresViewModel.Factory(context))
    val state by viewModel.uiState.collectAsState()

    val snackbar = remember { SnackbarHostState() }
    var proveedorAEliminar by remember { mutableStateOf<Proveedor?>(null) }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbar.showSnackbar(it)
            viewModel.limpiarError()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Proveedores", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Nuevo", style = MaterialTheme.typography.labelLarge) },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                onClick = viewModel::onNuevoProveedor
            )
        }
    ) { padding ->
        Crossfade(
            targetState = when {
                state.cargando -> 0
                state.proveedores.isEmpty() -> 1
                else -> 2
            },
            animationSpec = tween(300),
            label = "proveedores",
            modifier = Modifier.fillMaxSize().padding(padding)
        ) { estado ->
            when (estado) {
                0 -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                1 -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Groups, contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text("Aún no tienes proveedores", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Agrega tus proveedores para asociarlos a tus productos.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.proveedores, key = { it.id }) { proveedor ->
                        ProveedorCard(
                            proveedor = proveedor,
                            onEditar = { viewModel.onEditarProveedor(proveedor) },
                            onEliminar = { proveedorAEliminar = proveedor },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }

    // Bottom sheet para crear / editar
    if (state.mostrarFormulario) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = viewModel::onCerrarFormulario,
            sheetState = sheetState
        ) {
            FormProveedor(
                inicial = state.proveedorEnEdicion,
                guardando = state.guardando,
                onGuardar = { n, t, notas -> viewModel.guardar(n, t, notas) }
            )
        }
    }

    // Confirmación de eliminación
    proveedorAEliminar?.let { prov ->
        AlertDialog(
            onDismissRequest = { proveedorAEliminar = null },
            title = { Text("Eliminar proveedor") },
            text = { Text("¿Eliminar a \"${prov.nombre}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminar(prov)
                    proveedorAEliminar = null
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { proveedorAEliminar = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun ProveedorCard(
    proveedor: Proveedor,
    onEditar: () -> Unit,
    onEliminar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onEditar)
            .background(MaterialTheme.colorScheme.surface)
            .padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                proveedor.nombre.take(1).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                proveedor.nombre,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.Phone, contentDescription = null,
                    modifier = Modifier.size(15.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    proveedor.telefono,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        IconButton(onClick = onEditar) {
            Icon(Icons.Filled.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
        }
        IconButton(onClick = onEliminar) {
            Icon(Icons.Filled.DeleteOutline, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormProveedor(
    inicial: Proveedor?,
    guardando: Boolean,
    onGuardar: (String, String, String?) -> Unit
) {
    var nombre by remember(inicial) { mutableStateOf(inicial?.nombre ?: "") }
    var telefono by remember(inicial) { mutableStateOf(inicial?.telefono ?: "") }
    var notas by remember(inicial) { mutableStateOf(inicial?.notas ?: "") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            if (inicial == null) "Nuevo proveedor" else "Editar proveedor",
            style = MaterialTheme.typography.titleLarge
        )
        OutlinedTextField(
            value = nombre, onValueChange = { nombre = it },
            label = { Text("Nombre") }, singleLine = true,
            shape = MaterialTheme.shapes.small, modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = telefono, onValueChange = { telefono = it },
            label = { Text("Teléfono") }, singleLine = true,
            shape = MaterialTheme.shapes.small, modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = notas, onValueChange = { notas = it },
            label = { Text("Notas (opcional)") },
            shape = MaterialTheme.shapes.small, modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = { onGuardar(nombre, telefono, notas) },
            enabled = !guardando,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
        ) {
            if (guardando) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            else Text("Guardar", style = MaterialTheme.typography.labelLarge)
        }
    }
}
