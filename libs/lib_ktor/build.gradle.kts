plugins {
    alias(libs.plugins.nowinandroid.android.library)
}

android {
    namespace = "com.example.william.my.core.ktor"
}

dependencies {
    // 核心依赖
    api(libs.ktor.client.core)
    api(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.negotiation)

    // 功能模块
    api(libs.ktor.client.logging)
    api(libs.ktor.serialization.gson)

    testImplementation(libs.okhttp.mockwebserver)
}
