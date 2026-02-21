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

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var authManager: AuthManager
    @Inject lateinit var deleteAllLocalDataUseCase: DeleteAllLocalDataUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CashaTheme {
                CashaNavHost(
                    authManager = authManager,
                    deleteAllLocalDataUseCase = deleteAllLocalDataUseCase
                )
            }
        }
    }
}
