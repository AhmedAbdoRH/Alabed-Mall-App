package com.example.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

sealed interface LocationStreamEvent {
    data object PermissionMissing : LocationStreamEvent
    data object WaitingForSignal : LocationStreamEvent
    data class LocationUpdated(val location: Location) : LocationStreamEvent
    data class Error(val message: String) : LocationStreamEvent
}

class MallLocationClient(private val context: Context) {

    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    fun hasLocationPermission(): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineGranted || coarseGranted
    }

    @SuppressLint("MissingPermission")
    fun observeLocationUpdates(): Flow<LocationStreamEvent> = callbackFlow {
        if (!hasLocationPermission()) {
            trySend(LocationStreamEvent.PermissionMissing)
            close()
            return@callbackFlow
        }

        trySend(LocationStreamEvent.WaitingForSignal)

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000L)
            .setMinUpdateIntervalMillis(1500L)
            .setWaitForAccurateLocation(false)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation ?: return
                trySend(LocationStreamEvent.LocationUpdated(loc))
            }
        }

        try {
            fusedClient.lastLocation.addOnSuccessListener { lastLoc ->
                if (lastLoc != null) {
                    trySend(LocationStreamEvent.LocationUpdated(lastLoc))
                }
            }
            fusedClient.requestLocationUpdates(request, callback, Looper.getMainLooper())
        } catch (se: SecurityException) {
            trySend(LocationStreamEvent.PermissionMissing)
            close()
            return@callbackFlow
        } catch (e: Exception) {
            trySend(LocationStreamEvent.Error(e.localizedMessage ?: "Location error"))
        }

        awaitClose {
            fusedClient.removeLocationUpdates(callback)
        }
    }
}
