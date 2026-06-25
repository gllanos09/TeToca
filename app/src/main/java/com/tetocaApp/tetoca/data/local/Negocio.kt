package com.tetocaApp.tetoca.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Rubros de negocio soportados por TeToca. */
enum class Rubro(val etiqueta: String, val icono: String) {
    BODEGA("Bodega / Minimarket", "🛒"),
    FERRETERIA("Ferretería", "🔧"),
    TALLER("Taller / Servicio técnico", "⚙️"),
    BELLEZA("Salón de belleza / Spa", "💄"),
    ABARROTES("Abarrotes / Mayorista", "📦"),
    OTRO("Otro tipo de negocio", "🏪")
}

/** Modo de reposición: asistido por deep link o automatizado vía Cloud API. */
enum class ModoReposicion {
    ESTANDAR,     // deep link a WhatsApp — intervención humana
    AUTOMATIZADO  // Cloud API — sin intervención (requiere cuenta verificada Meta)
}

/**
 * Configuración del negocio del usuario.
 * Hay exactamente un registro por usuario (ownerUid único).
 * Se sincroniza en Firestore bajo usuarios/{uid}/config/negocio.
 */
@Entity(tableName = "negocio")
data class Negocio(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val nombreNegocio: String,
    val rubro: Rubro = Rubro.OTRO,
    val stockMinimoGlobal: Int = 5,
    val modoReposicion: ModoReposicion = ModoReposicion.ESTANDAR,
    val colorPrimario: String = "#059669",
    val ownerUid: String
)