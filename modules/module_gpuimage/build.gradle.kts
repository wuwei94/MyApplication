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

    // CameraX 官方全套组件（核心 API + Camera2 底层 + 生命周期感知 + PreviewView 取景 + VideoCapture 录制 + CameraEffect 特效）
    implementation(libs.bundles.androidx.camerax)
}
