package com.example.locationupdatespendingintentkotlin

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.LocationResult

class LocationUpdatesBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent != null) {
            val action = intent.action
            if (ACTION_PROCESS_UPDATES == action) {
                val result = LocationResult.extractResult(intent)
                if (result != null) {
                    val locations = result.locations
                    Utils.setLocationUpdatesResult(context, locations)
                    Utils.sendNotification(
                        context,
                        Utils.getLocationResultTitle(context, locations)
                    )
                    Log.i(TAG, Utils.getLocationUpdatesResult(context))
                }
            }
        }
    }

    companion object {
        private val TAG = "LUBroadcastReceiver"

        internal val ACTION_PROCESS_UPDATES =
            "com.google.android.gms.location.sample.locationupdatespendingintent.action" + ".PROCESS_UPDATES"
    }
}