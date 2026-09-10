plugins {
    alias(libs.plugins.nowinandroid.android.library)
    alias(libs.plugins.nowinandroid.android.arouter)
}

android {
    namespace = "com.example.william.my.module.gpuimage"
    resourcePrefix("gpuimage_")

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(projects.basic.basicLib)
    implementation(projects.basic.basicShared)

    // GPUImage 基于 OpenGL 的实时图像/滤镜处理库
    implementation(libs.gpuimage)

    // CameraX：相机实时帧滤镜页取流（ImageAnalysis），不使用 PreviewView 与 VideoCapture
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
}
