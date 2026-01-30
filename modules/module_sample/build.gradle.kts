plugins {
    alias(libs.plugins.nowinandroid.android.library)
    alias(libs.plugins.nowinandroid.android.arouter)
    alias(libs.plugins.nowinandroid.android.eventbus)
    alias(libs.plugins.nowinandroid.android.protobuf)
}

android {
    namespace = "com.example.william.my.module.sample"
    //resourcePrefix("sample_")
}

dependencies {
    implementation(project(":basic:basic_lib"))
    implementation(project(":basic:basic_module"))

    implementation(project(":basic:basic_data"))

    //DataStore
    implementation(libs.androidx.datastore.runtime)
    implementation(libs.androidx.datastore.rxjava3)
    implementation(libs.androidx.datastore.preferences.runtime)
    implementation(libs.androidx.datastore.preferences.rxjava3)
    //WorkManager
    implementation(libs.androidx.workmanager.ktx)
}
