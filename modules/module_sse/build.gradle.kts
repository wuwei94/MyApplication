import java.util.Properties

plugins {
    alias(libs.plugins.nowinandroid.android.library)
    alias(libs.plugins.nowinandroid.android.arouter)
    alias(libs.plugins.nowinandroid.android.eventbus)
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}
val deepseekApiKey: String = localProperties.getProperty("deepseek.api.key")
    ?: System.getenv("DEEPSEEK_API_KEY")
    ?: ""

android {
    namespace = "com.example.william.my.module.sse"

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        buildConfigField("String", "DEEPSEEK_API_KEY", "\"$deepseekApiKey\"")
    }
}

dependencies {
    implementation(projects.basic.basicLib)
    implementation(projects.basic.basicShared)

    implementation(projects.libs.libSseOkhttp)
    implementation(projects.libs.libSseKtor)
}
