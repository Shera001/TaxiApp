package uz.test.taxiapp.di

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import taxiapp.core.common.util.Constants.ACTION_STOP_LISTEN
import taxiapp.core.common.util.Constants.NOTIFICATION_CHANNEL_ID
import taxiapp.core.common.util.Constants.PENDING_INTENT_REQUEST_CODE_ACTIVITY
import taxiapp.core.common.util.Constants.PENDING_INTENT_REQUEST_CODE_SERVICE
import taxiapp.feature.home.location.LocationService
import uz.test.taxiapp.MainActivity
import uz.test.taxiapp.R
import javax.inject.Named

@Module
@InstallIn(ServiceComponent::class)
object NotificationModule {

    @ServiceScoped
    @Provides
    @Named("ForActivity")
    fun providePendingIntentForActivity(
        @ApplicationContext context: Context
    ): PendingIntent {
        return PendingIntent.getActivity(
            context,
            PENDING_INTENT_REQUEST_CODE_ACTIVITY,
            Intent(context, MainActivity::class.java),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )
    }

    @ServiceScoped
    @Provides
    @Named("ForService")
    fun providePendingIntentForService(
        @ApplicationContext context: Context
    ): PendingIntent {
        return PendingIntent.getService(
            context,
            PENDING_INTENT_REQUEST_CODE_SERVICE,
            Intent(context, LocationService::class.java).also {
                it.action = ACTION_STOP_LISTEN
            },
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )
    }

    @ServiceScoped
    @Provides
    fun provideNotificationBuilder(
        @ApplicationContext context: Context,
        @Named("ForActivity") pendingIntentForActivity: PendingIntent,
        @Named("ForService") pendingIntentForService: PendingIntent
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getString(R.string.tracking_location))
            .setContentIntent(pendingIntentForActivity)
            .addAction(
                R.drawable.baseline_close_24,
                context.getString(R.string.stop_tracing),
                pendingIntentForService
            )
    }

    @ServiceScoped
    @Provides
    fun provideNotificationManager(
        @ApplicationContext context: Context
    ): NotificationManager {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
}