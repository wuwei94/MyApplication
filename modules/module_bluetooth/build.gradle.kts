plugins {
    alias(libs.plugins.nowinandroid.android.library)
    alias(libs.plugins.nowinandroid.android.arouter)
}

android {
    namespace = "com.example.william.my.module.bluetooth"
}

dependencies {
    implementation(projects.basic.basicLib)
    implementation(projects.basic.basicShared)

    implementation(libs.nordic.ble)
    implementation(libs.nordic.ble.ktx)

    implementation(libs.fastble)

    implementation(libs.rxandroidble)
    implementation(libs.rxandroid)
}
