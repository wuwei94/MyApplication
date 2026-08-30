plugins {
    alias(libs.plugins.nowinandroid.android.library)
    alias(libs.plugins.nowinandroid.android.arouter)
}

android {
    namespace = "com.example.william.my.module.bluetooth"
}

dependencies {
    implementation(project(":basic:basic_lib"))
    implementation(project(":basic:basic_shared"))

    implementation(libs.nordic.ble)
    implementation(libs.nordic.ble.ktx)

    implementation(libs.fastble)

    implementation(libs.rxandroidble)
    implementation(libs.rxandroid)
}
