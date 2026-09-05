plugins {
    alias(libs.plugins.nowinandroid.android.library)
    alias(libs.plugins.nowinandroid.android.arouter)
    alias(libs.plugins.nowinandroid.android.protobuf)
    alias(libs.plugins.nowinandroid.android.room)
}

android {
    namespace = "com.example.william.my.module.jetpack"
}

dependencies {
    implementation(projects.basic.basicLib)
    implementation(projects.basic.basicShared)

    implementation(projects.basic.basicRepo)

    //lifecycle
    implementation(libs.androidx.lifecycle.process)

    //paging
    implementation(libs.autodispose)
    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.rxjava3)
}
