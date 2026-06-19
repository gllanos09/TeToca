package com.tetocaApp.tetoca.ui.productos

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tetocaApp.tetoca.data.local.Producto
import com.tetocaApp.tetoca.ui.theme.StockBajo
import com.tetocaApp.tetoca.ui.theme.StockCritico
import com.tetocaApp.tetoca.ui.theme.StockOk
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

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Detalle", style = MaterialTheme.typography.titleLarge) },
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
        bottomBar = {
            if (state.producto != null) {
                Surface(tonalElevation = 3.dp, color = MaterialTheme.colorScheme.surface) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { confirmar = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.DeleteOutline, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Eliminar")
                        }
                        Button(
                            onClick = { onEditar(productoId) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.Edit, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Editar")
                        }
                    }
                }
            }
        }
    ) { padding ->
        Crossfade(
            targetState = state.cargando to (state.producto != null),
            animationSpec = tween(300),
            label = "detalle",
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) { (cargando, hayProducto) ->
            when {
                cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                !hayProducto -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        state.error ?: "No se encontró el producto",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                else -> Contenido(
                    producto = state.producto!!,
                    proveedorNombre = state.proveedorNombre
                )
            }
        }
    }

    if (confirmar) {
        AlertDialog(
            onDismissRequest = { confirmar = false },
            icon = { Icon(Icons.Outlined.Warning, contentDescription = null) },
            title = { Text("Eliminar producto") },
            text = { Text("Esta acción no se puede deshacer. ¿Eliminar este producto?") },
            confirmButton = {
                TextButton(onClick = {
                    confirmar = false
                    viewModel.eliminar { onEliminado() }
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { confirmar = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun Contenido(producto: Producto, proveedorNombre: String?) {
    var visible by remember { mutableStateOf(false) }
    androidx.compose.runtime.LaunchedEffect(Unit) { visible = true }

    val (color, etiqueta) = nivelStock(producto.stockActual, producto.stockMinimo)

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 8 }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Cabecera
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    producto.nombre,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .background(color, RoundedCornerShape(50))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            etiqueta,
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "${producto.stockActual} en stock",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Datos
            FilaInfo(Icons.Outlined.Category, "Categoría", producto.categoria)
            FilaInfo(Icons.Outlined.Inventory2, "Stock mínimo", "${producto.stockMinimo} unidades")
            FilaInfo(
                Icons.Outlined.Sell,
                "Precio",
                producto.precio?.let { "S/ " + "%.2f".format(it) } ?: "Sin precio"
            )
            FilaInfo(Icons.Outlined.Groups, "Proveedor", proveedorNombre ?: "—")
        }
    }
}

@Composable
private fun FilaInfo(icono: ImageVector, etiqueta: String, valor: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(
                etiqueta,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                valor,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun nivelStock(actual: Int, minimo: Int): Pair<Color, String> = when {
    actual <= minimo -> StockCritico to "Crítico"
    actual <= minimo * 2 -> StockBajo to "Bajo"
    else -> StockOk to "Disponible"
}
