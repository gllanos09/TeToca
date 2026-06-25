package com.tetocaApp.tetoca.work

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tetocaApp.tetoca.MainActivity
import com.tetocaApp.tetoca.R
import com.tetocaApp.tetoca.data.local.TeTocaDatabase

/**
 * Tarea periódica de WorkManager que:
 * 1. Consulta Room buscando productos con stockActual <= stockMinimo.
 * 2. Si encuentra alguno, lanza una notificación local resumida.
 *
 * Se ejecuta cada 15 minutos (el mínimo que permite WorkManager).
 * No necesita internet — solo accede a la base de datos local.
 */
class StockBajoWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val db = TeTocaDatabase.getInstance(context)
            val productosBajos = db.productoDao().obtenerConStockBajoUnaVez()

            if (productosBajos.isNotEmpty()) {
                lanzarNotificacion(productosBajos.size, productosBajos.map { it.nombre })
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun lanzarNotificacion(cantidad: Int, nombres: List<String>) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val titulo = if (cantidad == 1) "⚠️ 1 producto con stock bajo"
        else "⚠️ $cantidad productos con stock bajo"

        val cuerpo = nombres.take(3).joinToString(", ") +
                if (nombres.size > 3) " y ${nombres.size - 3} más…" else ""

        val notificacion = NotificationCompat.Builder(context, CANAL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(titulo)
            .setContentText(cuerpo)
            .setStyle(NotificationCompat.BigTextStyle().bigText(cuerpo))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICACION_ID, notificacion)
    }

    companion object {
        const val CANAL_ID = "tetoca_stock_bajo"
        const val NOTIFICACION_ID = 1001
        const val WORK_NAME = "tetoca_stock_bajo_worker"
    }
}