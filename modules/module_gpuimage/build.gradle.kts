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
}
