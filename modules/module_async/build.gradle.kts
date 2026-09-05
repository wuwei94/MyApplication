plugins {
    alias(libs.plugins.nowinandroid.android.library)
    alias(libs.plugins.nowinandroid.android.arouter)
    alias(libs.plugins.nowinandroid.android.eventbus)
}

android {
    namespace = "com.example.william.my.module.async"
    resourcePrefix("async_")
}

dependencies {
    implementation(projects.basic.basicLib)
    implementation(projects.basic.basicShared)
}
