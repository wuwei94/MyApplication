plugins {
    alias(libs.plugins.nowinandroid.android.library)
    alias(libs.plugins.nowinandroid.android.arouter)
}

android {
    namespace = "com.example.william.my.core.server"
}

dependencies {
    implementation(project(":basic:basic_lib"))
    implementation(project(":basic:basic_shared"))

    // NanoHTTPD
    implementation(project(":libs:lib_nanohttpd"))
}
