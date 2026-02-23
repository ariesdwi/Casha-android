package com.casha.app.navigation

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.casha.app.core.auth.AuthManager
import com.casha.app.core.network.UnauthorizedEvent
import com.casha.app.domain.usecase.auth.logout.DeleteAllLocalDataUseCase
import com.casha.app.ui.feature.auth.ForgotPasswordScreen
import com.casha.app.ui.feature.auth.LoginScreen
import com.casha.app.ui.feature.auth.RegisterScreen
import com.casha.app.ui.feature.auth.SetupCurrencyScreen
import com.casha.app.ui.feature.loading.AppLoadingDestination
import com.casha.app.ui.feature.loading.AppLoadingScreen
import com.casha.app.ui.feature.splash.SplashScreen
import kotlinx.coroutines.flow.firstOrNull

/**
 * Root navigation host for the Casha app.
 *
 * Handles:
 * - Token check on launch → skip to AppLoading if token exists
 * - Splash screen when no token
 * - 401 Unauthorized auto-logout via [UnauthorizedEvent]
 */
@Composable
fun CashaNavHost(
    authManager: AuthManager,
    deleteAllLocalDataUseCase: DeleteAllLocalDataUseCase,
    notificationManager: com.casha.app.core.notification.NotificationManager
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    // ── Handle Global Notifications & Deep Linking ──
    LaunchedEffect(notificationManager) {
        notificationManager.notifications.collect { notification ->
            // Determine the target route based on notification type
            val route = when (notification.type) {
                com.casha.app.domain.model.NotificationType.TRANSACTION_ADDED -> {
                    val id = notification.data["transactionId"]
                    val type = notification.data["cashflowType"] ?: "EXPENSE"
                    if (id != null) NavRoutes.TransactionDetail.createRoute(id, type) else null
                }
                com.casha.app.domain.model.NotificationType.BUDGET_ALERT,
                com.casha.app.domain.model.NotificationType.BUDGET_CREATED -> NavRoutes.Budget.route
                com.casha.app.domain.model.NotificationType.MONTHLY_SUMMARY -> NavRoutes.Report.route
                else -> null
            }

            route?.let {
                // If we're not logged in, we shouldn't navigate to app content
                val token = authManager.accessToken.firstOrNull()
                if (!token.isNullOrBlank()) {
                    navController.navigate(it) {
                        // Avoid multiple copies of the same screen
                        launchSingleTop = true
                    }
                }
            }
        }
    }

    // ── Determine start destination based on stored token ──
    var startDestination by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(authManager) {
        // Collect the accessToken flow and take the first emission
        // This ensures we wait for the DataStore to load the initial value from disk
        authManager.accessToken.collect { token ->
            startDestination = if (!token.isNullOrBlank()) {
                NavRoutes.AppLoading.route
            } else {
                NavRoutes.Splash.route
            }
            // Once we have determined the start destination, we don't need to collect anymore
            // for the purpose of setting startDestination.
            return@collect 
        }
    }

    // ── Observe 401 Unauthorized events for auto-logout ──
    LaunchedEffect(Unit) {
        UnauthorizedEvent.events.collect {
            deleteAllLocalDataUseCase()
            Toast.makeText(context, "Session expired. Please login again.", Toast.LENGTH_LONG).show()
            navController.navigate(NavRoutes.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    // Show nothing while determining start destination
    if (startDestination == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    NavHost(
        navController = navController,
        startDestination = startDestination!!
    ) {
        // ── Splash ──
        composable(NavRoutes.Splash.route) {
            SplashScreen(
                onSplashComplete = {
                    navController.navigate(NavRoutes.Login.route) {
                        popUpTo(NavRoutes.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Auth ──
        composable(NavRoutes.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(NavRoutes.Register.route)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(NavRoutes.ForgotPassword.route)
                },
                onLoginSuccess = {
                    navController.navigate(NavRoutes.AppLoading.route) {
                        popUpTo(NavRoutes.Login.route) { inclusive = true }
                    }
                },
                onNeedsCurrencySetup = {
                    navController.navigate(NavRoutes.SetupCurrency.route) {
                        popUpTo(NavRoutes.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.Register.route) {
            RegisterScreen(
                onNavigateBack = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(NavRoutes.SetupCurrency.route) {
                        popUpTo(NavRoutes.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.ForgotPassword.route) {
            ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.SetupCurrency.route) {
            SetupCurrencyScreen(
                onCurrencySet = {
                    navController.navigate(NavRoutes.AppLoading.route) {
                        popUpTo(NavRoutes.SetupCurrency.route) { inclusive = true }
                    }
                }
            )
        }

        // ── App Loading ──
        composable(NavRoutes.AppLoading.route) {
            AppLoadingScreen(
                onLoadingComplete = { destination ->
                    val route = when (destination) {
                        AppLoadingDestination.DASHBOARD -> NavRoutes.Dashboard.route
                        AppLoadingDestination.SETUP_CURRENCY -> NavRoutes.SetupCurrency.route
                    }
                    navController.navigate(route) {
                        popUpTo(NavRoutes.AppLoading.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Main App Content ──
        composable(NavRoutes.Dashboard.route) {
            com.casha.app.ui.feature.main.MainScreen(parentNavController = navController)
        }
    }
}
