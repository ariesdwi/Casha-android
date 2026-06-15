package com.casha.app

import android.app.Application
import androidx.work.Configuration
import com.casha.app.core.config.AppConfig
import com.casha.app.widget.WidgetUpdater
import com.casha.app.widget.WidgetUpdateCoordinator
import com.casha.app.widget.WidgetUpdateDispatcher
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class CashaApplication : Application() {
    
    // Application-scoped coroutine for widget updates
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    override fun onCreate() {
        super.onCreate()
        AppConfig.printEnvironmentInfo()
        
        // Start periodic widget refresh worker
        WidgetUpdater.startPeriodicRefresh(this)
        
        // Initialize event-driven widget update system
        initializeWidgetUpdateListener()
    }
    
    /**
     * Initialize the widget update event listener.
     * This listens for widget update events and dispatches updates accordingly.
     */
    private fun initializeWidgetUpdateListener() {
        val dispatcher = WidgetUpdateDispatcher(this)
        
        applicationScope.launch {
            WidgetUpdateCoordinator.updateEvents.collect { event ->
                // Handle widget update events in background
                launch(Dispatchers.IO) {
                    dispatcher.updateWidgets(event)
                }
            }
        }
    }
}
