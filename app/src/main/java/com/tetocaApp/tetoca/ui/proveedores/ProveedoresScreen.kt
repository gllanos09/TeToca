package com.tetocaApp.tetoca.ui.proveedores

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
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
        containerColor = FondoClaro,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "DIRECTORIO DE PROVEEDORES",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 2.sp),
                        color = SobreVariante
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = AzulPrimario)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = FondoClaro)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = viewModel::onNuevoProveedor,
                containerColor = AzulPrimario,
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(Icons.Filled.Add, null, Modifier.size(30.dp))
            }
        }
    ) { padding ->
        Crossfade(
            targetState = when { state.cargando -> 0; state.proveedores.isEmpty() -> 1; else -> 2 },
            animationSpec = tween(400),
            label = "prov_state",
            modifier = Modifier.fillMaxSize().padding(padding)
        ) { est ->
            when (est) {
                0 -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = AzulPrimario) }
                1 -> EstadoVacioProveedores()
                else -> LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            "Gestione sus contactos de abastecimiento",
                            style = MaterialTheme.typography.labelSmall,
                            color = SobreVariante,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    items(state.proveedores, key = { it.id }) { prov ->
                        ProveedorCompactCard(
                            prov = prov,
                            onEditar = { viewModel.onEditarProveedor(prov) },
                            onEliminar = { aEliminar = prov }
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }

    if (state.mostrarFormulario) {
        ModalBottomSheet(
            onDismissRequest = viewModel::onCerrarFormulario,
            containerColor = SuperficieClara,
            dragHandle = { BottomSheetDefaults.DragHandle(color = BordeClaro) },
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
        ) {
            FormProveedorLimpio(state.proveedorEnEdicion, state.guardando) { n, t, notas ->
                viewModel.guardar(n, t, notas)
            }
        }
    }

    aEliminar?.let { prov ->
        AlertDialog(
            onDismissRequest = { aEliminar = null },
            containerColor = SuperficieClara,
            shape = RoundedCornerShape(24.dp),
            title = { Text("¿Eliminar contacto?", fontWeight = FontWeight.Bold) },
            text = { Text("¿Deseas quitar a \"${prov.nombre}\"? Esto no eliminará sus productos, pero quedarán sin proveedor asignado.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.eliminar(prov); aEliminar = null },
                    colors = ButtonDefaults.buttonColors(containerColor = RojoStock),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { aEliminar = null }) { Text("Cancelar", color = SobreFondoClaro) }
            }
        )
    }
}

@Composable
private fun ProveedorCompactCard(prov: Proveedor, onEditar: () -> Unit, onEliminar: () -> Unit) {
    Surface(
        onClick = onEditar,
        color = SuperficieClara,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 0.5.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, BordeClaro.copy(alpha = 0.5f))
    ) {
        Row(
            Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = AzulFondo,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        prov.nombre.take(1).uppercase(),
                        fontSize = 18.sp, fontWeight = FontWeight.Black, color = AzulPrimario
                    )
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(Modifier.weight(1f)) {
                Text(prov.nombre, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = SobreFondoClaro)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Phone, null, Modifier.size(12.dp), tint = SobreVariante)
                    Spacer(Modifier.width(4.dp))
                    Text(prov.telefono, style = MaterialTheme.typography.labelMedium, color = SobreVariante)
                }
            }
            
            IconButton(
                onClick = onEliminar,
                modifier = Modifier.size(36.dp).clip(CircleShape).background(RojoFondo.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Filled.DeleteOutline, null, tint = RojoStock, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun FormProveedorLimpio(inicial: Proveedor?, guardando: Boolean, onGuardar: (String, String, String?) -> Unit) {
    var nombre by remember(inicial) { mutableStateOf(inicial?.nombre ?: "") }
    var telefono by remember(inicial) { mutableStateOf(inicial?.telefono ?: "") }
    var notas by remember(inicial) { mutableStateOf(inicial?.notas ?: "") }

    Column(
        Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            if (inicial == null) "Registrar Proveedor" else "Actualizar Contacto",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
            color = SobreFondoClaro
        )
        
        TextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Razón Social / Nombre", fontSize = 13.sp) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = FondoClaro,
                unfocusedContainerColor = FondoClaro,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedLabelColor = AzulPrimario
            )
        )

        TextField(
            value = telefono,
            onValueChange = { telefono = it },
            label = { Text("Teléfono de contacto", fontSize = 13.sp) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = FondoClaro,
                unfocusedContainerColor = FondoClaro,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedLabelColor = AzulPrimario
            )
        )

        TextField(
            value = notas,
            onValueChange = { notas = it },
            label = { Text("Observaciones adicionales", fontSize = 13.sp) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            minLines = 3,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = FondoClaro,
                unfocusedContainerColor = FondoClaro,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedLabelColor = AzulPrimario
            )
        )
        
        Spacer(Modifier.height(8.dp))
        
        Button(
            onClick = { onGuardar(nombre, telefono, if(notas.isBlank()) null else notas) },
            enabled = !guardando && nombre.isNotBlank() && telefono.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AzulPrimario)
        ) {
            if (guardando) CircularProgressIndicator(Modifier.size(24.dp), color = Color.White)
            else Text("CONFIRMAR DATOS", fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
        }
    }
}

@Composable
private fun EstadoVacioProveedores() {
    Column(
        Modifier.fillMaxSize().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            color = AzulFondo.copy(alpha = 0.5f),
            shape = CircleShape,
            modifier = Modifier.size(100.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Groups, null, Modifier.size(48.dp), tint = AzulPrimario.copy(alpha = 0.3f))
            }
        }
        Spacer(Modifier.height(24.dp))
        Text("Sin contactos", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = SobreFondoClaro)
        Spacer(Modifier.height(8.dp))
        Text("Empieza registrando a tus proveedores para vincularlos con tus productos.", 
            textAlign = TextAlign.Center, fontSize = 14.sp, color = SobreVariante, lineHeight = 20.sp)
    }
}
