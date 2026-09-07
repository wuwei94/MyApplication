plugins {
    alias(libs.plugins.nowinandroid.android.library)
}

android {
    namespace = "com.example.william.my.basic.basic_sync"
    resourcePrefix("sync_")
}

dependencies {
    implementation(projects.basic.basicLib)
    implementation(projects.basic.basicShared)
    api(projects.basic.basicRepo)

    api(projects.basic.basicDatastore)

    implementation(libs.androidx.workmanager.ktx)

    testImplementation(libs.kotlinx.coroutines.test)
}
