package com.projects.geosense

import android.Manifest
import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.projects.geosense.databinding.ActivityMapsBinding

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, OnMapLongClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var autocompleteFragement: AutocompleteSupportFragment
    private lateinit var floatingText: TextView

    private var GEOFENCE_RADIUS=10.00
    private val LOCATION_PERMISSION_REQUEST_CODE = 123
    private val GEOFENCE_ID="SPIDER_MAN"

    private var geoFence: GeoFence? = null
    private var geofenceLatLng: Map<LatLng, Double>? = null
    private var TAG= MapsActivity::class.java.simpleName

    private val handler = Handler()
    private val locationSenderRunnable = object : Runnable {
        override fun run() {
            // Get the user's current location from gps of the device
            val fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(this@MapsActivity)
            if (ActivityCompat.checkSelfPermission(
                    this@MapsActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

            ) {
                // Permission already granted, get the user's location
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        if (location != null) {
                            val latitude = location.latitude
                            val longitude = location.longitude

                            // Get username from shared preferences
                            val sharedPreferences =
                                getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                            val username = sharedPreferences.getString("username", null)
                            Log.d("username", username.toString())
                            Log.d("userLocation", "Lat: $latitude, Long: $longitude")
                            SaveUserData(username, LatLng(latitude, longitude))
                            enableUserLocation()
                            viewSharedPreferences()
                            // Schedule the next execution after 1 minute (60,000 milliseconds)
                            handler.postDelayed(this, 6000)
                        }
                    }
                    .addOnFailureListener { e ->
                        // Handle any errors that may occur while getting the location
                        e.printStackTrace()

                        // Schedule the next execution after 1 minute (60,000 milliseconds)
                        handler.postDelayed(this, 6000)
                    }
            }
            else
            {
                // Permission not granted, request the permission
                ActivityCompat.requestPermissions(
                    this@MapsActivity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )

            }
            if (!isGPSEnabled()) {
                Toast.makeText(this@MapsActivity, "Please enable GPS", Toast.LENGTH_SHORT).show()
                // GPS is not enabled, request the user to enable it
                requestGPSEnabled()
            }

        }

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        floatingText = findViewById(R.id.floatingText)
        Places.initialize(applicationContext, getString(R.string.google_api));
        autocompleteFragement= supportFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                as AutocompleteSupportFragment
        autocompleteFragement.setPlaceFields(listOf(Place.Field.ID,Place.Field.NAME,Place.Field.LAT_LNG))
        autocompleteFragement.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                Log.d("Place", "Place: ${place.name}, ${place.id}, ${place.latLng}")
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.latLng, 20f))

            }

            override fun onError(status: Status) {
                Log.d("Place", "An error occurred: $status")
            }
        })
        geofencingClient=LocationServices.getGeofencingClient(this)
        geoFence= GeoFence(this)
        geofenceLatLng = retrieveGeofenceLatLng()
        val sharedPreferences= getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        if(sharedPreferences.contains("radius"))
        {
            GEOFENCE_RADIUS=sharedPreferences.getString("radius",null)!!.toDouble()
        }
       handler.postDelayed(locationSenderRunnable, 6000)
//    val serviceIntent= Intent(this, LocationSender::class.java)
//    startService(serviceIntent)

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val userLocation=getUserLocation(this)
        if (userLocation != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 20f))
        }else{
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(37.422160, -122.084270), 16f))
        }
        enableUserLocation()
