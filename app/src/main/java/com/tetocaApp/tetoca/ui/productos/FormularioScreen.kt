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
        containerColor = FondoClaro,
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        if (state.editando) "Editar Producto" else "Nuevo Producto",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                        color = SobreFondoClaro
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancelar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, Modifier.size(28.dp), tint = AzulPrimario)
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(containerColor = FondoClaro)
            )
        },
        bottomBar = {
            Surface(
                Modifier.fillMaxWidth(),
                color = FondoClaro,
                shadowElevation = 8.dp
            ) {
                Box(
                    Modifier
                        .navigationBarsPadding()
                        .padding(24.dp)
                ) {
                    Button(
                        onClick = viewModel::guardar,
                        enabled = !state.guardando && state.proveedores.isNotEmpty(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AzulPrimario),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (state.guardando) {
                            CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 3.dp, color = Color.White)
                        } else {
                            Text(
                                "CONFIRMAR",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold)
                            )
                        }
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
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            SeccionFormulario("INFORMACIÓN GENERAL") {
                CampoiOS(state.nombre, viewModel::onNombreChange, "Nombre del producto")
                CampoiOS(state.categoria, viewModel::onCategoriaChange, "Categoría")
            }
            
            SeccionFormulario("CONTROL DE STOCK") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    CampoiOS(state.stockActual, viewModel::onStockActualChange, "Actual",
                        KeyboardType.Number, Modifier.weight(1f))
                    CampoiOS(state.stockMinimo, viewModel::onStockMinimoChange, "Mínimo",
                        KeyboardType.Number, Modifier.weight(1f))
                }
            }
            
            SeccionFormulario("PRECIO Y PROVEEDOR") {
                CampoiOS(state.precio, viewModel::onPrecioChange, "Precio (PEN)", KeyboardType.Decimal)
                SelectorProveedor(state.proveedores, state.proveedorId, viewModel::onProveedorSeleccionado)
            }
            
            TasaCard(state.tasaCargando, state.tasa, state.tasaError, state.precio.toDoubleOrNull(), viewModel::cargarTipoCambio)

            AnimatedVisibility(state.error != null) {
                Surface(
                    color = RojoFondo,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        state.error ?: "", 
                        color = RojoStock, 
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            
            if (state.proveedores.isEmpty()) {
                Surface(
                    color = AmbarFondo,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "⚠ Crea un proveedor antes de registrar productos.",
                        style = MaterialTheme.typography.bodyMedium, 
                        color = AmbarStock,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SeccionFormulario(titulo: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            titulo, 
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold), 
            color = SobreVariante
        )
        Surface(
            color = SuperficieClara,
            shape = RoundedCornerShape(24.dp),
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                content = content
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CampoiOS(
    valor: String, onChange: (String) -> Unit, etiqueta: String,
    teclado: KeyboardType = KeyboardType.Text, modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = valor, onValueChange = onChange,
        label = { Text(etiqueta) },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(keyboardType = teclado),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AzulPrimario,
            unfocusedBorderColor = BordeClaro,
            focusedLabelColor = AzulPrimario
        ),
        modifier = modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectorProveedor(proveedores: List<Proveedor>, proveedorId: Long?, onSeleccion: (Long) -> Unit) {
    var expandido by remember { mutableStateOf(false) }
    val sel = proveedores.find { it.id == proveedorId }
    ExposedDropdownMenuBox(
        expanded = expandido,
        onExpandedChange = { expandido = !expandido }
    ) {
        OutlinedTextField(
            value = sel?.nombre ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Proveedor") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AzulPrimario,
                unfocusedBorderColor = BordeClaro,
                focusedLabelColor = AzulPrimario
            ),
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expandido,
            onDismissRequest = { expandido = false },
            containerColor = SuperficieClara
        ) {
            proveedores.forEach { prov ->
                DropdownMenuItem(
                    text = { Text(prov.nombre, color = SobreFondoClaro) },
                    onClick = {
                        onSeleccion(prov.id)
                        expandido = false
                    }
                )
            }
        }
    }
}

private enum class EstadoTasa { CARGANDO, OK, ERROR }

@Composable
private fun TasaCard(cargando: Boolean, tasa: Double?, error: String?, precioSoles: Double?, onReintentar: () -> Unit) {
    val est = when { cargando -> EstadoTasa.CARGANDO; error != null -> EstadoTasa.ERROR; else -> EstadoTasa.OK }
    Surface(
        color = AzulFondo,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.CurrencyExchange, null, Modifier.size(20.dp), tint = AzulPrimario)
                Spacer(Modifier.width(8.dp))
                Text("USD CONVERTER", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black), color = AzulPrimario)
            }
            AnimatedContent(est, transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(400)) }, label = "tasa") { e ->
                when (e) {
                    EstadoTasa.CARGANDO -> CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = AzulPrimario)
                    EstadoTasa.ERROR -> TextButton(onClick = onReintentar) { Text("Reintentar conexión", color = RojoStock) }
                    EstadoTasa.OK -> Column {
                        Text("1 USD = S/ ${tasa?.let { "%.3f".format(it) } ?: "—"}",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = SobreFondoClaro)
                        if (precioSoles != null && precioSoles > 0 && tasa != null) {
                            Text("Aprox. US$ ${"%.2f".format(precioSoles / tasa)}", color = SobreVariante)
                        }
                    }
                }
            }
        }
    }
}
