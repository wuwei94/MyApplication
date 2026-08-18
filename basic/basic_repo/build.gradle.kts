plugins {
    alias(libs.plugins.nowinandroid.android.library)
    alias(libs.plugins.nowinandroid.android.arouter)
    alias(libs.plugins.nowinandroid.android.eventbus)
    alias(libs.plugins.nowinandroid.android.room)
}

android {
    namespace = "com.example.william.my.basic.basic_repo"
    resourcePrefix("repo_")
}

dependencies {
    implementation(project(":basic:basic_lib"))
    implementation(project(":basic:basic_shared"))

    api(project(":libs:lib_okhttp"))
    api(project(":libs:lib_retrofit"))
    api(project(":libs:lib_retrofit_rx"))

    api(libs.androidx.lifecycle.livedata)
}