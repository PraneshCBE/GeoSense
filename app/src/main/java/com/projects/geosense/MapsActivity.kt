package com.projects.geosense

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
    private val GEOFENCE_ID="SPIDER_MAN"

    private var geoFence: GeoFence? = null
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
        addMarker(p0)
        addCircle(p0,GEOFENCE_RADIUS.toDouble())
//        Toast.makeText(this,"Geofence added",Toast.LENGTH_SHORT).show()
        addGeofence(p0,GEOFENCE_RADIUS.toDouble())
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
            geofencingClient.addGeofences(geofencingRequest,pendingIntent)
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

}