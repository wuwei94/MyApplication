plugins {
    alias(libs.plugins.nowinandroid.android.library)
    alias(libs.plugins.nowinandroid.android.arouter)
    alias(libs.plugins.nowinandroid.android.eventbus)
}

android {
    namespace = "com.example.william.my.module.tab"
    resourcePrefix = "tab_"
}

dependencies {
    implementation(project(":basic:basic_lib"))
    implementation(project(":basic:basic_shared"))

    // ViewPager2
    implementation(libs.androidx.viewPager2)
    // BottomNavigationView
    implementation(libs.google.material)
}
