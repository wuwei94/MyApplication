plugins {
    alias(libs.plugins.nowinandroid.android.library)
}

android {
    namespace = "com.example.william.my.core.widget"
    buildFeatures {
        dataBinding = true
    }
}

dependencies {
    //Utils
    implementation(libs.utils)
    //PAG
    implementation(libs.pag)
}