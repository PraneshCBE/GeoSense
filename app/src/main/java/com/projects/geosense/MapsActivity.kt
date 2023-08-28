package com.projects.geosense

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        geofencingClient=LocationServices.getGeofencingClient(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
        enableUserLocation()
        mMap.setOnMapLongClickListener(this)


    }
    private fun enableUserLocation(){
        if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)==
            android.content.pm.PackageManager.PERMISSION_GRANTED){
            mMap.isMyLocationEnabled=true
        }
        else{
            //Ask for permission
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),0)
            }
        }

    }
    public override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0]==android.content.pm.PackageManager.PERMISSION_GRANTED){
            enableUserLocation()
        }
        else{
            //Show a dialog that permission is not granted
        }
    }

    override fun onMapLongClick(p0: LatLng) {
        print("Long click")
        addMarker(p0)
        addCircle(p0,GEOFENCE_RADIUS.toDouble())
    }

    private fun addMarker(latLng: LatLng){
        mMap.addMarker(MarkerOptions().position(latLng))
    }
    private fun addCircle(latLng: LatLng, radius: Double) {
        val circleOptions = com.google.android.gms.maps.model.CircleOptions()
            .center(latLng)
            .radius(radius)
            .strokeColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
            .fillColor(ContextCompat.getColor(this, android.R.color.holo_green_light))
            .strokeWidth(3f)
        mMap.addCircle(circleOptions)
    }

}