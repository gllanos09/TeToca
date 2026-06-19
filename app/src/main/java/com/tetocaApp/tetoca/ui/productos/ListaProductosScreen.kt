package com.tetocaApp.tetoca.ui.productos

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tetocaApp.tetoca.data.local.Producto
import com.tetocaApp.tetoca.ui.theme.StockBajo
import com.tetocaApp.tetoca.ui.theme.StockCritico
import com.tetocaApp.tetoca.ui.theme.StockOk
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

    val listState = rememberLazyListState()
    val fabExpandido by remember {
        derivedStateOf { listState.firstVisibleItemIndex == 0 }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text("Mis productos", style = MaterialTheme.typography.headlineSmall)
                },
                actions = {
                    FilledTonalIconButton(onClick = onProveedoresClick) {
                        Icon(Icons.Outlined.Groups, contentDescription = "Proveedores")
                    }
                    Spacer(Modifier.width(4.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Nuevo", style = MaterialTheme.typography.labelLarge) },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                onClick = onNuevoProducto,
                expanded = fabExpandido
            )
        }
    ) { padding ->
        Crossfade(
            targetState = estadoLista(state.cargando, state.error, state.productos.isEmpty()),
            animationSpec = tween(350),
            label = "estadoLista",
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) { estado ->
            when (estado) {
                EstadoLista.CARGANDO ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }

                EstadoLista.ERROR ->
                    MensajeCentral(
                        icono = Icons.Outlined.Inventory2,
                        titulo = "Algo salió mal",
                        detalle = state.error ?: "Inténtalo de nuevo."
                    )

                EstadoLista.VACIO ->
                    MensajeCentral(
                        icono = Icons.Outlined.Inventory2,
                        titulo = "Aún no tienes productos",
                        detalle = "Toca el botón Nuevo para registrar el primero."
                    )

                EstadoLista.CON_DATOS ->
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 96.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.productos, key = { it.id }) { producto ->
                            ProductoCard(
                                producto = producto,
                                onClick = { onProductoClick(producto.id) },
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
            }
        }
    }
}

@Composable
private fun ProductoCard(
    producto: Producto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (colorObjetivo, etiqueta) = nivelStock(producto.stockActual, producto.stockMinimo)
    val color by animateColorAsState(colorObjetivo, tween(400), label = "stockColor")

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(color.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    producto.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    producto.categoria,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${producto.stockActual}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(etiqueta, style = MaterialTheme.typography.labelMedium, color = color)
            }
        }
    }
}

@Composable
private fun MensajeCentral(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    titulo: String,
    detalle: String
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icono,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(38.dp)
                )
            }
            Text(
                titulo,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                detalle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

private enum class EstadoLista { CARGANDO, ERROR, VACIO, CON_DATOS }

private fun estadoLista(cargando: Boolean, error: String?, vacio: Boolean): EstadoLista = when {
    cargando -> EstadoLista.CARGANDO
    error != null -> EstadoLista.ERROR
    vacio -> EstadoLista.VACIO
    else -> EstadoLista.CON_DATOS
}

private fun nivelStock(actual: Int, minimo: Int): Pair<Color, String> = when {
    actual <= minimo -> StockCritico to "Crítico"
    actual <= minimo * 2 -> StockBajo to "Bajo"
    else -> StockOk to "Disponible"
}
