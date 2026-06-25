package com.tetocaApp.tetoca.ui.proveedores

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
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

    val density = LocalDensity.current
    val staggerOffsetPx = remember(density) { with(density) { 24.dp.toPx() } }

    LaunchedEffect(state.error) {
        state.error?.let { snackbar.showSnackbar(it); viewModel.limpiarError() }
    }

    Scaffold(
        containerColor = FondoClaro,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            Column(Modifier.background(FondoClaro).statusBarsPadding()) {
                TopAppBar(
                    title = { Text("Proveedores", fontWeight = FontWeight.Bold) },
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

                OutlinedTextField(
                    value = state.query,
                    onValueChange = viewModel::onQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    placeholder = { Text("Buscar por nombre o teléfono...", fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Buscar proveedor",
                            tint = AzulPrimario,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SuperficieClara,
                        unfocusedContainerColor = SuperficieClara,
                        focusedBorderColor = AzulPrimario,
                        unfocusedBorderColor = BordeClaro
                    )
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = viewModel::onNuevoProveedor,
                containerColor = AzulPrimario,
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Agregar nuevo proveedor", Modifier.size(30.dp))
            }
        }
    ) { padding ->
        Crossfade(
            targetState = when {
                state.cargando -> 0
                state.proveedores.isEmpty() -> 1
                else -> 2
            },
            animationSpec = tween(280, easing = FastOutSlowInEasing),
            label = "prov_state",
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) { est ->
            when (est) {
                0 -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator(color = AzulPrimario)
                }
                1 -> EstadoVacioProveedores()
                else -> {
                    if (state.proveedoresFiltrados.isEmpty()) {
                        Box(Modifier.fillMaxSize(), Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(56.dp),
                                    tint = SobreVariante.copy(alpha = 0.3f)
                                )
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    "Sin resultados",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = SobreFondoClaro
                                )
                                Text(
                                    "No se encontraron proveedores para \"${state.query}\"",
                                    color = SobreVariante,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            itemsIndexed(
                                state.proveedoresFiltrados,
                                key = { _, p -> p.id }
                            ) { index, prov ->
                                ProveedorCard(
                                    prov = prov,
                                    index = index,
                                    staggerOffsetPx = staggerOffsetPx,
                                    onEditar = { viewModel.onEditarProveedor(prov) },
                                    onEliminar = { aEliminar = prov },
                                    onWhatsApp = {
                                        val telefono = prov.telefono
                                            .filter { it.isDigit() }
                                            .let { if (it.startsWith("51")) it else "51$it" }
                                        val url = "https://wa.me/$telefono"
                                        context.startActivity(
                                            Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                        )
                                    }
                                )
                            }
                            item { Spacer(Modifier.height(80.dp)) }
                        }
                    }
                }
            }
        }
    }

    if (state.mostrarFormulario) {
        ModalBottomSheet(
            onDismissRequest = viewModel::onCerrarFormulario,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
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
            text = {
                Text("¿Deseas quitar a \"${prov.nombre}\"? Esto no eliminará sus productos, pero quedarán sin proveedor asignado.")
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.eliminar(prov); aEliminar = null },
                    colors = ButtonDefaults.buttonColors(containerColor = RojoStock),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { aEliminar = null }) {
                    Text("Cancelar", color = SobreFondoClaro)
                }
            }
        )
    }
}

@Composable
private fun ProveedorCard(
    prov: Proveedor,
    index: Int,
    staggerOffsetPx: Float,
    onEditar: () -> Unit,
    onEliminar: () -> Unit,
    onWhatsApp: () -> Unit
) {
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
        onClick = onEditar,
        interactionSource = interactionSource,
        color = SuperficieClara,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, BordeClaro.copy(alpha = 0.4f)),
        modifier = Modifier.graphicsLayer {
            alpha = staggerProgress
            translationY = (1f - staggerProgress) * staggerOffsetPx
            scaleX = pressScale
            scaleY = pressScale
        }
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = AzulPrimario,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        prov.nombre.take(1).uppercase(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = SobreAzul
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    prov.nombre,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = SobreFondoClaro,
                    maxLines = 1
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Phone,
                        contentDescription = "Teléfono",
                        Modifier.size(14.dp),
                        tint = SobreVariante
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        prov.telefono,
                        style = MaterialTheme.typography.bodySmall,
                        color = SobreVariante
                    )
                }
                if (!prov.notas.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        prov.notas!!,
                        style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                        color = SobreVariante.copy(alpha = 0.7f),
                        maxLines = 2
                    )
                }
            }

            IconButton(
                onClick = onWhatsApp,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF25D366).copy(alpha = 0.15f))
            ) {
                Icon(
                    Icons.Filled.Message,
                    contentDescription = "Contactar a ${prov.nombre} por WhatsApp",
                    tint = Color(0xFF25D366),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(8.dp))

            IconButton(
                onClick = onEliminar,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(RojoFondo)
            ) {
                Icon(
                    Icons.Filled.DeleteOutline,
                    contentDescription = "Eliminar proveedor ${prov.nombre}",
                    tint = RojoStock,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun FormProveedorLimpio(
    inicial: Proveedor?,
    guardando: Boolean,
    onGuardar: (String, String, String?) -> Unit
) {
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
            onClick = { onGuardar(nombre, telefono, if (notas.isBlank()) null else notas) },
            enabled = !guardando && nombre.isNotBlank() && telefono.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AzulPrimario)
        ) {
            if (guardando) CircularProgressIndicator(Modifier.size(24.dp), color = Color.White)
            else Text("CONFIRMAR DATOS", fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
        }
    }
}

@Composable
private fun EstadoVacioProveedores() {
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
                Icon(
                    Icons.Outlined.Groups,
                    contentDescription = null,
                    Modifier.size(56.dp),
                    tint = AzulPrimario.copy(alpha = 0.3f)
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        Text(
            "Sin contactos",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 22.sp,
            color = SobreFondoClaro
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Empieza registrando a tus proveedores para vincularlos con tus productos.",
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            color = SobreVariante,
            lineHeight = 20.sp
        )
    }
}