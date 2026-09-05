plugins {
    alias(libs.plugins.nowinandroid.android.library)
    alias(libs.plugins.nowinandroid.android.arouter)
}

android {
    namespace = "com.example.william.my.core.server"
}

dependencies {
    implementation(projects.basic.basicLib)
    implementation(projects.basic.basicShared)

    // NanoHTTPD
    implementation(projects.libs.libNanohttpd)

    // WebSocket
    implementation(projects.libs.libWebsocketJava)
    implementation(projects.libs.libNetty)
}
