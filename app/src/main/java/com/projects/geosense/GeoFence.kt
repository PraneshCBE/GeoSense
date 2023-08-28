package com.projects.geosense

import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.widget.Toast
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.maps.model.LatLng

class GeoFence(base: Context?) : ContextWrapper(base) {
    companion object {
        private val TAG = "GeoFence"
    }
    var pendingIntent: PendingIntent? = null

    public fun getGeofencingRequest(geoFence: Geofence): GeofencingRequest {
        return GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geoFence)
            .build()
    }

    fun getGeofence(ID: String, latLng: LatLng, radius: Double, transitionTypes: Int): Geofence {
        return Geofence.Builder()
            .setCircularRegion(latLng.latitude, latLng.longitude, radius.toFloat())
            .setRequestId(ID)
            .setTransitionTypes(transitionTypes)
            .setLoiteringDelay(5000)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .build()
    }


    public fun retPendingIntent(): PendingIntent? {
        if (pendingIntent != null) {
            return pendingIntent
        }

        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
        return pendingIntent

    }

    public fun getErrorString(e:java.lang.Exception):String{
        if (e is java.lang.SecurityException){
            return "Security Exception"
        }
        return e.localizedMessage
    }
}