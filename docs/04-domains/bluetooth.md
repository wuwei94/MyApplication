# 低功耗蓝牙（BLE 客户端）开发指南

> 本文档系统梳理低功耗蓝牙（Bluetooth Low Energy, BLE）客户端的核心功能体系、GATT 通信机制以及各主流第三方库（Nordic BLE / FastBle / RxAndroidBle / Flutter）的功能覆盖与选型对比。

---

## 一、 BLE 客户端核心功能全景

BLE 客户端（Central / Client）开发主要包含以下 **8 个核心功能环节**：

### 1. 权限与状态检测
- **动态权限申请**：
  - Android 12+ (API 31+)：声明并申请 `BLUETOOTH_SCAN`（可配置 `neverForLocation`）、`BLUETOOTH_CONNECT`、`BLUETOOTH_ADVERTISE`。
  - Android 11 及以下：声明 `BLUETOOTH`、`BLUETOOTH_ADMIN`，并申请 `ACCESS_FINE_LOCATION` 或 `ACCESS_COARSE_LOCATION` 位置权限。
- **状态检测与监听**：检测手机蓝牙硬件是否开启，监听系统蓝牙开关状态的广播变化。

### 2. 扫描与发现
- **扫描生命周期控制**：开启扫描（`startScan`）、停止扫描（`stopScan`）。
- **超时与模式配置**：设置扫描超时时间（如搜索 10 秒后自动停止），配置扫描工作模式（低功耗模式 `SCAN_MODE_LOW_POWER` vs 低延迟高灵敏度模式 `SCAN_MODE_LOW_LATENCY`）。

### 3. 过滤与广播解析
- **多维度规则过滤**：按设备名称（Device Name）、MAC 地址、Service UUID 列表进行扫描过滤，避免处理无关设备。
- **信号强度与测距**：实时获取信号强度（RSSI，单位 dBm），结合发射功率（TxPower）估算物理距离。
- **广播包数据解析**：解析广播包（AdvData）与扫描响应包（ScanResponse）中的厂商自定义数据（Manufacturer Data）、广播状态标志（Flags）、iBeacon 数据包结构等。

### 4. 连接与状态维护
- **连接生命周期**：发起连接（`connectGatt`）、主动断开（`disconnect`）、销毁释放资源（`close`）。
- **状态监听**：监听连接状态迁移（Disconnected ➔ Connecting ➔ Connected ➔ Disconnecting）。
- **异常保护与重连**：连接超时保护、断线异常捕获与自动重连策略。
- **安全与配对**：配对（Pairing）与绑定（Bonding）状态管理及密钥保存。

### 5. 发现服务与特征（GATT 树）
- **服务发现**：连接成功后触发服务发现（`discoverServices`），构建完整的 GATT 树状层级。
- **结构与权限解析**：
  - **Service（服务）**：包含服务 UUID 与主从类型。
  - **Characteristic（特征值）**：核心数据交互节点，解析其权限属性（`Read` / `Write` / `Write Without Response` / `Notify` / `Indicate`）。
  - **Descriptor（描述符）**：特征值的配置描述项（如用于开启通知的客户端配置描述符 CCCD `0x2902`）。

### 6. 数据收发（核心交互）
- **读取（Read）**：客户端主动发起请求，拉取设备的一条数据（如设备电量、硬件版本号）。
- **写入（Write）**：
  - *Write with Response*：带响应写入，设备端接收并处理完后向客户端返回确认包（ACK），可靠性高。
  - *Write Without Response*：无响应写入，客户端连续快速发送，设备不回底层 ACK，吞吐速度快。
- **监听通知（Notify / Indicate）**：
  - *Notify*：开启监听后，设备端有新数据主动推送到客户端，无需客户端回 ACK。
  - *Indicate*：类似 Notify，但客户端收到数据后必须向设备端回传确认包。

### 7. MTU 协商（扩容单包容量）
- **单包限制**：BLE 默认的 ATT MTU 为 23 字节，去除 3 字节协议头后，**默认单包有效数据（Payload）最多仅 20 字节**。
- **MTU 协商**：连接建立后向设备端发起 MTU 请求（如协商至 512 字节），协商成功后单包有效载荷可扩展至数百字节，极大提升数据吞吐效率。

### 8. 指令排队与大包分包
- **GATT 指令排队**：Android 原生蓝牙底层是**严格单任务串行**的，若上一个指令尚未收到回调就发起下一个指令，系统会直接返回 `false` 并丢包。必须在上层构建 FIFO 串行队列保证指令按序执行。
- **大数据分包与拼包（Chunking & Merging）**：在传输长文本、图片或固件升级包（几 KB ~ 几 MB）时，自动根据当前 MTU 上限切片为多个小包依次发送；接收端流式收集各切片并合并还原为完整业务帧。

---

## 二、 方案与主流第三方库对比

各三方库在基础功能层面均覆盖了上述 8 大环节，但在**架构设计、易用性、底层并发保护与高级能力**上各有侧重：

