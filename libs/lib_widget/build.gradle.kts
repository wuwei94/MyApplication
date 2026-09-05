plugins {
    alias(libs.plugins.nowinandroid.android.library)
}

android {
    namespace = "com.example.william.my.core.widget"
    buildFeatures {
        dataBinding = true
    }
}

dependencies {
    // Android UI 基础组件
    api(libs.androidx.appCompat)
    api(libs.google.material)
    api(libs.androidx.recyclerView)

    // Utils
    implementation(libs.utils)
    // PAG
    implementation(libs.pag)
}
