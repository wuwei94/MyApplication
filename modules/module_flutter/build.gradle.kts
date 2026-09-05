val enableFlutter = providers.gradleProperty("enableFlutter")
    .orElse("true")
    .get()
    .toBoolean()

plugins {
    alias(libs.plugins.nowinandroid.android.library)
    alias(libs.plugins.nowinandroid.android.arouter)
    alias(libs.plugins.nowinandroid.android.eventbus)
    alias(libs.plugins.nowinandroid.android.hilt)
}

android {
    namespace = "com.example.william.my.module.flutter"

    sourceSets {
        getByName("main") {
            if (enableFlutter) {
                manifest.srcFile("src/flutter/AndroidManifest.xml")
                java.srcDirs("src/main/java", "src/flutter/java")
            } else {
                manifest.srcFile("src/noFlutter/AndroidManifest.xml")
                java.srcDirs("src/main/java", "src/noFlutter/java")
            }
        }
    }
}

dependencies {
    implementation(projects.basic.basicLib)
    implementation(projects.basic.basicShared)

    if (enableFlutter) {
        implementation(projects.flutter)
    }
}
