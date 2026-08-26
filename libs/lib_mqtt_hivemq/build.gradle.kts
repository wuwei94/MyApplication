plugins {
    alias(libs.plugins.nowinandroid.android.library)
}

android {
    namespace = "com.example.william.my.core.mqtt.hivemq"
}

dependencies {
    // 复用 lib_mqtt 的 MqttClientListener 回调接口
    api(project(":libs:lib_mqtt"))

    // HiveMQ MQTT Client（异步 API，支持 MQTT 3.1.1 / 5.0）
    // 排除其传递的模块化 Netty，统一使用项目的 netty-all，避免与 lib_netty 产生重复类冲突
    api(libs.hivemq) {
        exclude(group = "io.netty")
    }
    // netty-all（版本已与 HiveMQ 所需对齐）
    api(libs.netty)
}
