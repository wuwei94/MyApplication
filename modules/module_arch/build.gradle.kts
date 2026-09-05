plugins {
    alias(libs.plugins.nowinandroid.android.library)
    alias(libs.plugins.nowinandroid.android.library.compose)
    alias(libs.plugins.nowinandroid.android.arouter)
    alias(libs.plugins.nowinandroid.android.eventbus)
    alias(libs.plugins.nowinandroid.android.hilt)
}

android {
    namespace = "com.example.william.my.module.arch"
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(projects.basic.basicLib)
    implementation(projects.basic.basicShared)

    implementation(projects.basic.basicRepo)

    implementation(libs.mavericks)
    implementation(libs.smartrefresh.compose)
}
