plugins {
    alias(libs.plugins.nowinandroid.android.library)
}

android {
    namespace = "com.example.william.my.core.okhttp"

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    // core-ktx extension
    implementation(libs.androidx.core.ktx)
    implementation(libs.google.gson)
    api(libs.okhttp)
    api(libs.okhttp.logging)
}
