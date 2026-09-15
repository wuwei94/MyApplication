import 'package:flutter_blue_plus/flutter_blue_plus.dart';

/// 蓝牙适配器状态
enum BleAdapterState {
  unknown,
  unavailable,
  unauthorized,
  turningOn,
  on,
  turningOff,
  off;

  static BleAdapterState fromFbp(BluetoothAdapterState state) {
    switch (state) {
      case BluetoothAdapterState.unknown:
        return BleAdapterState.unknown;
      case BluetoothAdapterState.unavailable:
        return BleAdapterState.unavailable;
      case BluetoothAdapterState.unauthorized:
        return BleAdapterState.unauthorized;
      case BluetoothAdapterState.turningOn:
        return BleAdapterState.turningOn;
      case BluetoothAdapterState.on:
        return BleAdapterState.on;
      case BluetoothAdapterState.turningOff:
        return BleAdapterState.turningOff;
      case BluetoothAdapterState.off:
        return BleAdapterState.off;
    }
  }
}

/// 蓝牙连接状态
enum BleConnectionStatus {
  disconnected,
  connecting,
  connected,
  disconnecting;

  static BleConnectionStatus fromFbp(BluetoothConnectionState state) {
    if (state == BluetoothConnectionState.connected) {
      return BleConnectionStatus.connected;
    } else {
      return BleConnectionStatus.disconnected;
    }
  }
}

/// 扫描发现的 BLE 设备模型
class BleDeviceItem {
  const BleDeviceItem({
    required this.id,
    required this.name,
    required this.rssi,
    required this.serviceUuids,
    required this.connectable,
    required this.rawDevice,
    this.rawAdvData,
  });

  /// 设备的物理 MAC 或 iOS UUID 标识符
  final String id;

  /// 设备对外广播的名称
  final String name;

  /// 接收信号强度指示 (单位 dBm)
  final int rssi;

  /// 广播中携带的 Service UUID 列表 (16 位或 128 位)
  final List<String> serviceUuids;

  /// 是否为可连接型广播
  final bool connectable;

  /// 底层平台 BluetoothDevice 实例
  final BluetoothDevice rawDevice;

  /// 原始广播数据
  final AdvertisementData? rawAdvData;

  factory BleDeviceItem.fromScanResult(ScanResult result) {
    final String deviceName = result.device.platformName.isNotEmpty
        ? result.device.platformName
        : (result.advertisementData.advName.isNotEmpty
            ? result.advertisementData.advName
            : '未知设备');

    return BleDeviceItem(
      id: result.device.remoteId.str,
      name: deviceName,
      rssi: result.rssi,
      serviceUuids: result.advertisementData.serviceUuids
          .map((Guid g) => g.str)
          .toList(),
      connectable: result.advertisementData.connectable,
      rawDevice: result.device,
      rawAdvData: result.advertisementData,
    );
  }
}

/// GATT 服务抽象信息
class BleServiceInfo {
  const BleServiceInfo({
    required this.uuid,
    required this.characteristics,
    required this.rawService,
  });

  final String uuid;
  final List<BleCharInfo> characteristics;
  final BluetoothService rawService;
}

/// GATT 特征值抽象信息
class BleCharInfo {
  const BleCharInfo({
    required this.uuid,
    required this.canRead,
    required this.canWrite,
    required this.canWriteWithoutResponse,
    required this.canNotify,
    required this.canIndicate,
    required this.rawCharacteristic,
  });

  final String uuid;
  final bool canRead;
  final bool canWrite;
  final bool canWriteWithoutResponse;
  final bool canNotify;
  final bool canIndicate;
  final BluetoothCharacteristic rawCharacteristic;

  factory BleCharInfo.fromFbp(BluetoothCharacteristic c) {
    return BleCharInfo(
      uuid: c.uuid.str,
      canRead: c.properties.read,
      canWrite: c.properties.write,
      canWriteWithoutResponse: c.properties.writeWithoutResponse,
      canNotify: c.properties.notify,
      canIndicate: c.properties.indicate,
      rawCharacteristic: c,
    );
  }
}
