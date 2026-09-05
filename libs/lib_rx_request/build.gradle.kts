plugins {
    alias(libs.plugins.nowinandroid.android.library)
}

android {
    namespace = "com.example.william.my.core.rx.request"

}

dependencies {
    api(projects.libs.libRetrofitRx)
    implementation(libs.rxlifecycle)
}
