plugins {
    alias(libs.plugins.nowinandroid.android.library)
    alias(libs.plugins.nowinandroid.android.arouter)
    alias(libs.plugins.nowinandroid.android.eventbus)
}

android {
    namespace = "com.example.william.my.module.http"
}

dependencies {
    implementation(projects.basic.basicLib)
    implementation(projects.basic.basicShared)
    implementation(projects.basic.basicServer)
    implementation(projects.basic.basicRepo)

    implementation(projects.libs.libHttpurl)
    implementation(projects.libs.libVolley)
    implementation(projects.libs.libOkhttp)
    implementation(projects.libs.libRetrofit)
    implementation(projects.libs.libRetrofitRx)
    implementation(projects.libs.libRxRequest)
    implementation(projects.libs.libRxDownload)
    implementation(projects.libs.libRxUpload)
    implementation(projects.libs.libKtor)
}
