plugins {
    alias(libs.plugins.nowinandroid.android.library)
}

android {
    namespace = "com.example.william.my.core.rx.request"
    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }
}

dependencies {
    api(project(":libs:lib_retrofit_rx"))
    implementation(libs.rxlifecycle)

    testImplementation(libs.okhttp.mockwebserver)
}
