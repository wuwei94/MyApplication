plugins {
    alias(libs.plugins.nowinandroid.android.library)
    alias(libs.plugins.nowinandroid.android.hilt)
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
    // api：App 侧 Hilt 聚合与 SyncWorkerFactory 需解析 androidx.hilt.work 类型
    api(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.work.compiler)

    testImplementation(libs.kotlinx.coroutines.test)
}
