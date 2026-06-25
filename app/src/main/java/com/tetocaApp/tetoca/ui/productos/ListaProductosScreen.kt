package com.tetocaApp.tetoca.ui.productos

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.tetocaApp.tetoca.data.local.Producto
import com.tetocaApp.tetoca.ui.theme.*
import com.tetocaApp.tetoca.viewmodel.FiltroStock
import com.tetocaApp.tetoca.viewmodel.ProductosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaProductosScreen(
    onProductoClick: (productoId: Long) -> Unit,
    onNuevoProducto: () -> Unit,
    onProveedoresClick: () -> Unit,
    onReposicionClick: () -> Unit = {},
    onEstadisticasClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: ProductosViewModel = viewModel(factory = ProductosViewModel.Factory(context))
    val state by viewModel.uiState.collectAsState()

    val countCriticos = state.productos.count { it.stockActual <= it.stockMinimo }
    val countBajos = state.productos.count { it.stockActual > it.stockMinimo && it.stockActual <= it.stockMinimo * 2 }

    val density = LocalDensity.current
    val staggerOffsetPx = remember(density) { with(density) { 24.dp.toPx() } }

    Scaffold(
        containerColor = FondoClaro,
        topBar = {
            Column(Modifier.background(FondoClaro).statusBarsPadding()) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "STOCK CONTROL",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp,
                                color = AzulPrimario
                            )
                        )
                        Text(
                            "Panel de Inventario",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = SobreFondoClaro
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = onReposicionClick,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(AzulFondo)
                        ) {
                            Icon(Icons.Outlined.Warning, contentDescription = "Reposición de stock", tint = AzulPrimario)
                        }
                        IconButton(
                            onClick = onEstadisticasClick,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(AzulFondo)
                        ) {
                            Icon(Icons.Outlined.BarChart, contentDescription = "Ver estadísticas", tint = AzulPrimario)
                        }
                        IconButton(
                            onClick = onProveedoresClick,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(AzulFondo)
                        ) {
                            Icon(Icons.Outlined.Groups, contentDescription = "Directorio de proveedores", tint = AzulPrimario)
                        }
                        IconButton(
                            onClick = {
                                FirebaseAuth.getInstance().signOut()
                                (context as? Activity)?.let { activity ->
                                    activity.finish()
                                    activity.startActivity(activity.intent)
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(RojoFondo)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Cerrar sesión",
                                tint = RojoStock
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = state.query,
                    onValueChange = viewModel::onQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    placeholder = { Text("Buscar producto o categoría...", fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar", tint = AzulPrimario, modifier = Modifier.size(20.dp)) },
                    trailingIcon = {
                        if (state.filtro != FiltroStock.TODOS) {
                            Icon(
                                Icons.Outlined.FilterList,
                                contentDescription = "Limpiar filtro",
                                tint = AzulPrimario,
                                modifier = Modifier.clickable { viewModel.onFiltroChange(FiltroStock.TODOS) }
                            )
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SuperficieClara,
                        unfocusedContainerColor = SuperficieClara,
                        focusedBorderColor = AzulPrimario,
                        unfocusedBorderColor = BordeClaro,
                        focusedPlaceholderColor = SobreVariante.copy(alpha = 0.5f)
                    )
                )

                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FiltroCard("Todos", state.productos.size, null, state.filtro == FiltroStock.TODOS, Modifier.weight(1f)) {
                        viewModel.onFiltroChange(FiltroStock.TODOS)
                    }
                    FiltroCard("Crítico", countCriticos, RojoStock, state.filtro == FiltroStock.CRITICO, Modifier.weight(1f)) {
                        viewModel.onFiltroChange(FiltroStock.CRITICO)
                    }
                    FiltroCard("Bajo", countBajos, AmbarStock, state.filtro == FiltroStock.BAJO, Modifier.weight(1f)) {
                        viewModel.onFiltroChange(FiltroStock.BAJO)
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNuevoProducto,
                containerColor = AzulPrimario,
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Agregar nuevo producto", Modifier.size(30.dp))
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            Crossfade(
                targetState = when {
                    state.cargando -> 0
                    state.productosFiltrados.isEmpty() -> 1
                    else -> 2
                },
                animationSpec = tween(280, easing = FastOutSlowInEasing),
                label = "list_state"
            ) { screenState ->
                when (screenState) {
                    0 -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AzulPrimario)
                    }
                    1 -> EstadoVacio(
                        if (state.query.isEmpty()) "Sin productos" else "Sin resultados",
                        if (state.query.isEmpty()) "Agrega productos para gestionar tu stock." else "No encontramos coincidencias para \"${state.query}\"",
                        Icons.Outlined.Inventory2
                    )
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 100.dp, start = 20.dp, end = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            HeroResumen(
                                total = state.productos.size,
                                criticos = countCriticos,
                                bajos = countBajos
                            )
                        }
                        item {
                            Text(
                                "Mostrando ${state.productosFiltrados.size} resultados",
                                style = MaterialTheme.typography.labelSmall,
                                color = SobreVariante,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                        itemsIndexed(state.productosFiltrados, key = { _, p -> p.id }) { index, p ->
                            ProductoCompactRow(p, index, staggerOffsetPx) { onProductoClick(p.id) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroResumen(total: Int, criticos: Int, bajos: Int) {
    val ok = total - criticos - bajos

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val progress by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "hero"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = progress
                translationY = (1f - progress) * 30f
            },
        color = AzulFondo.copy(alpha = 0.6f),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeroStat(total.toString(), "Total", AzulPrimario, Modifier.weight(1f))
            HeroStat(ok.toString(), "Óptimo", VerdeStock, Modifier.weight(1f))
            HeroStat(criticos.toString(), "Crítico", RojoStock, Modifier.weight(1f))
            HeroStat(bajos.toString(), "Bajo", AmbarStock, Modifier.weight(1f))
        }
    }
}

@Composable
private fun HeroStat(valor: String, label: String, color: Color, modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(valor, fontSize = 22.sp, fontWeight = FontWeight.Black, color = color)
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = SobreVariante)
    }
}

@Composable
private fun FiltroCard(
    label: String,
    count: Int,
    color: Color?,
    seleccionado: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val fondo = if (seleccionado) (color ?: AzulPrimario).copy(alpha = 0.15f) else SuperficieClara
    val borde = if (seleccionado) (color ?: AzulPrimario) else BordeClaro
    val contentColor = if (seleccionado) (color ?: AzulPrimario) else SobreVariante

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(120, easing = FastOutSlowInEasing),
        label = "press_filtro"
    )

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier
            .height(52.dp)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            },
        color = fondo,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, borde)
    ) {
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = contentColor)
            Text(count.toString(), fontSize = 16.sp, fontWeight = FontWeight.Black, color = contentColor)
        }
    }
}

