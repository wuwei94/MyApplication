plugins {
    alias(libs.plugins.nowinandroid.android.library)
    alias(libs.plugins.nowinandroid.android.arouter)
    alias(libs.plugins.nowinandroid.android.eventbus)
}

android {
    namespace = "com.example.william.my.module.performance"
    resourcePrefix("performance_")
}

dependencies {
    implementation(project(":basic:basic_lib"))
    implementation(project(":basic:basic_shared"))

    implementation(libs.androidx.asyncLayoutInflater)

    // Startup & Baseline Profiles
    implementation(libs.androidx.startup)
    implementation(libs.androidx.profileinstaller)
}
