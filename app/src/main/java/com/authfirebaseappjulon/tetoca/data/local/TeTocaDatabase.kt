package com.authfirebaseappjulon.tetoca.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Producto::class, Proveedor::class],
    version = 1,
    exportSchema = false
)
abstract class TeTocaDatabase : RoomDatabase() {

    abstract fun productoDao(): ProductoDao
    abstract fun proveedorDao(): ProveedorDao

    companion object {
        @Volatile
        private var INSTANCE: TeTocaDatabase? = null

        fun getInstance(context: Context): TeTocaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instancia = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    TeTocaDatabase::class.java,
                    "tetoca_database"
                ).build()
                INSTANCE = instancia
                instancia
            }
        }
    }
}