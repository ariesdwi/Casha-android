package com.casha.app.core.config

import com.casha.app.BuildConfig

/**
 * Application configuration driven by build variants.
 *
 * Build types set BASE_URL, ENVIRONMENT, LOG_LEVEL, etc. via `buildConfigField`
 * in `app/build.gradle.kts`. This object provides typed access to those values.
 *
 * Equivalent to iOS `AppConfig.swift`.
 */
object AppConfig {

    val appName: String = "Casha"
    val baseUrl: String = BuildConfig.BASE_URL
    val environment: Environment = Environment.from(BuildConfig.ENVIRONMENT)
    val isDebug: Boolean = BuildConfig.DEBUG
    val logLevel: LogLevel = LogLevel.from(BuildConfig.LOG_LEVEL)
    val isAnalyticsEnabled: Boolean = BuildConfig.ENABLE_ANALYTICS
    val isCrashlyticsEnabled: Boolean = BuildConfig.ENABLE_CRASHLYTICS

    val appVersion: String = BuildConfig.VERSION_NAME
    val buildNumber: Int = BuildConfig.VERSION_CODE
    val applicationId: String = BuildConfig.APPLICATION_ID

    val displayName: String
        get() = when (environment) {
            Environment.DEVELOPMENT -> "$appName Dev"
            Environment.STAGING -> "$appName Staging"
            Environment.PRODUCTION -> appName
        }

    val configurationName: String
        get() = when (environment) {
            Environment.DEVELOPMENT -> "Debug"
            Environment.STAGING -> "Staging"
            Environment.PRODUCTION -> "Production"
        }

    val shouldLogNetworkRequests: Boolean
        get() = environment != Environment.PRODUCTION

    val shouldShowDebugMenu: Boolean
        get() = isDebug

    val apiTimeout: Long
        get() = when (environment) {
            Environment.DEVELOPMENT -> 30L
            Environment.STAGING -> 25L
            Environment.PRODUCTION -> 20L
        }

    // ── Logging ──

    fun log(message: String, level: LogLevel = LogLevel.DEBUG) {
        if (level.ordinal < logLevel.ordinal) return

        val tag = "Casha-${environment.shortName}"
        when (level) {
            LogLevel.DEBUG -> android.util.Log.d(tag, message)
            LogLevel.INFO -> android.util.Log.i(tag, message)
            LogLevel.WARNING -> android.util.Log.w(tag, message)
            LogLevel.ERROR -> android.util.Log.e(tag, message)
        }
    }

    fun printEnvironmentInfo() {
        log("=== Application Environment ===", LogLevel.INFO)
        log("App: $displayName", LogLevel.INFO)
        log("Version: $appVersion ($buildNumber)", LogLevel.INFO)
        log("Environment: ${environment.displayName}", LogLevel.INFO)
        log("Bundle: $applicationId", LogLevel.INFO)
        log("Base URL: $baseUrl", LogLevel.INFO)
        log("Analytics: ${if (isAnalyticsEnabled) "Enabled" else "Disabled"}", LogLevel.INFO)
        log("Crashlytics: ${if (isCrashlyticsEnabled) "Enabled" else "Disabled"}", LogLevel.INFO)
        log("Debug Features: ${if (isDebug) "Enabled" else "Disabled"}", LogLevel.INFO)
        log("===============================", LogLevel.INFO)
    }

    val debugInformation: Map<String, String>
        get() = mapOf(
            "App Name" to displayName,
            "Version" to "$appVersion ($buildNumber)",
            "Environment" to environment.displayName,
            "Bundle ID" to applicationId,
            "Base URL" to baseUrl,
            "Configuration" to configurationName,
            "Firebase Analytics" to if (isAnalyticsEnabled) "Enabled" else "Disabled",
            "Crashlytics" to if (isCrashlyticsEnabled) "Enabled" else "Disabled",
            "Debug Mode" to if (isDebug) "Yes" else "No"
        )

    // ── Enums ──

    enum class Environment(val displayName: String, val shortName: String, val badgeText: String) {
        DEVELOPMENT("Development", "DEV", "DEV"),
        STAGING("Staging", "STG", "STG"),
        PRODUCTION("Production", "PROD", "");

        companion object {
            fun from(value: String): Environment = when (value.lowercase()) {
                "development" -> DEVELOPMENT
                "staging" -> STAGING
                "production" -> PRODUCTION
                else -> PRODUCTION
            }
        }
    }

    enum class LogLevel {
        DEBUG, INFO, WARNING, ERROR;

        companion object {
            fun from(value: String): LogLevel = when (value.lowercase()) {
                "debug" -> DEBUG
                "info" -> INFO
                "warning" -> WARNING
                "error" -> ERROR
                else -> WARNING
            }
        }
    }
}
