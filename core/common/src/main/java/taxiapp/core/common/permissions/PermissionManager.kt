package taxiapp.core.common.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.lifecycle.LifecycleOwner
import taxiapp.core.common.permissions.PermissionDialog.showDialog
import taxiapp.core.common.util.hasPermission
import taxiapp.core.common.util.openAppSettings
import taxiapp.core.common.util.shouldShowRequestPermissionRationaleCompat
import javax.inject.Inject

class PermissionManager(
    activity: Activity,
    private val context: Context,
    activityResultRegistry: ActivityResultRegistry,
    lifecycleOwner: LifecycleOwner,
    private val notificationTextProvider: NotificationTextProvider,
    private val locationTextProvider: LocationTextProvider,
    private val onPermissionGranted: () -> Unit
) {

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val requestNotificationsPermissionLauncher = activityResultRegistry.register(
        POST_NOTIFICATIONS_KEY,
        lifecycleOwner,
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            onNotificationPermissionGranted()
        } else {
            showDialog(
                context,
                permissionTextProvider = notificationTextProvider,
                isPermanentlyDeclined = !activity.shouldShowRequestPermissionRationaleCompat(
                    POST_NOTIFICATIONS
                ),
                onOkClick = {
                    requestPostNotificationsPermission()
                },
                onGoToAppSettingsClick = {
                    context.openAppSettings()
                }
            )
        }
    }

    private val requestLocationPermissionLauncher = activityResultRegistry.register(
        COARSE_LOCATION_KEY,
        lifecycleOwner,
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            onPermissionGranted()
        } else {
            showDialog(
                context,
                permissionTextProvider = locationTextProvider,
                isPermanentlyDeclined = !activity.shouldShowRequestPermissionRationaleCompat(
                    COARSE_LOCATION
                ),
                onOkClick = {
                    requestLocationPermission()
                },
                onGoToAppSettingsClick = {
                    context.openAppSettings()
                }
            )
        }
    }

    private fun onNotificationPermissionGranted() {
        if (context.hasPermission(COARSE_LOCATION)) {
            onPermissionGranted()
        } else {
            requestLocationPermission()
        }
    }

    fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!context.hasPermission(POST_NOTIFICATIONS)) {
                requestNotificationsPermissionLauncher.launch(POST_NOTIFICATIONS)
            } else {
                onNotificationPermissionGranted()
            }
        } else {
            onNotificationPermissionGranted()
        }
    }

    private fun requestLocationPermission() {
        requestLocationPermissionLauncher.launch(COARSE_LOCATION)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestPostNotificationsPermission() {
        requestNotificationsPermissionLauncher.launch(POST_NOTIFICATIONS)
    }

    companion object {
        const val COARSE_LOCATION_KEY = "coarse_location"
        const val POST_NOTIFICATIONS_KEY = "post_notifications"

        const val COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        const val POST_NOTIFICATIONS = Manifest.permission.POST_NOTIFICATIONS
    }
}