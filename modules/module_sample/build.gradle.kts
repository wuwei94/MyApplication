plugins {
    alias(libs.plugins.nowinandroid.android.library)
    alias(libs.plugins.nowinandroid.android.arouter)
    alias(libs.plugins.nowinandroid.android.eventbus)
}

android {
    namespace = "com.example.william.my.module.sample"
    resourcePrefix("sample_")
    buildFeatures {
        aidl = true
    }
}

dependencies {
    implementation(project(":basic:basic_lib"))
    implementation(project(":basic:basic_module"))

    //TODO
    implementation(project(":libs:lib_widget"))

    //TODO
    implementation(libs.google.flexBox)
    implementation(libs.google.material)
}
