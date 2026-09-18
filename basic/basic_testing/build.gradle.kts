plugins {
    alias(libs.plugins.nowinandroid.android.library)
    alias(libs.plugins.nowinandroid.android.hilt)
}

android {
    namespace = "com.example.william.my.basic.basic_testing"
}

dependencies {
    // 测试替身需实现 basic_lib 暴露的 NetworkMonitor 等契约
    api(projects.basic.basicLib)
    api(libs.kotlinx.coroutines.test)
    api(libs.junit)

    // Hilt 测试装配：@TestInstallIn 替换生产 DispatchersModule
    implementation(libs.androidx.hilt.android.testing)
}
