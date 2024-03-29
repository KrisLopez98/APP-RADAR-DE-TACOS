package com.example.appradardetacos

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.appradardetacos.databinding.ActivityMapsBinding
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton

private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private val taqueria = mutableListOf<Taqueria>()
    private val userLocation = Location("")
    private lateinit var myLocationButton: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        myLocationButton = findViewById(R.id.my_location_button)

        addFakeTaquerias()

        requestLocationPermission()
    }

    private fun addFakeTaquerias(){
        taqueria.add(Taqueria("Tacos de asada", -33.954044, 151.241283))
        taqueria.add(Taqueria("Tacos de pastor", -33.967154, 151.264715))
        taqueria.add(Taqueria("Tacos de cochinita", -33.943820, 151.243603))
        taqueria.add(Taqueria("Tacos de barbacoa", -33.936577, 151.259410))
        taqueria.add(Taqueria("Tacos de Birria", -33.936255, 151.239293))
        taqueria.add(Taqueria("Burritos", -33.938594, 151.224316))
    }

    private fun requestLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                getUserLocation()
            } else {
                val permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
                requestPermissions(permissionsArray, LOCATION_PERMISSION_REQUEST_CODE)
            }
        } else{
            getUserLocation()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE){
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)){
                getUserLocation()
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)){
                showLocationPermissionRationaleDialog()
            } else{
                finish()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun showLocationPermissionRationaleDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Necesitas permiso de ubicación")
            .setMessage("Acepta el permiso")
            .setPositiveButton(android.R.string.ok){_ , _ ->
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE)
            }.setNegativeButton("No"){ _ , _ ->
                finish()
            }
        dialog.show()
    }

    @SuppressLint("MissingPermission")
    private fun getUserLocation(){
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation.addOnSuccessListener {
            location: Location? ->
            if(location != null){
                userLocation.latitude = location.latitude
                userLocation.longitude = location.longitude
                setupMap()
            }
        }
    }

    private fun setupMap(){
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
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

        val icon = getTacoIcon()
        for (taqueria in taqueria){
            val tacoPosition = LatLng(taqueria.latitude, taqueria.longitude)
            val tacoName = taqueria.name

            val tacoLocation = Location("")
            tacoLocation.latitude = taqueria.latitude
            tacoLocation.longitude = taqueria.longitude

            val distance: Float= tacoLocation.distanceTo(userLocation)

            val markerOptions = MarkerOptions().position(tacoPosition).title(tacoName)
                .snippet("Distance: $distance")
                .icon(icon)
            mMap.addMarker(markerOptions)
        }

        // Add a marker in Sydney and move the camera
        val userMarker = LatLng(userLocation.latitude, userLocation.longitude)
        mMap.addMarker(MarkerOptions().position(userMarker).title("User location"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userMarker, 13.0f))

        myLocationButton.setOnClickListener {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userMarker, 13.0f))
        }
    }

    private fun getTacoIcon(): BitmapDescriptor{
        val drawable = ContextCompat.getDrawable(this, R.drawable.taco_kawaii)
        drawable?.setBounds(0,0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        val bitmap = Bitmap.createBitmap(drawable?.intrinsicWidth ?:0, drawable?.intrinsicHeight ?:0,
        Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable?.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}