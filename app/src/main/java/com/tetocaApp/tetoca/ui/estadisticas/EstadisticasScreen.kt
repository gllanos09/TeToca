package com.tetocaApp.tetoca.ui.estadisticas

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
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tetocaApp.tetoca.data.local.TeTocaDatabase
import com.tetocaApp.tetoca.ui.theme.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

// ── ViewModel ─────────────────────────────────────────────────────────────────

class EstadisticasViewModel(db: TeTocaDatabase) : ViewModel() {

    val totalSkus: StateFlow<Int> =
        db.productoDao().contarSkus()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val valorTotal: StateFlow<Double?> =
        db.productoDao().calcularValorTotal()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val categoriaMasStock: StateFlow<String?> =
        db.productoDao().categoriaMasStock()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val totalUnidades: StateFlow<Int?> =
        db.productoDao().totalUnidades()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    companion object {
        fun Factory(db: TeTocaDatabase) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>) =
                EstadisticasViewModel(db) as T
        }
    }
}

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstadisticasScreen(
    onVolver: () -> Unit,
    onConfiguracionClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val db = TeTocaDatabase.getInstance(context)
    val viewModel: EstadisticasViewModel = viewModel(
        factory = EstadisticasViewModel.Factory(db)
    )

    val totalSkus by viewModel.totalSkus.collectAsState()
    val valorTotal by viewModel.valorTotal.collectAsState()
    val categoriaMasStock by viewModel.categoriaMasStock.collectAsState()
    val totalUnidades by viewModel.totalUnidades.collectAsState()

    Scaffold(
        containerColor = FondoClaro,
        topBar = {
            TopAppBar(
                title = { Text("Estadísticas", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver atrás")
                    }
                },
                actions = {
                    IconButton(onClick = onConfiguracionClick) {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = "Configuración del negocio",
                            tint = SobreAzul
                        )
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Resumen del inventario",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = SobreFondoClaro
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricaCard(
                    icono = Icons.Filled.Inventory2,
                    titulo = "SKUs activos",
                    valorNumerico = totalSkus.toFloat(),
                    esEntero = true,
                    descripcion = "Productos distintos",
                    index = 0,
                    modifier = Modifier.weight(1f)
                )
                MetricaCard(
                    icono = Icons.Filled.LocalShipping,
                    titulo = "Total unidades",
                    valorNumerico = (totalUnidades ?: 0).toFloat(),
                    esEntero = true,
                    descripcion = "En inventario",
                    index = 1,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricaCard(
                    icono = Icons.Filled.MonetizationOn,
                    titulo = "Valor total",
                    valorNumerico = (valorTotal ?: 0.0).toFloat(),
                    esEntero = false,
                    prefijo = "S/ ",
                    descripcion = "Precio × stock",
                    index = 2,
                    modifier = Modifier.weight(1f)
                )
                MetricaCardTexto(
                    icono = Icons.AutoMirrored.Filled.TrendingUp,
                    titulo = "Top categoría",
                    valor = categoriaMasStock ?: "—",
                    descripcion = "Más unidades",
                    index = 3,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MetricaCard(
    icono: ImageVector,
    titulo: String,
    valorNumerico: Float,
    esEntero: Boolean,
    descripcion: String,
    index: Int,
    modifier: Modifier = Modifier,
    prefijo: String = ""
) {
    var animStarted by remember { mutableStateOf(false) }
    LaunchedEffect(valorNumerico) {
        kotlinx.coroutines.delay(index * 60L)
        animStarted = true
    }

    val animatedValue by animateFloatAsState(
        targetValue = if (animStarted) valorNumerico else 0f,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "num_$index"
    )

    val staggerAlpha by animateFloatAsState(
        targetValue = if (animStarted) 1f else 0f,
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "stagger_$index"
    )

    Surface(
        modifier = modifier.graphicsLayer { alpha = staggerAlpha },
        color = SuperficieClara,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, BordeClaro.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AzulFondo),
                contentAlignment = Alignment.Center
            ) {
                Icon(icono, contentDescription = titulo, tint = AzulPrimario, modifier = Modifier.size(22.dp))
            }
            Text(
                titulo,
                style = MaterialTheme.typography.labelMedium,
                color = SobreVariante
            )
            Text(
                text = if (esEntero) "$prefijo${animatedValue.toInt()}"
                else "$prefijo${"%.2f".format(animatedValue.toDouble())}",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                color = SobreFondoClaro
            )
            Text(
                descripcion,
                style = MaterialTheme.typography.bodySmall,
                color = SobreVariante
            )
        }
    }
}

@Composable
private fun MetricaCardTexto(
    icono: ImageVector,
    titulo: String,
    valor: String,
    descripcion: String,
    index: Int,
    modifier: Modifier = Modifier
) {
    var animStarted by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * 60L)
        animStarted = true
    }

    val staggerAlpha by animateFloatAsState(
        targetValue = if (animStarted) 1f else 0f,
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "stagger_txt_$index"
    )

    Surface(
        modifier = modifier.graphicsLayer { alpha = staggerAlpha },
        color = SuperficieClara,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, BordeClaro.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AzulFondo),
                contentAlignment = Alignment.Center
            ) {
                Icon(icono, contentDescription = titulo, tint = AzulPrimario, modifier = Modifier.size(22.dp))
            }
            Text(
                titulo,
                style = MaterialTheme.typography.labelMedium,
                color = SobreVariante
            )
            Text(
                valor,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                color = SobreFondoClaro,
                maxLines = 1
            )
            Text(
                descripcion,
                style = MaterialTheme.typography.bodySmall,
                color = SobreVariante
            )
        }
    }
}