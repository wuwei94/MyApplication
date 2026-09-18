package com.example.william.my.module.mqtt.paho.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.constant.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.mqtt.MqttClientListener
import com.example.william.my.core.mqtt.paho.PahoServiceClientManager

/**
 * Eclipse Paho Android Service — 绑定 MqttService 的 MQTT 客户端
 *
 * 核心机制与避坑点：
 * 1. Service 绑定：MqttAndroidClient 通过绑定 MqttService 维持后台连接，页面销毁时必须 disconnect 释放 Service
 * 2. 协议版本：MqttAndroidClient 默认 MQTT 3.1.1，不支持 MQTT 5.0 特性
 * 3. Fork 依赖：官方 1.1.1 在 targetSdk 34+ 注册 Receiver 缺少导出标志会 SecurityException，本页使用 hannesa2 维护 fork（AAR 自带 Service 声明）
 * 4. 断线重连：MqttService 内置自动重连，onConnectionLost 后等待恢复；QoS 0/1/2 语义按 MQTT 3.1.1 规范
 *
 * 官方参考：
 * https://github.com/eclipse-paho/paho.mqtt.android
 */
@Route(path = RouterPath.Mqtt.PahoServiceClient)
class PahoServiceClientActivity : BasicResponseActivity() {

    private val broker: String = Constants.Mqtt_Broker
    private val topic: String = Constants.Mqtt_Topic

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription(
            "[Paho Android Service] MqttAndroidClient（MQTT 3.1.1）\nBroker：$broker\nTopic：$topic\n" +
                "覆盖连接 / 订阅下行 / 发布上行 / 服务内置自动重连 / 关闭注销",
        )
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "1. 连接 Broker（Connect）",
        "2. 订阅主题（Subscribe QoS 2 + 下行监听）",
        "3. 发布消息（Publish QoS 0）",
        "4. 发布消息（Publish QoS 1）",
        "5. 发布消息（Publish QoS 2）",
        "6. 查询连接与重连状态（Connection Status）",
        "7. 断开连接（Disconnect）",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> connect()
            1 -> subscribe()
            2 -> publish(0)
            3 -> publish(1)
            4 -> publish(2)
            5 -> showConnectionStatus()
            6 -> disconnect()
        }
    }

    /**
     * MqttService 内置自动重连；本项输出连接态与重连语义。
     */
    private fun showConnectionStatus() {
        val connected = PahoServiceClientManager.isConnected()
        appendLog("✓ [状态] isConnected=$connected")
        appendLog("✓ [重连] MqttService 内置自动重连，onConnectionLost 后等待恢复")
    }

    override fun onDestroy() {
        super.onDestroy()
        PahoServiceClientManager.disconnect()
    }

    private fun connect() {
        appendLog("[连接] 正在连接 $broker ...")
        PahoServiceClientManager.connect(
            context = this,
            broker = broker,
            listener = object : MqttClientListener() {
                override fun onConnectSuccess(reconnect: Boolean) {
                    appendLogAccent(if (reconnect) "[连接] 已重连成功" else "[连接] 已连接")
                }

                override fun onConnectionLost() {
                    appendLogAccent("[连接] 连接丢失，等待自动重连...")
                }

                override fun onMessageArrived(topic: String, payload: String) {
                    appendLogAccent("[消息] topic=$topic\npayload=$payload")
                }

                override fun onError(message: String) {
                    appendLogAccent("✗ $message")
                }
            },
        )
    }

    private fun subscribe() {
        if (!PahoServiceClientManager.isConnected()) {
            appendLog("✗ 未连接，请先连接")
            return
        }
        PahoServiceClientManager.subscribe(topic, qos = 2)
        appendLog("[订阅] 已订阅 $topic（QoS 2）")
    }

    private fun publish(qos: Int) {
        if (!PahoServiceClientManager.isConnected()) {
            appendLog("✗ 未连接，请先连接")
            return
        }
        val payload = "Hello Paho Service! qos=$qos time=${System.currentTimeMillis()}"
        PahoServiceClientManager.publish(topic, payload, qos = qos)
        appendLog("[发布] $topic（QoS $qos）\n$payload")
    }

    private fun disconnect() {
        PahoServiceClientManager.disconnect()
        appendLog("[断开] 已断开连接")
    }
}
