plugins {
    alias(libs.plugins.nowinandroid.android.library)
}

android {
    namespace = "com.example.william.my.core.rx.upload"
}

dependencies {
    api(projects.libs.libRetrofitRx)
    implementation(libs.rxlifecycle)
}
