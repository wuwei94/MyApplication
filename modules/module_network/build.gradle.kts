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
    implementation(project(":basic:basic_server"))

    implementation(project(":basic:basic_repository"))

    implementation(project(":libs:lib_ktor"))
    implementation(project(":libs:lib_volley"))
}
