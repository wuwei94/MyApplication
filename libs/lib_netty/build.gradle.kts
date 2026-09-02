plugins {
    alias(libs.plugins.nowinandroid.android.library)
}

android {
    namespace = "com.example.william.my.core.netty"
    //Netty
    //packaging {
    //    resources.excludes.add("META-INF/INDEX.LIST")
    //    resources.excludes.add("META-INF/io.netty.versions.properties")
    //}
}

dependencies {
    // Netty
    api(libs.netty)
    // SLF4J (Netty logging)
    implementation(libs.slf4j)
    // RxJava
    api(libs.rxandroid)
    // Coroutines Flow — Flow 属于公开 API 类型
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.coroutines.android)
}
