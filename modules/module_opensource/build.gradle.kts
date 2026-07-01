plugins {
    alias(libs.plugins.nowinandroid.android.library)
    alias(libs.plugins.nowinandroid.android.arouter)
    alias(libs.plugins.nowinandroid.android.eventbus)
    alias(libs.plugins.nowinandroid.android.hilt)
    alias(libs.plugins.nowinandroid.android.firebase)
    alias(libs.plugins.nowinandroid.android.objectbox)
}

android {
    namespace = "com.example.william.my.module.opensource"
    resourcePrefix("opensource_")
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(project(":basic:basic_lib"))
    implementation(project(":basic:basic_module"))

    implementation(libs.banner)

    implementation(libs.citypicker)
    implementation(libs.countdownview)
    implementation(libs.easyfloat)
    implementation(libs.tablayout)
    implementation(libs.photoview)
    implementation(libs.pickerview)
    implementation(libs.pictureselector)
    //CustomPopWindow
    //implementation(libs.custompopwindow)
    implementation(libs.shadowlayout)
    implementation(libs.swipelayout)

    implementation(libs.pag)
    implementation(libs.lottie)
    implementation(libs.svgaPlayer)

    implementation(libs.mmkv)
    implementation(libs.permission)
    implementation(libs.loadsir)

    implementation(libs.rxandroid)

    implementation(libs.blurview)

    //extra
    implementation(libs.glide)//pictureselector
}

//https://github.com/greenrobot/greenDAO/issues/1110
//Kotlin
//tasks.configureEach {
//    if (name.matches(Regex("\\w*compile\\w*Kotlin"))) {
//        dependsOn("greendao")
//    }
//    if (name.matches(Regex("\\w*kaptGenerateStubs\\w*Kotlin"))) {
//        dependsOn("greendao")
//    }
//    if (name.matches(Regex("\\w*kapt\\w*Kotlin"))) {
//        dependsOn("greendao")
//    }
//}
//Groovy
//tasks.configureEach { task ->
//    if (task.name.matches("\\w*compile\\w*Kotlin")) {
//        task.dependsOn('greendao')
//    }
//    if (task.name.matches("\\w*kaptGenerateStubs\\w*Kotlin")) {
//        task.dependsOn('greendao')
//    }
//    if (task.name.matches("\\w*kapt\\w*Kotlin")) {
//        task.dependsOn('greendao')
//    }
//}