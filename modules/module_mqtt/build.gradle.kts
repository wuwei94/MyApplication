plugins {
    alias(libs.plugins.nowinandroid.android.library)
    alias(libs.plugins.nowinandroid.android.arouter)
}

android {
    namespace = "com.example.william.my.module.mqtt"
}

dependencies {
    implementation(project(":basic:basic_lib"))
    implementation(project(":basic:basic_shared"))

    implementation(project(":libs:lib_mqtt"))
    implementation(project(":libs:lib_mqtt_hivemq"))
    implementation(project(":libs:lib_mqtt_paho_service"))
}
