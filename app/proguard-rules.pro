# Add project specific ProGuard rules here.

# Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.casha.app.**$$serializer { *; }
-keepclassmembers class com.casha.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.casha.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep class * extends androidx.room.Dao
-keep class * extends androidx.room.Entity

# Hilt / Dagger
-keep class dagger.hilt.android.internal.managers.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.HiltWrapper*
-keep class com.casha.app.CashaApplication_HiltComponents** { *; }
-keep @dagger.hilt.android.EntryPoint class *
-keep @dagger.hilt.InstallIn class *

# Keep public fields for JSON mapping if not already covered
-keepclassmembers class com.casha.app.data.remote.dto.** { *; }
