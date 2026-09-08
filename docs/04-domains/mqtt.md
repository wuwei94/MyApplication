# MQTT 发布/订阅开发指南

> 本文档系统梳理 `:modules:module_mqtt` 的 MQTT（Message Queuing Telemetry Transport）能力：围绕「连接（Connect）→ 订阅（Subscribe）→ 发布（Publish，QoS 0/1/2）→ 断开（Disconnect）」的完整生命周期，提供 **HiveMQ MQTT Client**（异步 API）与 **Eclipse Paho Android Service**（`MqttAndroidClient`）两条客户端实现路径的对比。模块 Activity 清单见 [模块总览](../05-catalog/modules.md)。

---

## 一、核心概念：Broker、Topic 与 QoS

**MQTT** 是基于发布/订阅模型的轻量级消息协议，客户端不与对方直接通信，而是统一连接 **Broker（消息代理）**，通过 **Topic（主题）** 的层次化通配订阅实现解耦。本模块全部示例使用 **EMQX 公共 Broker**（`broker.emqx.io:1883`，无需账号），**订阅与发布到同一 Topic 即可收到自己发出的消息**，便于本地验证闭环。

**QoS（服务质量）** 决定消息投递的可靠性等级：

| QoS | 语义 | 投递保证 | 示例中的使用 |
| :--- | :--- | :--- | :--- |
| 0 | At most once（最多一次） | 尽力而为，不确认、不重发 | `publish(topic, msg, qos = 0)` |
| 1 | At least once（至少一次） | 至少送达一次，可能重复 | `publish(topic, msg, qos = 1)` |
| 2 | Exactly once（恰好一次） | 四段握手确保不重不丢 | `subscribe(topic, qos = 2)`、`publish(topic, msg, qos = 2)` |

> 注意区分：QoS 是「客户端 ↔ Broker」之间单跳的投递约定，并非端到端保证；发布与订阅两侧各自声明 QoS，实际生效等级取两者较低者。

---

## 二、两条客户端实现路径对比

模块同时封装了两套业界主流 MQTT 客户端，共用同一个回调抽象（`MqttClientListener`，见第四节），示例页面操作流程完全一致（先连接、再订阅、最后发布），可无感切换对比：

| 维度 | HiveMQ MQTT Client | Eclipse Paho Android Service |
| :--- | :--- | :--- |
| **封装库** | `lib_mqtt_hivemq` | `lib_mqtt_paho_service` |
| **门面类** | `HiveMqClientManager` | `PahoServiceClientManager` |
| **底层客户端** | `Mqtt3AsyncClient`（异步 API） | `MqttAndroidClient` |
| **API 风格** | 流式 Builder + `CompletableFuture` 回调 | `IMqttActionListener` 动作回调 + `MqttCallbackExtended` 消息回调 |
| **协议版本** | 使用 MQTT 3.1.1（库本身支持 MQTT 5.0） | MQTT 3.1.1 |
| **Android 集成** | 纯异步客户端，不依赖系统组件 | 绑定 `MqttService`（BroadcastReceiver + Service 通信） |
| **额外依赖** | Netty（NIO 事件循环） | 官方 1.1.1 已停更，使用 hannesa2 fork（适配 targetSdk 34+） |
| **示例路由** | `/Mqtt/HiveMqClient` | `/Mqtt/PahoServiceClient` |

**选型建议**：偏好现代异步 API、需要 MQTT 5.0 特性（共享订阅、消息过期等）时选 **HiveMQ**；希望借助 Android Service 组件在后台维持连接、自动重连（`isAutomaticReconnect`）时选 **Paho Android Service**。

---

## 三、HiveMQ 封装（`lib_mqtt_hivemq`）

`HiveMqClientManager` 是一个无状态门面单例，内部持有 `Mqtt3AsyncClient` 并通过 `Handler(Looper.getMainLooper())` 把全部回调切回主线程，页面可直接更新 UI：

