package com.authfirebaseappjulon.tetoca.navigation

sealed class Rutas(val ruta: String) {

    data object ListaProductos : Rutas("lista_productos")

    data object Detalle : Rutas("detalle/{productoId}") {
        fun crearRuta(productoId: Long) = "detalle/$productoId"
    }

    data object Formulario : Rutas("formulario?productoId={productoId}") {
        // productoId == null  -> modo "crear producto nuevo"
        // productoId != null  -> modo "editar producto existente"
        fun crearRutaNuevo() = "formulario"
        fun crearRutaEditar(productoId: Long) = "formulario?productoId=$productoId"
    }

    data object Proveedores : Rutas("proveedores")
}