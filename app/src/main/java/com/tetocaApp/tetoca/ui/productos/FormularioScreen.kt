package com.tetocaApp.tetoca.ui.productos

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CurrencyExchange
import androidx.compose.material3.Button
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tetocaApp.tetoca.data.local.Proveedor
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

    LaunchedEffect(state.guardado) {
        if (state.guardado) onGuardado()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (state.editando) "Editar producto" else "Nuevo producto",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancelar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cancelar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp, color = MaterialTheme.colorScheme.surface) {
                Button(
                    onClick = viewModel::guardar,
                    enabled = !state.guardando && state.proveedores.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(54.dp)
                ) {
                    if (state.guardando) {
                        CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                    } else {
                        Text(
                            if (state.editando) "Guardar cambios" else "Crear producto",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Campo(state.nombre, viewModel::onNombreChange, "Nombre del producto")
            Campo(state.categoria, viewModel::onCategoriaChange, "Categoría")

            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Campo(
                    state.stockActual, viewModel::onStockActualChange, "Stock actual",
                    KeyboardType.Number, Modifier.weight(1f)
                )
                Campo(
                    state.stockMinimo, viewModel::onStockMinimoChange, "Stock mínimo",
                    KeyboardType.Number, Modifier.weight(1f)
                )
            }
            Campo(state.precio, viewModel::onPrecioChange, "Precio en soles (opcional)", KeyboardType.Decimal)

            SelectorProveedor(state.proveedores, state.proveedorId, viewModel::onProveedorSeleccionado)

            TipoCambioCard(
                cargando = state.tasaCargando,
                tasa = state.tasa,
                error = state.tasaError,
                precioSoles = state.precio.toDoubleOrNull(),
                onReintentar = viewModel::cargarTipoCambio
            )

            AnimatedVisibility(visible = state.error != null) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        state.error ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            AnimatedVisibility(visible = state.proveedores.isEmpty()) {
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
private fun Campo(
    valor: String,
    onChange: (String) -> Unit,
    etiqueta: String,
    teclado: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onChange,
        label = { Text(etiqueta) },
        singleLine = true,
        shape = MaterialTheme.shapes.small,
        keyboardOptions = KeyboardOptions(keyboardType = teclado),
        modifier = modifier.fillMaxWidth()
    )
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

    ExposedDropdownMenuBox(expanded = expandido, onExpandedChange = { expandido = !expandido }) {
        OutlinedTextField(
            value = seleccionado?.nombre ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Proveedor") },
            placeholder = { Text("Selecciona un proveedor") },
            shape = MaterialTheme.shapes.small,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
            proveedores.forEach { proveedor ->
                DropdownMenuItem(
                    text = { Text(proveedor.nombre) },
                    onClick = { onSeleccion(proveedor.id); expandido = false }
                )
            }
        }
    }
}

private enum class EstadoTasa { CARGANDO, OK, ERROR }

@Composable
private fun TipoCambioCard(
    cargando: Boolean,
    tasa: Double?,
    error: String?,
    precioSoles: Double?,
    onReintentar: () -> Unit
) {
    val estado = when {
        cargando -> EstadoTasa.CARGANDO
        error != null -> EstadoTasa.ERROR
        else -> EstadoTasa.OK
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.tertiaryContainer, RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Outlined.CurrencyExchange,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "Tipo de cambio (referencia)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
        AnimatedContent(
            targetState = estado,
            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
            label = "tasa"
        ) { e ->
            when (e) {
                EstadoTasa.CARGANDO -> Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Obteniendo tipo de cambio...", style = MaterialTheme.typography.bodyMedium)
                }
                EstadoTasa.ERROR -> Column {
                    Text(
                        error ?: "No se pudo obtener el tipo de cambio.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    TextButton(onClick = onReintentar) { Text("Reintentar") }
                }
                EstadoTasa.OK -> Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        "1 USD = ${tasa?.let { "%.3f".format(it) } ?: "—"} PEN",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    if (precioSoles != null && precioSoles > 0 && tasa != null) {
                        Text(
                            "Tu precio ≈ US$ ${"%.2f".format(precioSoles / tasa)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                    Text(
                        "Tasas por ExchangeRate-API",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
    }
}
