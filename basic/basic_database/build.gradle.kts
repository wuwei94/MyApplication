plugins {
    alias(libs.plugins.nowinandroid.android.library)
    alias(libs.plugins.nowinandroid.android.room)
}

android {
    namespace = "com.example.william.my.basic.basic_database"
    resourcePrefix("database_")
}

dependencies {
    implementation(projects.basic.basicLib)
    implementation(projects.basic.basicShared)
    api(projects.basic.basicModel)
    api(libs.androidx.room)
    api(libs.androidx.room.ktx)

    testImplementation(libs.kotlinx.coroutines.test)
}
