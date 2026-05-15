package com.example.madhusiri.data.models

data class User(
    val uid: String = "",
    val name: String = "",
    val phone: String = "",
    val role: String = "",
    val fcmToken: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

data class Hive(
    val id: String = "",
    val ownerId: String = "",
    val ownerName: String = "",
    val name: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val healthStatus: String = "good",
    val honeyProduction: Double = 0.0,
    val notes: String = "",
    val lastChecked: Long = System.currentTimeMillis()
)

data class SprayAlert(
    val id: String = "",
    val farmerId: String = "",
    val farmerName: String = "",
    val pesticide: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val radius: Double = 2000.0,
    val active: Boolean = true,
    val acknowledgedBy: List<String> = emptyList()   // ← THIS was missing
)

data class HiveHealthLog(
    val id: String = "",
    val hiveId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val healthStatus: String = "",
    val honeyProduction: Double = 0.0,
    val notes: String = ""
)