package com.tetocaApp.tetoca.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Producto::class, Proveedor::class, Negocio::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(TeTocaConverters::class)
abstract class TeTocaDatabase : RoomDatabase() {

    abstract fun productoDao(): ProductoDao
    abstract fun proveedorDao(): ProveedorDao
    abstract fun negocioDao(): NegocioDao

    companion object {
        @Volatile
        private var INSTANCE: TeTocaDatabase? = null

        fun getInstance(context: Context): TeTocaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instancia = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    TeTocaDatabase::class.java,
                    "tetoca_database"
                )
                    .fallbackToDestructiveMigration() // borra y recrea si hay cambio de versión
                    .build()
                INSTANCE = instancia
                instancia
            }
        }
    }
}