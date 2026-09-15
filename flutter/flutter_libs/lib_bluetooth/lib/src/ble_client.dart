import 'dart:async';
import 'package:flutter_blue_plus/flutter_blue_plus.dart';
import 'ble_models.dart';
import 'ble_session.dart';

/// 蓝牙客户端全局门面
///
/// 统一管理蓝牙硬件状态感知、设备扫描与过滤、连接会话创建。
class BleClient {
  BleClient._();

  static final BleClient _instance = BleClient._();
  static BleClient get instance => _instance;

  factory BleClient() => _instance;

  /// 监听手机蓝牙适配器状态变化
  Stream<BleAdapterState> get adapterState =>
      FlutterBluePlus.adapterState.map(BleAdapterState.fromFbp);

  /// 监听当前是否处于扫描中
  Stream<bool> get isScanning => FlutterBluePlus.isScanning;

  /// 实时扫描发现的设备列表流
  Stream<List<BleDeviceItem>> get scanResults => FlutterBluePlus.scanResults.map(
        (List<ScanResult> list) => list.map(BleDeviceItem.fromScanResult).toList(),
      );

  /// 开启 BLE 扫描
  Future<void> startScan({
    Duration timeout = const Duration(seconds: 15),
    List<String>? withServices,
    bool androidUsesFineLocation = true,
  }) async {
    final List<Guid> serviceGuids = withServices != null
        ? withServices.map((String uuid) => Guid(uuid)).toList()
        : <Guid>[];

    await FlutterBluePlus.startScan(
      timeout: timeout,
      withServices: serviceGuids,
      androidUsesFineLocation: androidUsesFineLocation,
    );
  }

  /// 停止 BLE 扫描
  Future<void> stopScan() async {
    await FlutterBluePlus.stopScan();
  }

  /// 为指定设备创建交互会话
  BleSession createSession(dynamic device) {
    if (device is BleDeviceItem) {
      return BleSession(device.rawDevice);
    } else if (device is BluetoothDevice) {
      return BleSession(device);
    } else if (device is String) {
      return BleSession(BluetoothDevice.fromId(device));
    }
    throw ArgumentError('Unsupported device type: ${device.runtimeType}');
  }

  /// 便捷连接并返回会话
  Future<BleSession> connect(
    dynamic device, {
    Duration timeout = const Duration(seconds: 10),
    bool autoConnect = false,
    int? requestMtuOnConnected,
  }) async {
    final BleSession session = createSession(device);
    await session.connect(
      timeout: timeout,
      autoConnect: autoConnect,
      requestMtuOnConnected: requestMtuOnConnected,
    );
    return session;
  }
}
