package com.projects.geosense

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class LoginActivity : AppCompatActivity() {

    // Define SharedPreferences
    private lateinit var sharedPreferences: SharedPreferences
    private val LOCATION_PERMISSION_REQUEST_CODE = 123
    private val REQUEST_CODE_NOTIFICATION_POLICY_ACCESS = 2
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val usernameEditText = findViewById<EditText>(R.id.username_edittext)
        val radiusEditText = findViewById<EditText>(R.id.password_edittext)
        val loginButton = findViewById<Button>(R.id.loginButton)

        val fineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (fineLocationPermission == PackageManager.PERMISSION_GRANTED && coarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
            Log.d("enableUserLocation","Permission Granted")
        } else {
            // Request the permission
            Log.d("enableUserLocation","Requesting Permission")
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            !isNotificationPolicyAccessGranted()) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            startActivityForResult(intent, REQUEST_CODE_NOTIFICATION_POLICY_ACCESS)
        }
        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        loginButton.setOnClickListener {
                val username = usernameEditText.text.toString()

                val radius = radiusEditText.text.toString()
            saveLoginInfo(username,radius)
                val intent = Intent(this, MapsActivity::class.java)
                startActivity(intent)
                finish()
        }
    }
    private fun isNotificationPolicyAccessGranted(): Boolean {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return notificationManager.isNotificationPolicyAccessGranted
    }

    private fun saveLoginInfo(username: String, radius: String) {
        // Save the username and set isLoggedIn to true in SharedPreferences
        val editor = sharedPreferences.edit()
        editor.putString("radius", radius)
        editor.putString("username", username)
        editor.putBoolean("isLoggedIn", true)
        editor.apply()
    }
}