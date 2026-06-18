package com.tetocaApp.tetoca.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tetocaApp.tetoca.ui.productos.DetalleScreen
import com.tetocaApp.tetoca.ui.productos.FormularioScreen
import com.tetocaApp.tetoca.ui.productos.ListaProductosScreen
import com.tetocaApp.tetoca.ui.proveedores.ProveedoresScreen

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Rutas.ListaProductos.ruta
    ) {
        composable(Rutas.ListaProductos.ruta) {
            ListaProductosScreen(
                onProductoClick = { productoId ->
                    navController.navigate(Rutas.Detalle.crearRuta(productoId))
                },
                onNuevoProducto = {
                    navController.navigate(Rutas.Formulario.crearRutaNuevo())
                },
                onProveedoresClick = {
                    navController.navigate(Rutas.Proveedores.ruta)
                }
            )
        }

        composable(
            route = Rutas.Detalle.ruta,
            arguments = listOf(navArgument("productoId") { type = NavType.LongType })
        ) { backStackEntry ->
            val productoId = backStackEntry.arguments?.getLong("productoId") ?: 0L
            DetalleScreen(
                productoId = productoId,
                onVolver = { navController.popBackStack() },
                onEditar = { id ->
                    navController.navigate(Rutas.Formulario.crearRutaEditar(id))
                },
                onEliminado = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Rutas.Formulario.ruta,
            arguments = listOf(
                navArgument("productoId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val idRecibido = backStackEntry.arguments?.getLong("productoId") ?: -1L
            val productoId = if (idRecibido == -1L) null else idRecibido
            FormularioScreen(
                productoId = productoId,
                onCancelar = { navController.popBackStack() },
                onGuardado = {
                    navController.popBackStack()
                }
            )
        }

        composable(Rutas.Proveedores.ruta) {
            ProveedoresScreen(onVolver = { navController.popBackStack() })
        }
    }
}