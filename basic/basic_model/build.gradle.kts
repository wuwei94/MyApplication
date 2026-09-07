plugins {
    alias(libs.plugins.nowinandroid.jvm.library)
}

dependencies {
    // 纯 Kotlin 模块，提供全工程共享的领域模型与数据契约，零 Android SDK 开销
}
