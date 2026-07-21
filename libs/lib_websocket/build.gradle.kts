plugins {
    alias(libs.plugins.nowinandroid.android.library)
}

android {
    namespace = "com.example.william.my.core.websocket"
}

dependencies {
    // okhttp — WebSocket 类型属于公开 API
    api(libs.okhttp)
    // rxjava — Observable/Disposable 属于公开 API 类型
    api(libs.rxandroid)
}
