plugins {
    alias(libs.plugins.nowinandroid.android.library)
    alias(libs.plugins.nowinandroid.android.arouter)
}

android {
    namespace = "com.example.william.my.module.mqtt"
}

dependencies {
    implementation(projects.basic.basicLib)
    implementation(projects.basic.basicShared)

    implementation(projects.libs.libMqtt)
    implementation(projects.libs.libMqttHivemq)
    implementation(projects.libs.libMqttPahoService)
}
