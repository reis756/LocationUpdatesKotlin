package com.example.locationupdatespendingintentkotlin

import android.app.IntentService
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.LocationResult


class LocationUpdatesIntentService : IntentService(TAG) {

    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            val action = intent.action
            if (ACTION_PROCESS_UPDATES == action) {
                val result = LocationResult.extractResult(intent)
                if (result != null) {
                    val locations = result.locations
                    Utils.setLocationUpdatesResult(this, locations)
                    Utils.sendNotification(this, Utils.getLocationResultTitle(this, locations))
                    Log.i(TAG, Utils.getLocationUpdatesResult(this))
                }
            }
        }
    }

    companion object {

        private val ACTION_PROCESS_UPDATES =
            "com.google.android.gms.location.sample.locationupdatespendingintent.action" + ".PROCESS_UPDATES"
        private val TAG = LocationUpdatesIntentService::class.java.simpleName
    }
}// Name the worker thread.