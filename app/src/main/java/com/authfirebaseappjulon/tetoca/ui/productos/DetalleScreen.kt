package com.authfirebaseappjulon.tetoca.ui.productos

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun DetalleScreen(
    productoId: Long,
    onEditar: (productoId: Long) -> Unit,
    onEliminado: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("DetalleScreen (placeholder) - productoId=$productoId")
    }
}