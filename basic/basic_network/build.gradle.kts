plugins {
    alias(libs.plugins.nowinandroid.android.library)
}

android {
    namespace = "com.example.william.my.basic.basic_network"
    resourcePrefix("network_")
}

dependencies {
    implementation(projects.basic.basicLib)
    implementation(projects.basic.basicShared)
    api(projects.basic.basicModel)

    api(projects.libs.libOkhttp)
    api(projects.libs.libRetrofit)
    api(projects.libs.libRetrofitRx)

    testImplementation(libs.kotlinx.coroutines.test)
}
