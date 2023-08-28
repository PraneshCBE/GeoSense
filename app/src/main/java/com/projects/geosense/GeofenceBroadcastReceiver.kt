package com.projects.geosense

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (GeofencingEvent.fromIntent(intent)?.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            // Entered geofence area
            Toast.makeText(context, "Entered geofence area", Toast.LENGTH_SHORT).show()
        } else if (GeofencingEvent.fromIntent(intent)?.geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            // Exited geofence area
            Toast.makeText(context, "Exited geofence area", Toast.LENGTH_SHORT).show()
        } else if (GeofencingEvent.fromIntent(intent)?.geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {
            // Dwell inside geofence area
            Toast.makeText(context, "Dwelling inside geofence area", Toast.LENGTH_SHORT).show()
        }
    }
}