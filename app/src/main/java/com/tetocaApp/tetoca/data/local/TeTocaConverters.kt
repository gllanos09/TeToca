package com.tetocaApp.tetoca.data.local

import androidx.room.TypeConverter

class TeTocaConverters {

    @TypeConverter
    fun fromRubro(rubro: Rubro): String = rubro.name

    @TypeConverter
    fun toRubro(value: String): Rubro = Rubro.valueOf(value)

    @TypeConverter
    fun fromModoReposicion(modo: ModoReposicion): String = modo.name

    @TypeConverter
    fun toModoReposicion(value: String): ModoReposicion = ModoReposicion.valueOf(value)
}