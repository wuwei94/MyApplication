plugins {
    alias(libs.plugins.nowinandroid.android.library)
}

android {
    namespace = "com.example.william.my.core.okttpsse"
}

dependencies {
    // OkHttp 与 OkHttp-SSE
    api(libs.okhttp)
    api(libs.okhttp.sse)

    // RxJava（Observable/Disposable）
    api(libs.rxandroid)

    // 协程 Flow
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.coroutines.android)

    // JSON 解析器
    implementation(libs.google.gson)
}
