plugins {
    alias(libs.plugins.nowinandroid.android.library)
    alias(libs.plugins.nowinandroid.android.arouter)
}

android {
    namespace = "com.example.william.my.module.chart"
    resourcePrefix("chart_")
}

dependencies {
    implementation(projects.basic.basicLib)
    implementation(projects.basic.basicShared)

    // MPAndroidChart 数据可视化图表库（折线 / 柱状 / 饼图 / 雷达）
    implementation(libs.mpandroidchart)
}
