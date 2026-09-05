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

    // TensorFlow Lite
    implementation(libs.tensorflow.lite)
    implementation(libs.tensorflow.lite.gpu)
    implementation(libs.tensorflow.lite.gpu.api)
}
