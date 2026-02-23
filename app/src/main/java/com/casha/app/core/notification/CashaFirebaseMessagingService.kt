package com.casha.app.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.casha.app.MainActivity
import com.casha.app.R
import com.casha.app.domain.model.NotificationCasha
import com.casha.app.domain.model.NotificationType
import com.casha.app.domain.repository.AuthRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CashaFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var notificationManager: com.casha.app.core.notification.NotificationManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token: $token")
        serviceScope.launch {
            try {
                authRepository.registerPushToken(token)
            } catch (e: Exception) {
                Log.e("FCM", "Failed to register token: ${e.message}")
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("FCM", "Message received: ${remoteMessage.data}")

        val data = remoteMessage.data
        val typeStr = data["type"] ?: "UNKNOWN"
        val type = try {
            NotificationType.valueOf(typeStr)
        } catch (e: Exception) {
            NotificationType.UNKNOWN
        }

        val notification = NotificationCasha(
            id = remoteMessage.messageId ?: System.currentTimeMillis().toString(),
            title = data["title"] ?: remoteMessage.notification?.title ?: "Casha Alert",
            body = data["body"] ?: remoteMessage.notification?.body ?: "",
            type = type,
            imageUrl = data["imageUrl"],
            data = data.filterKeys { it != "title" && it != "body" && it != "type" && it != "imageUrl" }
        )

        // 1. Dispatch to in-app manager
        serviceScope.launch {
            notificationManager.dispatch(notification)
        }

        // 2. Show system notification
        showNotification(notification)
    }

    private fun showNotification(notification: NotificationCasha) {
        val channelId = "casha_notifications"
        val nManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Casha Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            nManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notification_id", notification.id)
            putExtra("notification_type", notification.type.name)
            notification.data.forEach { (key, value) ->
                putExtra(key, value)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(notification.title)
            .setContentText(notification.body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        nManager.notify(notification.id.hashCode(), builder.build())
    }
}
