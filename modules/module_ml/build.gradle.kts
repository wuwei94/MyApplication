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

    // LiteRT / TensorFlow Lite 端侧推理与 GPU 加速套件
    implementation(libs.bundles.tensorflow.lite)
}
