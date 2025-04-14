plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.google.gms.google.services) apply false
    alias(libs.plugins.androidx.navigation.safe.args) apply false  // Use this instead of direct reference
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.20" apply false
    id("com.google.dagger.hilt.android") version "2.51" apply false
}