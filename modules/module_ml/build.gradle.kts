plugins {
    alias(libs.plugins.nowinandroid.android.library)
    alias(libs.plugins.nowinandroid.android.arouter)
}

android {
    namespace = "com.example.william.my.module.ml"
    resourcePrefix("ml_")

    androidResources {
        noCompress += listOf("tflite")
    }
}

dependencies {
    implementation(projects.basic.basicLib)
    implementation(projects.basic.basicShared)

    // LiteRT（原 TensorFlow Lite）端侧推理套件：CompiledModel 运行时 + GPU 后端 + 模型元数据
    implementation(libs.bundles.litert)

    // ASR 的分词器 / 模型元数据解析用 org.json；它在 Android 上是框架内置的（主代码零依赖），
    // 但单测跑的是 android.jar 里的空桩，因此只在测试期补一份真实实现 —— 官方 ASR 样例同样如此。
    testImplementation(libs.json.org)
}
