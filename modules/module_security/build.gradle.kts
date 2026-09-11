plugins {
    alias(libs.plugins.nowinandroid.android.library)
    alias(libs.plugins.nowinandroid.android.arouter)
}

android {
    namespace = "com.example.william.my.module.security"
    resourcePrefix("security_")
}

dependencies {
    implementation(projects.basic.basicLib)
    implementation(projects.basic.basicShared)
}
