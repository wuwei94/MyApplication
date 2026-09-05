plugins {
    alias(libs.plugins.nowinandroid.android.library)
    alias(libs.plugins.nowinandroid.android.arouter)
    alias(libs.plugins.nowinandroid.android.room)
    alias(libs.plugins.nowinandroid.android.objectbox)
}

android {
    namespace = "com.example.william.my.module.database"
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(projects.basic.basicLib)
    implementation(projects.basic.basicShared)

    implementation(libs.rxandroid)
}
