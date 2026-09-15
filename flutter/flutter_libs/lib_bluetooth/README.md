# lib_bluetooth

MyApplication 的 Flutter 蓝牙通信封装库，基于 `flutter_blue_plus` 进行面向对象抽象与统一设计。

公共 API 统一从 `package:lib_bluetooth/lib_bluetooth.dart` 导出，不依赖 `flutter_demo`。

## 核心契约与架构

- **`BleClient`**：全局单例（`instance` 与工厂构造返回同一实例），负责适配器状态感知（`adapterState`）、扫描启停（`startScan` / `stopScan`）与设备列表流（`scanResults`）。
- **`BleSession`**：单设备交互会话，封装设备连接（`connect` / `disconnect`）、MTU 协商（`requestMtu`）、服务与特征值发现（`discoverServices`）、特征值读写（`readCharacteristic` / `writeCharacteristic`）与 Notify 监听流（`listenNotification`）。
- **`BleUtils`**：提供通用辅助工具，包括大数据分包切片（`chunkBytes`）、CRC16 校验码计算（`calculateCrc16`）与 Hex / Bytes 互转。
- **`BleModels`**：定义跨平台统一的枚举与数据结构（`BleAdapterState` / `BleConnectionStatus` / `BleDeviceItem` / `BleServiceInfo` / `BleCharacteristicInfo` 等）。

## 基本用法

### 1. 扫描设备

```dart
final client = BleClient.instance;

// 监听扫描到的设备列表
client.scanResults.listen((List<BleDeviceItem> devices) {
  for (final device in devices) {
    print('发现设备: ${device.name} (${device.id}) RSSI: ${device.rssi}');
  }
});

// 开启扫描
await client.startScan(timeout: const Duration(seconds: 10));
```

### 2. 连接与 GATT 读写

```dart
// 为目标设备创建交互会话
final session = BleClient.instance.createSession(targetDevice);

// 发起连接
await session.connect();

// 协商 MTU
await session.requestMtu(247);

// 发现服务
final services = await session.discoverServices();

// 读取与写入特征值
final data = await session.readCharacteristic(serviceUuid, charUuid);
await session.writeCharacteristic(serviceUuid, charUuid, [0x01, 0x02, 0x03]);

// 订阅 Notify 数据流
session.listenNotification(serviceUuid, charUuid).listen((data) {
  print('收到外设推送数据: $data');
});

// 断开并释放
await session.disconnect();
session.dispose();
```
