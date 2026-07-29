plugins {
    alias(libs.plugins.nowinandroid.android.library)
}

android {
    namespace = "com.example.william.my.core.retrofit.rx"
}

dependencies {
    api(project(":libs:lib_retrofit"))
    api(libs.retrofit.adapter.rxjava3)
    api(libs.rxandroid)
    implementation(libs.rxlifecycle)
}
