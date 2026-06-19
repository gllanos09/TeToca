package com.tetocaApp.tetoca.ui.proveedores

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Phone
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
import com.tetocaApp.tetoca.data.local.Proveedor
import com.tetocaApp.tetoca.ui.theme.*
import com.tetocaApp.tetoca.viewmodel.ProveedoresViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProveedoresScreen(onVolver: () -> Unit) {
    val context = LocalContext.current
    val viewModel: ProveedoresViewModel = viewModel(factory = ProveedoresViewModel.Factory(context))
    val state by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var aEliminar by remember { mutableStateOf<Proveedor?>(null) }

    LaunchedEffect(state.error) {
        state.error?.let { snackbar.showSnackbar(it); viewModel.limpiarError() }
    }

    Scaffold(
        containerColor = Fondo,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Proveedores", style = MaterialTheme.typography.headlineSmall, color = SobreFondo) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, Modifier.size(26.dp), tint = SobreFondo)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Fondo)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("NUEVO", style = MaterialTheme.typography.labelLarge) },
                icon = { Icon(Icons.Filled.Add, null, Modifier.size(22.dp)) },
                onClick = viewModel::onNuevoProveedor,
                containerColor = Violeta, contentColor = Color.White
            )
        }
    ) { padding ->
        Crossfade(
            when { state.cargando -> 0; state.proveedores.isEmpty() -> 1; else -> 2 },
            tween(300), label = "prov",
            modifier = Modifier.fillMaxSize().padding(padding)
        ) { est ->
            when (est) {
                0 -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = Violeta) }
                1 -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                        Box(Modifier.size(96.dp).clip(CircleShape).background(SuperficieAlt), Alignment.Center) {
                            Icon(Icons.Outlined.Groups, null, Modifier.size(48.dp), tint = SobreVariante)
                        }
                        Spacer(Modifier.height(16.dp))
                        Text("Sin proveedores", style = MaterialTheme.typography.headlineSmall, color = SobreFondo)
                        Spacer(Modifier.height(6.dp))
                        Text("Agrega proveedores para asociarlos a tus productos.",
                            style = MaterialTheme.typography.bodyMedium, color = SobreVariante, textAlign = TextAlign.Center)
                    }
                }
                else -> LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.proveedores, key = { it.id }) { prov ->
                        ProveedorCard(prov, { viewModel.onEditarProveedor(prov) }, { aEliminar = prov }, Modifier.animateItem())
                    }
                }
            }
        }
    }

    if (state.mostrarFormulario) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(onDismissRequest = viewModel::onCerrarFormulario, sheetState = sheetState,
            containerColor = Superficie) {
            FormProveedor(state.proveedorEnEdicion, state.guardando) { n, t, notas ->
                viewModel.guardar(n, t, notas)
            }
        }
    }

    aEliminar?.let { prov ->
        AlertDialog(
            onDismissRequest = { aEliminar = null },
            containerColor = Superficie,
            title = { Text("¿Eliminar proveedor?", color = SobreFondo) },
            text = { Text("¿Eliminar a \"${prov.nombre}\"? Si tiene productos asociados, no se podrá eliminar.", color = SobreVariante) },
            confirmButton = {
                TextButton(onClick = { viewModel.eliminar(prov); aEliminar = null }) {
                    Text("Eliminar", color = RojoStock, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { aEliminar = null }) { Text("Cancelar", color = SobreVariante) }
            }
        )
    }
}

@Composable
private fun ProveedorCard(prov: Proveedor, onEditar: () -> Unit, onEliminar: () -> Unit, modifier: Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Superficie)
            .clickable(onClick = onEditar)
            .padding(start = 16.dp, top = 14.dp, bottom = 14.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(50.dp).clip(CircleShape).background(VioletaFondo),
            Alignment.Center
        ) {
            Text(
                prov.nombre.take(1).uppercase(),
                fontSize = 22.sp, fontWeight = FontWeight.Black, color = VioletaClaro
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(prov.nombre, style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold, color = SobreFondo)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Phone, null, Modifier.size(13.dp), tint = SobreVariante)
                Spacer(Modifier.width(4.dp))
                Text(prov.telefono, style = MaterialTheme.typography.bodySmall, color = SobreVariante)
            }
        }
        IconButton(onClick = onEditar) { Icon(Icons.Filled.Edit, null, Modifier.size(22.dp), tint = VioletaClaro) }
        IconButton(onClick = onEliminar) { Icon(Icons.Filled.DeleteOutline, null, Modifier.size(22.dp), tint = RojoStock) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormProveedor(inicial: Proveedor?, guardando: Boolean, onGuardar: (String, String, String?) -> Unit) {
    var nombre by remember(inicial) { mutableStateOf(inicial?.nombre ?: "") }
    var telefono by remember(inicial) { mutableStateOf(inicial?.telefono ?: "") }
    var notas by remember(inicial) { mutableStateOf(inicial?.notas ?: "") }

    Column(
        Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(if (inicial == null) "Nuevo proveedor" else "Editar proveedor",
            style = MaterialTheme.typography.headlineSmall, color = SobreFondo)
        CampoSheet(nombre, { nombre = it }, "Nombre")
        CampoSheet(telefono, { telefono = it }, "Teléfono")
        CampoSheet(notas, { notas = it }, "Notas (opcional)")
        Button(
            onClick = { onGuardar(nombre, telefono, notas) },
            enabled = !guardando,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Violeta),
            shape = RoundedCornerShape(14.dp)
        ) {
            if (guardando) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
            else Text("GUARDAR", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CampoSheet(valor: String, onChange: (String) -> Unit, etiqueta: String) {
    OutlinedTextField(
        value = valor, onValueChange = onChange, label = { Text(etiqueta) },
        singleLine = etiqueta != "Notas (opcional)",
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Violeta, unfocusedBorderColor = Borde,
            focusedLabelColor = VioletaClaro, unfocusedLabelColor = SobreVariante,
            focusedTextColor = SobreFondo, unfocusedTextColor = SobreSuperficie,
            cursorColor = Violeta, focusedContainerColor = SuperficieAlt, unfocusedContainerColor = SuperficieAlt
        ),
        modifier = Modifier.fillMaxWidth()
    )
}
