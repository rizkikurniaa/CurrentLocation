package com.kikulabs.currentlocation

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.kikulabs.currentlocation.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val locationPermissionReqCode = 1000;
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private lateinit var lm: LocationManager
    private var gpsEnabled = false
    private var networkEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // initialize fused location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        lm = applicationContext.getSystemService(LOCATION_SERVICE) as LocationManager

        binding.btGetLocation.setOnClickListener {
            //checking is location service enabled or not
            try {
                gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
            } catch (ex: Exception) {
            }

            try {
                networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            } catch (ex: Exception) {
            }

            if (!gpsEnabled && !networkEnabled) {
                // notify user
                AlertDialog.Builder(this)
                    .setMessage(R.string.gps_network_not_enabled)
                    .setPositiveButton(
                        R.string.open_location_settings
                    ) { _, _ ->
                        startActivity(
                            Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        )
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
            } else {
                getCurrentLocation()
            }
        }

        binding.btOpenMap.setOnClickListener {
            openMap(latitude, longitude)
        }
    }

    private fun getCurrentLocation() {
        // checking location permission
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // request permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), locationPermissionReqCode
            )
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    // getting the last known or current location
                    latitude = location.latitude
                    longitude = location.longitude

                    binding.tvLatitude.text = "Latitude: ${location.latitude}"
                    binding.tvLongitude.text = "Longitude: ${location.longitude}"
                    binding.tvProvider.text = "Provider: ${location.provider}"
                    binding.btOpenMap.visibility = View.VISIBLE
                } else {
                    // notify user to recallibrate maps
                    AlertDialog.Builder(this)
                        .setMessage(R.string.recalibrate)
                        .setPositiveButton(
                            R.string.open_google_maps
                        ) { _, _ ->
                            //open maps Padang City
                            openMap(-0.942942, 100.371857)
                        }
                        .setNegativeButton(R.string.cancel, null)
                        .show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(
                    this, R.string.failed,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            locationPermissionReqCode -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    // permission granted
                } else {
                    // permission denied
                    Toast.makeText(
                        this, R.string.grant_permission,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun openMap(lat: Double, long: Double) {
        val uri = Uri.parse("geo:${lat},${long}")
        val mapIntent = Intent(Intent.ACTION_VIEW, uri)
        mapIntent.setPackage("com.google.android.apps.maps")
        startActivity(mapIntent)
    }
}