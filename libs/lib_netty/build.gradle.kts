plugins {
    alias(libs.plugins.nowinandroid.android.library)
}

android {
    namespace = "com.example.william.my.core.netty"
}

dependencies {
    // Netty 网络通信框架核心库
    api(libs.netty)
    // SLF4J (Netty 内部日志门面)
    implementation(libs.slf4j)
    // 响应式扩展 RxJava 3
    api(libs.rxandroid)
    // Kotlin 协程与 Flow（Flow 属于公共 API 暴露类型）
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.coroutines.android)
}
