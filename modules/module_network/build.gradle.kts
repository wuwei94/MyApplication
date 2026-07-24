plugins {
    alias(libs.plugins.nowinandroid.android.library)
    alias(libs.plugins.nowinandroid.android.arouter)
    alias(libs.plugins.nowinandroid.android.eventbus)
}

android {
    namespace = "com.example.william.my.module.network"
}

dependencies {
    implementation(project(":basic:basic_lib"))
    implementation(project(":basic:basic_shared"))

    implementation(project(":basic:basic_repository"))

    implementation(libs.slf4j)
    implementation(project(":libs:lib_nanohttpd"))
    implementation(project(":libs:lib_ktor"))
    implementation(project(":libs:lib_volley"))
    implementation(project(":libs:lib_okhttp"))
    implementation(project(":libs:lib_retrofit"))
    implementation(project(":libs:lib_download"))
}
