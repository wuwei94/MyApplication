plugins {
    alias(libs.plugins.nowinandroid.android.library)
    alias(libs.plugins.nowinandroid.android.arouter)
    alias(libs.plugins.nowinandroid.android.eventbus)
}

android {
    namespace = "com.example.william.my.module.rxretrofit"
}

dependencies {
    implementation(project(":basic:basic_lib"))
    implementation(project(":basic:basic_shared"))

    implementation(project(":libs:lib_rx_request"))
    implementation(project(":libs:lib_rx_download"))
    implementation(project(":libs:lib_rx_upload"))
}
