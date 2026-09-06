package com.example.william.my.core.mqtt.hivemq

import android.os.Handler
import android.os.Looper
import com.example.william.my.core.mqtt.MqttClientListener
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient

/**
 * HiveMQ MQTT 客户端门面。
 *
 * 基于 HiveMQ MQTT Client 的异步 API（[Mqtt3AsyncClient]），
 * 统一封装连接、订阅、发布与断开，回调通过 [MqttClientListener] 统一切回主线程。
 *
 * HiveMQ 依赖 Netty、支持 MQTT 5.0，API 采用流式 Builder + CompletableFuture 回调。
 *
 * https://github.com/hivemq/hivemq-mqtt-client
 */
object HiveMqClientManager {

    private val mainHandler = Handler(Looper.getMainLooper())

    @Volatile
    private var client: Mqtt3AsyncClient? = null

    @Volatile
    private var listener: MqttClientListener? = null

    @Volatile
    private var connected: Boolean = false

    /**
     * 建立 MQTT 3.1.1 连接。
     *
     * @param host             Broker 主机名，形如 `broker.emqx.io`
     * @param port             Broker 端口，默认 1883
     * @param clientId         客户端 ID，同一 Broker 下需唯一
     * @param cleanSession     是否清除会话
     * @param keepAliveInterval 心跳间隔（秒）
     * @param listener         回调
     */
    fun connect(
        host: String,
        port: Int = 1883,
        clientId: String = "android_" + System.currentTimeMillis(),
        cleanSession: Boolean = true,
        keepAliveInterval: Int = 60,
        listener: MqttClientListener? = null,
    ) {
        this.listener = listener
        connected = false
        val newClient: Mqtt3AsyncClient = MqttClient.builder()
            .useMqttVersion3()
            .serverHost(host)
            .serverPort(port)
            .identifier(clientId)
            .buildAsync()
        client = newClient
        newClient.connectWith()
            .cleanSession(cleanSession)
            .keepAlive(keepAliveInterval)
            .send()
            .whenComplete { _, throwable ->
                if (throwable == null) {
                    connected = true
                    mainHandler.post {
                        this@HiveMqClientManager.listener?.onConnectSuccess(false)
                    }
                } else {
                    mainHandler.post {
                        this@HiveMqClientManager.listener?.onError(throwable.message ?: "connect failed")
                    }
                }
            }
    }

    /**
     * 订阅主题（回调接收消息）。
     */
    fun subscribe(topic: String, qos: Int = 1) {
        val current = client ?: return
        current.subscribeWith()
            .topicFilter(topic)
            .qos(toQos(qos))
            .callback { publish ->
                val payload = String(publish.payloadAsBytes, Charsets.UTF_8)
                mainHandler.post {
                    this@HiveMqClientManager.listener?.onMessageArrived(publish.topic.toString(), payload)
                }
            }
            .send()
            .whenComplete { _, throwable ->
                if (throwable != null) {
                    mainHandler.post {
                        this@HiveMqClientManager.listener?.onError(throwable.message ?: "subscribe failed")
                    }
                }
            }
    }

    /**
     * 发布消息。
     */
    fun publish(topic: String, payload: String, qos: Int = 1) {
        val current = client ?: return
        current.publishWith()
            .topic(topic)
            .payload(payload.toByteArray(Charsets.UTF_8))
            .qos(toQos(qos))
            .send()
            .whenComplete { _, throwable ->
                if (throwable != null) {
                    mainHandler.post {
                        this@HiveMqClientManager.listener?.onError(throwable.message ?: "publish failed")
                    }
                }
            }
    }

    /**
     * 断开连接并释放资源。
     */
    fun disconnect() {
        val current = client ?: return
        connected = false
        try {
            current.disconnect()
        } catch (_: Exception) {
            // 忽略异常
        } finally {
            client = null
            listener = null
        }
    }

    /**
     * 当前是否已连接。
     */
    fun isConnected(): Boolean = connected

    private fun toQos(qos: Int): MqttQos = when (qos) {
        0 -> MqttQos.AT_MOST_ONCE
        2 -> MqttQos.EXACTLY_ONCE
        else -> MqttQos.AT_LEAST_ONCE
    }
}
