plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
    // Uncomment when google-services.json is added:
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

android {
    namespace = "com.casha.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.casha.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"842221844066-4ompq96p8q1mj2u0a6fuc2kdjjdv4oa2.apps.googleusercontent.com\"")
    }

    signingConfigs {
        create("release") {
            // Placeholder keys for the user to update
            keyAlias = "casha"
            keyPassword = "password"
            storeFile = file("../release.keystore")
            storePassword = "password"
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            buildConfigField("String", "BASE_URL", "\"http://10.168.71.180:3000/\"") // Use 10.0.2.2 for emulator
            buildConfigField("String", "ENVIRONMENT", "\"development\"")
            buildConfigField("String", "LOG_LEVEL", "\"debug\"")
            buildConfigField("Boolean", "ENABLE_ANALYTICS", "false")
            buildConfigField("Boolean", "ENABLE_CRASHLYTICS", "false")
        }
        create("staging") {
            isDebuggable = true
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-staging"
            signingConfig = signingConfigs.getByName("debug")
            buildConfigField("String", "BASE_URL", "\"https://api-staging.casha.com/\"")
            buildConfigField("String", "ENVIRONMENT", "\"staging\"")
            buildConfigField("String", "LOG_LEVEL", "\"info\"")
            buildConfigField("Boolean", "ENABLE_ANALYTICS", "true")
            buildConfigField("Boolean", "ENABLE_CRASHLYTICS", "true")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Production environment settings
            buildConfigField("String", "BASE_URL", "\"http://10.168.71.180:3000/\"")
            buildConfigField("String", "ENVIRONMENT", "\"production\"")
            buildConfigField("String", "LOG_LEVEL", "\"error\"")
            buildConfigField("Boolean", "ENABLE_ANALYTICS", "true")
            buildConfigField("Boolean", "ENABLE_CRASHLYTICS", "true")

            // Fallback to debug signing if release keystore doesn't exist yet, to prevent build failure
            signingConfig = if (file("../release.keystore").exists()) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    debugImplementation(libs.compose.ui.tooling)

    // Navigation
    implementation(libs.navigation.compose)

    // Lifecycle
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.compose)

    // Hilt DI
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Network
    implementation(libs.retrofit)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit.kotlinx.serialization)

    // Room Database
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // DataStore
    implementation(libs.datastore.preferences)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.messaging)

    // Google Auth
    implementation(libs.credentials)
    implementation(libs.credentials.play.services)
    implementation(libs.google.id.identity)

    // Play Billing
    implementation(libs.play.billing)

    // Image Loading
    implementation(libs.coil.compose)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.test.manifest)
}
