plugins {
    alias(libs.plugins.nowinandroid.android.library)
    alias(libs.plugins.nowinandroid.android.arouter)
    alias(libs.plugins.nowinandroid.android.eventbus)
    alias(libs.plugins.nowinandroid.android.hilt)
}

android {
    namespace = "com.example.william.my.core.base"
    resourcePrefix("base_")

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    // AndroidX 核心组件
    api(libs.androidx.core.ktx)
    api(libs.androidx.activity.ktx)
    api(libs.androidx.fragment.ktx)
    api(libs.androidx.appCompat)
    api(libs.androidx.constraintLayout)
    api(libs.androidx.recyclerView)
    api(libs.androidx.viewPager2)
    api(libs.google.material)

    // UI 扩展库
    api(libs.brvah)
    api(libs.bundles.smartrefresh)

    // 通用工具与 Rx
    api(libs.utils)
    api(libs.google.gson)
    api(libs.google.guava)
    api(libs.rxlifecycle)
}