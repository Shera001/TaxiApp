package taxiapp.feature.home.location

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import taxiapp.core.common.util.Constants.ACTION_STOP_LISTEN
import taxiapp.core.common.util.Constants.NOTIFICATION_CHANNEL_ID
import taxiapp.core.common.util.Constants.NOTIFICATION_CHANNEL_NAME
import taxiapp.core.common.util.Constants.NOTIFICATION_ID
import taxiapp.core.domain.use_case.PostLocationUseCase
import taxiapp.core.model.Location
import javax.inject.Inject

@AndroidEntryPoint
class LocationService : Service() {

    @Inject
    lateinit var postLocation: PostLocationUseCase

    @Inject
    lateinit var notificationManager: NotificationManager

    @Inject
    lateinit var notification: NotificationCompat.Builder

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var locationClient: LocationClient

    override fun onCreate() {
        super.onCreate()
        locationClient = DefaultLocationClient(
            LocationServices.getFusedLocationProviderClient(applicationContext)
        )
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null && ACTION_STOP_LISTEN == intent.action) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY;
        }

        startForegroundService()
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startForegroundService() {
        createNotificationChannel()

        locationClient
            .getLocationUpdates(10000L)
            .catch { e -> e.printStackTrace() }
            .onEach { location ->
                val lat = location.latitude
                val long = location.longitude
                val currentLocation = Location(lat, long)
                postLocation(currentLocation)
            }
            .launchIn(serviceScope)


        startForeground(NOTIFICATION_ID, notification.build())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
    }
}