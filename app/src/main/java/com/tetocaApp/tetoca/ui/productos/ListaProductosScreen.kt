package com.tetocaApp.tetoca.ui.productos

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.animateFloatAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tetocaApp.tetoca.data.local.Producto
import com.tetocaApp.tetoca.ui.theme.Amber40
import com.tetocaApp.tetoca.ui.theme.Emerald40
import com.tetocaApp.tetoca.ui.theme.Red40
import com.tetocaApp.tetoca.viewmodel.ProductosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaProductosScreen(
    onProductoClick: (productoId: Long) -> Unit,
    onNuevoProducto: () -> Unit,
    onProveedoresClick: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: ProductosViewModel = viewModel(factory = ProductosViewModel.Factory(context))
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Productos") },
                actions = {
                    TextButton(onClick = onProveedoresClick) { Text("Proveedores") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNuevoProducto,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Nuevo producto")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.cargando -> CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.Center)
                )

                state.error != null -> Text(
                    text = state.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp)
                )

                state.productos.isEmpty() -> Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Filled.Inventory2,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Aún no hay productos.\nToca + para crear el primero.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.productos, key = { it.id }) { producto ->
                        ProductoItem(
                            producto = producto,
                            onClick = { onProductoClick(producto.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductoItem(producto: Producto, onClick: () -> Unit) {
    val (color, etiqueta) = nivelStock(producto.stockActual, producto.stockMinimo)

    // Feedback táctil: la tarjeta se "encoge" levemente al presionar,
    // confirmando que el toque fue recibido (sigue el criterio de
    // Emil Kowalski: animación corta y con propósito, no decorativa).
    val interactionSource = remember { MutableInteractionSource() }
    val presionado by interactionSource.collectIsPressedAsState()
    val escala by animateFloatAsState(
        targetValue = if (presionado) 0.97f else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "escalaTarjetaProducto"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(escala)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Barra lateral de color: comunica el nivel de stock con más
            // fuerza que un punto pequeño, sin depender solo del color
            // (la etiqueta de texto a la derecha refuerza el significado).
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(56.dp)
                    .clip(RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                    .background(color)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        producto.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        producto.categoria,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Stock: ${producto.stockActual}", style = MaterialTheme.typography.bodyMedium)
                    Text(etiqueta, style = MaterialTheme.typography.labelSmall, color = color)
                }
            }
        }
    }
}

/** Color y etiqueta según el stock actual frente al mínimo. */
private fun nivelStock(actual: Int, minimo: Int): Pair<Color, String> = when {
    actual <= minimo -> Red40 to "Crítico"
    actual <= minimo * 2 -> Amber40 to "Bajo"
    else -> Emerald40 to "OK"
}