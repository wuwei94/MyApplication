import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import com.google.samples.apps.nowinandroid.NiaBuildType
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

plugins {
    alias(libs.plugins.nowinandroid.android.application)
    alias(libs.plugins.nowinandroid.android.application.compose)
    alias(libs.plugins.nowinandroid.android.arouter)
    alias(libs.plugins.nowinandroid.android.eventbus)
    alias(libs.plugins.nowinandroid.android.hilt)
    alias(libs.plugins.dependency.guard)
    alias(libs.plugins.baselineprofile)
}

android {
    namespace = "com.example.william.my.application"

    defaultConfig {
        applicationId = "com.example.william.my.application"
        // 语义化版本自动联动 (读取 libs.versions.toml 中的 version-major/minor/patch)
        val vMajor = libs.versions.version.major.get().toInt()
        val vMinor = libs.versions.version.minor.get().toInt()
        val vPatch = libs.versions.version.patch.get().toInt()
        versionCode = vMajor * 10000 + vMinor * 100 + vPatch // 例：1.0.0 -> 10000
        versionName = "$vMajor.$vMinor.$vPatch"               // 例："1.0.0"

        // 测试 Runner
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }

        ndk {
            // 支持的主流 CPU 架构
            abiFilters.addAll(arrayOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64"))
        }

        // 统一打包产物命名规范：MyApplication_版本_渠道_构建日期.apk
        applicationVariants.all {
            outputs.all {
                val outputImpl = this as BaseVariantOutputImpl
                val createTime =
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd"))
                outputImpl.outputFileName =
                    "MyApplication_${versionName}_${baseName}_$createTime.apk"
            }
        }

        addManifestPlaceholders(mutableMapOf("APP_NAME" to "My Application"))
    }

    buildTypes {
        debug {
            applicationIdSuffix = NiaBuildType.DEBUG.applicationIdSuffix
        }
        getByName("release") {
            // 启用 R8 代码混淆与代码缩减（剔除无用代码）
            isMinifyEnabled = true
            // 启用资源缩减（剔除无用资源文件，如未引用的图片、布局等，需配合 isMinifyEnabled 一起使用）
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            applicationIdSuffix = NiaBuildType.RELEASE.applicationIdSuffix

            // 本地发布使用 debug 签名作为默认凭证
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            // Netty 各模块 jar 均携带 INDEX.LIST / io.netty.versions.properties，合并时防止重复冲突
            excludes += "/META-INF/INDEX.LIST"
            excludes += "/META-INF/io.netty.versions.properties"
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    // 基础公共层
    implementation(project(":basic:basic_lib"))
    implementation(project(":basic:basic_shared"))

    // 应用启动与启动页
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.startup)
}

dependencyGuard {
    // 生产环境 Release 配置 - 监控依赖树变更以防止依赖膨胀
    configuration("prodReleaseRuntimeClasspath")
    // 生产环境 Debug 配置
    configuration("prodDebugRuntimeClasspath")
}

baselineProfile {
    // 允许使用已有基线配置文件进行 AOT 优化打包，常规构建无需连接设备实时生成
    automaticGenerationDuringBuild = false
}
