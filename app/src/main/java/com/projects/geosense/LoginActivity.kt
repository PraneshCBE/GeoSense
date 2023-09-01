package com.projects.geosense

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class LoginActivity : AppCompatActivity() {

    // Define SharedPreferences
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val usernameEditText = findViewById<EditText>(R.id.username_edittext)
        val passwordEditText = findViewById<EditText>(R.id.password_edittext)
        val loginButton = findViewById<Button>(R.id.loginButton)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        loginButton.setOnClickListener {
                val username = usernameEditText.text.toString()
                saveLoginInfo(username)

                val intent = Intent(this, MapsActivity::class.java)
                startActivity(intent)
                finish()
        }
    }


    private fun saveLoginInfo(username: String) {
        // Save the username and set isLoggedIn to true in SharedPreferences
        val editor = sharedPreferences.edit()
        editor.putString("username", username)
        editor.putBoolean("isLoggedIn", true)
        editor.apply()
    }
}