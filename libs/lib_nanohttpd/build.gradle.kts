plugins {
    alias(libs.plugins.nowinandroid.android.library)
}

android {
    namespace = "com.example.william.my.core.nanohttpd"
}

dependencies {
    // NanoHTTPD
    api(libs.nanohttpd)
}
