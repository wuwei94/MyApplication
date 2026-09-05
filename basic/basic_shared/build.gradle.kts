import java.util.Properties

plugins {
    alias(libs.plugins.nowinandroid.android.library)
    alias(libs.plugins.nowinandroid.android.arouter)
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
    namespace = "com.example.william.my.basic.basic_shared"
    resourcePrefix("shared_")

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        buildConfigField("String", "DEEPSEEK_API_KEY", "\"$deepseekApiKey\"")
    }
}

dependencies {
    implementation(projects.basic.basicLib)

    implementation(libs.utils)
}
