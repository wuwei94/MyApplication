plugins {
    alias(libs.plugins.nowinandroid.android.library)
    alias(libs.plugins.nowinandroid.android.arouter)
    alias(libs.plugins.nowinandroid.android.hilt)
}

android {
    namespace = "com.example.william.my.module.di"
}

dependencies {
    implementation(projects.basic.basicLib)
    implementation(projects.basic.basicShared)

    implementation(libs.koin.android)
}
