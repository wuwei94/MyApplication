plugins {
    alias(libs.plugins.nowinandroid.android.library)
}

android {
    namespace = "com.example.william.my.core.ktorsse"
}

dependencies {
    // Ktor Core & OkHttp Engine (SSE is built into ktor-client-core)
    api(libs.ktor.client.core)
    api(libs.ktor.client.okhttp)

    // Coroutines Flow — Flow
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.coroutines.android)
}
