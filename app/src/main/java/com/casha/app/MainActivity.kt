package com.casha.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.casha.app.core.auth.AuthManager
import com.casha.app.domain.usecase.auth.logout.DeleteAllLocalDataUseCase
import com.casha.app.navigation.CashaNavHost
import com.casha.app.ui.theme.CashaTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var authManager: AuthManager
    @Inject lateinit var deleteAllLocalDataUseCase: DeleteAllLocalDataUseCase
    @Inject lateinit var notificationManager: com.casha.app.core.notification.NotificationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle notification click if app was cold started
        handleNotificationIntent(intent)

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
