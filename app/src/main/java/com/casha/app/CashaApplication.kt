package com.casha.app

import android.app.Application
import androidx.work.Configuration
import com.casha.app.core.config.AppConfig
import com.casha.app.widget.WidgetUpdater
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CashaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppConfig.printEnvironmentInfo()
        WidgetUpdater.startPeriodicRefresh(this)
    }
}
