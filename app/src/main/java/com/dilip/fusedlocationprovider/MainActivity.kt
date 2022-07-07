package com.dilip.fusedlocationprovider

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dilip.fusedlocationprovider.databinding.ActivityMainBinding
import com.google.android.gms.location.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val locationPermissionRequestCode = 100

    override fun onStart() {
        super.onStart()
        checkLocationPermissionAndGps()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    /**
     *  First checking location permission. If permission enabled checking device GPS status. If both enabled settingUp location listener.
     *  If Permission denied, So asking again by showing custom dialog. Navigating user to app settings page on click of Yes button.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            locationPermissionRequestCode -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    when {
                        PermissionUtils.isGPSEnabled(this) -> setUpLocationListener()
                        else -> PermissionUtils.showGPSNotEnabledDialog(this)
                    }
                }
                else {
                    PermissionUtils.showPermissionEnabledDialog(this)
                }
            }
        }
    }

    /**
     * As soon as app starts, First checking location permission next GPS status. If both enabled settingUp location listener.
     * If permission not granted, Showing default permission dialog where user can see Approve, Denied & Only for now.
     */
    private fun checkLocationPermissionAndGps(){
        when {
            PermissionUtils.isLocationPermissionGranted(this) -> {
                when {
                    PermissionUtils.isGPSEnabled(this) -> setUpLocationListener()
                    else -> PermissionUtils.showGPSNotEnabledDialog(this)
                }
            }
            else -> PermissionUtils.requestAccessLocationPermission(this, locationPermissionRequestCode)
        }
    }

    private fun showToast(message:String){
        Toast.makeText(this,message,Toast.LENGTH_LONG).show()
    }

    /**
     * If Both location permission & Mobile GPS enabled setting-up Fused location listener.
     */
    @SuppressLint("MissingPermission")
    private fun setUpLocationListener() {
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        val locationRequest = LocationRequest.create().apply {
            interval = 3000
            fastestInterval = 1000
            priority = Priority.PRIORITY_HIGH_ACCURACY
            maxWaitTime = 1000
        }

        if(PermissionUtils.isLocationPermissionGranted(this)){
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        super.onLocationResult(locationResult)
                        for (location in locationResult.locations) {
                            binding.tvLatLang.text = "${location.latitude}, ${location.longitude}"
                        }
                    }
                },
                Looper.myLooper()
            )
        }
    }
}