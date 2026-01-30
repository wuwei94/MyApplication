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
    //RxEventBus
    api(libs.rxandroid)
    //FlowEventBus viewModelScope
    implementation(libs.androidx.lifecycle.runtime)
    //FlowEventBus ProcessLifecycleOwner
    implementation(libs.androidx.lifecycle.process)
}