```kotlin
// 连接（clientId 默认 "android_<时间戳>"，cleanSession = true，keepAlive = 60s）
HiveMqClientManager.connect(host = "broker.emqx.io", port = 1883, listener = ...)

// 订阅（示例 QoS 2）
HiveMqClientManager.subscribe("mqtt/example", qos = 2)

// 发布（示例依次演示 QoS 0 / 1 / 2）
HiveMqClientManager.publish("mqtt/example", "Hello HiveMQ!", qos = 1)

// 断开并释放资源
HiveMqClientManager.disconnect()
```

内部使用 `MqttClient.builder().useMqttVersion3()...buildAsync()` 构建客户端，连接成功后由订阅回调（`subscribeWith().callback {}`）接收消息并以 UTF-8 解码 payload；每次调用前页面通过 `isConnected()` 校验连接状态，`onDestroy()` 中统一 `disconnect()` 防泄漏。

---

## 四、Paho 封装（`lib_mqtt_paho_service`）

`PahoServiceClientManager` 基于 hannesa2 fork 的 `MqttAndroidClient`（`info.mqtt.android.service.MqttService`），连接参数默认 `cleanSession = true`、`autoReconnect = true`、`keepAlive = 60s`、`connectionTimeout = 10s`：

```kotlin
// 连接（broker 需带协议前缀；内部使用 applicationContext 避免持有 Activity）
PahoServiceClientManager.connect(context, broker = "tcp://broker.emqx.io:1883", listener = ...)

PahoServiceClientManager.subscribe("mqtt/example", qos = 2)
PahoServiceClientManager.publish("mqtt/example", "Hello Paho!", qos = 1)
PahoServiceClientManager.disconnect()
```

**关键坑位（已在库内注释标注）**：

* 官方 `org.eclipse.paho` 1.1.1 已停更，其 `AlarmPingSender` 注册 Receiver 缺少导出标志，在 **targetSdk 34+ 上连接成功即抛 SecurityException**；
* 因此依赖改用 hannesa2 fork（`com.github.hannesa2:paho.mqtt.android`），fork 的 AAR 已自带 Service 声明，**无需在 AndroidManifest 手动注册**；
* `MqttAndroidClient` 基于 BroadcastReceiver + Service，连接异步执行：首次连接成功由 `MqttCallbackExtended.connectComplete(reconnect, serverURI)` 回调（`reconnect` 区分自动重连），失败由 `IMqttActionListener.onFailure` 回调。

---

## 五、统一回调抽象（`lib_mqtt` 的 `MqttClientListener`）

两条实现共享同一抽象监听器（位于 `lib_mqtt` 的 `core/mqtt` 包，两个 Manager 库共同依赖），**所有回调均在主线程执行**：

| 回调 | 触发时机 |
| :--- | :--- |
| `onConnectSuccess(reconnect: Boolean)` | 连接成功；`reconnect = true` 表示自动重连成功后触发 |
| `onConnectionLost()` | 连接丢失（Paho 线将等待自动重连） |
| `onMessageArrived(topic: String, payload: String)` | 收到订阅消息，payload 已按 UTF-8 解码 |
| `onError(message: String)` | 连接 / 订阅 / 发布失败的错误描述 |

常量约定（`basic_shared` 的 `Constants`）：`Mqtt_Broker = "tcp://broker.emqx.io:1883"`、`Mqtt_Host = "broker.emqx.io"`、`Mqtt_Port = 1883`、`Mqtt_Topic = "mqtt/example"`。接入自有 Broker 时只需替换这组常量。

---

## 六、工程示例索引

入口：`MqttMainActivity`（路由 `/Mqtt/Main`，以分组列表同时展示两条实现）。完整 Activity 与路由清单见 [modules.md](../05-catalog/modules.md)；封装库位于 `libs/lib_mqtt`（回调抽象）、`libs/lib_mqtt_hivemq`（HiveMQ 门面）与 `libs/lib_mqtt_paho_service`（Paho 门面）。
