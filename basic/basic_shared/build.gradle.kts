plugins {
    alias(libs.plugins.nowinandroid.android.library)
    alias(libs.plugins.nowinandroid.android.arouter)
}

android {
    namespace = "com.example.william.my.basic.basic_shared"
    resourcePrefix("shared_")
}

dependencies {
    implementation(project(":basic:basic_lib"))
}