plugins {
    alias(libs.plugins.nowinandroid.android.library)
}

android {
    namespace = "com.example.william.my.core.ktorsse"
}

dependencies {
    // Ktor Core 与 OkHttp 引擎（SSE 内置于 ktor-client-core）
    api(libs.ktor.client.core)
    api(libs.ktor.client.okhttp)

    // 协程 Flow
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.coroutines.android)
}
