/**
 * @file NotificationHelper.kt
 * @brief Singleton pomocniczy do tworzenia kanału i wysyłania lokalnych powiadomień.
 */
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
/**
 * Singleton pomocniczy do tworzenia kanału powiadomień i wysyłania lokalnych
 * powiadomień push.
 *
 * Kanał powiadomień (`offers_channel`) jest tworzony raz przy uruchomieniu aplikacji
 * wewnątrz `MainActivity.onCreate`. Wszystkie powiadomienia wysyłane przez tego pomocnika
 * i przez [MyFirebaseMessagingService] współdzielą ten kanał.
 */
object NotificationHelper {
    /** Identyfikator kanału powiadomień współdzielony z [MyFirebaseMessagingService]. */
    const val CHANNEL_ID = "offers_channel"

    /**
     * Tworzy kanał powiadomień `offers_channel` (tylko Android 8+).
     *
     * Bezpieczne do wielokrotnego wywoływania — Android deduplikuje tworzenie kanałów po ID.
     *
     * @param context Kontekst aplikacji lub Activity.
     * @param strings Aktywne [AppStrings] dostarczające zlokalizowaną nazwę kanału.
     */
    fun createNotificationChannel(context: Context, strings: AppStrings) {
        val name = strings.newOffers
        val descriptionText = "Powiadomienia z aplikacji OtomotUZplus"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    /**
     * Wysyła lokalne powiadomienie z podanym tytułem i treścią.
     *
     * Używa identyfikatora powiadomienia opartego na znaczniku czasu, aby kolejne wywołania
     * nie nadpisywały się nawzajem. Dotknięcie powiadomienia uruchamia [MainActivity]
     * i czyści stos back.
     *
     * @param context Kontekst aplikacji lub Activity.
     * @param title   Ciąg tytułu powiadomienia.
     * @param message Tekst treści powiadomienia.
     */
    fun sendNotification(context: Context, title: String, message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title) // Używamy przekazanego tytułu
            .setContentText(message) // Używamy przekazanej treści
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}