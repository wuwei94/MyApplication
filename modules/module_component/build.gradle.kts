plugins {
    alias(libs.plugins.nowinandroid.android.library)
    alias(libs.plugins.nowinandroid.android.hilt)
    alias(libs.plugins.nowinandroid.android.eventbus)
}

android {
    namespace = "com.example.william.my.module.component"
    resourcePrefix("component_")
}

dependencies {
    implementation(projects.basic.basicLib)
    implementation(projects.basic.basicShared)
}
