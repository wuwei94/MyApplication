plugins {
    alias(libs.plugins.nowinandroid.android.library)
}

android {
    namespace = "com.example.william.my.core.mqtt"
}

// 仅承载 MQTT 各客户端复用的回调接口 MqttClientListener，无第三方依赖
