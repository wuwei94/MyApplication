package com.example.william.my.module.mqtt

import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.activity.RouterRecyclerActivity
import com.example.william.my.basic.basic_shared.router.item.RouterItem
import com.example.william.my.basic.basic_shared.router.path.RouterPath

/**
 * MQTT 示例入口
 *
 * 演示 MQTT（Message Queuing Telemetry Transport）发布/订阅消息队列协议的核心能力：
 * 连接（Connect）、订阅（Subscribe）、发布（Publish）、QoS 与断开（Disconnect）。
 *
 * 提供两种客户端实现对比：
 * - HiveMQ MQTT Client（依赖 Netty，异步 API）
 * - Eclipse Paho Android Service（MqttAndroidClient，绑定 Service）
 */
@Route(path = RouterPath.Mqtt.Main)
class MqttMainActivity : RouterRecyclerActivity() {

    override fun buildRouter(): ArrayList<RouterItem> {
        val routerItems = arrayListOf<RouterItem>()
        routerItems.add(RouterItem("── HiveMQ ──", ""))
        routerItems.add(RouterItem("HiveMQ MQTT Client（异步 API）", RouterPath.Mqtt.HiveMqClient))
        routerItems.add(RouterItem("", ""))
        routerItems.add(RouterItem("── Paho Android Service ──", ""))
        routerItems.add(RouterItem("Paho Android Service（MqttAndroidClient）", RouterPath.Mqtt.PahoServiceClient))
        return routerItems
    }
}
