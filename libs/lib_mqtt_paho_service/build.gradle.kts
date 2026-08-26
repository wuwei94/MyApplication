plugins {
    alias(libs.plugins.nowinandroid.android.library)
}

android {
    namespace = "com.example.william.my.core.mqtt.paho"
}

dependencies {
    // 复用 lib_mqtt 的 MqttClientListener 回调接口
    api(project(":libs:lib_mqtt"))

    // Paho Android Service（MqttAndroidClient，通过绑定 MqttService 通信）
    // 使用 hannesa2 维护 fork：官方 org.eclipse.paho 1.1.1 已停更，
    // 其 AlarmPingSender 注册 Receiver 缺少 RECEIVER_EXPORTED/NOT_EXPORTED，
    // 在 targetSdk 34+ 上连接成功即抛 SecurityException。
    // fork 的 AAR 已自带 Service 声明与 WAKE_LOCK 等权限，无需手动注册。
    api(libs.paho.android.service)
    // MqttAndroidClient 依赖的 Paho 纯 Java 客户端（与 lib_mqtt 版本对齐，fork 同样基于 1.2.5）
    api(libs.mqtt)
}
