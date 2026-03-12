package com.casha.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.casha.app.core.auth.AuthManager
import com.casha.app.domain.usecase.auth.logout.DeleteAllLocalDataUseCase
import com.casha.app.domain.usecase.notification.FcmRegistrationUseCase
import com.casha.app.navigation.CashaNavHost
import com.casha.app.ui.theme.CashaTheme
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject lateinit var authManager: AuthManager
    @Inject lateinit var deleteAllLocalDataUseCase: DeleteAllLocalDataUseCase
    @Inject lateinit var notificationManager: com.casha.app.core.notification.NotificationManager
    @Inject lateinit var fcmRegistrationUseCase: FcmRegistrationUseCase

    // Request notification permission launcher (Android 13+)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted — now register the FCM token with the backend
            fetchAndRegisterFcmToken()
        }
        // If denied, notifications simply won't show — no crash
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle notification click if app was cold started
        handleNotificationIntent(intent)

        // Request POST_NOTIFICATIONS permission on Android 13+
        askNotificationPermission()

        // Observe user currency globally to maintain a single source of truth
        lifecycleScope.launch {
            authManager.selectedCurrency.collect { currency ->
                com.casha.app.core.util.CurrencyFormatter.defaultCurrency = currency ?: "IDR"
            }
        }

        setContent {
            CashaTheme {
                CashaNavHost(
                    authManager = authManager,
                    deleteAllLocalDataUseCase = deleteAllLocalDataUseCase,
                    notificationManager = notificationManager
                )
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        handleNotificationIntent(intent)
    }

    /**
     * Bug fix #1: POST_NOTIFICATIONS is a dangerous permission on Android 13+.
     * Declaring it in the manifest is NOT enough — we must request it at runtime.
     */
    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Already granted — still proactively refresh + register the FCM token
                    fetchAndRegisterFcmToken()
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // Pre-Android 13 — notifications are granted by default; just register token
            fetchAndRegisterFcmToken()
        }
    }

    /**
     * Bug fix #2: `onNewToken` in FCM is only called on first install or token reset,
     * not on every login. We must proactively fetch the token and register it.
     */
    private fun fetchAndRegisterFcmToken() {
        lifecycleScope.launch {
            try {
                // Fetch current token from Firebase
                val token = FirebaseMessaging.getInstance().token.await()
                android.util.Log.d("FCM_TOKEN", token)
                
                // Save it as pending
                authManager.savePendingFcmToken(token)
                
                // Attempt to register it (only succeeds if user has valid JWT)
                fcmRegistrationUseCase()
            } catch (e: Exception) {
                android.util.Log.e("FCM", "Failed to register FCM token on launch: ${e.message}")
            }
        }
    }

    private fun handleNotificationIntent(intent: android.content.Intent) {
        val typeStr = intent.getStringExtra("notification_type") ?: return
        val id = intent.getStringExtra("notification_id") ?: ""
        
        // Use a simple map to extract all data extras
        val data = mutableMapOf<String, String>()
        intent.extras?.keySet()?.forEach { key ->
            if (key != "notification_type" && key != "notification_id") {
                intent.getStringExtra(key)?.let { data[key] = it }
            }
        }

        val type = try {
            com.casha.app.domain.model.NotificationType.valueOf(typeStr)
        } catch (e: Exception) {
            com.casha.app.domain.model.NotificationType.UNKNOWN
        }

        val notification = com.casha.app.domain.model.NotificationCasha(
            id = id,
            title = "", // Not needed for navigation
            body = "",
            type = type,
            data = data
        )

        // Dispatch to manager for the NavHost to handle
        lifecycleScope.launch {
            notificationManager.dispatch(notification)
        }
    }
}
