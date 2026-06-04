package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        
        if (geofencingEvent?.hasError() == false) {
            val transition = geofencingEvent.geofenceTransition
            
            if (transition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                transition == Geofence.GEOFENCE_TRANSITION_DWELL
            ) {
                val merchantId = geofencingEvent.triggeringGeofences?.firstOrNull()?.requestId
                merchantId?.let {
                    NotificationService.showProximityNotification(context, it)
                }
            }
        }
    }
}
