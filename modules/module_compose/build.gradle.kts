plugins {
    alias(libs.plugins.nowinandroid.android.library)
    alias(libs.plugins.nowinandroid.android.library.compose)
    alias(libs.plugins.nowinandroid.android.arouter)
    alias(libs.plugins.nowinandroid.android.eventbus)
}

android {
    namespace = "com.example.william.my.module.compose"
    //kotlin 2.0.0 以下时配置
    //buildFeatures {
    //    compose = true
    //}
    //https://developer.android.com/jetpack/androidx/releases/compose-kotlin?hl=zh-cn
    //composeOptions {
    //    kotlinCompilerExtensionVersion = "1.5.15" // kotlin:1.9.25
    //}
}

dependencies {
    implementation(project(":basic:basic_lib"))
    implementation(project(":basic:basic_module"))

//    implementation(libs.androidx.material)
//    implementation(libs.androidx.material3)
//    implementation(libs.androidx.activity.compose)
//    implementation(libs.androidx.navigation.compose)
//    implementation(libs.androidx.constraintlayout.compose)

//    implementation(platform(libs.androidx.compose.bom))
//    androidTestImplementation(platform(libs.androidx.compose.bom))

//    implementation(libs.androidx.compose.ui)
//    implementation(libs.androidx.compose.ui.graphics)
//    implementation(libs.androidx.compose.ui.tooling.preview)

//    debugImplementation(libs.androidx.compose.ui.tooling)
//    debugImplementation(libs.androidx.compose.ui.test.manifest)

//    implementation("androidx.compose.material:material-icons-core")
//    implementation("androidx.compose.material:material-icons-extended")

//    implementation("com.google.accompanist:accompanist-pager:0.34.0")

    implementation(libs.smartrefresh.compose)
}

//composeCompiler {
//    enableStrongSkippingMode = true
//
//    reportsDestination = layout.buildDirectory.dir("compose_compiler")
//    stabilityConfigurationFile = rootProject.layout.projectDirectory.file("stability_config.conf")
//}