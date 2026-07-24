plugins {
    alias(libs.plugins.nowinandroid.android.library)
}

android {
    namespace = "com.example.william.my.core.volley"
}

dependencies {
    // Gson
    api(libs.google.gson)
    // Volley
    api(libs.volley)
    // OkHttp3
    api(libs.okhttp)
    // OkHttp3 Logging
    api(libs.okhttp.logging)
}