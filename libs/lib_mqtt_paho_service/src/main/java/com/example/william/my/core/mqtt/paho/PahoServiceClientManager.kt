package com.example.william.my.core.mqtt.paho

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.example.william.my.core.mqtt.MqttClientListener
import info.mqtt.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage

/**
 * Eclipse Paho Android Service 客户端门面。
 *
 * 基于 [MqttAndroidClient]，通过绑定 `info.mqtt.android.service.MqttService`
 * 在 Android 平台运行（基于 BroadcastReceiver + Service 通信）。
 * 回调通过 [MqttClientListener] 统一切回主线程。
 *
 * 注意：使用 hannesa2 维护 fork（`com.github.hannesa2:paho.mqtt.android`）。
 * 官方 `org.eclipse.paho` 1.1.1 已停更，其 AlarmPingSender 注册 Receiver 缺少
 * 导出标志，在 targetSdk 34+ 上连接成功即抛 SecurityException。
 *
 * 注意：MqttAndroidClient 基于 BroadcastReceiver + Service，连接操作异步执行；
 * 首次连接成功由 [MqttCallbackExtended.connectComplete] 回调，失败由 [IMqttActionListener.onFailure] 回调。
 *
 * https://github.com/hannesa2/paho.mqtt.android
 */
object PahoServiceClientManager {

    private val mainHandler = Handler(Looper.getMainLooper())

    @Volatile
    private var client: MqttAndroidClient? = null

    @Volatile
    private var listener: MqttClientListener? = null

    /**
     * 建立 MQTT 连接。
     *
     * @param context          Context（内部使用 applicationContext 避免持有 Activity）
     * @param broker           Broker 地址，形如 `tcp://broker.emqx.io:1883`
     * @param clientId         客户端 ID，同一 Broker 下需唯一
     * @param cleanSession     是否清除会话
     * @param autoReconnect    是否自动重连
     * @param keepAliveInterval 心跳间隔（秒）
     * @param listener         回调
     */
    fun connect(
        context: Context,
        broker: String,
        clientId: String = "android_" + System.currentTimeMillis(),
        cleanSession: Boolean = true,
        autoReconnect: Boolean = true,
        keepAliveInterval: Int = 60,
        listener: MqttClientListener? = null,
    ) {
        this.listener = listener
        val newClient = MqttAndroidClient(context.applicationContext, broker, clientId)
        newClient.setCallback(object : MqttCallbackExtended {
            override fun connectComplete(reconnect: Boolean, serverURI: String) {
                mainHandler.post {
                    this@PahoServiceClientManager.listener?.onConnectSuccess(reconnect)
                }
            }

            override fun connectionLost(cause: Throwable?) {
                mainHandler.post { this@PahoServiceClientManager.listener?.onConnectionLost() }
            }

            override fun messageArrived(topic: String, message: MqttMessage) {
                val payload = String(message.payload, Charsets.UTF_8)
                mainHandler.post {
                    this@PahoServiceClientManager.listener?.onMessageArrived(topic, payload)
                }
            }

            override fun deliveryComplete(token: IMqttDeliveryToken) {
                // 发布完成回调，默认不处理
            }
        })
        client = newClient
        val options = MqttConnectOptions().apply {
            isCleanSession = cleanSession
            isAutomaticReconnect = autoReconnect
            connectionTimeout = 10
            this.keepAliveInterval = keepAliveInterval
        }
        newClient.connect(
            options,
            null,
            object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    // 连接成功由 connectComplete 回调统一处理
                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    mainHandler.post {
                        this@PahoServiceClientManager.listener?.onError(exception.message ?: "connect failed")
                    }
                }
            },
        )
    }

    /**
     * 订阅主题。
     */
    fun subscribe(topic: String, qos: Int = 1) {
        val current = client ?: return
        current.subscribe(
            topic,
            qos,
            null,
            object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    // 订阅成功，忽略
                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    mainHandler.post {
                        this@PahoServiceClientManager.listener?.onError(exception.message ?: "subscribe failed")
                    }
                }
            },
        )
    }

    /**
     * 发布消息。
     */
    fun publish(topic: String, payload: String, qos: Int = 1, retained: Boolean = false) {
        val current = client ?: return
        val message = MqttMessage(payload.toByteArray(Charsets.UTF_8)).apply {
            this.qos = qos
            isRetained = retained
        }
        current.publish(
            topic,
            message,
            null,
            object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    // 发布成功，忽略
                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    mainHandler.post {
                        this@PahoServiceClientManager.listener?.onError(exception.message ?: "publish failed")
                    }
                }
            },
        )
    }

    /**
     * 断开连接并释放资源。
     */
    fun disconnect() {
        val current = client ?: return
        try {
            current.disconnect(
                null,
                object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken) {
                        // 忽略
                    }

                    override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                        // 忽略
                    }
                },
            )
            current.close()
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
    fun isConnected(): Boolean = client?.isConnected == true
}
