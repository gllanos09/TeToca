package com.tetocaApp.tetoca.ui.productos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tetocaApp.tetoca.data.local.Proveedor
import com.tetocaApp.tetoca.ui.theme.Emerald40
import com.tetocaApp.tetoca.viewmodel.FormularioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioScreen(
    productoId: Long?,
    onCancelar: () -> Unit,
    onGuardado: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: FormularioViewModel = viewModel(factory = FormularioViewModel.Factory(context, productoId))
    val state by viewModel.uiState.collectAsState()

    // Cuando el guardado termina bien, regresamos a la lista.
    LaunchedEffect(state.guardado) {
        if (state.guardado) onGuardado()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.editando) "Editar producto" else "Nuevo producto") },
                navigationIcon = {
                    IconButton(onClick = onCancelar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cancelar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = state.nombre,
                onValueChange = viewModel::onNombreChange,
                label = { Text("Nombre") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.categoria,
                onValueChange = viewModel::onCategoriaChange,
                label = { Text("Categoría") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = state.stockActual,
                    onValueChange = viewModel::onStockActualChange,
                    label = { Text("Stock actual") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = state.stockMinimo,
                    onValueChange = viewModel::onStockMinimoChange,
                    label = { Text("Stock mínimo") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
            OutlinedTextField(
                value = state.precio,
                onValueChange = viewModel::onPrecioChange,
                label = { Text("Precio en soles (opcional)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            SelectorProveedor(
                proveedores = state.proveedores,
                proveedorId = state.proveedorId,
                onSeleccion = viewModel::onProveedorSeleccionado
            )

            TipoCambioCard(
                cargando = state.tasaCargando,
                tasa = state.tasa,
                error = state.tasaError,
                precioSoles = state.precio.toDoubleOrNull(),
                onReintentar = viewModel::cargarTipoCambio
            )

            if (state.error != null) {
                Text(state.error!!, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = viewModel::guardar,
                enabled = !state.guardando && state.proveedores.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.guardando) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(if (state.editando) "Guardar cambios" else "Crear producto")
                }
            }

            if (state.proveedores.isEmpty()) {
                Text(
                    "No hay proveedores. Crea uno en la pantalla de Proveedores antes de registrar un producto.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectorProveedor(
    proveedores: List<Proveedor>,
    proveedorId: Long?,
    onSeleccion: (Long) -> Unit
) {
    var expandido by remember { mutableStateOf(false) }
    val seleccionado = proveedores.find { it.id == proveedorId }

    ExposedDropdownMenuBox(
        expanded = expandido,
        onExpandedChange = { expandido = !expandido }
    ) {
        OutlinedTextField(
            value = seleccionado?.nombre ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Proveedor") },
            placeholder = { Text("Selecciona un proveedor") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expandido,
            onDismissRequest = { expandido = false }
        ) {
            proveedores.forEach { proveedor ->
                DropdownMenuItem(
                    text = { Text(proveedor.nombre) },
                    onClick = {
                        onSeleccion(proveedor.id)
                        expandido = false
                    }
                )
            }
        }
    }
}

@Composable
private fun TipoCambioCard(
    cargando: Boolean,
    tasa: Double?,
    error: String?,
    precioSoles: Double?,
    onReintentar: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                "Tipo de cambio (referencia)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            when {
                cargando -> Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Text("Obteniendo tipo de cambio...", style = MaterialTheme.typography.bodySmall)
                }

                error != null -> Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    TextButton(onClick = onReintentar) { Text("Reintentar") }
                }

                tasa != null -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = Emerald40,
                            modifier = Modifier.size(16.dp)
                        )
                        Text("1 USD = ${"%.3f".format(tasa)} PEN", style = MaterialTheme.typography.bodyMedium)
                    }
                    if (precioSoles != null && precioSoles > 0) {
                        Text(
                            "Tu precio ≈ US$ ${"%.2f".format(precioSoles / tasa)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        "Tasas por ExchangeRate-API",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}