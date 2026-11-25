package com.willard.avisoscampus.notificacion

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.willard.avisoscampus.MainActivity
import com.willard.avisoscampus.R
import com.willard.avisoscampus.model.Aviso

object Notificador {
    const val CANAL = "avisos"
    const val CANAL_URGENTE = "urgentes"

    fun crearCanal(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CANAL,
                "Avisos",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones de la app"
            }
            val canalUrgente = NotificationChannel(
                CANAL_URGENTE,
                "Urgentes",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Avisos importantes que aparecen en pantalla"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(canal)
            manager.createNotificationChannel(canalUrgente)
        }
    }

    fun tienePermiso(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    fun mostrar(context: Context, aviso: Aviso, urgente: Boolean = false): Boolean {
        if (!tienePermiso(context)) return false

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendiente = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val canalId = if (urgente) CANAL_URGENTE else CANAL
        val prioridad = if (urgente) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT

        val textoLargo = "${aviso.texto}\n\nOrigen: ${aviso.origen.texto} · Hora: ${aviso.hora}\nToca para abrir AvisosCampus y revisar el historial."

        val notificacion = NotificationCompat.Builder(context, canalId)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setContentTitle(aviso.titulo)
            .setContentText(aviso.texto)
            .setStyle(NotificationCompat.BigTextStyle().bigText(textoLargo))
            .setPriority(prioridad)
            .setContentIntent(pendiente)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(aviso.id, notificacion)
        return true
    }
}