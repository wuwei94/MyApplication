plugins {
    alias(libs.plugins.nowinandroid.android.library)
    alias(libs.plugins.nowinandroid.android.arouter)
    alias(libs.plugins.nowinandroid.android.eventbus)
}

android {
    namespace = "com.example.william.my.module.performance"
    resourcePrefix("performance_")
}

dependencies {
    implementation(projects.basic.basicLib)
    implementation(projects.basic.basicShared)

    implementation(libs.androidx.asyncLayoutInflater)

    // Startup & Baseline Profiles
    implementation(libs.androidx.startup)
    implementation(libs.androidx.profileinstaller)

    // 运行时性能监控闭环：JankStats 卡顿帧采集 + 自定义系统追踪
    implementation(libs.androidx.metrics.performance)
    implementation(libs.androidx.tracing)
}
