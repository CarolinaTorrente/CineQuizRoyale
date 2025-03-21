package com.example.cinequizroyale

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap  // Google Map instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps) // Link to activity_maps.xml

        // Get the SupportMapFragment and notify when the map is ready
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        // nuevo comentariooooo

        // List of Madrid cinemas with latitude & longitude
        val cinemas = listOf(
            LatLng(40.4183, -3.7070) to "Cines Callao",
            LatLng(40.4233, -3.7044) to "Yelmo Cines Ideal",
            LatLng(40.4381, -3.6919) to "Cinesa Manoteras",
            LatLng(40.3906, -3.6996) to "Cines Renoir Plaza de España",
            LatLng(40.4425, -3.6775) to "Cinesa Heron City"
        )

        // Add markers and show their info windows
        val markerList = mutableListOf<com.google.android.gms.maps.model.Marker>()

        for ((location, name) in cinemas) {
            val marker = mMap.addMarker(MarkerOptions().position(location).title(name))
            marker?.showInfoWindow() // Show name immediately
            markerList.add(marker!!)
        }

        // Move the camera to Madrid
        val madrid = LatLng(40.4168, -3.7038) // Madrid center
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(madrid, 12f))
    }


}
