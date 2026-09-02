plugins {
    alias(libs.plugins.nowinandroid.android.library)
}

android {
    namespace = "com.example.william.my.core.okttpsse"
}

dependencies {
    // OkHttp & OkHttp-SSE
    api(libs.okhttp)
    api(libs.okhttp.sse)

    // RxJava — Observable/Disposable
    api(libs.rxandroid)

    // Coroutines Flow — Flow
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.coroutines.android)

    // JSON Parser
    implementation(libs.google.gson)
}