//        setMapStyle(mMap)
        mMap.setOnMyLocationButtonClickListener {
            val userLocation = mMap.cameraPosition.target
            saveUserLocation(this, userLocation)
            false // Return false to allow the default behavior (centering the camera)
        }
        mMap.setOnMapLongClickListener(this)
        if (geofenceLatLng != null) {
            val geofenceLocation = geofenceLatLng!!.keys.first()
            addMarker(geofenceLocation)
            addCircleInit(geofenceLocation, GEOFENCE_RADIUS)
            addGeofence(geofenceLocation, geofenceLatLng!!.values.first())
        }


    }


    private fun setMapStyle(map:GoogleMap)
    {
        try {
            val success=map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,R.raw.map_style))
            if(!success)
            {
                Log.e(TAG,"Style parsing failed")
            }
        }catch (e:Exception)
        {
            Log.e(TAG,"Can't find style. Error: ",e)
        }
    }
    private fun isGPSEnabled(): Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    // Function to prompt user to enable GPS
    private fun requestGPSEnabled() {
        val enableGPSIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivityForResult(enableGPSIntent, LOCATION_PERMISSION_REQUEST_CODE)
    }
    private fun enableUserLocation() {
        Log.d("enableUserLocation","enableUserLocation")
        val fineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        val backgroundLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        if (fineLocationPermission == PackageManager.PERMISSION_GRANTED && coarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
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
        if (backgroundLocationPermission != PackageManager.PERMISSION_GRANTED) {
            // Request the permission
            Log.d("enableUserLocation","Requesting Permission")
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onMapLongClick(p0: LatLng) {
        geofenceLatLng = retrieveGeofenceLatLng()
        if (geofenceLatLng != null) {
            //add a confirmation for removing the existing Geofence
            AlertDialog.Builder(this)
                .setTitle("Remove Existing Geofence")
                .setMessage("Are you sure you want to remove the existing geofence?")
                .setPositiveButton("Yes") { dialog, which ->
                    removeGeofence()
                    mMap.clear()
                    val dialogView = layoutInflater.inflate(R.layout.radius_input, null)
                    val editText = dialogView.findViewById<android.widget.EditText>(R.id.editTextRadius)
                    AlertDialog.Builder(this)
                        .setTitle("Enter Radius")
                        .setView(dialogView)
                        .setCancelable(false)
                        .setPositiveButton("OK"){dialog, which ->
                            GEOFENCE_RADIUS=editText.text.toString().toDouble()
                            val sharedPreferences= getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                            val editor = sharedPreferences.edit()
                            Log.d("GEOFENCE_RADIUS",GEOFENCE_RADIUS.toString())
                            editor.putString("radius", GEOFENCE_RADIUS.toString())
                            editor.apply()
                            addMarker(p0)
                            addCircle(p0)
                            GeofenceBroadcastReceiver.triggerCount=0
                            addGeofence(p0, GEOFENCE_RADIUS)
                            saveGeofenceLatLng(p0, GEOFENCE_RADIUS)
                        }
                        .setNegativeButton("Cancel",null)
                        .show()
                }
                .setNegativeButton("No", null)
                .show()
        }
        else {
            addMarker(p0)
            addCircle(p0)
            addGeofence(p0, GEOFENCE_RADIUS)
            saveGeofenceLatLng(p0, GEOFENCE_RADIUS)
            GeofenceBroadcastReceiver.triggerCount=0
        }
    }

    private fun SaveUserData(username : String?,location: LatLng)
    {
        Log.d("DB Store","Saving User Data in DB")
        val database = Firebase.database
        Log.d("Database", database.toString())
        val dBRef = database.getReference("Users").child(username!!)
        Log.d("DB ref", dBRef.toString())
        val fcmToken= getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).getString("fcmToken", null)
        val user = UsersModel(username, "${location.latitude},${location.longitude}", fcmToken)


        dBRef.setValue(user)
            .addOnCompleteListener { Log.d("DB Store","Successfully") }
            .addOnFailureListener{ Log.d("DB Store","Failed") }
    }

    private fun addGeofence(latLng: LatLng, radius: Double){

        val geofence=geoFence!!.getGeofence(GEOFENCE_ID,latLng,radius,Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_DWELL or Geofence.GEOFENCE_TRANSITION_EXIT)

        val geofencingRequest=geoFence!!.getGeofencingRequest(geofence)
        val pendingIntent=geoFence!!.retPendingIntent()
        floatingText.visibility = View.GONE
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions

        }
        if (pendingIntent != null) {
            geofencingClient.addGeofences(geofencingRequest,pendingIntent).addOnSuccessListener {
                Log.d("GEOADD","pendingIntent $pendingIntent")
                Toast.makeText(this,"Geofence added",Toast.LENGTH_SHORT).show()
            } .addOnFailureListener { e ->
                val errorMessage = "Permission Denied"
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()

            }

        }

    }
    private fun removeGeofence() {
        val geofencingClient = LocationServices.getGeofencingClient(this)

        val geofenceRequestIds = listOf(GEOFENCE_ID) // Replace with your existing geofence ID
        geofencingClient.removeGeofences(geofenceRequestIds)
            .addOnSuccessListener {
                // Geofences removed successfully
                geofenceLatLng = null // Update geofenceLatLng to null
                Toast.makeText(this, "Existing geofence removed", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                // Failed to remove geofences
                Toast.makeText(this, "Failed to remove existing geofence", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addMarker(latLng: LatLng){
        mMap.addMarker(MarkerOptions().position(latLng))
    }
    private fun addCircle(latLng: LatLng) {
            val circleOptions = com.google.android.gms.maps.model.CircleOptions()
                .center(latLng)
                .radius(GEOFENCE_RADIUS)
                .strokeColor(Color.argb(255, 0, 255, 0))
                .fillColor(Color.argb(64, 0, 0, 255))
                .strokeWidth(3f)
            mMap.addCircle(circleOptions)

    }
    private fun addCircleInit(latLng: LatLng, radius: Double) {
        val circleOptions = com.google.android.gms.maps.model.CircleOptions()
            .center(latLng)
            .radius(radius)
            .strokeColor(Color.argb(255, 0, 255, 0))
            .fillColor(Color.argb(64, 0, 0, 255))
            .strokeWidth(3f)
        mMap.addCircle(circleOptions)
    }
    private fun saveUserLocation(context: Context, latLng: LatLng){
        val sharedPreferences = context.getSharedPreferences("UserLocation", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putFloat("LATITUDE", latLng.latitude.toFloat())
        editor.putFloat("LONGITUDE", latLng.longitude.toFloat())
        editor.apply()
        Log.d(getUserLocation(context).toString(),"saveUserLocation"  )
    }
    private fun getUserLocation(context: Context): LatLng? {
        val sharedPreferences = context.getSharedPreferences("UserLocation", Context.MODE_PRIVATE)
        val latitude = sharedPreferences.getFloat("LATITUDE", 0f)
        val longitude = sharedPreferences.getFloat("LONGITUDE", 0f)
        if (latitude != 0f && longitude != 0f) {
            return LatLng(latitude.toDouble(), longitude.toDouble())
        }
        return null
    }
    private fun saveGeofenceLatLng(location: LatLng,radius: Double) {
        // Save the geofence location using SharedPreferences or another persistence method
        val sharedPreferences = getSharedPreferences("GeofencePrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit {
            putString("latitude", location.latitude.toString())
            putString("longitude", location.longitude.toString())
            putString("radius", radius.toString())
        }
    }
    private fun retrieveGeofenceLatLng(): Map<LatLng, Double>? {
        // Retrieve the stored geofence location from SharedPreferences
        val sharedPreferences = getSharedPreferences("GeofencePrefs", Context.MODE_PRIVATE)
        val latitude = sharedPreferences.getString("latitude", null)?.toDoubleOrNull()
        val longitude = sharedPreferences.getString("longitude", null)?.toDoubleOrNull()
        val radius = sharedPreferences.getString("radius", null)?.toDoubleOrNull()
        return if (latitude != null && longitude != null) {
            mapOf(LatLng(latitude, longitude) to radius!!)
        } else {
            null
        }
    }

    private fun viewSharedPreferences(){
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val radius = sharedPreferences.getString("radius", null)
        val username = sharedPreferences.getString("username", null)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        val userLocation=getUserLocation(this)
        Log.d("MyPrefs", "Radius: $radius, Username: $username, isLoggedIn: $isLoggedIn, userLocation: $userLocation")

    }
}