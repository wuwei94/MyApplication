plugins {
    alias(libs.plugins.nowinandroid.android.library)
    alias(libs.plugins.nowinandroid.android.arouter)
    alias(libs.plugins.nowinandroid.android.eventbus)
}

android {
    namespace = "com.example.william.my.module.ui"
    resourcePrefix("ui_")
}

dependencies {
    implementation(project(":basic:basic_lib"))
    implementation(project(":basic:basic_shared"))

    //TODO
    implementation(project(":libs:lib_widget"))
    implementation(libs.google.flexBox)
    implementation(libs.google.material)
}
