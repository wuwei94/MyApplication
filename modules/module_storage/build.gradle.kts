plugins {
    alias(libs.plugins.nowinandroid.android.library)
    alias(libs.plugins.nowinandroid.android.arouter)
    alias(libs.plugins.nowinandroid.android.protobuf)
}

android {
    namespace = "com.example.william.my.module.storage"
}

dependencies {
    implementation(projects.basic.basicLib)
    implementation(projects.basic.basicShared)

    // DataStore
    implementation(libs.androidx.datastore.runtime)
    implementation(libs.androidx.datastore.rxjava3)
    implementation(libs.androidx.datastore.preferences.runtime)
    implementation(libs.androidx.datastore.preferences.rxjava3)

    // MMKV
    implementation(libs.mmkv)
}
