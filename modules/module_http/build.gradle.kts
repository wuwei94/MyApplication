plugins {
    alias(libs.plugins.nowinandroid.android.library)
    alias(libs.plugins.nowinandroid.android.arouter)
    alias(libs.plugins.nowinandroid.android.eventbus)
}

android {
    namespace = "com.example.william.my.module.http"
}

dependencies {
    implementation(project(":basic:basic_lib"))
    implementation(project(":basic:basic_shared"))
    implementation(project(":basic:basic_server"))
    implementation(project(":basic:basic_repo"))

    implementation(project(":libs:lib_httpurl"))
    implementation(project(":libs:lib_volley"))
    implementation(project(":libs:lib_okhttp"))
    implementation(project(":libs:lib_retrofit"))
    implementation(project(":libs:lib_retrofit_rx"))
    implementation(project(":libs:lib_rx_request"))
    implementation(project(":libs:lib_rx_download"))
    implementation(project(":libs:lib_rx_upload"))
    implementation(project(":libs:lib_ktor"))
}
