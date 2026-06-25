package com.tetocaApp.tetoca.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tetocaApp.tetoca.ui.auth.LoginScreen
import com.tetocaApp.tetoca.ui.auth.RegisterScreen
import com.tetocaApp.tetoca.ui.estadisticas.EstadisticasScreen
import com.tetocaApp.tetoca.ui.productos.DetalleScreen
import com.tetocaApp.tetoca.ui.productos.FormularioScreen
import com.tetocaApp.tetoca.ui.productos.ListaProductosScreen
import com.tetocaApp.tetoca.ui.proveedores.ProveedoresScreen
import com.tetocaApp.tetoca.ui.reposicion.ReposicionScreen
import com.tetocaApp.tetoca.viewmodel.AuthViewModel

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()

    val inicio = if (authViewModel.haySession) Rutas.ListaProductos.ruta
    else Rutas.Login.ruta

    NavHost(navController = navController, startDestination = inicio) {

        // ── Auth ────────────────────────────────────────────────────────
        composable(Rutas.Login.ruta) {
            LoginScreen(
                onLoginExitoso = {
                    navController.navigate(Rutas.ListaProductos.ruta) {
                        popUpTo(Rutas.Login.ruta) { inclusive = true }
                    }
                },
                onIrARegistro = { navController.navigate(Rutas.Registro.ruta) }
            )
        }

        composable(Rutas.Registro.ruta) {
            RegisterScreen(
                onRegistroExitoso = {
                    navController.navigate(Rutas.ListaProductos.ruta) {
                        popUpTo(Rutas.Login.ruta) { inclusive = true }
                    }
                },
                onIrALogin = { navController.popBackStack() }
            )
        }

        // ── App principal ───────────────────────────────────────────────
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
                },
                onReposicionClick = {
                    navController.navigate(Rutas.Reposicion.ruta)
                },
                onEstadisticasClick = {
                    navController.navigate(Rutas.Estadisticas.ruta)
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
                onEliminado = { navController.popBackStack() }
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
                onGuardado = { navController.popBackStack() }
            )
        }

        composable(Rutas.Proveedores.ruta) {
            ProveedoresScreen(onVolver = { navController.popBackStack() })
        }

        composable(Rutas.Reposicion.ruta) {
            ReposicionScreen(onVolver = { navController.popBackStack() })
        }

        composable(Rutas.Estadisticas.ruta) {
            EstadisticasScreen(onVolver = { navController.popBackStack() })
        }
    }
}