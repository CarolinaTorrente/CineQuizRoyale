package com.example.cinequizroyale

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val cinemas = listOf(
        LatLng(40.4183, -3.7070) to "Cines Callao",
        LatLng(40.4233, -3.7044) to "Yelmo Cines Ideal",
        LatLng(40.4381, -3.6919) to "Cinesa Manoteras",
        LatLng(40.3906, -3.6996) to "Cines Renoir Plaza de España",
        LatLng(40.4425, -3.6775) to "Cinesa Heron City"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(40.4168, -3.7038), 12f))

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    showClosestCinemas(it)
                }
            }
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }
    }

    private fun showClosestCinemas(userLocation: Location) {
        val userLatLng = LatLng(userLocation.latitude, userLocation.longitude)

        mMap.addMarker(
            MarkerOptions()
                .position(userLatLng)
                .title("You are here")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
        )

        val cinemaDistances = cinemas.map { (latLng, name) ->
            val cinemaLocation = Location("").apply {
                latitude = latLng.latitude
                longitude = latLng.longitude
            }
            val distance = userLocation.distanceTo(cinemaLocation)
            Triple(name, latLng, distance)
        }.sortedBy { it.third }

        val closestThree = cinemaDistances.take(3)

        closestThree.forEachIndexed { index, (name, location, distance) ->
            val marker = mMap.addMarker(
                MarkerOptions().position(location)
                    .title("$name (${(distance / 1000).format(2)} km)")
            )
            Handler(Looper.getMainLooper()).postDelayed({
                marker?.showInfoWindow()
            }, index * 500L)
        }

        Toast.makeText(this, "Showing 3 closest cinemas", Toast.LENGTH_SHORT).show()
    }

    private fun Float.format(decimals: Int): String = "%.${decimals}f".format(this)
}
