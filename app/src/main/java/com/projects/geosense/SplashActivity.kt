package com.projects.geosense

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.projects.geosense.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)

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
        }, 4000) // Display splash screen for 2 seconds
    }
}