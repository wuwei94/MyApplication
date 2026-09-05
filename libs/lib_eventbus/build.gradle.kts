plugins {
    alias(libs.plugins.nowinandroid.android.library)
}

android {
    namespace = "com.example.william.my.core.eventbus"
    buildFeatures {
        dataBinding = true
    }
}

dependencies {
    // RxEventBus
    api(libs.rxandroid)
    // FlowEventBus & LiveEventBus Lifecycle & Fragment 支持
    api(libs.androidx.fragment.ktx)
    api(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.process)
}
