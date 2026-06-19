package com.tetocaApp.tetoca.ui.productos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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

    val criticos = state.productos.count { it.stockActual <= it.stockMinimo }
    val bajos = state.productos.count { it.stockActual > it.stockMinimo && it.stockActual <= it.stockMinimo * 2 }
    val ok = state.productos.count { it.stockActual > it.stockMinimo * 2 }

    Scaffold(
        containerColor = FondoClaro,
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        "Mi Inventario",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                        color = SobreFondoClaro
                    )
                },
                actions = {
                    IconButton(
                        onClick = onProveedoresClick,
                        modifier = Modifier.padding(end = 8.dp).size(48.dp).clip(CircleShape).background(AzulFondo)
                    ) {
                        Icon(Icons.Outlined.Groups, null, tint = AzulPrimario)
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = FondoClaro,
                    titleContentColor = SobreFondoClaro
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNuevoProducto,
                containerColor = AzulPrimario,
                contentColor = Color.White,
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(Icons.Filled.Add, null, Modifier.size(28.dp))
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            // Dashboard de Estadísticas
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard("Críticos", criticos.toString(), RojoStock, RojoFondo, Icons.Outlined.WarningAmber, Modifier.weight(1f))
                StatCard("Bajos", bajos.toString(), AmbarStock, AmbarFondo, Icons.AutoMirrored.Outlined.TrendingDown, Modifier.weight(1f))
                StatCard("OK", ok.toString(), VerdeStock, VerdeFondo, Icons.AutoMirrored.Outlined.TrendingUp, Modifier.weight(1f))
            }

            when {
                state.cargando -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator(color = AzulPrimario)
                }
                state.productos.isEmpty() -> EstadoVacio(
                    "Sin productos", "Tu inventario está listo para empezar.", Icons.Outlined.Inventory2
                )
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(state.productos, key = { it.id }) { p ->
                            ProductoSquareCard(p) { onProductoClick(p.id) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, color: Color, fondo: Color, icon: ImageVector, modifier: Modifier) {
    Surface(
        modifier = modifier,
        color = fondo,
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.Start) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(8.dp))
            Text(value, fontSize = 36.sp, fontWeight = FontWeight.Black, color = color, lineHeight = 36.sp)
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color.copy(alpha = 0.7f))
        }
    }
}

@Composable
private fun ProductoSquareCard(p: Producto, onClick: () -> Unit) {
    val (color, _) = nivelStock(p.stockActual, p.stockMinimo)
    
    Surface(
        onClick = onClick,
        color = SuperficieClara,
        shape = RoundedCornerShape(32.dp),
        shadowElevation = 2.dp,
        modifier = Modifier.aspectRatio(1f)
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Surface(
                    color = color.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "${p.stockActual}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Black,
                        color = color,
                        fontSize = 28.sp,
                        lineHeight = 28.sp
                    )
                }
                Icon(Icons.Outlined.Inventory2, null, tint = SobreVariante.copy(alpha = 0.3f), modifier = Modifier.size(20.dp))
            }
            
            Column {
                Text(
                    p.nombre,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, lineHeight = 20.sp),
                    color = SobreFondoClaro
                )
                Text(
                    p.categoria,
                    style = MaterialTheme.typography.labelSmall,
                    color = SobreVariante,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun EstadoVacio(titulo: String, detalle: String, icono: ImageVector) {
    Column(
        Modifier.fillMaxSize().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icono, null, Modifier.size(80.dp), tint = AzulFondo)
        Spacer(Modifier.height(24.dp))
        Text(titulo, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = SobreFondoClaro)
        Text(detalle, textAlign = TextAlign.Center, color = SobreVariante)
    }
}

private fun nivelStock(actual: Int, minimo: Int): Pair<Color, String> = when {
    actual <= minimo       -> RojoStock  to "Crítico"
    actual <= minimo * 2   -> AmbarStock to "Bajo"
    else                   -> VerdeStock to "OK"
}
