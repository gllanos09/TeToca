package com.tetocaApp.tetoca.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.tetocaApp.tetoca.MainActivity
import com.tetocaApp.tetoca.work.StockBajoWorker

/**
 * Servicio que recibe mensajes FCM (Firebase Cloud Messaging).
 *
 * Dos casos de uso:
 * 1. onNewToken → cada vez que Firebase asigna un nuevo token al dispositivo,
 *    lo guardamos en Firestore para que en el futuro un servidor pueda
 *    enviar notificaciones push personalizadas a este usuario.
 *
 * 2. onMessageReceived → cuando llega un mensaje desde Firebase Console
 *    (o desde un servidor externo), lo convertimos en una notificación
 *    visible aunque la app esté cerrada.
 */
class TeTocaMessagingService : FirebaseMessagingService() {

    /**
     * Se llama cuando FCM asigna un nuevo token al dispositivo.
     * Guardamos el token en Firestore bajo el UID del usuario autenticado,
     * para que un servidor externo pueda enviarle notificaciones dirigidas.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("usuarios")
            .document(uid)
            .update("fcmToken", token)
            .addOnFailureListener {
                // Si falla (ej. primera vez que el usuario no existe aún),
                // el token se guardará en el próximo login.
            }
    }

    /**
     * Se llama cuando llega un mensaje FCM con la app en primer o segundo plano.
     * Extrae el título y cuerpo del mensaje y muestra una notificación local.
     *
     * Nota: si la app está completamente cerrada (killed), Android muestra
     * la notificación automáticamente sin pasar por aquí — ese es el poder
     * de FCM push frente a notificaciones locales.
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        // El mensaje puede tener notificación (título/cuerpo) o datos (key-value)
        val titulo = message.notification?.title
            ?: message.data["titulo"]
            ?: "TeToca"

        val cuerpo = message.notification?.body
            ?: message.data["cuerpo"]
            ?: "Tienes productos que necesitan atención."

        mostrarNotificacion(titulo, cuerpo)
    }

    private fun mostrarNotificacion(titulo: String, cuerpo: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificacion = NotificationCompat.Builder(this, StockBajoWorker.CANAL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(titulo)
            .setContentText(cuerpo)
            .setStyle(NotificationCompat.BigTextStyle().bigText(cuerpo))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(2001, notificacion)
    }
}