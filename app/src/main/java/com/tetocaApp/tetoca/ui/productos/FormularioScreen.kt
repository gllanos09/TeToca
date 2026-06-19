package com.tetocaApp.tetoca.ui.productos

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.outlined.CurrencyExchange
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        if (state.editando) "EDITAR PRODUCTO" else "NUEVO PRODUCTO",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 2.sp),
                        color = SobreVariante
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancelar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = AzulPrimario)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = FondoClaro,
                    titleContentColor = SobreVariante
                )
            )
        },
        bottomBar = {
            Surface(
                Modifier.fillMaxWidth(),
                color = FondoClaro,
                shadowElevation = 8.dp
            ) {
                Box(Modifier.navigationBarsPadding().padding(20.dp)) {
                    Button(
                        onClick = viewModel::guardar,
                        enabled = !state.guardando && state.proveedores.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AzulPrimario),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (state.guardando) {
                            CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 3.dp, color = Color.White)
                        } else {
                            Text("GUARDAR CAMBIOS", fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            SeccionLimpia("IDENTIFICACIÓN", Icons.AutoMirrored.Outlined.Label) {
                CampoiOS(state.nombre, viewModel::onNombreChange, "Nombre del producto")
                CampoiOS(state.categoria, viewModel::onCategoriaChange, "Categoría / Familia")
            }
            
            SeccionLimpia("CONTROL DE EXISTENCIAS", Icons.Outlined.Inventory2) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CampoiOS(state.stockActual, viewModel::onStockActualChange, "Stock Actual",
                        KeyboardType.Number, Modifier.weight(1f))
                    CampoiOS(state.stockMinimo, viewModel::onStockMinimoChange, "Stock Crítico",
                        KeyboardType.Number, Modifier.weight(1f))
                }
            }
            
            SeccionLimpia("VALORIZACIÓN Y ORIGEN", Icons.Outlined.Payments) {
                CampoiOS(state.precio, viewModel::onPrecioChange, "Precio Unitario (S/)", KeyboardType.Decimal)
                Spacer(Modifier.height(8.dp))
                SelectorProveedorLimpio(state.proveedores, state.proveedorId, viewModel::onProveedorSeleccionado)
            }
            
            TasaCardLimpia(state.tasaCargando, state.tasa, state.tasaError, state.precio.toDoubleOrNull(), viewModel::cargarTipoCambio)

            if (state.error != null) {
                Surface(color = RojoFondo, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Text(state.error!!, color = RojoStock, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
                }
            }
            
            if (state.proveedores.isEmpty()) {
                Surface(color = AmbarFondo, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Text("Debe registrar al menos un proveedor antes.", color = AmbarStock, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
                }
            }
            
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun SeccionLimpia(titulo: String, icono: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 4.dp)) {
            Icon(icono, null, Modifier.size(14.dp), tint = SobreVariante)
            Spacer(Modifier.width(8.dp))
            Text(titulo, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp), color = SobreVariante)
        }
        Surface(
            color = SuperficieClara,
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BordeClaro.copy(alpha = 0.5f))
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp), content = content)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CampoiOS(
    valor: String, onChange: (String) -> Unit, etiqueta: String,
    teclado: KeyboardType = KeyboardType.Text, modifier: Modifier = Modifier
) {
    TextField(
        value = valor, onValueChange = onChange,
        label = { Text(etiqueta, fontSize = 13.sp) },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = teclado),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = FondoClaro,
            unfocusedContainerColor = FondoClaro,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = AzulPrimario,
            focusedLabelColor = AzulPrimario
        ),
        modifier = modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectorProveedorLimpio(proveedores: List<Proveedor>, proveedorId: Long?, onSeleccion: (Long) -> Unit) {
    var expandido by remember { mutableStateOf(false) }
    val sel = proveedores.find { it.id == proveedorId }
    ExposedDropdownMenuBox(expanded = expandido, onExpandedChange = { expandido = !expandido }) {
        TextField(
            value = sel?.nombre ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Proveedor Responsable", fontSize = 13.sp) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = FondoClaro,
                unfocusedContainerColor = FondoClaro,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }, containerColor = SuperficieClara) {
            proveedores.forEach { prov ->
                DropdownMenuItem(
                    text = { Text(prov.nombre, fontWeight = FontWeight.Medium) },
                    onClick = { onSeleccion(prov.id); expandido = false }
                )
            }
        }
    }
}

@Composable
private fun TasaCardLimpia(cargando: Boolean, tasa: Double?, error: String?, precioSoles: Double?, onReintentar: () -> Unit) {
    Surface(
        color = AzulFondo.copy(alpha = 0.5f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.CurrencyExchange, null, tint = AzulPrimario, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                if (cargando) {
                    LinearProgressIndicator(Modifier.fillMaxWidth(), color = AzulPrimario, trackColor = Color.Transparent)
                } else if (error != null) {
                    Text("Error al conectar", style = MaterialTheme.typography.labelSmall, color = RojoStock)
                    Text("Reintentar", Modifier.clickable { onReintentar() }, color = AzulPrimario, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                } else {
                    Text("Equivalente en USD", style = MaterialTheme.typography.labelSmall, color = SobreVariante)
                    Text("S/ 1.00 = $${"%.3f".format(1.0 / (tasa ?: 3.7))}", fontWeight = FontWeight.Bold, color = SobreFondoClaro)
                }
            }
            if (!cargando && error == null && precioSoles != null && tasa != null) {
                Text(
                    "$ ${"%.2f".format(precioSoles / tasa)}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = AzulPrimario
                )
            }
        }
    }
}
