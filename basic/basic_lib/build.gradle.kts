plugins {
    alias(libs.plugins.nowinandroid.android.library)
    alias(libs.plugins.nowinandroid.android.arouter)
    alias(libs.plugins.nowinandroid.android.eventbus)
    alias(libs.plugins.nowinandroid.android.hilt)
}

android {
    namespace = "com.example.william.my.core.base"
    resourcePrefix("base_")
    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    //Utils
    implementation(libs.utils)
    //Permission
    implementation(libs.permission)
    //ImmersionBar
    implementation(libs.immersionbar)
    implementation(libs.immersionbar.ktx)
    //LifecycleProvider
    implementation(libs.rxlifecycle)
}