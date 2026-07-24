plugins {
    alias(libs.plugins.nowinandroid.android.library)
    alias(libs.plugins.nowinandroid.android.arouter)
    alias(libs.plugins.nowinandroid.android.eventbus)
}

android {
    namespace = "com.example.william.my.module.websocket"
}

dependencies {
    implementation(project(":basic:basic_lib"))
    implementation(project(":basic:basic_shared"))
    implementation(project(":basic:basic_server"))

    implementation(project(":libs:lib_websocket_java"))
    implementation(project(":libs:lib_websocket_okhttp"))
    implementation(project(":libs:lib_netty"))
}
