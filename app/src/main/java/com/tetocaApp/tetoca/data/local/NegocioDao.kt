package com.tetocaApp.tetoca.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NegocioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(negocio: Negocio): Long

    @Update
    suspend fun actualizar(negocio: Negocio)

    @Query("SELECT * FROM negocio WHERE ownerUid = :uid LIMIT 1")
    fun observarPorUid(uid: String): Flow<Negocio?>

    @Query("SELECT * FROM negocio WHERE ownerUid = :uid LIMIT 1")
    suspend fun obtenerPorUid(uid: String): Negocio?
}