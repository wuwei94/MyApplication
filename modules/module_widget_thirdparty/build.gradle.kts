plugins {
    alias(libs.plugins.nowinandroid.android.library)
    alias(libs.plugins.nowinandroid.android.arouter)
    alias(libs.plugins.nowinandroid.android.hilt)
}

android {
    namespace = "com.example.william.my.module.widget_thirdparty"
}

dependencies {
    implementation(project(":basic:basic_lib"))
    implementation(project(":basic:basic_shared"))

    // 第三方 UI 控件库
    implementation(libs.banner)
    implementation(libs.countdownview)
    implementation(libs.easyfloat)
    implementation(libs.photoview)
    implementation(libs.shadowlayout)
    implementation(libs.swipelayout)
    implementation(libs.blurview)

    // 第三方选择器 / 多媒体选择控件
    implementation(libs.citypicker)
    implementation(libs.pickerview)
    implementation(libs.pictureselector)

    implementation(libs.glide)
}
