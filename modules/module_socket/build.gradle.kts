plugins {
    alias(libs.plugins.nowinandroid.android.library)
    alias(libs.plugins.nowinandroid.android.arouter)
    alias(libs.plugins.nowinandroid.android.eventbus)
}

android {
    namespace = "com.example.william.my.module.socket"
}

dependencies {
    implementation(projects.basic.basicLib)
    implementation(projects.basic.basicShared)

    implementation(projects.libs.libWebsocketJava)
    implementation(projects.libs.libWebsocketOkhttp)
    implementation(projects.libs.libNetty)
}
