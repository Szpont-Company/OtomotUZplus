/**
 * @file MyFirebaseMessagingService.kt
 * @brief Serwis FCM odbierający przychodzące wiadomości push.
 */
package com.example.otomotuzplus.fcm

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.otomotuzplus.MainActivity
import com.example.otomotuzplus.R
import com.example.otomotuzplus.data.PreferenceManager
import com.example.otomotuzplus.ui.models.EnglishStrings
import com.example.otomotuzplus.ui.models.PolishStrings
import com.example.otomotuzplus.utils.NotificationHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Serwis Firebase Cloud Messaging obsługujący przychodzące wiadomości push.
 *
 * Obsługiwane są dwa formaty ładunku:
 * - **Wiadomość danych** (`remoteMessage.data` niepusta): klucze `"title"` i `"body"`
 *   są wyodrębniane i przekazywane do [sendNotification].
 * - **Wiadomość powiadomienia**: używane są standardowe pola powiadomień z Firebase.
 *
 * [onNewToken] jest wywoływany przez SDK FCM gdy token rejestracji ulegnie zmianie.
 * Jeśli użytkownik jest wtedy zalogowany, nowy token powinien być zapisany przez
 * [FirebaseRepository.updateFcmToken] (jeszcze nie zaimplementowane tutaj).
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val lang = PreferenceManager(this).getLanguage()
        val s = if (lang == "Polski") PolishStrings else EnglishStrings
        if (remoteMessage.data.isNotEmpty()) {
            val title = remoteMessage.data["title"] ?: "OtomotUZplus"
            val body = remoteMessage.data["body"] ?: s.notificationNewUpdates
            sendNotification(title, body)
        }
        else {
            remoteMessage.notification?.let {
                sendNotification(it.title ?: "OtomotUZplus", it.body ?: "")
            }
        }
    }

    private fun sendNotification(title: String, messageBody: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val uniqueNotificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(uniqueNotificationId, notificationBuilder.build())
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Jeśli użytkownik jest zalogowany, wyślij nowy token do bazy danych
    }
}
