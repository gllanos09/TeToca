package com.tetocaApp.tetoca.ui.productos

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tetocaApp.tetoca.data.local.Producto
import com.tetocaApp.tetoca.ui.theme.*
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
    val fabExpandido by remember { derivedStateOf { listState.firstVisibleItemIndex == 0 } }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {},
                actions = {
                    IconButton(onClick = onProveedoresClick) {
                        Icon(
                            Icons.Outlined.Groups,
                            contentDescription = "Proveedores",
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("AGREGAR", style = MaterialTheme.typography.labelLarge) },
                icon = { Icon(Icons.Filled.Add, null, Modifier.size(22.dp)) },
                onClick = onNuevoProducto,
                expanded = fabExpandido,
                containerColor = Violeta,
                contentColor = Color.White
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            // Hero header
            Spacer(Modifier.height(4.dp))
            Text(
                "Inventario",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (!state.cargando && state.error == null) {
                Text(
                    "${state.productos.size} productos registrados",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(20.dp))

            when {
                state.cargando -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator(color = Violeta)
                }
                state.error != null -> EstadoVacio(
                    "Error al cargar", state.error!!, Icons.Outlined.Inventory2
                )
                state.productos.isEmpty() -> EstadoVacio(
                    "Sin productos aún",
                    "Toca AGREGAR para registrar tu primer producto.",
                    Icons.Outlined.Inventory2
                )
                else -> LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(state.productos, key = { it.id }) { p ->
                        ProductoCard(p, { onProductoClick(p.id) }, Modifier.animateItem())
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductoCard(p: Producto, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val (color, etiqueta) = nivelStock(p.stockActual, p.stockMinimo)
    val accentColor by animateColorAsState(color, spring(Spring.DampingRatioMediumBouncy), label = "")

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Superficie)
            .clickable(onClick = onClick)
    ) {
        // borde izquierdo de color
        Box(
            Modifier
                .fillMaxHeight()
                .width(4.dp)
                .background(accentColor)
                .align(Alignment.CenterStart)
        )
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 16.dp, bottom = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    p.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    p.categoria.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${p.stockActual}",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    color = accentColor,
                    lineHeight = 38.sp
                )
                Box(
                    Modifier
                        .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(50))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        etiqueta.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        letterSpacing = 0.8.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun EstadoVacio(
    titulo: String, detalle: String,
    icono: androidx.compose.ui.graphics.vector.ImageVector
) {
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Box(
                Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(SuperficieAlt),
                Alignment.Center
            ) {
                Icon(icono, null, Modifier.size(44.dp), tint = SobreVariante)
            }
            Spacer(Modifier.height(16.dp))
            Text(titulo, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(6.dp))
            Text(detalle, style = MaterialTheme.typography.bodyMedium, color = SobreVariante, textAlign = TextAlign.Center)
        }
    }
}

private fun nivelStock(actual: Int, minimo: Int): Pair<Color, String> = when {
    actual <= minimo       -> RojoStock  to "Crítico"
    actual <= minimo * 2   -> AmbarStock to "Bajo"
    else                   -> VerdeStock to "OK"
}
