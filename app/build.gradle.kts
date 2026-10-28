plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("kotlinx-serialization")
}

android {
    namespace = "com.domeos.agent"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.domeos.agent"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            isMinifyEnabled = false
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf("-Xopt-in=kotlin.RequiresOptIn")
    }

    buildFeatures {
        compose = true
        viewBinding = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packagingOptions {
        resources.excludes += "META-INF/*.kotlin_module"
    }

    dataBinding {
        enabled = true
    }
}

val versions = mapOf(
    "kotlin" to "2.0.20",
    "coroutines" to "1.8.0",
    "lifecycle" to "2.8.0",
    "room" to "2.6.1",
    "hilt" to "2.52",
    "composeBom" to "2024.06.00",
    "material3" to "1.2.1",
    "activity" to "1.9.0",
    "navigation" to "2.8.0",
    "retrofit" to "2.11.0",
    "okhttp" to "4.12.0",
    "moshi" to "1.15.1",
    "coil" to "2.7.0",
    "markdown" to "1.0.0",
    "math" to "1.0.0",
    "workmanager" to "2.9.0",
    "datastore" to "1.1.1",
    "biometric" to "1.2.0-alpha03"
)

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.activity:activity-compose:$versions.activity")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$versions.lifecycle")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$versions.lifecycle")
    implementation("androidx.navigation:navigation-compose:$versions.navigation")

    // Compose Material 3 Expressive
    val composeBom = platform("androidx.compose:compose-bom:$versions.composeBom")
    implementation(composeBom)
    implementation("androidx.compose.material3:material3:$versions.material3")
    implementation("androidx.compose.material3:material3-window-size-class")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.runtime:runtime-livedata")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.animation:animation-graphics")

    // Material 3 Expressive specific
    implementation("androidx.compose.material3:material3-adaptive")
    implementation("androidx.compose.material3:material3-adaptive-navigation-suite")

    // Hilt Dependency Injection
    implementation("com.google.dagger:hilt-android:$versions.hilt")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    ksp("com.google.dagger:hilt-android-compiler:$versions.hilt")
    ksp("androidx.hilt:hilt-compiler:1.2.0")

    // Room Database
    implementation("androidx.room:room-runtime:$versions.room")
    implementation("androidx.room:room-ktx:$versions.room")
    ksp("androidx.room:room-compiler:$versions.room")

    // DataStore Preferences
    implementation("androidx.datastore:datastore-preferences:$versions.datastore")

    // Networking
    implementation("com.squareup.retrofit2:retrofit:$versions.retrofit")
    implementation("com.squareup.retrofit2:converter-moshi:$versions.retrofit")
    implementation("com.squareup.moshi:moshi-kotlin:$versions.moshi")
    implementation("com.squareup.moshi:moshi-adapters:$versions.moshi")
    ksp("com.squareup.moshi:moshi-kotlin-codegen:$versions.moshi")
    implementation("com.squareup.okhttp3:okhttp:$versions.okhttp")
    implementation("com.squareup.okhttp3:logging-interceptor:$versions.okhttp")

    // Coroutines & Flow
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$versions.coroutines")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:$versions.coroutines")

    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // Image Loading
    implementation("io.coil-kt:coil-compose:$versions.coil")
    implementation("io.coil-kt:coil-network-okhttp:$versions.coil")

    // Markdown Rendering
    implementation("io.github.xinliang28:markdown-compose:1.0.0-beta03")
    implementation("io.noties.markwon:core:4.6.3")
    implementation("io.noties.markwon:html:4.6.3")
    implementation("io.noties.markwon:table:4.6.3")
    implementation("io.noties.markwon:tasklist:4.6.3")

    // Math Formula Rendering (KaTeX via WebView or Compose)
    implementation("io.github.kexanie:math:0.3.3")

    // WorkManager for background tasks
    implementation("androidx.work:work-runtime-ktx:$versions.workmanager")

    // Biometric Auth
    implementation("androidx.biometric:biometric:$versions.biometric")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Foreground Service / System Tray
    implementation("androidx.core:core-splashscreen:1.0.1")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.compose.ui:ui-test-manifest")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-tooling-data")
}