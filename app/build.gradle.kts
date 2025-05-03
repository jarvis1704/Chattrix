plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.androidx.navigation.safe.args)  // Use this instead of "androidx.navigation.safe.args"
    id("kotlin-kapt")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("kotlin-parcelize")
    id("com.google.dagger.hilt.android")
}
android {
    namespace = "com.biprangshu.chattrix"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.biprangshu.chattrix"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}



dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.firebase.database)
    implementation(libs.firebase.firestore)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation ("com.google.android.gms:play-services-auth:21.3.0")

    // Dagger Hilt
    implementation("com.google.dagger:hilt-android:2.51")
    kapt("com.google.dagger:hilt-compiler:2.51")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Kotlin Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // Kotlin Parcelize
    implementation("org.jetbrains.kotlin:kotlin-parcelize-runtime:1.9.20")

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.0-alpha01")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.0-alpha01")
    kapt("androidx.lifecycle:lifecycle-compiler:2.8.0-alpha01")

    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
    // For Compose navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")


    implementation("com.airbnb.android:lottie:6.6.4")
    implementation ("com.airbnb.android:lottie-compose:6.6.4")
}