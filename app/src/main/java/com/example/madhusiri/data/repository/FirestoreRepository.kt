package com.example.madhusiri.data.repository

import com.example.madhusiri.data.models.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreRepository {
    private val db = Firebase.firestore
    val auth = Firebase.auth
    val currentUserId get() = auth.currentUser?.uid ?: ""

    // ─── Users ────────────────────────────────────────────────────────────────

    fun getUserFlow(uid: String): Flow<User?> = callbackFlow {
        val l = db.collection("users").document(uid)
            .addSnapshotListener { snap, _ ->
                trySend(snap?.toObject(User::class.java)?.copy(uid = snap.id))
            }
        awaitClose { l.remove() }
    }

    suspend fun saveUser(user: User) {
        db.collection("users").document(user.uid).set(user).await()
    }

    suspend fun updateUserLocation(uid: String, lat: Double, lng: Double) {
        db.collection("users").document(uid)
            .update(mapOf("latitude" to lat, "longitude" to lng)).await()
    }

    // ─── Hives ────────────────────────────────────────────────────────────────

    fun getHivesFlow(): Flow<List<Hive>> = callbackFlow {
        val l = db.collection("hives").addSnapshotListener { snap, _ ->
            trySend(snap?.documents?.mapNotNull {
                it.toObject(Hive::class.java)?.copy(id = it.id)
            } ?: emptyList())
        }
        awaitClose { l.remove() }
    }

    suspend fun addHive(hive: Hive): String =
        db.collection("hives").add(hive).await().id

    suspend fun updateHive(hive: Hive) {
        db.collection("hives").document(hive.id).set(hive).await()
    }

    suspend fun deleteHive(hiveId: String) {
        db.collection("hives").document(hiveId).delete().await()
    }

    // ─── Spray Alerts ─────────────────────────────────────────────────────────

    fun getActiveSprayAlertsFlow(): Flow<List<SprayAlert>> = callbackFlow {
        val l = db.collection("sprayAlerts")
            .whereEqualTo("active", true)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                trySend(snap?.documents?.mapNotNull {
                    it.toObject(SprayAlert::class.java)?.copy(id = it.id)
                } ?: emptyList())
            }
        awaitClose { l.remove() }
    }

    suspend fun postSprayAlert(alert: SprayAlert): String =
        db.collection("sprayAlerts").add(alert).await().id

    suspend fun deactivateAlert(alertId: String) {
        db.collection("sprayAlerts").document(alertId)
            .update("active", false).await()
    }

    // ← NEW: Beekeeper acknowledges by adding their UID to the array
    suspend fun acknowledgeAlert(alertId: String, beekeeperId: String) {
        db.collection("sprayAlerts").document(alertId)
            .update("acknowledgedBy", FieldValue.arrayUnion(beekeeperId)).await()
    }

    // ─── Health Logs ──────────────────────────────────────────────────────────

    fun getHealthLogsForHive(hiveId: String): Flow<List<HiveHealthLog>> = callbackFlow {
        val l = db.collection("hiveHealthLogs")
            .whereEqualTo("hiveId", hiveId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                trySend(snap?.documents?.mapNotNull {
                    it.toObject(HiveHealthLog::class.java)?.copy(id = it.id)
                } ?: emptyList())
            }
        awaitClose { l.remove() }
    }

    suspend fun addHealthLog(log: HiveHealthLog) {
        db.collection("hiveHealthLogs").add(log).await()
    }
}