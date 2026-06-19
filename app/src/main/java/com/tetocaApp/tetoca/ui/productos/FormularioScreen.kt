package com.tetocaApp.tetoca.ui.productos

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CurrencyExchange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tetocaApp.tetoca.data.local.Proveedor
import com.tetocaApp.tetoca.ui.theme.*
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
    LaunchedEffect(state.guardado) { if (state.guardado) onGuardado() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (state.editando) "Editar producto" else "Nuevo producto",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancelar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, Modifier.size(26.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.background) {
                Button(
                    onClick = viewModel::guardar,
                    enabled = !state.guardando && state.proveedores.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth().navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Violeta),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (state.guardando) {
                        CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.5.dp, color = Color.White)
                    } else {
                        Text(
                            if (state.editando) "GUARDAR CAMBIOS" else "CREAR PRODUCTO",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(Modifier.height(4.dp))
            CampoOscuro(state.nombre, viewModel::onNombreChange, "Nombre del producto")
            CampoOscuro(state.categoria, viewModel::onCategoriaChange, "Categoría")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CampoOscuro(state.stockActual, viewModel::onStockActualChange, "Stock actual",
                    KeyboardType.Number, Modifier.weight(1f))
                CampoOscuro(state.stockMinimo, viewModel::onStockMinimoChange, "Stock mínimo",
                    KeyboardType.Number, Modifier.weight(1f))
            }
            CampoOscuro(state.precio, viewModel::onPrecioChange, "Precio en soles (opcional)", KeyboardType.Decimal)
            SelectorProveedor(state.proveedores, state.proveedorId, viewModel::onProveedorSeleccionado)
            TasaCard(state.tasaCargando, state.tasa, state.tasaError, state.precio.toDoubleOrNull(), viewModel::cargarTipoCambio)

            AnimatedVisibility(state.error != null) {
                Box(
                    Modifier.fillMaxWidth()
                        .background(ErrorFondo, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Text(state.error ?: "", color = RojoStock, style = MaterialTheme.typography.bodyMedium)
                }
            }
            AnimatedVisibility(state.proveedores.isEmpty()) {
                Text(
                    "⚠ Primero crea un proveedor desde la pantalla de Proveedores.",
                    style = MaterialTheme.typography.bodySmall, color = AmbarStock
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CampoOscuro(
    valor: String, onChange: (String) -> Unit, etiqueta: String,
    teclado: KeyboardType = KeyboardType.Text, modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = valor, onValueChange = onChange,
        label = { Text(etiqueta) },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = teclado),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Violeta,
            unfocusedBorderColor = Borde,
            focusedLabelColor = VioletaClaro,
            unfocusedLabelColor = SobreVariante,
            focusedTextColor = SobreFondo,
            unfocusedTextColor = SobreSuperficie,
            cursorColor = Violeta,
            focusedContainerColor = Superficie,
            unfocusedContainerColor = Superficie
        ),
        modifier = modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectorProveedor(proveedores: List<Proveedor>, proveedorId: Long?, onSeleccion: (Long) -> Unit) {
    var expandido by remember { mutableStateOf(false) }
    val sel = proveedores.find { it.id == proveedorId }
    ExposedDropdownMenuBox(expandido, { expandido = !expandido }) {
        OutlinedTextField(
            value = sel?.nombre ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Proveedor") },
            placeholder = { Text("Selecciona", color = SobreVariante) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandido) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Violeta, unfocusedBorderColor = Borde,
                focusedLabelColor = VioletaClaro, unfocusedLabelColor = SobreVariante,
                focusedTextColor = SobreFondo, unfocusedTextColor = SobreSuperficie,
                focusedContainerColor = Superficie, unfocusedContainerColor = Superficie
            ),
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
        )
        ExposedDropdownMenu(expandido, { expandido = false }, containerColor = SuperficieAlt) {
            proveedores.forEach { prov ->
                DropdownMenuItem(
                    text = { Text(prov.nombre, color = SobreFondo) },
                    onClick = { onSeleccion(prov.id); expandido = false }
                )
            }
        }
    }
}

private enum class EstadoTasa { CARGANDO, OK, ERROR }

@Composable
private fun TasaCard(cargando: Boolean, tasa: Double?, error: String?, precioSoles: Double?, onReintentar: () -> Unit) {
    val est = when { cargando -> EstadoTasa.CARGANDO; error != null -> EstadoTasa.ERROR; else -> EstadoTasa.OK }
    Column(
        Modifier.fillMaxWidth()
            .background(VioletaFondo, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.CurrencyExchange, null, Modifier.size(20.dp), tint = VioletaClaro)
            Spacer(Modifier.width(8.dp))
            Text("TIPO DE CAMBIO", style = MaterialTheme.typography.labelMedium, color = VioletaClaro, letterSpacing = 1.sp)
        }
        AnimatedContent(est, transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(200)) }, label = "tasa") { e ->
            when (e) {
                EstadoTasa.CARGANDO -> Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = Violeta)
                    Spacer(Modifier.width(8.dp))
                    Text("Consultando...", color = SobreVariante, style = MaterialTheme.typography.bodyMedium)
                }
                EstadoTasa.ERROR -> Column {
                    Text(error ?: "Sin conexión", color = RojoStock, style = MaterialTheme.typography.bodyMedium)
                    TextButton(onClick = onReintentar) { Text("Reintentar", color = VioletaClaro, fontWeight = FontWeight.Bold) }
                }
                EstadoTasa.OK -> Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("1 USD = ${tasa?.let { "%.3f".format(it) } ?: "—"} PEN",
                        style = MaterialTheme.typography.titleMedium, color = SobreFondo, fontWeight = FontWeight.Bold)
                    if (precioSoles != null && precioSoles > 0 && tasa != null)
                        Text("Tu precio ≈ US$ ${"%.2f".format(precioSoles / tasa)}", color = SobreVariante,
                            style = MaterialTheme.typography.bodySmall)
                    Text("Tasas por ExchangeRate-API", color = SobreVariante, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
