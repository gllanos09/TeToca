package com.tetocaApp.tetoca.navigation

sealed class Rutas(val ruta: String) {

    data object Login : Rutas("login")
    data object Registro : Rutas("registro")

    data object ListaProductos : Rutas("lista_productos")

    data object Detalle : Rutas("detalle/{productoId}") {
        fun crearRuta(productoId: Long) = "detalle/$productoId"
    }

    data object Formulario : Rutas("formulario?productoId={productoId}") {
        fun crearRutaNuevo() = "formulario"
        fun crearRutaEditar(productoId: Long) = "formulario?productoId=$productoId"
    }

    data object Proveedores : Rutas("proveedores")
    data object Reposicion : Rutas("reposicion")
    data object Estadisticas : Rutas("estadisticas")
    data object Configuracion : Rutas("configuracion")
}