package com.example.madhusiri.utils

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat

object LocalNotificationHelper {

    fun showSprayAlert(context: Context, farmerName: String, pesticide: String) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, "spray_alerts")
            .setContentTitle("⚠️ Spray Alert Nearby!")
            .setContentText("$farmerName is spraying $pesticide. Close your hives!")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("$farmerName is spraying $pesticide nearby. Close your hives for at least 4 hours!"))
            .build()
        nm.notify(System.currentTimeMillis().toInt(), notification)
    }

    fun showAckAlert(context: Context, count: Int) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, "ack_alerts")
            .setContentTitle("✅ Beekeeper Responded!")
            .setContentText("$count beekeeper(s) have closed their hives.")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        nm.notify(System.currentTimeMillis().toInt(), notification)
    }
}