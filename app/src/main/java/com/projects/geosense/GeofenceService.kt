package com.projects.geosense


import android.app.Service
import android.content.Intent

import android.os.IBinder


class GeofenceService : Service() {

    companion object {
        const val NOTIFICATION_ID = 123
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        val notification = createNotification()
//        startForeground(NOTIFICATION_ID, notification)
//        // Your geofence monitoring logic goes here
        return START_STICKY
    }

//    private fun createNotification(): Notification {
//        val notificationHelper = NotificationHelper(this)
//          }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
