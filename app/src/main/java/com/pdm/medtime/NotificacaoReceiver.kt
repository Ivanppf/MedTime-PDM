package com.pdm.medtime

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class NotificacaoReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        notificar(context, intent)
    }

    fun notificar(context: Context, intent: Intent) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "canal_padrao"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                channelId,
                "Canal de Notificações",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(canal)
        }
        val notificacao = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Remédio")
            .setContentText("Está na hora de tomar: ${intent.getStringExtra("nomeMedicamento")}")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notificacao)
    }
}