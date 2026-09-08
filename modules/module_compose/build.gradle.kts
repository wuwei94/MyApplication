plugins {
    alias(libs.plugins.nowinandroid.android.library)
    // Compose 基础能力（由 convention 插件自动配置 Kotlin 2.0 Compose 编译器、BOM、UI 核心库与稳定性配置）
    alias(libs.plugins.nowinandroid.android.library.compose)
    alias(libs.plugins.nowinandroid.android.arouter)
    alias(libs.plugins.nowinandroid.android.eventbus)
    // Roborazzi 截图测试（提供 recordRoborazziDemoDebug / verifyRoborazziDemoDebug 任务）
    alias(libs.plugins.roborazzi)
    // Kotlin 官方序列化插件（用于 Navigation 2.8+ 类型安全导航路由模型）
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.example.william.my.module.compose"

    testOptions {
        // 截图测试依赖 Robolectric 在 JVM 上模拟 Android 运行时
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

roborazzi {
    // 基准图随源码入库（build/ 下会被 clean 清掉），便于 PR 中直接 review 像素级差异
    outputDir.set(file("src/roborazzi/screenshots"))
}

dependencies {
    implementation(projects.basic.basicLib)
    implementation(projects.basic.basicShared)

    // Kotlin 官方序列化解析（Navigation 类型安全路由参数解析）
    implementation(libs.kotlinx.serialization.json)

    // Compose 下拉刷新扩展组件
    implementation(libs.smartrefresh.compose)

    // Compose 截图测试：测试源集显式对齐 Compose BOM（convention 只为 main sourceSet 挂 BOM）
    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.bundles.testing.screenshot)
}
