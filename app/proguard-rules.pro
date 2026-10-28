# DomeOS Agent ProGuard Rules

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class androidx.hilt.** { *; }

# Keep Room entities and DAOs
-keep class com.domeos.agent.data.model.** { *; }
-keep class com.domeos.agent.data.local.dao.** { *; }
-keep class com.domeos.agent.data.local.DomeOSDatabase { *; }

# Keep Moshi/JSON serialization
-keep class com.squareup.moshi.** { *; }
-keep @kotlinx.serialization.Serializable class * { *; }
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}

# Keep Retrofit/OkHttp
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-keep class okio.** { *; }

# Keep Coroutines
-keep class kotlinx.coroutines.** { *; }

# Keep DataStore
-keep class androidx.datastore.** { *; }

# Keep Biometric
-keep class androidx.biometric.** { *; }

# Keep WorkManager
-keep class androidx.work.** { *; }

# Keep Coil
-keep class coil3.** { *; }
-keep class io.coil-kt.** { *; }

# Keep Markdown libraries
-keep class io.noties.markwon.** { *; }
-keep class io.github.xinliang28.markdown.** { *; }

# Keep Material 3
-keep class androidx.compose.material3.** { *; }
-keep class androidx.compose.material.** { *; }

# Keep Compose runtime
-keep class androidx.compose.runtime.** { *; }

# Keep Navigation
-keep class androidx.navigation.** { *; }

# Keep Lifecycle/ViewModel
-keep class androidx.lifecycle.** { *; }

# Prevent obfuscation of resource names
-keepclassmembers class **.R$* {
    public static <fields>;
}

# Keep parcelable creators
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep enum values
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Kotlin metadata
-keep class kotlin.Metadata { *; }

# Keep inline classes
-keep class * implements kotlin.jvm.internal.InlineClass { *; }