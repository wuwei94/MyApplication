plugins {
    alias(libs.plugins.nowinandroid.android.library)
}

android {
    namespace = "com.example.william.my.core.server"
}

dependencies {
    implementation(project(":basic:basic_lib"))
    implementation(project(":basic:basic_shared"))

    // NanoHTTPD
    implementation(project(":libs:lib_nanohttpd"))

    // WebSocket
    implementation(project(":libs:lib_websocket_java"))
    implementation(project(":libs:lib_netty"))
}
