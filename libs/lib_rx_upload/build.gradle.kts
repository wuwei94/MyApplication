plugins {
    alias(libs.plugins.nowinandroid.android.library)
}

android {
    namespace = "com.example.william.my.core.rx.upload"
}

dependencies {
    api(project(":libs:lib_retrofit_rx"))
    implementation(libs.rxlifecycle)
}

