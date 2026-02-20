package com.casha.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.casha.app.core.auth.AuthManager
import com.casha.app.domain.usecase.auth.ResetPasswordUseCase
import com.casha.app.domain.usecase.auth.UpdateProfileUseCase
import com.casha.app.ui.feature.auth.ForgotPasswordScreen
import com.casha.app.ui.feature.auth.LoginScreen
import com.casha.app.ui.feature.auth.RegisterScreen
import com.casha.app.ui.feature.auth.SetupCurrencyScreen

/**
 * Root navigation host for the Casha app.
 * TODO: Replace remaining placeholder composables with actual screens as modules are implemented.
 */
@Composable
fun CashaNavHost(
    authManager: AuthManager,
    resetPasswordUseCase: ResetPasswordUseCase,
    updateProfileUseCase: UpdateProfileUseCase
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = NavRoutes.Login.route
    ) {
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
                    navController.navigate(NavRoutes.Dashboard.route) {
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
                resetPasswordUseCase = resetPasswordUseCase,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.SetupCurrency.route) {
            SetupCurrencyScreen(
                authManager = authManager,
                updateProfileUseCase = updateProfileUseCase,
                onCurrencySet = {
                    navController.navigate(NavRoutes.Dashboard.route) {
                        popUpTo(NavRoutes.SetupCurrency.route) { inclusive = true }
                    }
                }
            )
        }

        // ── App Loading ──
        composable(NavRoutes.AppLoading.route) {
            PlaceholderScreen("Loading...")
        }

        // ── Main App Content ──
        composable(NavRoutes.Dashboard.route) {
            com.casha.app.ui.feature.main.MainScreen(parentNavController = navController)
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
