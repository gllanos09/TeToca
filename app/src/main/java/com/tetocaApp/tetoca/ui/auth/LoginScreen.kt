package com.tetocaApp.tetoca.ui.auth

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tetocaApp.tetoca.ui.theme.*
import com.tetocaApp.tetoca.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    onLoginExitoso: () -> Unit,
    onIrARegistro: () -> Unit
) {
    val viewModel: AuthViewModel = viewModel()
    val state by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var verPassword by remember { mutableStateOf(false) }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val enterProgress by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "enter"
    )

    LaunchedEffect(state.exitoso) {
        if (state.exitoso) {
            viewModel.limpiarExitoso()
            onLoginExitoso()
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbar.showSnackbar(it)
            viewModel.limpiarError()
        }
    }

    Scaffold(
        containerColor = FondoClaro,
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.graphicsLayer {
                    alpha = enterProgress
                    translationY = (1f - enterProgress) * 60f
                },
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "TeToca",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Black
                    ),
                    color = AzulPrimario
                )
                Text(
                    text = "Inventario inteligente",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SobreVariante
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo electrónico") },
                    leadingIcon = { Icon(Icons.Filled.Email, contentDescription = "Correo electrónico") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AzulPrimario,
                        unfocusedBorderColor = BordeClaro,
                        focusedContainerColor = SuperficieClara,
                        unfocusedContainerColor = SuperficieClara,
                        cursorColor = AzulPrimario,
                        focusedLabelColor = AzulPrimario,
                        focusedLeadingIconColor = AzulPrimario
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = "Contraseña") },
                    trailingIcon = {
                        IconButton(onClick = { verPassword = !verPassword }) {
                            Icon(
                                if (verPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (verPassword) "Ocultar contraseña" else "Mostrar contraseña"
                            )
                        }
                    },
                    visualTransformation = if (verPassword) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AzulPrimario,
                        unfocusedBorderColor = BordeClaro,
                        focusedContainerColor = SuperficieClara,
                        unfocusedContainerColor = SuperficieClara,
                        cursorColor = AzulPrimario,
                        focusedLabelColor = AzulPrimario,
                        focusedLeadingIconColor = AzulPrimario
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { viewModel.login(email, password) },
                    enabled = !state.cargando,
                    colors = ButtonDefaults.buttonColors(containerColor = AzulPrimario),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    if (state.cargando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = SobreAzul,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            "Iniciar sesión",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    }
                }

                TextButton(onClick = onIrARegistro) {
                    Text(
                        "¿No tienes cuenta? Regístrate",
                        color = AzulPrimario,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
