package com.example.madhusiri

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class MadhuSiriApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(
                NotificationChannel("spray_alerts", "Spray Alerts",
                    NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Alerts when nearby farmers spray pesticides"
                    enableVibration(true)
                }
            )
        }
    }
}