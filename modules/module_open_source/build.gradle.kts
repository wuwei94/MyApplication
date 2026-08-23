plugins {
    alias(libs.plugins.nowinandroid.android.library)
    alias(libs.plugins.nowinandroid.android.arouter)
    alias(libs.plugins.nowinandroid.android.eventbus)
}

android {
    namespace = "com.example.william.my.module.opensource"
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(project(":basic:basic_lib"))
    implementation(project(":basic:basic_shared"))

    implementation(libs.citypicker)
    implementation(libs.easyfloat)
    implementation(libs.tablayout)
    implementation(libs.photoview)
    implementation(libs.pickerview)
    implementation(libs.pictureselector)
    //CustomPopWindow
    //implementation(libs.custompopwindow)

    implementation(libs.coil)
    implementation(libs.glide)

    implementation(libs.permission)

    implementation(libs.rxandroid)
}
