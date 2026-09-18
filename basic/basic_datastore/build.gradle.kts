plugins {
    alias(libs.plugins.nowinandroid.android.library)
    alias(libs.plugins.nowinandroid.android.protobuf)
}

android {
    namespace = "com.example.william.my.basic.basic_datastore"
    resourcePrefix("datastore_")
}

dependencies {
    implementation(projects.basic.basicLib)
    implementation(projects.basic.basicShared)
    api(projects.basic.basicModel)

    implementation(libs.androidx.datastore.runtime)

    testImplementation(libs.kotlinx.coroutines.test)
}
