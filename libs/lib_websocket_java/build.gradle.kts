plugins {
    alias(libs.plugins.nowinandroid.android.library)
}

android {
    namespace = "com.example.william.my.core.javaws"
}

dependencies {
    // Java-WebSocket
    api(libs.websocket)
    // RxJava — Observable/Disposable 属于公开 API 类型
    api(libs.rxandroid)
}
