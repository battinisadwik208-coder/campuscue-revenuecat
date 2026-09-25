plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.battinisadwik.campuscue"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.battinisadwik.campuscue"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        val revenueCatKey = project.findProperty("REVENUECAT_PUBLIC_SDK_KEY")?.toString()
            ?: "REPLACE_WITH_REVENUECAT_PUBLIC_SDK_KEY"
        buildConfigField("String", "REVENUECAT_PUBLIC_SDK_KEY", "\"$revenueCatKey\"")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.10.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("com.revenuecat.purchases:purchases:10.15.1")
    implementation("com.revenuecat.purchases:purchases-ui:10.15.1")
}
