package com.projects.geosense

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.nfc.Tag
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.projects.geosense.databinding.ActivityMapsBinding

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, OnMapLongClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var geofencingClient: GeofencingClient
    private val GEOFENCE_RADIUS=200
    private val LOCATION_PERMISSION_REQUEST_CODE = 123
    private val GEOFENCE_ID="SPIDER_MAN"

    private var geoFence: GeoFence? = null
    private var geofenceLatLng: Map<LatLng, Double>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        geofencingClient=LocationServices.getGeofencingClient(this)
        geoFence= GeoFence(this)
        geofenceLatLng = retrieveGeofenceLatLng()

    }
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val userLocation=getUserLocation(this)
        if (userLocation != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 16f))
        }else{
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(37.422160, -122.084270), 16f))
        }
        // Add a marker in Sydney and move the camera
        enableUserLocation()
        mMap.setOnMyLocationButtonClickListener {
            val userLocation = mMap.cameraPosition.target
            saveUserLocation(this, userLocation)
            false // Return false to allow the default behavior (centering the camera)
        }
        mMap.setOnMapLongClickListener(this)
        if (geofenceLatLng != null) {
            val geofenceLocation = geofenceLatLng!!.keys.first()
            addMarker(geofenceLocation)
            addCircle(geofenceLocation, geofenceLatLng!!.values.first())
            addGeofence(geofenceLocation, geofenceLatLng!!.values.first())
        }


    }
    private fun enableUserLocation() {
        val fineLocationPermission = android.Manifest.permission.ACCESS_FINE_LOCATION
        val granted = PackageManager.PERMISSION_GRANTED
        if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)==
            android.content.pm.PackageManager.PERMISSION_GRANTED){
            mMap.isMyLocationEnabled=true
        }
        // Check and request foreground location permission
        if (ContextCompat.checkSelfPermission(this, fineLocationPermission) != granted) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(fineLocationPermission),
                0
            )
            return
        }

        // Check and request background location permission
        val backgroundLocationPermission = android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, backgroundLocationPermission) != granted) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(backgroundLocationPermission),
                    0
                )
                return
            }
        }

        // If both permissions are granted, enable location on the map
        mMap.isMyLocationEnabled = true
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
                    addMarker(p0)
                    addCircle(p0, GEOFENCE_RADIUS.toDouble())
                    addGeofence(p0, GEOFENCE_RADIUS.toDouble())
                    saveGeofenceLatLng(p0, GEOFENCE_RADIUS.toDouble())
                }
                .setNegativeButton("No", null)
                .show()
        }
        else {
            addMarker(p0)
            addCircle(p0, GEOFENCE_RADIUS.toDouble())
            addGeofence(p0, GEOFENCE_RADIUS.toDouble())
            saveGeofenceLatLng(p0, GEOFENCE_RADIUS.toDouble())
        }
    }
    private fun addGeofence(latLng: LatLng, radius: Double){

        val geofence=geoFence!!.getGeofence(GEOFENCE_ID,latLng,radius,Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_DWELL or Geofence.GEOFENCE_TRANSITION_EXIT)

        val geofencingRequest=geoFence!!.getGeofencingRequest(geofence)
        val pendingIntent=geoFence!!.retPendingIntent()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        if (pendingIntent != null) {
            geofencingClient.addGeofences(geofencingRequest,pendingIntent).addOnSuccessListener {
                Log.d("GEOADD","pendingIntent $pendingIntent")
                Toast.makeText(this,"Geofence added",Toast.LENGTH_SHORT).show()
            } .addOnFailureListener { e ->
                val errorMessage = geoFence?.getErrorString(e) ?: "Geofence not added"
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
    private fun addCircle(latLng: LatLng, radius: Double) {
        val circleOptions = com.google.android.gms.maps.model.CircleOptions()
            .center(latLng)
            .radius(radius)
            .strokeColor(Color.argb(255, 0, 255, 0))
            .fillColor(Color.argb(64, 255, 0, 0))
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
}