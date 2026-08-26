package com.example.william.my.module.mqtt.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.constant.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.mqtt.MqttClientListener
import com.example.william.my.core.mqtt.paho.PahoServiceClientManager

/**
 * Eclipse Paho Android Service — 客户端
 *
 * 基于 MqttAndroidClient，通过绑定 `MqttService` 在 Android 平台运行。
 * 使用 hannesa2 维护 fork（官方 1.1.1 已停更，在 targetSdk 34+ 上注册
 * Receiver 缺少导出标志会导致 SecurityException 崩溃），
 * fork 的 AAR 已自带 Service 声明，无需在 AndroidManifest 手动注册。
 *
 * 使用 EMQX 公共 Broker（无需账号），订阅与发布到同一 Topic 即可收到自己发出的消息。
 *
 * 基本用法：
 * ```kotlin
 * // 连接
 * PahoServiceClientManager.connect(context, broker = "tcp://broker.emqx.io:1883", listener = ...)
 *
 * // 订阅
 * PahoServiceClientManager.subscribe("mqtt/example", qos = 2)
 *
 * // 发布
 * PahoServiceClientManager.publish("mqtt/example", "Hello Paho!", qos = 1)
 *
 * // 断开
 * PahoServiceClientManager.disconnect()
 * ```
 *
 * https://github.com/hannesa2/paho.mqtt.android
 */
@Route(path = RouterPath.Mqtt.PahoServiceClient)
class PahoServiceClientActivity : BasicResponseActivity() {

    private val broker: String = Constants.Mqtt_Broker
    private val topic: String = Constants.Mqtt_Topic

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription("【Paho Android Service】MqttAndroidClient\nBroker：$broker\nTopic：$topic\n\n先连接，再订阅，最后发布。")
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "连接 Broker（Connect）",
            "订阅主题（Subscribe QoS 2）",
            "发布消息（Publish QoS 0）",
            "发布消息（Publish QoS 1）",
            "发布消息（Publish QoS 2）",
            "断开连接（Disconnect）",
        )
    }

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
        PahoServiceClientManager.disconnect()
    }

    private fun connect() {
        appendLog("【连接】正在连接 $broker ...")
        PahoServiceClientManager.connect(
            context = this,
            broker = broker,
            listener = object : MqttClientListener() {
                override fun onConnectSuccess(reconnect: Boolean) {
                    appendLogAccent(if (reconnect) "【连接】已重连成功" else "【连接】已连接")
                }

                override fun onConnectionLost() {
                    appendLogAccent("【连接】连接丢失，等待自动重连...")
                }

                override fun onMessageArrived(topic: String, payload: String) {
                    appendLogAccent("【消息】topic=$topic\npayload=$payload")
                }

                override fun onError(message: String) {
                    appendLogAccent("【错误】$message")
                }
            }
        )
    }

    private fun subscribe() {
        if (!PahoServiceClientManager.isConnected()) {
            appendLog("【错误】未连接，请先连接")
            return
        }
        PahoServiceClientManager.subscribe(topic, qos = 2)
        appendLog("【订阅】已订阅 $topic（QoS 2）")
    }

    private fun publish(qos: Int) {
        if (!PahoServiceClientManager.isConnected()) {
            appendLog("【错误】未连接，请先连接")
            return
        }
        val payload = "Hello Paho Service! qos=$qos time=${System.currentTimeMillis()}"
        PahoServiceClientManager.publish(topic, payload, qos = qos)
        appendLog("【发布】$topic（QoS $qos）\n$payload")
    }

    private fun disconnect() {
        PahoServiceClientManager.disconnect()
        appendLog("【断开】已断开连接")
    }
}
