package com.casha.app

import android.app.Application
import com.casha.app.core.config.AppConfig
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CashaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppConfig.printEnvironmentInfo()
    }
}
