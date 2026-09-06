plugins {
    alias(libs.plugins.nowinandroid.android.library)
}

android {
    namespace = "com.example.william.my.core.volley"
}

dependencies {
    // Gson
    api(libs.google.gson)
    // Volley
    api(libs.volley)
    // OkHttp3
    api(libs.okhttp)
    // OkHttp3 日志拦截器
    api(libs.okhttp.logging)
}
