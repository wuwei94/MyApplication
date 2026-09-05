plugins {
    alias(libs.plugins.nowinandroid.android.library)
    // Compose 基础能力（由 convention 插件自动配置 Kotlin 2.0 Compose 编译器、BOM、UI 核心库与稳定性配置）
    alias(libs.plugins.nowinandroid.android.library.compose)
    alias(libs.plugins.nowinandroid.android.arouter)
    alias(libs.plugins.nowinandroid.android.eventbus)
}

android {
    namespace = "com.example.william.my.module.compose"
}

dependencies {
    implementation(projects.basic.basicLib)
    implementation(projects.basic.basicShared)

    // Compose 下拉刷新扩展组件
    implementation(libs.smartrefresh.compose)
}
