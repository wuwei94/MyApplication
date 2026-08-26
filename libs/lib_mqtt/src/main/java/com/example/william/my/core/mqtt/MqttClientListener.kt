package com.example.william.my.core.mqtt

/**
 * MQTT 客户端回调接口。
 *
 * 所有回调均在主线程执行，可直接更新 UI。
 */
abstract class MqttClientListener {

    /**
     * 连接成功。
     *
     * @param reconnect true 表示自动重连成功后触发，false 表示首次连接成功
     */
    open fun onConnectSuccess(reconnect: Boolean) {}

    /**
     * 连接丢失（等待自动重连）。
     */
    open fun onConnectionLost() {}

    /**
     * 收到订阅消息。
     *
     * @param topic   消息主题
     * @param payload 消息内容（UTF-8 解码）
     */
    open fun onMessageArrived(topic: String, payload: String) {}

    /**
     * 发生错误。
     *
     * @param message 错误描述
     */
    open fun onError(message: String) {}
}
