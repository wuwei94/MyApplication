package com.example.william.my.module.mqtt.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.constant.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.mqtt.MqttClientListener
import com.example.william.my.core.mqtt.hivemq.HiveMqClientManager

/**
 * HiveMQ MQTT — 客户端
 *
 * 基于 HiveMQ MQTT Client 的异步 API（流式 Builder + CompletableFuture 回调），
 * 演示连接、订阅、发布（QoS 0/1/2）与断开。与 Paho 的区别在于 HiveMQ 依赖 Netty、
 * 支持 MQTT 5.0，且 API 更现代。
 *
 * 使用 EMQX 公共 Broker（无需账号），订阅与发布到同一 Topic 即可收到自己发出的消息。
 *
 * 基本用法：
 * ```kotlin
 * // 连接
 * HiveMqClientManager.connect(host = "broker.emqx.io", port = 1883, listener = ...)
 *
 * // 订阅
 * HiveMqClientManager.subscribe("mqtt/example", qos = 2)
 *
 * // 发布
 * HiveMqClientManager.publish("mqtt/example", "Hello HiveMQ!", qos = 1)
 *
 * // 断开
 * HiveMqClientManager.disconnect()
 * ```
 *
 * https://github.com/hivemq/hivemq-mqtt-client
 */
@Route(path = RouterPath.Mqtt.HiveMqClient)
class HiveMqClientActivity : BasicResponseActivity() {

    private val host: String = Constants.Mqtt_Host
    private val port: Int = Constants.Mqtt_Port
    private val topic: String = Constants.Mqtt_Topic

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("【HiveMQ】异步 API（MQTT 3.1.1）\nBroker：$host:$port\nTopic：$topic\n\n先连接，再订阅，最后发布。")
    }

    override fun buildList(): ArrayList<String> = arrayListOf(
        "连接 Broker（Connect）",
        "订阅主题（Subscribe QoS 2）",
        "发布消息（Publish QoS 0）",
        "发布消息（Publish QoS 1）",
        "发布消息（Publish QoS 2）",
        "断开连接（Disconnect）",
    )

    override fun onRecyclerClick(position: Int, string: String) {
        super.onRecyclerClick(position, string)
        when (position) {
            0 -> connect()
            1 -> subscribe()
            2 -> publish(0)
            3 -> publish(1)
            4 -> publish(2)
            5 -> disconnect()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        HiveMqClientManager.disconnect()
    }

    private fun connect() {
        appendLog("【连接】正在连接 $host:$port ...")
        HiveMqClientManager.connect(
            host = host,
            port = port,
            listener = object : MqttClientListener() {
                override fun onConnectSuccess(reconnect: Boolean) {
                    appendLogAccent(if (reconnect) "【连接】已重连成功" else "【连接】已连接")
                }

                override fun onConnectionLost() {
                    appendLogAccent("【连接】连接丢失")
                }

                override fun onMessageArrived(topic: String, payload: String) {
                    appendLogAccent("【消息】topic=$topic\npayload=$payload")
                }

                override fun onError(message: String) {
                    appendLogAccent("【错误】$message")
                }
            },
        )
    }

    private fun subscribe() {
        if (!HiveMqClientManager.isConnected()) {
            appendLog("【错误】未连接，请先连接")
            return
        }
        HiveMqClientManager.subscribe(topic, qos = 2)
        appendLog("【订阅】已订阅 $topic（QoS 2）")
    }

    private fun publish(qos: Int) {
        if (!HiveMqClientManager.isConnected()) {
            appendLog("【错误】未连接，请先连接")
            return
        }
        val payload = "Hello HiveMQ! qos=$qos time=${System.currentTimeMillis()}"
        HiveMqClientManager.publish(topic, payload, qos = qos)
        appendLog("【发布】$topic（QoS $qos）\n$payload")
    }

    private fun disconnect() {
        HiveMqClientManager.disconnect()
        appendLog("【断开】已断开连接")
    }
}
