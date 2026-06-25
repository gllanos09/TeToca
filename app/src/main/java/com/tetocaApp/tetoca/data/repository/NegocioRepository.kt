package com.tetocaApp.tetoca.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.tetocaApp.tetoca.data.local.Negocio
import com.tetocaApp.tetoca.data.local.ModoReposicion
import com.tetocaApp.tetoca.data.local.NegocioDao
import com.tetocaApp.tetoca.data.local.Rubro
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class NegocioRepository(private val dao: NegocioDao) {

    private val db = FirebaseFirestore.getInstance()

    fun observar(uid: String): Flow<Negocio?> = dao.observarPorUid(uid)

    suspend fun obtener(uid: String): Negocio? = dao.obtenerPorUid(uid)

    /** Guarda la configuración del negocio en Room y la sincroniza con Firestore. */
    suspend fun guardar(negocio: Negocio) {
        dao.insertar(negocio)
        runCatching {
            db.collection("usuarios")
                .document(negocio.ownerUid)
                .collection("config")
                .document("negocio")
                .set(
                    mapOf(
                        "nombreNegocio"    to negocio.nombreNegocio,
                        "rubro"            to negocio.rubro.name,
                        "stockMinimoGlobal" to negocio.stockMinimoGlobal,
                        "modoReposicion"   to negocio.modoReposicion.name,
                        "colorPrimario"    to negocio.colorPrimario,
                        "ownerUid"         to negocio.ownerUid
                    )
                ).await()
        }
    }
}