plugins {
    alias(libs.plugins.nowinandroid.android.library)
    alias(libs.plugins.nowinandroid.android.arouter)
}

android {
    namespace = "com.example.william.my.module.reactive"
}

dependencies {
    implementation(projects.basic.basicLib)
    implementation(projects.basic.basicShared)

    // 响应式编程：Kotlin Flow
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.lifecycle.runtime)
    // 响应式编程：RxJava 3
    implementation(libs.rxandroid)

    // 单元测试：把 Observable 转成 Flow 以复用 Turbine 断言（kotlinx-coroutines-rx3 互操作）
    testImplementation(libs.kotlinx.coroutines.rx3)
    // Turbine Flow 断言 + 协程测试（runTest / TestDispatcher）
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
}
