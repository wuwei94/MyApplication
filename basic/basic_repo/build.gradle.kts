plugins {
    alias(libs.plugins.nowinandroid.android.library)
    alias(libs.plugins.nowinandroid.android.room)
}

android {
    namespace = "com.example.william.my.basic.basic_repo"
    resourcePrefix("repo_")
}

dependencies {
    implementation(projects.basic.basicLib)
    implementation(projects.basic.basicShared)

    api(projects.libs.libOkhttp)
    api(projects.libs.libRetrofit)
    api(projects.libs.libRetrofitRx)

    api(libs.androidx.lifecycle.livedata)
    api(libs.kotlinx.coroutines.rx3)
}
