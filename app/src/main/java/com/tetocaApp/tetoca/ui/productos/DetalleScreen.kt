package com.tetocaApp.tetoca.ui.productos

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, Modifier.size(26.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            if (state.producto != null) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { confirmar = true },
                            modifier = Modifier.weight(1f).height(54.dp),
                            border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                                brush = androidx.compose.ui.graphics.SolidColor(Error)
                            )
                        ) {
                            Icon(Icons.Filled.DeleteOutline, null, tint = Error)
                            Spacer(Modifier.width(8.dp))
                            Text("Eliminar", color = Error, fontWeight = FontWeight.SemiBold)
                        }
                        Button(
                            onClick = { onEditar(productoId) },
                            modifier = Modifier.weight(1f).height(54.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Violeta)
                        ) {
                            Icon(Icons.Filled.Edit, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Editar", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    ) { padding ->
        when {
            state.cargando -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator(color = Violeta)
            }
            state.producto == null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text(state.error ?: "Producto no encontrado", color = Error)
            }
            else -> {
                val p = state.producto!!
                val (color, etiqueta) = nivelStock(p.stockActual, p.stockMinimo)

                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(350)) + slideInVertically(tween(350)) { it / 6 }
                ) {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Hero: número grande
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(color.copy(alpha = 0.1f))
                                .padding(24.dp)
                        ) {
                            Column {
                                Text(
                                    p.nombre,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(Modifier.height(12.dp))
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        "${p.stockActual}",
                                        fontSize = 72.sp,
                                        fontWeight = FontWeight.Black,
                                        color = color,
                                        lineHeight = 72.sp
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Column(modifier = Modifier.padding(bottom = 8.dp)) {
                                        Box(
                                            Modifier
                                                .background(color.copy(0.18f), RoundedCornerShape(50))
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                etiqueta.uppercase(),
                                                style = MaterialTheme.typography.labelMedium,
                                                color = color,
                                                letterSpacing = 1.sp
                                            )
                                        }
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            "mín. ${p.stockMinimo}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = SobreVariante
                                        )
                                    }
                                }
                            }
                        }

                        // Filas de detalle
                        FilaDetalle(Icons.Outlined.Category, "CATEGORÍA", p.categoria)
                        FilaDetalle(Icons.Outlined.Sell, "PRECIO",
                            p.precio?.let { "S/ ${"%.2f".format(it)}" } ?: "Sin precio")
                        FilaDetalle(Icons.Outlined.Groups, "PROVEEDOR", state.proveedorNombre ?: "—")
                    }
                }
            }
        }
    }

    if (confirmar) {
        AlertDialog(
            onDismissRequest = { confirmar = false },
            containerColor = Superficie,
            title = { Text("¿Eliminar producto?", color = SobreFondo) },
            text = { Text("Esta acción no se puede deshacer.", color = SobreVariante) },
            confirmButton = {
                TextButton(onClick = { confirmar = false; viewModel.eliminar { onEliminado() } }) {
                    Text("Eliminar", color = Error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmar = false }) { Text("Cancelar", color = SobreVariante) }
            }
        )
    }
}

@Composable
private fun FilaDetalle(icono: ImageVector, etiqueta: String, valor: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Superficie)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(VioletaFondo),
            Alignment.Center
        ) {
            Icon(icono, null, Modifier.size(22.dp), tint = VioletaClaro)
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(etiqueta, style = MaterialTheme.typography.labelSmall, color = SobreVariante, letterSpacing = 1.sp)
            Text(valor, style = MaterialTheme.typography.bodyLarge, color = SobreFondo, fontWeight = FontWeight.SemiBold)
        }
    }
}

private fun nivelStock(actual: Int, minimo: Int): Pair<Color, String> = when {
    actual <= minimo     -> RojoStock  to "Crítico"
    actual <= minimo * 2 -> AmbarStock to "Bajo"
    else                 -> VerdeStock to "Disponible"
}
