package com.example.service

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class GeofenceService(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val _geofenceEvents = MutableStateFlow<GeofenceEvent?>(null)
    val geofenceEvents: StateFlow<GeofenceEvent?> = _geofenceEvents

    data class GeofenceEvent(
        val merchantId: String,
        val type: String, // ENTER, EXIT, DWELL
        val latitude: Double,
        val longitude: Double
    )

    fun registerGeofence(
        merchantId: String,
        latitude: Double,
        longitude: Double,
        radius: Float = 200f
    ) {
        val geofence = Geofence.Builder()
            .setRequestId(merchantId)
            .setCircularRegion(latitude, longitude, radius)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(
                Geofence.GEOFENCE_TRANSITION_ENTER or
                Geofence.GEOFENCE_TRANSITION_DWELL
            )
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            LocationServices.getGeofencingClient(context)
                .addGeofences(geofencingRequest, geofencePendingIntent)
        }
    }

    private val geofencePendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }
}
