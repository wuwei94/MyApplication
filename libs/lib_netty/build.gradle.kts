plugins {
    alias(libs.plugins.nowinandroid.android.library)
}

android {
    namespace = "com.example.william.my.core.netty"
    //Netty
    //packaging {
    //    resources.excludes.add("META-INF/INDEX.LIST")
    //    resources.excludes.add("META-INF/io.netty.versions.properties")
    //}
}

dependencies {
    // Netty
    api(libs.netty)
    // RxJava
    api(libs.rxandroid)
}
