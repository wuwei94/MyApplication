plugins {
    alias(libs.plugins.nowinandroid.android.library)
    alias(libs.plugins.nowinandroid.android.arouter)
    alias(libs.plugins.nowinandroid.android.eventbus)
    alias(libs.plugins.nowinandroid.android.protobuf)
    alias(libs.plugins.nowinandroid.android.room)
    alias(libs.plugins.nowinandroid.android.hilt)
}

android {
    namespace = "com.example.william.my.module.jetpack"
    resourcePrefix("jetpack_")
}

dependencies {
    implementation(project(":basic:basic_lib"))
    implementation(project(":basic:basic_module"))

    implementation(project(":basic:basic_data"))
    implementation(project(":basic:basic_repo"))

    //DataStore
    implementation(libs.androidx.datastore.runtime)
    implementation(libs.androidx.datastore.rxjava3)
    implementation(libs.androidx.datastore.preferences.runtime)
    implementation(libs.androidx.datastore.preferences.rxjava3)
    //Room
    //implementation(libs.androidx.room.runtime)
    //implementation(libs.androidx.room.ktx)
    //implementation(libs.androidx.room.rxjava3)
    //implementation(libs.androidx.room.paging)
    //kapt(libs.androidx.room.compiler)
    //paging
    implementation(libs.autodispose)
    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.rxjava3)
    //WorkManager
    implementation(libs.androidx.workmanager.ktx)
}
