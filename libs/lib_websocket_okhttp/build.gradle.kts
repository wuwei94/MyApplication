plugins {
    alias(libs.plugins.nowinandroid.android.library)
}

android {
    namespace = "com.example.william.my.core.okhttpws"
}

dependencies {
    // Okhttp—WebSocket
    api(libs.okhttp)
    // RxJava — Observable/Disposable 属于公开 API 类型
    api(libs.rxandroid)
    // Coroutines Flow — Flow 属于公开 API 类型
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.coroutines.android)
}
