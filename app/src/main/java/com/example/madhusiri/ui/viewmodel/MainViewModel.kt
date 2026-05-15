package com.example.madhusiri.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.madhusiri.data.models.*
import com.example.madhusiri.data.repository.FirestoreRepository
import com.example.madhusiri.utils.LocalNotificationHelper
import com.example.madhusiri.utils.LocationHelper
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainViewModel(private val appContext: Context) : ViewModel() {

    private val repo = FirestoreRepository()
    val isLoggedIn get() = repo.auth.currentUser != null

    private val _userProfile = MutableStateFlow<User?>(null)
    val userProfile: StateFlow<User?> = _userProfile

    val hives: StateFlow<List<Hive>> = repo.getHivesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sprayAlerts: StateFlow<List<SprayAlert>> = repo.getActiveSprayAlertsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Tracks already-seen alert IDs so we don't re-notify on app start
    private val knownAlertIds = mutableSetOf<String>()
    private var alertsInitialized = false

    init {
        repo.auth.currentUser?.uid?.let { uid ->
            viewModelScope.launch {
                repo.getUserFlow(uid).collect { _userProfile.value = it }
            }
        }
        watchAlertsForLocalNotifications()
    }

    // ─── Auth ─────────────────────────────────────────────────────────────────

    fun signInWithPhone(phone: String, password: String, onResult: (Boolean, String?) -> Unit) {
        val email = phoneToEmail(phone)
        viewModelScope.launch {
            try {
                repo.auth.signInWithEmailAndPassword(email, password).await()
                updateFcmToken()
                repo.auth.currentUser?.uid?.let { uid ->
                    launch { repo.getUserFlow(uid).collect { _userProfile.value = it } }
                }
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, "Invalid phone number or password")
            }
        }
    }

    fun registerWithPhone(
        phone: String, password: String,
        name: String, role: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val email = phoneToEmail(phone)
        viewModelScope.launch {
            try {
                val result = repo.auth.createUserWithEmailAndPassword(email, password).await()
                val uid = result.user!!.uid
                val token = try { Firebase.messaging.token.await() } catch (e: Exception) { "" }
                repo.saveUser(User(
                    uid = uid, name = name, phone = phone,
                    role = role, fcmToken = token
                ))
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.message ?: "Registration failed")
            }
        }
    }

    fun signOut() {
        repo.auth.signOut()
        _userProfile.value = null
        knownAlertIds.clear()
        alertsInitialized = false
    }

    // Converts phone number to a valid Firebase Auth email format
    private fun phoneToEmail(phone: String): String {
        val cleaned = phone.filter { it.isDigit() }
        return "user_$cleaned@madhusiri.app"
    }

    // ─── Local Notifications (replaces Cloud Functions) ───────────────────────

    private fun watchAlertsForLocalNotifications() {
        viewModelScope.launch {
            // Combine alerts + user profile so we know the role
            combine(sprayAlerts, userProfile) { alerts, user ->
                Pair(alerts, user)
            }.collect { (alerts, user) ->
                if (user == null) return@collect

                // On first load, populate knownAlertIds silently (no notification)
                if (!alertsInitialized) {
                    alerts.forEach { knownAlertIds.add(it.id) }
                    alertsInitialized = true
                    return@collect
                }

                alerts.forEach { alert ->
                    // New spray alert — notify beekeepers
                    if (alert.id !in knownAlertIds) {
                        knownAlertIds.add(alert.id)
                        if (user.role == "beekeeper") {
                            LocalNotificationHelper.showSprayAlert(
                                context    = appContext,
                                farmerName = alert.farmerName,
                                pesticide  = alert.pesticide
                            )
                        }
                    }

                    // Acknowledgement changed — notify farmer
                    if (user.role == "farmer" && alert.farmerId == user.uid) {
                        val ackKey = "${alert.id}_ack_${alert.acknowledgedBy.size}"
                        if (ackKey !in knownAlertIds && alert.acknowledgedBy.isNotEmpty()) {
                            knownAlertIds.add(ackKey)
                            LocalNotificationHelper.showAckAlert(
                                context = appContext,
                                count   = alert.acknowledgedBy.size
                            )
                        }
                    }
                }
            }
        }
    }

    // ─── Location ─────────────────────────────────────────────────────────────

    fun saveLocationToProfile(context: Context) {
        val uid = repo.currentUserId.takeIf { it.isNotEmpty() } ?: return
        LocationHelper(context).getCurrentLocation(
            onResult = { lat, lng ->
                viewModelScope.launch {
                    try { repo.updateUserLocation(uid, lat, lng) }
                    catch (e: Exception) { Log.w("MadhuSiri", "Location update failed: ${e.message}") }
                }
            },
            onError = { Log.w("MadhuSiri", "Could not get location") }
        )
    }

    fun updateProfileLocation(lat: Double, lng: Double) {
        val uid = repo.currentUserId.takeIf { it.isNotEmpty() } ?: return
        viewModelScope.launch {
            try { repo.updateUserLocation(uid, lat, lng) }
            catch (e: Exception) { Log.w("MadhuSiri", "Profile location update failed") }
        }
    }

    // ─── Hives ────────────────────────────────────────────────────────────────

    fun addHive(name: String, lat: Double, lng: Double) {
        viewModelScope.launch {
            val user = _userProfile.value ?: return@launch
            repo.addHive(Hive(
                ownerId   = user.uid,
                ownerName = user.name,
                name      = name,
                latitude  = lat,
                longitude = lng
            ))
        }
    }

    fun updateHive(hive: Hive) {
        viewModelScope.launch { repo.updateHive(hive) }
    }

    fun deleteHive(hiveId: String) {
        viewModelScope.launch { repo.deleteHive(hiveId) }
    }

    // ─── Spray Alerts ─────────────────────────────────────────────────────────

    fun postSprayAlert(pesticide: String, lat: Double, lng: Double) {
        viewModelScope.launch {
            val user = _userProfile.value ?: return@launch
            repo.postSprayAlert(SprayAlert(
                farmerId   = user.uid,
                farmerName = user.name,
                pesticide  = pesticide,
                latitude   = lat,
                longitude  = lng
            ))
        }
    }

    fun deactivateAlert(alertId: String) {
        viewModelScope.launch { repo.deactivateAlert(alertId) }
    }

    fun acknowledgeAlert(alertId: String) {
        val uid = repo.currentUserId.takeIf { it.isNotEmpty() } ?: return
        viewModelScope.launch { repo.acknowledgeAlert(alertId, uid) }
    }

    // ─── Health Logs ──────────────────────────────────────────────────────────

    fun addHealthLog(log: HiveHealthLog, updatedHive: Hive) {
        viewModelScope.launch {
            repo.addHealthLog(log)
            repo.updateHive(updatedHive)
        }
    }

    // ─── FCM Token ────────────────────────────────────────────────────────────

    private suspend fun updateFcmToken() {
        val uid = repo.auth.currentUser?.uid ?: return
        try {
            val token = Firebase.messaging.token.await()
            Firebase.firestore.collection("users").document(uid)
                .update("fcmToken", token).await()
        } catch (e: Exception) {
            Log.w("MadhuSiri", "FCM token update failed: ${e.message}")
        }
    }
}

// ─── Factory (required since ViewModel now takes Context) ─────────────────────
class MainViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return MainViewModel(context.applicationContext) as T
    }
}