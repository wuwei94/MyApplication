// 在主 Android 应用模块的 build.gradle.kts 文件中添加以下配置
// 文件路径: /Users/mac/StudioProjects/MyApplication/app/build.gradle.kts

// 1. 在 android 块中添加 compileOptions 配置，启用核心库 desugaring
android {
    // ... 其他配置 ...
    
    // 启用核心库 desugaring，支持 Java 8+ 特性
    compileOptions {
        coreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    
    // 如果使用 Kotlin，还需要添加以下配置
    kotlinOptions {
        jvmTarget = "1.8"
    }
    
    // ... 其他配置 ...
}

// 2. 在 dependencies 块中添加 desugaring 依赖
dependencies {
    // ... 其他依赖 ...
    
    // 添加核心库 desugaring 依赖
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")
    
    // ... 其他依赖 ...
}
