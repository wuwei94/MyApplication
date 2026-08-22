plugins {
    alias(libs.plugins.nowinandroid.android.library)
    alias(libs.plugins.nowinandroid.android.arouter)
    alias(libs.plugins.nowinandroid.android.protobuf)
    alias(libs.plugins.nowinandroid.android.room)
    alias(libs.plugins.nowinandroid.android.objectbox)
}

android {
    namespace = "com.example.william.my.module.storage"
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(project(":basic:basic_lib"))
    implementation(project(":basic:basic_shared"))

    //Room
    implementation(libs.rxandroid)

    //DataStore
    implementation(libs.androidx.datastore.runtime)
    implementation(libs.androidx.datastore.rxjava3)
    implementation(libs.androidx.datastore.preferences.runtime)
    implementation(libs.androidx.datastore.preferences.rxjava3)

    //MMKV
    implementation(libs.mmkv)
}
