package com.projects.geosense

import com.google.firebase.messaging.FirebaseMessagingService

class MyFirebaseInstaceIdService : FirebaseMessagingService(){
    override fun onNewToken(token: String) {
        super.onNewToken(token)

    }

}