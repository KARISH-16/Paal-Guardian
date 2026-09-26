package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import com.example.data.local.AppDatabase

class PaalGuardianApp : Application() {

    lateinit var database: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = AppDatabase.getInstance(this)
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val alertSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                .build()

            val tempWarningChannel = NotificationChannel(
                CHANNEL_TEMP_WARNING,
                "Milk Temperature Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Urgent alerts when milk chilling temperature exceeds 8°C"
                enableLights(true)
                enableVibration(true)
                setSound(alertSoundUri, audioAttributes)
            }

            val syncChannel = NotificationChannel(
                CHANNEL_SYNC,
                "Cloud Synchronization",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background sync status"
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(tempWarningChannel)
            notificationManager?.createNotificationChannel(syncChannel)
        }
    }

    companion object {
        const val CHANNEL_TEMP_WARNING = "paal_temp_warning_channel"
        const val CHANNEL_SYNC = "paal_sync_channel"

        lateinit var instance: PaalGuardianApp
            private set
    }
}
