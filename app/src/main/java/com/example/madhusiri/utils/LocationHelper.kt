package com.example.madhusiri.utils

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.*

class LocationHelper(private val context: Context) {
    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(onResult: (lat: Double, lng: Double) -> Unit, onError: () -> Unit) {
        fusedClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) onResult(location.latitude, location.longitude)
                else requestFreshLocation(onResult, onError)
            }
            .addOnFailureListener { onError() }
    }

    @SuppressLint("MissingPermission")
    private fun requestFreshLocation(onResult: (Double, Double) -> Unit, onError: () -> Unit) {
        val request = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMaxUpdateAgeMillis(0)
            .setDurationMillis(5000)
            .build()
        fusedClient.getCurrentLocation(request, null)
            .addOnSuccessListener { loc ->
                if (loc != null) onResult(loc.latitude, loc.longitude)
                else onError()
            }
            .addOnFailureListener { onError() }
    }
}