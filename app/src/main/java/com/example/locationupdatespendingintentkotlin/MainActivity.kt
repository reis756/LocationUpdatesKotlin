package com.example.locationupdatespendingintentkotlin

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar

class MainActivity : FragmentActivity(), SharedPreferences.OnSharedPreferenceChangeListener {
    private var mLocationRequest: LocationRequest? = null
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var mRequestUpdatesButton: Button? = null
    private var mRemoveUpdatesButton: Button? = null
    private var mLocationUpdatesResultView: TextView? = null

    private val pendingIntent: PendingIntent
        get() {
            val intent = Intent(this, LocationUpdatesBroadcastReceiver::class.java)
            intent.action = LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES
            return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mRequestUpdatesButton = findViewById(R.id.request_updates_button) as Button
        mRemoveUpdatesButton = findViewById(R.id.remove_updates_button) as Button
        mLocationUpdatesResultView = findViewById(R.id.location_updates_result) as TextView

        if (!checkPermissions()) {
            requestPermissions()
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createLocationRequest()
    }

    override fun onStart() {
        super.onStart()
        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(this)
    }


    override fun onResume() {
        super.onResume()
        updateButtonsState(Utils.getRequestingLocationUpdates(this))
        mLocationUpdatesResultView!!.setText(Utils.getLocationUpdatesResult(this))
    }

    override fun onStop() {
        PreferenceManager.getDefaultSharedPreferences(this)
            .unregisterOnSharedPreferenceChangeListener(this)
        super.onStop()
    }

    private fun createLocationRequest() {
        mLocationRequest = LocationRequest()
        mLocationRequest!!.setInterval(UPDATE_INTERVAL)
        mLocationRequest!!.setFastestInterval(FASTEST_UPDATE_INTERVAL)
        mLocationRequest!!.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        mLocationRequest!!.setMaxWaitTime(MAX_WAIT_TIME)
    }

    private fun checkPermissions(): Boolean {
        val fineLocationPermissionState = ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        )

       /* val backgroundLocationPermissionState = ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )*/

        return fineLocationPermissionState == PackageManager.PERMISSION_GRANTED /*&& backgroundLocationPermissionState == PackageManager.PERMISSION_GRANTED*/
    }

    private fun requestPermissions() {

        val permissionAccessFineLocationApproved = ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        /*val backgroundLocationPermissionApproved = ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) == PackageManager.PERMISSION_GRANTED*/

        if (permissionAccessFineLocationApproved) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.")
            Snackbar.make(
                findViewById(R.id.activity_main),
                R.string.permission_rationale,
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction(R.string.ok, View.OnClickListener {
                    // Request permission
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION
                            //Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        ),
                        REQUEST_PERMISSIONS_REQUEST_CODE
                    )
                })
                .show()
        } else {
            Log.i(TAG, "Requesting permission")
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION
                    //Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ),
                REQUEST_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.i(TAG, "onRequestPermissionResult")
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.size <= 0) {
                Log.i(TAG, "User interaction was cancelled.")

            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                requestLocationUpdates(null)

            } else {
                Snackbar.make(
                    findViewById(R.id.activity_main),
                    R.string.permission_denied_explanation,
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction(R.string.settings, View.OnClickListener {
                        // Build intent that displays the App settings screen.
                        val intent = Intent()
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        val uri = Uri.fromParts(
                            "package",
                            BuildConfig.APPLICATION_ID, null
                        )
                        intent.data = uri
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    })
                    .show()
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, s: String) {
        if (s == Utils.KEY_LOCATION_UPDATES_RESULT) {
            mLocationUpdatesResultView!!.setText(Utils.getLocationUpdatesResult(this))
        } else if (s == Utils.KEY_LOCATION_UPDATES_REQUESTED) {
            updateButtonsState(Utils.getRequestingLocationUpdates(this))
        }
    }

    fun requestLocationUpdates(view: View?) {
        try {
            Log.i(TAG, "Starting location updates")
            Utils.setRequestingLocationUpdates(this, true)
            mFusedLocationClient!!.requestLocationUpdates(mLocationRequest, pendingIntent)
        } catch (e: SecurityException) {
            Utils.setRequestingLocationUpdates(this, false)
            e.printStackTrace()
        }

    }

    fun removeLocationUpdates(view: View) {
        Log.i(TAG, "Removing location updates")
        Utils.setRequestingLocationUpdates(this, false)
        mFusedLocationClient!!.removeLocationUpdates(pendingIntent)
    }

    private fun updateButtonsState(requestingLocationUpdates: Boolean) {
        if (requestingLocationUpdates) {
            mRequestUpdatesButton!!.isEnabled = false
            mRemoveUpdatesButton!!.isEnabled = true
        } else {
            mRequestUpdatesButton!!.isEnabled = true
            mRemoveUpdatesButton!!.isEnabled = false
        }
    }

    companion object {

        private val TAG = MainActivity::class.java.simpleName
        private val REQUEST_PERMISSIONS_REQUEST_CODE = 34
        private val UPDATE_INTERVAL: Long = 60000 // Every 60 seconds.
        private val FASTEST_UPDATE_INTERVAL: Long = 30000 // Every 30 seconds
        private val MAX_WAIT_TIME = UPDATE_INTERVAL * 5 // Every 5 minutes.
    }
}

