package com.example.william.my.module.mqtt.hivemq.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.constant.Constants
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.mqtt.MqttClientListener
import com.example.william.my.core.mqtt.hivemq.HiveMqClientManager

/**
 * HiveMQ MQTT Client — 基于 Netty 的异步 MQTT 客户端
 *
 * 核心机制与避坑点：
 * 1. 异步 API：流式 Builder + CompletableFuture 回调，无阻塞；本页通过 useMqttVersion3() 使用 MQTT 3.1.1（Mqtt3AsyncClient）
 * 2. 线程模型：Netty 事件循环驱动 IO，客户端回调运行在非主线程；UI 更新需切回主线程
 * 3. 连接生命周期：onDestroy 必须 disconnect 释放连接与 Netty 线程；断线重连由 onConnectionLost 通知，页面需自行恢复订阅
 * 4. QoS 语义：QoS 0 至多一次、1 至少一次、2 恰好一次；订阅与发布的 QoS 取两者较小值生效
 *
 * 官方参考：
 * https://github.com/hivemq/hivemq-mqtt-client
 */
@Route(path = RouterPath.Mqtt.HiveMqClient)
class HiveMqClientActivity : BasicResponseActivity() {

    private val host: String = Constants.Mqtt_Host
    private val port: Int = Constants.Mqtt_Port
    private val topic: String = Constants.Mqtt_Topic

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription(
            "[HiveMQ] 异步 API（MQTT 3.1.1）\nBroker：$host:$port\nTopic：$topic\n" +
                "覆盖连接 / 订阅下行 / 发布上行 / 重连状态 / 关闭注销",
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
     * 输出当前连接态；断线恢复依赖 onConnectionLost 通知与业务侧重订阅。
     */
    private fun showConnectionStatus() {
        val connected = HiveMqClientManager.isConnected()
        appendLog("✓ [状态] isConnected=$connected")
        appendLog("✓ [重连] onConnectionLost 通知断线，页面需自行恢复订阅")
    }

    override fun onDestroy() {
        super.onDestroy()
        HiveMqClientManager.disconnect()
    }

    private fun connect() {
        appendLog("[连接] 正在连接 $host:$port ...")
        HiveMqClientManager.connect(
            host = host,
            port = port,
            listener = object : MqttClientListener() {
                override fun onConnectSuccess(reconnect: Boolean) {
                    appendLogAccent(if (reconnect) "[连接] 已重连成功" else "[连接] 已连接")
                }

                override fun onConnectionLost() {
                    appendLogAccent("[连接] 连接丢失")
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
        if (!HiveMqClientManager.isConnected()) {
            appendLog("✗ 未连接，请先连接")
            return
        }
        HiveMqClientManager.subscribe(topic, qos = 2)
        appendLog("[订阅] 已订阅 $topic（QoS 2）")
    }

    private fun publish(qos: Int) {
        if (!HiveMqClientManager.isConnected()) {
            appendLog("✗ 未连接，请先连接")
            return
        }
        val payload = "Hello HiveMQ! qos=$qos time=${System.currentTimeMillis()}"
        HiveMqClientManager.publish(topic, payload, qos = qos)
        appendLog("[发布] $topic（QoS $qos）\n$payload")
    }

    private fun disconnect() {
        HiveMqClientManager.disconnect()
        appendLog("[断开] 已断开连接")
    }
}
