package com.tetocaApp.tetoca.ui.estadisticas

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tetocaApp.tetoca.data.local.TeTocaDatabase
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
fun EstadisticasScreen(onVolver: () -> Unit) {
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
        topBar = {
            TopAppBar(
                title = { Text("Estadísticas") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Resumen del inventario",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // Fila 1: SKUs + Unidades totales
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MetricaCard(
                    icono = Icons.Filled.Inventory2,
                    titulo = "SKUs activos",
                    valor = totalSkus.toString(),
                    descripcion = "Productos distintos",
                    modifier = Modifier.weight(1f)
                )
                MetricaCard(
                    icono = Icons.Filled.LocalShipping,
                    titulo = "Total unidades",
                    valor = (totalUnidades ?: 0).toString(),
                    descripcion = "En inventario",
                    modifier = Modifier.weight(1f)
                )
            }

            // Fila 2: Valor total
            MetricaCard(
                icono = Icons.Filled.MonetizationOn,
                titulo = "Valor total del inventario",
                valor = valorTotal?.let { "S/ ${"%.2f".format(it)}" } ?: "S/ 0.00",
                descripcion = "Precio × stock de cada producto",
                modifier = Modifier.fillMaxWidth()
            )

            // Fila 3: Categoría con más stock
            MetricaCard(
                icono = Icons.Filled.TrendingUp,
                titulo = "Categoría con más stock",
                valor = categoriaMasStock ?: "—",
                descripcion = "Mayor cantidad de unidades",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun MetricaCard(
    icono: ImageVector,
    titulo: String,
    valor: String,
    descripcion: String,
    modifier: Modifier = Modifier
) {
    // Fade-in al aparecer
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "fadeMetrica"
    )

    Card(
        modifier = modifier.alpha(alpha),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icono,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Text(
                titulo,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                valor,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                descripcion,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}