package com.tetocaApp.tetoca.ui.reposicion

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tetocaApp.tetoca.data.local.Producto
import com.tetocaApp.tetoca.data.local.TeTocaDatabase
import com.tetocaApp.tetoca.ui.theme.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

// ── ViewModel ─────────────────────────────────────────────────────────────────

class ReposicionViewModel(db: TeTocaDatabase) : ViewModel() {

    val productosBajos: StateFlow<List<Producto>> =
        db.productoDao().obtenerConStockBajo()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    companion object {
        fun Factory(db: TeTocaDatabase) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>) =
                ReposicionViewModel(db) as T
        }
    }
}

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReposicionScreen(onVolver: () -> Unit) {
    val context = LocalContext.current
    val db = TeTocaDatabase.getInstance(context)
    val viewModel: ReposicionViewModel = viewModel(factory = ReposicionViewModel.Factory(db))
    val productos by viewModel.productosBajos.collectAsState()

    val density = LocalDensity.current
    val staggerOffsetPx = remember(density) { with(density) { 24.dp.toPx() } }

    Scaffold(
        containerColor = FondoClaro,
        topBar = {
            TopAppBar(
                title = { Text("Reposición de stock", fontWeight = FontWeight.Bold) },
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
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (productos.isEmpty()) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        color = VerdeFondo,
                        shape = CircleShape,
                        modifier = Modifier.size(100.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = VerdeStock.copy(alpha = 0.5f)
                            )
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                    Text(
                        "¡Todo en orden!",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        color = SobreFondoClaro
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "No hay productos que necesiten reposición.",
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        color = SobreVariante,
                        lineHeight = 20.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Surface(
                            color = RojoFondo.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.Warning,
                                    contentDescription = null,
                                    tint = RojoStock,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    "${productos.size} producto(s) necesitan reposición",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = RojoStock
                                )
                            }
                        }
                    }
                    itemsIndexed(productos, key = { _, p -> p.id }) { index, producto ->
                        ProductoReposicionItem(
                            producto = producto,
                            index = index,
                            staggerOffsetPx = staggerOffsetPx,
                            onContactarProveedor = { telefono, mensaje ->
                                val url = "https://wa.me/$telefono?text=${Uri.encode(mensaje)}"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductoReposicionItem(
    producto: Producto,
    index: Int,
    staggerOffsetPx: Float,
    onContactarProveedor: (telefono: String, mensaje: String) -> Unit
) {
    var itemVisible by remember { mutableStateOf(false) }
    LaunchedEffect(producto.id) {
        kotlinx.coroutines.delay(index * 50L)
        itemVisible = true
    }
    val staggerProgress by animateFloatAsState(
        targetValue = if (itemVisible) 1f else 0f,
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "stagger_$index"
    )

    val deficit = (producto.stockMinimo - producto.stockActual).coerceAtLeast(0)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = staggerProgress
                translationY = (1f - staggerProgress) * staggerOffsetPx
            },
        color = ColorErrorFondo.copy(alpha = 0.15f),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, RojoStock.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        producto.nombre,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = SobreFondoClaro
                    )
                    Text(
                        producto.categoria,
                        style = MaterialTheme.typography.bodySmall,
                        color = SobreVariante
                    )
                }
                Surface(
                    color = RojoStock,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        "+$deficit",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                        color = SobreAzul
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StockChip(label = "Actual", valor = producto.stockActual.toString(), esError = true)
                StockChip(label = "Mínimo", valor = producto.stockMinimo.toString(), esError = false)
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    val mensaje = buildString {
                        append("Hola, necesito reponer el producto *${producto.nombre}*.\n")
                        append("Stock actual: ${producto.stockActual} unidades.\n")
                        append("Stock mínimo requerido: ${producto.stockMinimo} unidades.\n")
                        append("Por favor, coordinar reabastecimiento. Gracias.")
                    }
                    onContactarProveedor("51999999999", mensaje)
                },
                colors = ButtonDefaults.buttonColors(containerColor = VerdeStock),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Chat,
                    contentDescription = "Contactar por WhatsApp",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Contactar por WhatsApp",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun StockChip(label: String, valor: String, esError: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = SobreVariante
        )
        Text(
            valor,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = if (esError) RojoStock else SobreFondoClaro
        )
    }
}
