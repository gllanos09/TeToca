package com.tetocaApp.tetoca

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.tetocaApp.tetoca.navigation.NavGraph
import com.tetocaApp.tetoca.ui.theme.TetocaTheme
import com.tetocaApp.tetoca.work.StockBajoWorker
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    // Lanzador para pedir permiso de notificaciones en Android 13+
    private val permisosLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* No hacemos nada si rechaza — la app funciona igual */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        crearCanalNotificaciones()
        pedirPermisoNotificaciones()
        programarWorkerStockBajo()

        setContent {
            TetocaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavGraph()
                }
            }
        }
    }

    /** Crea el canal de notificaciones (obligatorio en Android 8+). */
    private fun crearCanalNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                StockBajoWorker.CANAL_ID,
                "Stock bajo",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Avisos cuando uno o más productos caen por debajo del stock mínimo"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(canal)
        }
    }

    /** Pide permiso POST_NOTIFICATIONS en Android 13+ (API 33). */
    private fun pedirPermisoNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permisosLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    /**
     * Programa la tarea periódica de detección de stock bajo.
     * KEEP_EXISTING: si ya estaba programada, no la reprograma
     * (evita duplicados al girar la pantalla o recrear la Activity).
     */
    private fun programarWorkerStockBajo() {
        val request = PeriodicWorkRequestBuilder<StockBajoWorker>(
            15, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            StockBajoWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}