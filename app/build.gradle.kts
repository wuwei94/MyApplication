import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import com.google.samples.apps.nowinandroid.NiaBuildType
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

//apply(file("../.signing/configs-signing.gradle"))

plugins {
    alias(libs.plugins.nowinandroid.android.application)
    alias(libs.plugins.nowinandroid.android.application.compose)
    alias(libs.plugins.nowinandroid.android.arouter)
    alias(libs.plugins.nowinandroid.android.eventbus)
    alias(libs.plugins.nowinandroid.android.hilt)
    alias(libs.plugins.dependency.guard)
}

android {
    namespace = "com.example.william.my.application"
    defaultConfig {
        applicationId = "com.example.william.my.application"
        versionCode = 1
        versionName = "1.0.0" // X.Y.Z; X = Major, Y = minor, Z = Patch level

        // Custom test runner to set up Hilt dependency graph
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }

        ndk {
            // armeabi：万金油架构平台（占用率：0%）
            // armeabi-v7a：曾经主流的架构平台（占用率：10%）
            // arm64-v8a：目前主流架构平台（占用率：90%）
            //abiFilters 'armeabi-v7a', 'arm64-v8a' // , 'x86', 'x86_64'
            abiFilters.addAll(arrayOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64"))
        }

        applicationVariants.all {
            outputs.all {
                val outputImpl = this as BaseVariantOutputImpl
                val createTime =
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd"))
                outputImpl.outputFileName =
                    "MyApplication" + "_${versionName}_${baseName}_$createTime.apk"
            }
        }

        addManifestPlaceholders(mutableMapOf("APP_NAME" to "My Application")) // 配置主包的应用名称
    }

    buildTypes {
        debug {
            applicationIdSuffix = NiaBuildType.DEBUG.applicationIdSuffix
        }
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            applicationIdSuffix = NiaBuildType.RELEASE.applicationIdSuffix

            // To publish on the Play store a private signing key is required, but to allow anyone
            // who clones the code to sign and run the release variant, use the debug signing key.
            // TODO: Abstract the signing configuration to a separate file to avoid hardcoding this.
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    implementation(project(":basic:basic_lib"))
    implementation(project(":basic:basic_module"))

    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.startup)
}

dependencyGuard {
    // 生产环境 Release 配置 - 建议监控此配置以了解生产构建中包含的内容
    configuration("prodReleaseRuntimeClasspath") {
        // 可选：添加允许规则，例如禁止测试依赖进入生产环境
        // allowedFilter = { !it.contains("junit") }
    }
    // 生产环境 Debug 配置
    configuration("prodDebugRuntimeClasspath") {
        // 可选：添加允许规则
        // allowedFilter = { !it.contains("junit") }
    }
}