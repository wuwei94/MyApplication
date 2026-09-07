plugins {
    alias(libs.plugins.nowinandroid.android.library)
}

android {
    namespace = "com.example.william.my.basic.basic_datastore"
    resourcePrefix("datastore_")
}

dependencies {
    implementation(projects.basic.basicLib)
    implementation(projects.basic.basicShared)
    api(projects.basic.basicModel)

    api(libs.mmkv)

    testImplementation(libs.kotlinx.coroutines.test)
}
