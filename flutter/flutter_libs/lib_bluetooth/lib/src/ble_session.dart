import 'dart:async';
import 'dart:math';
import 'package:flutter_blue_plus/flutter_blue_plus.dart';
import 'ble_models.dart';
import 'ble_utils.dart';

/// 单个 BLE 设备的交互会话
///
/// 封装连接维护、MTU 协商、服务树遍历、基础读写、**大数据自动切片分包发送** 与 **Notify 数据流订阅**。
class BleSession {
  BleSession(this.rawDevice) {
    _initStreams();
  }

  /// 底层 BluetoothDevice 实例
  final BluetoothDevice rawDevice;

  String get deviceId => rawDevice.remoteId.str;
  String get deviceName => rawDevice.platformName;

  int _mtu = 23;
  int get mtu => _mtu;

  /// 单包有效载荷限制 (MTU - 3)
  int get payloadLimit => max(20, _mtu - 3);

  final StreamController<BleConnectionStatus> _statusController =
      StreamController<BleConnectionStatus>.broadcast();
  Stream<BleConnectionStatus> get connectionStatus => _statusController.stream;

  final StreamController<int> _mtuController = StreamController<int>.broadcast();
  Stream<int> get mtuStream => _mtuController.stream;

  StreamSubscription<BluetoothConnectionState>? _connStateSub;
  StreamSubscription<int>? _mtuSub;

  List<BleServiceInfo> _services = <BleServiceInfo>[];
  List<BleServiceInfo> get services => List<BleServiceInfo>.unmodifiable(_services);

  void _initStreams() {
    _connStateSub = rawDevice.connectionState.listen((BluetoothConnectionState state) {
      final BleConnectionStatus status = BleConnectionStatus.fromFbp(state);
      _statusController.add(status);
    });

    _mtuSub = rawDevice.mtu.listen((int m) {
      _mtu = m;
      _mtuController.add(m);
    });
  }

  /// 建立 GATT 连接
  Future<void> connect({
    Duration timeout = const Duration(seconds: 10),
    bool autoConnect = false,
    int? requestMtuOnConnected,
  }) async {
    await rawDevice.connect(timeout: timeout, autoConnect: autoConnect);
    if (requestMtuOnConnected != null && requestMtuOnConnected > 23) {
      try {
        await requestMtu(requestMtuOnConnected);
      } catch (_) {}
    }
  }

  /// 断开连接
  Future<void> disconnect() async {
    await rawDevice.disconnect();
  }

  /// 发起 MTU 协商扩容
  Future<int> requestMtu(int desiredMtu) async {
    final int result = await rawDevice.requestMtu(desiredMtu);
    _mtu = result;
    return result;
  }

  /// 发现并解析 GATT 服务树
  Future<List<BleServiceInfo>> discoverServices() async {
    final List<BluetoothService> rawServices = await rawDevice.discoverServices();
    _services = rawServices.map((BluetoothService s) {
      final List<BleCharInfo> chars = s.characteristics.map(BleCharInfo.fromFbp).toList();
      return BleServiceInfo(
        uuid: s.uuid.str,
        characteristics: chars,
        rawService: s,
      );
    }).toList();
    return _services;
  }

  /// 依据 UUID 查找特征值实例
  BluetoothCharacteristic? _findCharacteristic(String charUuid, {String? serviceUuid}) {
    for (final BleServiceInfo service in _services) {
      if (serviceUuid != null &&
          !service.uuid.toLowerCase().contains(serviceUuid.toLowerCase())) {
        continue;
      }
      for (final BleCharInfo char in service.characteristics) {
        if (char.uuid.toLowerCase().contains(charUuid.toLowerCase())) {
          return char.rawCharacteristic;
        }
      }
    }
    return null;
  }

  /// 读取特征值数据
  Future<List<int>> read({
    required String characteristicUuid,
    String? serviceUuid,
  }) async {
    final BluetoothCharacteristic? char = _findCharacteristic(
      characteristicUuid,
      serviceUuid: serviceUuid,
    );
    if (char == null) {
      throw StateError('Characteristic not found: $characteristicUuid');
    }
    return await char.read();
  }

  /// 单包写入数据
  Future<void> write({
    required String characteristicUuid,
    required List<int> data,
    String? serviceUuid,
    bool withoutResponse = false,
  }) async {
    final BluetoothCharacteristic? char = _findCharacteristic(
      characteristicUuid,
      serviceUuid: serviceUuid,
    );
    if (char == null) {
      throw StateError('Characteristic not found: $characteristicUuid');
    }
    await char.write(data, withoutResponse: withoutResponse);
  }

  /// 大数据自动切片分包发送（Chunking 发送引擎）
  ///
  /// 根据当前协商的 MTU（或指定的 [customChunkSize]）自动将 [data] 切分为多个子包，
  /// 并在每个切片发送后插入 [interval] 流控等待，避免底层蓝牙栈拥塞。
  Future<void> writeChunked({
    required String characteristicUuid,
    required List<int> data,
    String? serviceUuid,
    int? customChunkSize,
    Duration interval = const Duration(milliseconds: 50),
    bool withoutResponse = false,
    void Function(int currentChunk, int totalChunks, double progress)? onProgress,
  }) async {
    final BluetoothCharacteristic? char = _findCharacteristic(
      characteristicUuid,
      serviceUuid: serviceUuid,
    );
    if (char == null) {
      throw StateError('Characteristic not found: $characteristicUuid');
    }

    final int chunkSize = customChunkSize ?? payloadLimit;
    final List<List<int>> chunks = BleUtils.chunkBytes(data, chunkSize);
    final int total = chunks.length;

    for (int i = 0; i < total; i++) {
      await char.write(chunks[i], withoutResponse: withoutResponse);
      onProgress?.call(i + 1, total, (i + 1) / total);

      if (i < total - 1 && interval > Duration.zero) {
        await Future<void>.delayed(interval);
      }
    }
  }

  /// 开启或关闭特征值的 Notify / Indicate 通知，并返回数据流
  Stream<List<int>> listenNotification({
    required String characteristicUuid,
    String? serviceUuid,
    bool enable = true,
  }) {
    final BluetoothCharacteristic? char = _findCharacteristic(
      characteristicUuid,
      serviceUuid: serviceUuid,
    );
    if (char == null) {
      throw StateError('Characteristic not found: $characteristicUuid');
    }

    char.setNotifyValue(enable);
    return char.lastValueStream;
  }

  /// 释放与清理会话
  Future<void> dispose() async {
    _connStateSub?.cancel();
    _mtuSub?.cancel();
    await _statusController.close();
    await _mtuController.close();
  }
}
