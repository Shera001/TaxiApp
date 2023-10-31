package taxiapp.feature.home.location

import android.annotation.SuppressLint
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

class DefaultLocationClient(
    private val client: FusedLocationProviderClient
) : LocationClient {

    private var oldLocation: Location? = null

    @SuppressLint("MissingPermission")
    override fun getLocationUpdates(interval: Long): Flow<Location> {
        return callbackFlow {

            val request = LocationRequest.Builder(interval)
                .setMaxUpdates(interval.toInt())
                .build()

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    super.onLocationResult(result)
                    result.locations.lastOrNull()?.let { location: Location ->
                        if (!locationIsEquals(oldLocation, location)) {
                            launch { send(location) }
                            oldLocation = location
                        }
                    }
                }
            }

            client.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
            )

            awaitClose {
                client.removeLocationUpdates(locationCallback)
            }
        }
    }

    private fun locationIsEquals(oldLocation: Location?, newLocation: Location): Boolean {
        oldLocation ?: return false

        val distance = FloatArray(1)

        Location.distanceBetween(
            oldLocation.latitude,
            oldLocation.longitude,
            newLocation.latitude,
            newLocation.longitude,
            distance
        )

        return distance[0] < 8.0
    }
}