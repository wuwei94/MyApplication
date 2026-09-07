plugins {
    alias(libs.plugins.nowinandroid.android.library)
}

android {
    namespace = "com.example.william.my.basic.basic_repo"
    resourcePrefix("repo_")
}

dependencies {
    implementation(projects.basic.basicLib)
    implementation(projects.basic.basicShared)
    api(projects.basic.basicModel)
    api(projects.basic.basicDatabase)
    api(projects.basic.basicNetwork)

    api(libs.androidx.lifecycle.livedata)
    api(libs.kotlinx.coroutines.rx3)


    testImplementation(libs.kotlinx.coroutines.test)
}
