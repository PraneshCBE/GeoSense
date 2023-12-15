package com.projects.geosense

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.projects.geosense.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private var fcmToken: String=""
    private lateinit var token : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)
        getFCMToken()
        Handler().postDelayed({
            if (isLoggedIn) {
                // User is logged in, navigate to MapsActivity
                val intent = Intent(this, MapsActivity::class.java)
                startActivity(intent)
            } else {
                // User is not logged in, navigate to LoginActivity
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
            finish()
        }, 3000) // Display splash screen for 2 seconds
    }
    private fun getFCMToken() {

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM TOKEN", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            val token = (task.result).toString()
            Log.d("FCM Token", token)
            val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            sharedPreferences.edit().putString("fcmToken", token).apply()
            Log.d("FCM Token in sHARED", sharedPreferences.getString("fcmToken", "null").toString())
        })
    }
}