@Composable
private fun ProductoCompactRow(p: Producto, index: Int, staggerOffsetPx: Float, onClick: () -> Unit) {
    val (color, _) = nivelStock(p.stockActual, p.stockMinimo)

    var itemVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * 50L)
        itemVisible = true
    }
    val staggerProgress by animateFloatAsState(
        targetValue = if (itemVisible) 1f else 0f,
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "stagger_$index"
    )

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(120, easing = FastOutSlowInEasing),
        label = "press_$index"
    )

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        color = SuperficieClara,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 0.5.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, BordeClaro.copy(alpha = 0.3f)),
        modifier = Modifier.graphicsLayer {
            alpha = staggerProgress
            translationY = (1f - staggerProgress) * staggerOffsetPx
            scaleX = pressScale
            scaleY = pressScale
        }
    ) {
        Row(
            Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .width(4.dp)
                    .height(32.dp)
                    .clip(CircleShape)
                    .background(color)
            )

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    p.nombre,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = SobreFondoClaro
                )
                Text(
                    p.categoria.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.5.sp),
                    color = SobreVariante
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${p.stockActual}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                        color = color
                    )
                    Text(
                        " UN",
                        style = MaterialTheme.typography.labelSmall,
                        color = color.copy(alpha = 0.7f)
                    )
                }
                p.precio?.let {
                    Text(
                        "S/ ${"%.2f".format(it)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = SobreVariante
                    )
                }
            }
        }
    }
}

@Composable
private fun EstadoVacio(titulo: String, detalle: String, icono: ImageVector) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            color = AzulFondo.copy(alpha = 0.5f),
            shape = CircleShape,
            modifier = Modifier.size(100.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icono, contentDescription = null, Modifier.size(56.dp), tint = AzulPrimario.copy(alpha = 0.3f))
            }
        }
        Spacer(Modifier.height(24.dp))
        Text(titulo, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = SobreFondoClaro)
        Spacer(Modifier.height(8.dp))
        Text(detalle, textAlign = TextAlign.Center, fontSize = 14.sp, color = SobreVariante, lineHeight = 20.sp)
    }
}

private fun nivelStock(actual: Int, minimo: Int): Pair<Color, String> = when {
    actual <= 0          -> RojoStock  to "Agotado"
    actual <= minimo     -> RojoStock  to "Crítico"
    actual <= minimo * 2 -> AmbarStock to "Bajo"
    else                 -> VerdeStock to "Óptimo"
}
