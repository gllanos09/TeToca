package com.tetocaApp.tetoca.ui.config

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.tetocaApp.tetoca.data.local.ModoReposicion
import com.tetocaApp.tetoca.data.local.Negocio
import com.tetocaApp.tetoca.data.local.Rubro
import com.tetocaApp.tetoca.data.local.TeTocaDatabase
import com.tetocaApp.tetoca.data.repository.NegocioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ── UiState ───────────────────────────────────────────────────────────────────

data class ConfigUiState(
    val nombreNegocio: String = "",
    val rubroSeleccionado: Rubro = Rubro.OTRO,
    val stockMinimoGlobal: String = "5",
    val cargando: Boolean = false,
    val guardado: Boolean = false,
    val error: String? = null
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

class ConfigViewModel(private val repo: NegocioRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ConfigUiState())
    val uiState: StateFlow<ConfigUiState> = _uiState.asStateFlow()

    private val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    init {
        // Cargar configuración existente si ya hay una
        viewModelScope.launch {
            val negocio = repo.obtener(uid)
            if (negocio != null) {
                _uiState.value = ConfigUiState(
                    nombreNegocio = negocio.nombreNegocio,
                    rubroSeleccionado = negocio.rubro,
                    stockMinimoGlobal = negocio.stockMinimoGlobal.toString()
                )
            }
        }
    }

    fun onNombreChange(v: String) { _uiState.value = _uiState.value.copy(nombreNegocio = v) }
    fun onRubroChange(r: Rubro) { _uiState.value = _uiState.value.copy(rubroSeleccionado = r) }
    fun onStockMinimoChange(v: String) { _uiState.value = _uiState.value.copy(stockMinimoGlobal = v) }

    fun guardar() {
        val nombre = _uiState.value.nombreNegocio.trim()
        if (nombre.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "El nombre del negocio es obligatorio")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true, error = null)
            try {
                repo.guardar(
                    Negocio(
                        nombreNegocio = nombre,
                        rubro = _uiState.value.rubroSeleccionado,
                        stockMinimoGlobal = _uiState.value.stockMinimoGlobal.toIntOrNull() ?: 5,
                        modoReposicion = ModoReposicion.ESTANDAR,
                        ownerUid = uid
                    )
                )
                _uiState.value = _uiState.value.copy(cargando = false, guardado = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    error = "No se pudo guardar la configuración"
                )
            }
        }
    }

    fun limpiarError() { _uiState.value = _uiState.value.copy(error = null) }

    companion object {
        fun Factory(db: TeTocaDatabase) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>) =
                ConfigViewModel(NegocioRepository(db.negocioDao())) as T
        }
    }
}

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfiguracionScreen(
    onVolver: () -> Unit,
    onGuardado: () -> Unit
) {
    val context = LocalContext.current
    val db = TeTocaDatabase.getInstance(context)
    val viewModel: ConfigViewModel = viewModel(factory = ConfigViewModel.Factory(db))
    val state by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.guardado) {
        if (state.guardado) onGuardado()
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbar.showSnackbar(it)
            viewModel.limpiarError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configura tu negocio") },
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
        },
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            Button(
                onClick = viewModel::guardar,
                enabled = !state.cargando,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(52.dp)
            ) {
                if (state.cargando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Guardar configuración", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // Nombre del negocio
            OutlinedTextField(
                value = state.nombreNegocio,
                onValueChange = viewModel::onNombreChange,
                label = { Text("Nombre del negocio") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Stock mínimo global
            OutlinedTextField(
                value = state.stockMinimoGlobal,
                onValueChange = viewModel::onStockMinimoChange,
                label = { Text("Stock mínimo global (unidades)") },
                supportingText = { Text("Se aplica a nuevos productos que no tengan uno propio") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Selector de rubro
            Text(
                "Tipo de negocio",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(Rubro.values()) { rubro ->
                    RubroCard(
                        rubro = rubro,
                        seleccionado = state.rubroSeleccionado == rubro,
                        onClick = { viewModel.onRubroChange(rubro) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RubroCard(
    rubro: Rubro,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (seleccionado) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.outlineVariant,
        animationSpec = tween(150),
        label = "borderRubro"
    )
    val containerColor by animateColorAsState(
        targetValue = if (seleccionado) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surface,
        animationSpec = tween(150),
        label = "bgRubro"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = if (seleccionado) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(rubro.icono, style = MaterialTheme.typography.headlineMedium)
            Text(
                rubro.etiqueta,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (seleccionado) FontWeight.SemiBold else FontWeight.Normal,
                color = if (seleccionado) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}