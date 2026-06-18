package com.tetocaApp.tetoca.ui.productos

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
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
    var mostrarDialogo by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del producto") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
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

                state.producto != null -> {
                    val p = state.producto!!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            p.nombre,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        // Ficha de producto: agrupar los datos en una sola
                        // tarjeta da jerarquía visual clara frente a texto
                        // suelto directamente sobre el fondo.
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Campo("Categoría", p.categoria)
                                Campo("Stock actual", p.stockActual.toString())
                                Campo("Stock mínimo", p.stockMinimo.toString())
                                Campo("Precio", p.precio?.let { "S/ " + "%.2f".format(it) } ?: "—")
                                Campo("Proveedor", state.proveedorNombre ?: "—")
                            }
                        }

                        Spacer(Modifier.weight(1f))

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            BotonConFeedback(
                                onClick = { mostrarDialogo = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedButton(
                                    onClick = { mostrarDialogo = true },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text("Eliminar") }
                            }
                            BotonConFeedback(
                                onClick = { onEditar(p.id) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Button(
                                    onClick = { onEditar(p.id) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text("Editar") }
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            title = { Text("Eliminar producto") },
            text = { Text("¿Seguro que deseas eliminar este producto? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    mostrarDialogo = false
                    viewModel.eliminar { onEliminado() }
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogo = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun Campo(etiqueta: String, valor: String) {
    Column {
        Text(
            etiqueta,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(valor, style = MaterialTheme.typography.bodyLarge)
    }
}

/**
 * Envuelve un botón con feedback táctil (encogimiento leve al presionar).
 * El contenido recibe su propio onClick porque el M3 Button no expone
 * directamente su InteractionSource interno; este wrapper solo aplica
 * la escala visual alrededor.
 */
@Composable
private fun BotonConFeedback(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val presionado by interactionSource.collectIsPressedAsState()
    val escala by animateFloatAsState(
        targetValue = if (presionado) 0.96f else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "escalaBoton"
    )
    Box(modifier = modifier.scale(escala)) {
        content()
    }
}