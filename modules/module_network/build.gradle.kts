plugins {
    alias(libs.plugins.nowinandroid.android.library)
    alias(libs.plugins.nowinandroid.android.arouter)
    alias(libs.plugins.nowinandroid.android.eventbus)
}

android {
    namespace = "com.example.william.my.module.network"
    //Netty
    //packaging {
    //    resources.excludes.add("META-INF/INDEX.LIST")
    //    resources.excludes.add("META-INF/io.netty.versions.properties")
    //}
}

dependencies {
    implementation(project(":basic:basic_lib"))
    implementation(project(":basic:basic_shared"))

    implementation(project(":basic:basic_repository"))

    implementation(libs.slf4j)
    implementation(libs.netty)
    implementation(libs.nanohttpd)
    implementation(libs.websocket)
    implementation(project(":libs:lib_ktor"))
    implementation(project(":libs:lib_volley"))
    implementation(project(":libs:lib_okhttp"))
    implementation(project(":libs:lib_retrofit"))
    implementation(project(":libs:lib_download"))
    implementation(project(":libs:lib_websocket"))
}