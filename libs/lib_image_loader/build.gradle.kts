plugins {
    alias(libs.plugins.nowinandroid.android.library)
    alias(libs.plugins.nowinandroid.android.ksp)
}

android {
    namespace = "com.example.william.my.core.imageloader"
}

dependencies {
    implementation(libs.glide)
    ksp(libs.glide.ksp)
    implementation(libs.coil)
    implementation(libs.coil.gif)
}
