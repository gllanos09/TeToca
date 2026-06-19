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
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
            LargeTopAppBar(
                title = { 
                    Text(
                        "Proveedores", 
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black), 
                        color = SobreFondoClaro
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, Modifier.size(28.dp), tint = AzulPrimario)
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(containerColor = FondoClaro)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = viewModel::onNuevoProveedor,
                containerColor = AzulPrimario,
                contentColor = Color.White,
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(Icons.Filled.Add, null, Modifier.size(28.dp))
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
                1 -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(40.dp)) {
                        Icon(Icons.Outlined.Groups, null, Modifier.size(80.dp), tint = AzulFondo)
                        Spacer(Modifier.height(24.dp))
                        Text("Sin contactos", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = SobreFondoClaro)
                        Text("Agrega tu primer proveedor para gestionar pedidos.", textAlign = TextAlign.Center, color = SobreVariante)
                    }
                }
                else -> LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.proveedores, key = { it.id }) { prov ->
                        ProveedorHeroCard(
                            prov = prov, 
                            onEditar = { viewModel.onEditarProveedor(prov) }, 
                            onEliminar = { aEliminar = prov }
                        )
                    }
                }
            }
        }
    }

    if (state.mostrarFormulario) {
        ModalBottomSheet(
            onDismissRequest = viewModel::onCerrarFormulario,
            containerColor = SuperficieClara,
            dragHandle = { BottomSheetDefaults.DragHandle(color = BordeClaro) }
        ) {
            FormProveedor(state.proveedorEnEdicion, state.guardando) { n, t, notas ->
                viewModel.guardar(n, t, notas)
            }
        }
    }

    aEliminar?.let { prov ->
        AlertDialog(
            onDismissRequest = { aEliminar = null },
            containerColor = SuperficieClara,
            shape = RoundedCornerShape(28.dp),
            title = { Text("¿Eliminar proveedor?", color = SobreFondoClaro, fontWeight = FontWeight.Bold) },
            text = { Text("¿Deseas eliminar a \"${prov.nombre}\"? Se perderá la asociación con sus productos.", color = SobreVariante) },
            confirmButton = {
                TextButton(onClick = { viewModel.eliminar(prov); aEliminar = null }) {
                    Text("Eliminar", color = RojoStock, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { aEliminar = null }) { Text("Cancelar", color = SobreFondoClaro) }
            }
        )
    }
}

@Composable
private fun ProveedorHeroCard(prov: Proveedor, onEditar: () -> Unit, onEliminar: () -> Unit) {
    Surface(
        onClick = onEditar,
        color = SuperficieClara,
        shape = RoundedCornerShape(32.dp),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.fillMaxWidth()) {
            // "Hero" Header de la Card
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Brush.horizontalGradient(listOf(AzulPrimario, AzulPrimario.copy(alpha = 0.7f)))),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(Modifier.padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                prov.nombre.take(1).uppercase(),
                                fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White
                            )
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(prov.nombre, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        Text("Proveedor Activo", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                }
            }
            
            // Cuerpo de la Card
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Phone, null, Modifier.size(16.dp), tint = AzulPrimario)
                        Spacer(Modifier.width(8.dp))
                        Text(prov.telefono, fontWeight = FontWeight.Bold, color = SobreFondoClaro)
                    }
                    if (!prov.notas.isNullOrBlank()) {
                        Text(
                            prov.notas!!, 
                            style = MaterialTheme.typography.bodySmall, 
                            color = SobreVariante,
                            maxLines = 1,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                
                IconButton(
                    onClick = onEliminar,
                    modifier = Modifier.clip(CircleShape).background(RojoFondo)
                ) {
                    Icon(Icons.Filled.DeleteOutline, null, tint = RojoStock, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun FormProveedor(inicial: Proveedor?, guardando: Boolean, onGuardar: (String, String, String?) -> Unit) {
    var nombre by remember(inicial) { mutableStateOf(inicial?.nombre ?: "") }
    var telefono by remember(inicial) { mutableStateOf(inicial?.telefono ?: "") }
    var notas by remember(inicial) { mutableStateOf(inicial?.notas ?: "") }

    Column(
        Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            if (inicial == null) "Nuevo Proveedor" else "Editar Detalles",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
            color = SobreFondoClaro
        )
        
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre de la Empresa") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AzulPrimario,
                unfocusedBorderColor = BordeClaro,
                focusedLabelColor = AzulPrimario
            )
        )

        OutlinedTextField(
            value = telefono,
            onValueChange = { telefono = it },
            label = { Text("Teléfono / Contacto") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AzulPrimario,
                unfocusedBorderColor = BordeClaro
            )
        )

        OutlinedTextField(
            value = notas,
            onValueChange = { notas = it },
            label = { Text("Notas o descripción") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            minLines = 3,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AzulPrimario,
                unfocusedBorderColor = BordeClaro
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
            else Text("CONFIRMAR", fontWeight = FontWeight.Bold)
        }
    }
}
