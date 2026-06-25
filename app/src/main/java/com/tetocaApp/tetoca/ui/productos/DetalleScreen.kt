package com.tetocaApp.tetoca.ui.productos

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
    var confirmarEliminar by remember { mutableStateOf(false) }

    var contentVisible by remember { mutableStateOf(false) }
    LaunchedEffect(state.producto) {
        if (state.producto != null) contentVisible = true
    }
    val enterProgress by animateFloatAsState(
        targetValue = if (contentVisible) 1f else 0f,
        animationSpec = tween(280, easing = FastOutSlowInEasing),
        label = "enter"
    )

    Scaffold(
        containerColor = FondoClaro,
        topBar = {
            TopAppBar(
                title = { Text("Detalles", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AzulPrimario,
                    titleContentColor = SobreAzul,
                    navigationIconContentColor = SobreAzul
                )
            )
        },
        bottomBar = {
            if (state.producto != null) {
                Surface(
                    Modifier.fillMaxWidth(),
                    color = SuperficieClara,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        Modifier
                            .navigationBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { confirmarEliminar = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RojoStock),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, RojoStock.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Eliminar producto", Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Eliminar", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { onEditar(productoId) },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AzulPrimario),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Filled.Edit, contentDescription = "Editar producto", Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Editar", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.cargando -> CircularProgressIndicator(
                    Modifier.align(Alignment.Center),
                    color = AzulPrimario
                )
                state.producto == null -> Text(
                    state.error ?: "Producto no encontrado",
                    Modifier.align(Alignment.Center),
                    color = RojoStock
                )
                else -> {
                    val p = state.producto!!
                    val (colorStock, etiquetaStock) = nivelStock(p.stockActual, p.stockMinimo)

                    Column(
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp)
                            .graphicsLayer {
                                alpha = enterProgress
                                translationY = (1f - enterProgress) * 40f
                            },
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Text(
                            p.nombre,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                            color = AzulPrimario,
                            textAlign = TextAlign.Start
                        )

                        Text(
                            p.categoria.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 1.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = SobreVariante
                        )

                        Surface(
                            color = SuperficieClara,
                            shape = RoundedCornerShape(24.dp),
                            shadowElevation = 2.dp,
                            border = androidx.compose.foundation.BorderStroke(1.dp, BordeClaro.copy(alpha = 0.5f))
                        ) {
                            Column(
                                Modifier
                                    .padding(24.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        "${p.stockActual}",
                                        fontSize = 72.sp,
                                        fontWeight = FontWeight.Black,
                                        color = colorStock,
                                        lineHeight = 72.sp
                                    )
                                    Text(
                                        " UNIDADES",
                                        modifier = Modifier.padding(bottom = 12.dp, start = 8.dp),
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = colorStock.copy(alpha = 0.6f)
                                    )
                                }

                                Spacer(Modifier.height(16.dp))

                                Surface(
                                    color = colorStock.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        etiquetaStock.uppercase(),
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                                        color = colorStock
                                    )
                                }
                            }
                        }

                        Text(
                            "DATOS DEL INVENTARIO",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = SobreVariante,
                            modifier = Modifier.padding(start = 4.dp)
                        )

                        Surface(
                            color = SuperficieClara,
                            shape = RoundedCornerShape(24.dp),
                            shadowElevation = 2.dp,
                            border = androidx.compose.foundation.BorderStroke(1.dp, BordeClaro.copy(alpha = 0.5f))
                        ) {
                            Column(Modifier.padding(8.dp)) {
                                DatoFila(
                                    Icons.Outlined.Payments, "Precio Unitario",
                                    p.precio?.let { "S/ ${"%.2f".format(it)}" } ?: "No definido"
                                )
                                HorizontalDivider(
                                    Modifier.padding(horizontal = 16.dp),
                                    color = BordeClaro.copy(alpha = 0.4f)
                                )
                                DatoFila(Icons.Outlined.LowPriority, "Stock Mínimo", "${p.stockMinimo} unid.")
                                HorizontalDivider(
                                    Modifier.padding(horizontal = 16.dp),
                                    color = BordeClaro.copy(alpha = 0.4f)
                                )
                                DatoFila(
                                    Icons.Outlined.BusinessCenter, "Proveedor",
                                    state.proveedorNombre ?: "Sin asignar"
                                )
                            }
                        }

                        Spacer(Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    if (confirmarEliminar) {
        AlertDialog(
            onDismissRequest = { confirmarEliminar = false },
            containerColor = SuperficieClara,
            shape = RoundedCornerShape(24.dp),
            title = { Text("¿Eliminar de inventario?", fontWeight = FontWeight.Bold) },
            text = { Text("Esta acción quitará el producto permanentemente de tu lista.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.eliminar { onEliminado() } },
                    colors = ButtonDefaults.buttonColors(containerColor = RojoStock),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { confirmarEliminar = false }) {
                    Text("Cancelar", color = SobreFondoClaro)
                }
            }
        )
    }
}

@Composable
private fun DatoFila(icono: ImageVector, titulo: String, valor: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(AzulFondo),
            contentAlignment = Alignment.Center
        ) {
            Icon(icono, contentDescription = titulo, Modifier.size(18.dp), tint = AzulPrimario)
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(titulo, style = MaterialTheme.typography.labelSmall, color = SobreVariante)
            Text(
                valor,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = SobreFondoClaro
            )
        }
    }
}

private fun nivelStock(actual: Int, minimo: Int): Pair<Color, String> = when {
    actual <= 0          -> RojoStock  to "Agotado"
    actual <= minimo     -> RojoStock  to "Crítico"
    actual <= minimo * 2 -> AmbarStock to "Bajo"
    else                 -> VerdeStock to "Óptimo"
}
