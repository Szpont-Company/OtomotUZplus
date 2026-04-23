package com.example.otomotuzplus.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.otomotuzplus.MainActivity
import com.example.otomotuzplus.R
import com.example.otomotuzplus.ui.models.AppStrings
object NotificationHelper {
    const val CHANNEL_ID = "offers_channel"

    fun createNotificationChannel(context: Context, strings: AppStrings) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = strings.newOffers
            val descriptionText = "Powiadomienia z aplikacji OtomotUZplus"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun sendTestNotification(context: Context, strings: AppStrings) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(strings.dealNotification) // Używamy Twoich stringów
            .setContentText(strings.dealNotificationDescription)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1001, notification)
    }
}