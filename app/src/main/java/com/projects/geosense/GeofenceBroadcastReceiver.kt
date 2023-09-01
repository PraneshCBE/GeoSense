package com.projects.geosense

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.util.Log
import android.widget.Toast
import com.example.geofencing.NotificationHelper
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent


private const val TAG = "GeofenceBroadcastReceiver"

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    companion object
    {
        var triggerCount=0
    }

    override fun onReceive(context: Context, intent: Intent) {
        triggerCount++

        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        Log.d(TAG,"geofencingEvent $intent" )
        if (geofencingEvent != null) {
            if (geofencingEvent.hasError()) {
                val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
                Log.e(TAG, errorMessage)
                return
            }
        }

        val geofenceTransition = geofencingEvent?.geofenceTransition
        val notificationHelper = NotificationHelper(context)
        Toast.makeText(context, "Geofence Triggered", Toast.LENGTH_SHORT).show()
        if (triggerCount==1)
        {
            notificationHelper.sendHighPriorityNotification(
                "Geofence Triggered",
                "You have Entered the geofenced area.",
                MapsActivity::class.java)
        }
        else if (triggerCount==2)
        {
            notificationHelper.sendHighPriorityNotification(
                "Geofence Triggered",
                "You have put a tent in the geofenced area.",
                MapsActivity::class.java)
        }
        else if(triggerCount==3)
        {
            notificationHelper.sendHighPriorityNotification(
                "Geofence Triggered",
                "You have exited the geofenced area.",
                MapsActivity::class.java)
            triggerCount=0
        }

//        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
//            Log.d(TAG, "Geofence Entered")
//            Toast.makeText(context, "Geofence Entered", Toast.LENGTH_SHORT).show()
//            notificationHelper.sendHighPriorityNotification(
//                "Geofence Entered",
//                "You have entered a geofenced area.",
//                MapsActivity::class.java)
//
//        }
//        else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
//            Log.d(TAG, "Geofence Exited")
//            Toast.makeText(context, "Geofence Exited", Toast.LENGTH_SHORT).show()
//            notificationHelper.sendHighPriorityNotification(
//                "Geofence Exited",
//                "You have exited a geofenced area.",
//                MapsActivity::class.java)
//        }
//        else {
//            Log.e(TAG, "Invalid type transition $geofenceTransition")
//
//        }
    }



}