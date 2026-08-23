plugins {
    alias(libs.plugins.nowinandroid.android.library)
    alias(libs.plugins.nowinandroid.android.arouter)
    alias(libs.plugins.nowinandroid.android.hilt)
}

android {
    namespace = "com.example.william.my.module.mavericks"
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(project(":basic:basic_lib"))
    implementation(project(":basic:basic_shared"))

    implementation(project(":basic:basic_repo"))

    // Mavericks 文章示例复用 module_arch 中的通用文章适配器
    implementation(project(":modules:module_arch"))

    implementation(libs.mavericks)
}