| 方案 / 库 | 架构定位 | 核心优势 | 局限性 / 适用场景 |
| :--- | :--- | :--- | :--- |
| **Android 原生 SDK**<br>(`android.bluetooth.*`) | 系统底层 API | 零三方依赖，完整展现 Android 底层蓝牙状态机与回调流程。 | ❌ 无内置队列，并发调用直接失败；回调嵌套深。<br>🎯 适合用于**理解底层机制与教学**。 |
| **FastBle**<br>(`com.github.Jasonchenlijian:FastBle`) | 国内极简轻量封装库 | 1. 极简链式调用，上手成本极低。<br>2. 读写直接传入 UUID 字符串，隐藏底层复杂对象。<br>3. `BleScanRuleConfig` 快速配置过滤规则。 | ⚠️ 大数据分包需业务层自行处理。<br>🎯 适合**中小型项目、业务逻辑简单的硬件快速对接**。 |
| **Nordic BLE Library**<br>(`no.nordicsemi.android:ble`) | 芯片原厂工业级标准库 | 1. 工业级严密的 GATT 事务请求队列与重试机制。<br>2. 内置 `.split()` 自动按 MTU 切包与 `.merge()` 拼包。<br>3. 完善的 Bonding 配对兼容性处理。<br>4. 支持 Kotlin 协程 `suspend` 挂起调用风格。 | ⚠️ 架构相对较重，需继承 `BleManager`。<br>🎯 **企业级 IoT、医疗健康、智能车载、OTA 固件升级首选**。 |
| **RxAndroidBle**<br>(`com.polidea.rxandroidble3:rxandroidble`) | 响应式流控封装库 | 1. 一切皆为 RxJava `Observable` 数据流。<br>2. 丰富的操作符（`filter` / `sample` / `debounce`）轻松实现高频数据流控。<br>3. `Disposable.dispose()` 自动释放连接与监听，避免泄漏。 | ⚠️ 团队需具备 RxJava 熟练度。<br>🎯 适合**重度采用 RxJava 响应式架构、多传感器数据融合项目**。 |
| **Flutter (`flutter_blue_plus`)**<br>(`flutter_blue_plus`) | 跨平台统一封装库 | 1. 抹平 Android 与 iOS (`CoreBluetooth`) 的底层差异。<br>2. 现代化 Dart `Stream` 与 `async/await` 接口。 | ⚠️ 跨平台受限于底层系统差异，部分极特殊特性需原生通道补充。<br>🎯 **Flutter 跨端应用首选**。 |

---

## 三、 工程示例索引

| 模块 / 平台 | 示例文件 | 对应路由 / 标识 | 演示重点 |
| :--- | :--- | :--- | :--- |
| `module_bluetooth` (Android 原生) | `BleNativeScanActivity.kt` | `/Bluetooth/NativeScan` | 原生扫描、权限申请、RSSI 原位刷新与广播包解析 |
| `module_bluetooth` (Android 原生) | `BleNativeConnectActivity.kt` | `/Bluetooth/NativeConnect` | 原生 GATT 连接、服务树发现、特征读写与 CCCD 订阅 |
| `module_bluetooth` (Android 原生) | `BleNativeQueueActivity.kt` | `/Bluetooth/NativeQueue` | 协程 `Channel` 串行队列防并发冲突、大数据 Chunking 切片 |
| `module_bluetooth` (Nordic 方案) | `BleNordicScanActivity.kt` | `/Bluetooth/NordicScan` | Nordic 过滤扫描与广播规范 |
| `module_bluetooth` (Nordic 方案) | `BleNordicConnectActivity.kt` | `/Bluetooth/NordicConnect` | `BleManager` 工业级架构、自动重试与 `suspend` 读写 |
| `module_bluetooth` (Nordic 方案) | `BleNordicTransferActivity.kt` | `/Bluetooth/NordicTransfer` | `.split()` 自动切包、`.merge()` 拼包与传输速率统计 |
| `module_bluetooth` (FastBle 方案) | `BleFastScanActivity.kt` | `/Bluetooth/FastScan` | FastBle 规则配置与单例扫描 |
| `module_bluetooth` (FastBle 方案) | `BleFastConnectActivity.kt` | `/Bluetooth/FastConnect` | FastBle 链式连接、UUID 驱动读写与 Notify 回调 |
| `module_bluetooth` (RxBle 方案) | `BleRxScanActivity.kt` | `/Bluetooth/RxScan` | Observable 扫描数据流与 Rx 操作符流控 |
| `module_bluetooth` (RxBle 方案) | `BleRxConnectActivity.kt` | `/Bluetooth/RxConnect` | `establishConnection` 管道、`flatMap` 串联与自动释放 |
| `flutter_demo` (Flutter 端) | `ble_scan_demo.dart` | `BluetoothCatalog.scan` | 蓝牙状态流监听、扫描启停与过滤 |
| `flutter_demo` (Flutter 端) | `ble_device_demo.dart` | `BluetoothCatalog.device` | 设备连接、MTU 协商、GATT 树折叠浏览与特征交互 |
| `flutter_demo` (Flutter 端) | `ble_transfer_demo.dart` | `BluetoothCatalog.transfer` | MTU 切换、大文本切片流控发送与拼包合并 |
