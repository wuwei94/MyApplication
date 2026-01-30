plugins {
    alias(libs.plugins.nowinandroid.android.library)
}

android {
    namespace = "com.example.william.my.core.ninepatch"
    buildFeatures {
        dataBinding = true
    }
}

dependencies {
    implementation(libs.glide)
}