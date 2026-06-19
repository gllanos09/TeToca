package com.tetocaApp.tetoca.ui.productos

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tetocaApp.tetoca.ui.theme.*
import com.tetocaApp.tetoca.viewmodel.DetalleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleScreen(
    productoId: Long,
    onVolver: () -> Unit,
    onEditar: (productoId: Long) -> Unit,
    onEliminado: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: DetalleViewModel = viewModel(factory = DetalleViewModel.Factory(context, productoId))
    val state by viewModel.uiState.collectAsState()
    var confirmar by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(state.producto) { if (state.producto != null) visible = true }

    Scaffold(
        containerColor = FondoClaro,
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        "Detalle del Producto",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                        color = SobreFondoClaro
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, Modifier.size(28.dp), tint = AzulPrimario)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onEditar(productoId) },
                        modifier = Modifier.padding(end = 8.dp).clip(CircleShape).background(AzulFondo)
                    ) {
                        Icon(Icons.Filled.Edit, null, Modifier.size(24.dp), tint = AzulPrimario)
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(containerColor = FondoClaro)
            )
        },
        bottomBar = {
            if (state.producto != null) {
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
                            onClick = { confirmar = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RojoFondo,
                                contentColor = RojoStock
                            ),
                            shape = RoundedCornerShape(16.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                        ) {
                            Icon(Icons.Filled.DeleteOutline, null, Modifier.size(22.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "ELIMINAR DEL INVENTARIO", 
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold)
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize()) {
            when {
                state.cargando -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator(color = AzulPrimario)
                }
                state.producto == null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text(state.error ?: "Producto no encontrado", color = RojoStock, style = MaterialTheme.typography.titleLarge)
                }
                else -> {
                    val p = state.producto!!
                    val (color, etiqueta) = nivelStock(p.stockActual, p.stockMinimo)

                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 8 }
                    ) {
                        Column(
                            Modifier
                                .fillMaxSize()
                                .padding(padding)
                                .verticalScroll(rememberScrollState())
                                .padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            // Card Principal de Stock
                            Surface(
                                color = SuperficieClara,
                                shape = RoundedCornerShape(32.dp),
                                shadowElevation = 2.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Surface(
                                        color = color.copy(alpha = 0.05f),
                                        shape = CircleShape,
                                        modifier = Modifier.size(160.dp),
                                        border = androidx.compose.foundation.BorderStroke(4.dp, color.copy(alpha = 0.1f))
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    "${p.stockActual}",
                                                    fontSize = 64.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = color
                                                )
                                                Text(
                                                    "UNIDADES",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = color.copy(alpha = 0.6f),
                                                    letterSpacing = 1.sp
                                                )
                                            }
                                        }
                                    }
                                    Spacer(Modifier.height(24.dp))
                                    Text(
                                        p.nombre,
                                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                                        color = SobreFondoClaro,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Surface(
                                        color = color.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            etiqueta.uppercase(),
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp),
                                            color = color
                                        )
                                    }
                                }
                            }

                            // Sección de detalles
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    "ESPECIFICACIONES", 
                                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold), 
                                    color = SobreVariante
                                )
                                
                                Surface(
                                    color = SuperficieClara,
                                    shape = RoundedCornerShape(24.dp),
                                    shadowElevation = 1.dp,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(Modifier.padding(8.dp)) {
                                        ItemDetallePremium(Icons.Outlined.Category, "Categoría", p.categoria)
                                        HorizontalDivider(Modifier.padding(horizontal = 56.dp), color = BordeClaro.copy(alpha = 0.5f))
                                        ItemDetallePremium(Icons.Outlined.Payments, "Precio Unitario", 
                                            p.precio?.let { "S/ ${"%.2f".format(it)}" } ?: "Sin precio")
                                        HorizontalDivider(Modifier.padding(horizontal = 56.dp), color = BordeClaro.copy(alpha = 0.5f))
                                        ItemDetallePremium(Icons.Outlined.Inventory, "Mínimo Requerido", "${p.stockMinimo} unid.")
                                        HorizontalDivider(Modifier.padding(horizontal = 56.dp), color = BordeClaro.copy(alpha = 0.5f))
                                        ItemDetallePremium(Icons.Outlined.BusinessCenter, "Proveedor", state.proveedorNombre ?: "No asignado")
                                    }
                                }
                            }
                            
                            Spacer(Modifier.height(40.dp))
                        }
                    }
                }
            }
        }
    }

    if (confirmar) {
        AlertDialog(
            onDismissRequest = { confirmar = false },
            containerColor = SuperficieClara,
            shape = RoundedCornerShape(28.dp),
            title = { Text("¿Eliminar producto?", color = SobreFondoClaro, fontWeight = FontWeight.Bold) },
            text = { Text("Esta acción es irreversible y el producto desaparecerá del inventario.", color = SobreVariante) },
            confirmButton = {
                TextButton(onClick = { confirmar = false; viewModel.eliminar { onEliminado() } }) {
                    Text("Eliminar", color = RojoStock, fontWeight = FontWeight.ExtraBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmar = false }) { Text("Cancelar", color = SobreFondoClaro) }
            }
        )
    }
}

@Composable
private fun ItemDetallePremium(icono: ImageVector, etiqueta: String, valor: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(AzulFondo),
            contentAlignment = Alignment.Center
        ) {
            Icon(icono, null, Modifier.size(20.dp), tint = AzulPrimario)
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(etiqueta, style = MaterialTheme.typography.labelSmall, color = SobreVariante)
            Text(valor, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = SobreFondoClaro)
        }
    }
}

private fun nivelStock(actual: Int, minimo: Int): Pair<Color, String> = when {
    actual <= 0          -> RojoStock  to "Agotado"
    actual <= minimo     -> RojoStock  to "Crítico"
    actual <= minimo * 2 -> AmbarStock to "Bajo"
    else                 -> VerdeStock to "Óptimo"
}
