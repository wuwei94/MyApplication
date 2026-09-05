plugins {
    alias(libs.plugins.nowinandroid.android.library)
    alias(libs.plugins.nowinandroid.android.arouter)
    alias(libs.plugins.nowinandroid.android.eventbus)
}

android {
    namespace = "com.example.william.my.module.widget"
}

dependencies {
    implementation(projects.basic.basicLib)
    implementation(projects.basic.basicShared)

    implementation(projects.libs.libWidget)
    implementation(libs.google.flexBox)
}
