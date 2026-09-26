import 'dart:async';

import 'package:flutter_demo/core/basic/basic.dart';
import 'package:lib_bluetooth/lib_bluetooth.dart';

/// BLE 连接 — GATT 连接、服务发现与特征值读写
///
/// 核心机制与避坑点：
/// 1. 调用顺序：`connect` → `discoverServices` 之后才能 `read` / `write` / `listenNotification`。
/// 2. MTU 协商：默认 23（Payload 20），`requestMtu` 成功后再做大包写入。
/// 3. 特征查找：`characteristicUuid` 走子串匹配，未命中抛 `StateError`。
///
/// 官方参考：
/// https://pub.dev/packages/flutter_blue_plus
class BleDeviceDemoPage extends BasicResponsePage {
  const BleDeviceDemoPage({super.key, required super.title});

  @override
  BasicResponsePageState<BleDeviceDemoPage> createState() =>
      _BleDeviceDemoPageState();
}

class _BleDeviceDemoPageState
    extends BasicResponsePageState<BleDeviceDemoPage> {
  static const String _statusKey = 'status';
  static const String _notifyKey = 'notify';
  static const int _desiredMtu = 512;
  static const Duration _scanTimeout = Duration(seconds: 4);
  static const Duration _connectTimeout = Duration(seconds: 10);

  BleDeviceItem? _targetDevice;
  BleSession? _session;
  List<BleServiceInfo> _services = <BleServiceInfo>[];
  int _currentMtu = 23;

  StreamSubscription<BleConnectionStatus>? _connectionStateSub;
  StreamSubscription<int>? _mtuSub;
  StreamSubscription<List<int>>? _notifySub;
  StreamSubscription<List<BleDeviceItem>>? _scanPickSub;

  @override
  void initState() {
    super.initState();
    showDescription(
      'BLE 连接示例：扫描选设备 → 连接 → 发现服务 → MTU 协商 → 读写 / Notify',
    );
  }

  @override
  void dispose() {
    _connectionStateSub?.cancel();
    _mtuSub?.cancel();
    _notifySub?.cancel();
    _scanPickSub?.cancel();
    final BleSession? session = _session;
    _session = null;
    if (session != null) {
      unawaited(session.disconnect());
      unawaited(session.dispose());
    }
    super.dispose();
  }

  @override
  List<String> buildList() => const <String>[
        '1. 扫描并选择首个设备',
        '2. 连接目标设备',
        '3. 发现 GATT 服务',
        '4. 请求扩展 MTU',
        '5. 读取首个可读特征值',
        '6. 写入测试数据',
        '7. 订阅 Notify 通知',
        '8. 断开设备连接',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        _scanAndSelectDevice();
      case 1:
        _connectDevice();
      case 2:
        _discoverServices();
      case 3:
        _requestMtu();
      case 4:
        _readCharacteristic();
      case 5:
        _writeCharacteristic();
      case 6:
        _toggleNotification();
      case 7:
        _disconnectDevice();
    }
  }

  BleSession? get _readySession {
    final BleSession? session = _session;
    if (session == null) {
      appendLog('✗ 请先扫描并连接目标设备');
      return null;
    }
    return session;
  }

  BleCharInfo? _pickChar(bool Function(BleCharInfo char) predicate) {
    for (final BleServiceInfo service in _services) {
      for (final BleCharInfo char in service.characteristics) {
        if (predicate(char)) {
          return char;
        }
      }
    }
    return null;
  }

  Future<void> _scanAndSelectDevice() async {
    appendLog('→ [Scan] 正在扫描周围 BLE 设备（$_scanTimeout）...');
    final Completer<BleDeviceItem> completer = Completer<BleDeviceItem>();
    final StreamSubscription<List<BleDeviceItem>>? oldScanSub = _scanPickSub;
    _scanPickSub = null;
    if (oldScanSub != null) {
      unawaited(oldScanSub.cancel());
    }
    _scanPickSub =
        BleClient.instance.scanResults.listen((List<BleDeviceItem> results) {
      if (results.isEmpty || completer.isCompleted) {
        return;
      }
      completer.complete(results.first);
    });

    try {
      unawaited(
        BleClient.instance.startScan(timeout: _scanTimeout),
      );
      final BleDeviceItem device = await completer.future.timeout(
        _scanTimeout + const Duration(seconds: 1),
      );
      await BleClient.instance.stopScan();
      if (!mounted) return;
      appendLog('✓ [Scan] 发现目标设备: ${device.name} (${device.id})');
      _attachDevice(device);
    } on TimeoutException {
      await BleClient.instance.stopScan();
      appendLog('✗ [Scan] 扫描超时，未发现设备');
    } catch (error) {
      await BleClient.instance.stopScan();
      appendLog('✗ [Scan] 扫描失败: $error');
    } finally {
      final StreamSubscription<List<BleDeviceItem>>? sub = _scanPickSub;
      _scanPickSub = null;
      if (sub != null) {
        unawaited(sub.cancel());
      }
    }
  }

  void _attachDevice(BleDeviceItem device) {
    _connectionStateSub?.cancel();
    _mtuSub?.cancel();
    _notifySub?.cancel();
    final BleSession? oldSession = _session;
    _session = null;
    if (oldSession != null) {
      unawaited(oldSession.dispose());
    }

    _targetDevice = device;
    final BleSession session = BleClient.instance.createSession(device);
    _session = session;

    _connectionStateSub =
        session.connectionStatus.listen((BleConnectionStatus state) {
      updateLog(_statusKey, '连接状态: ${state.name}');
      appendLog('→ [Status] 连接状态变更: ${state.name}');
      if (state == BleConnectionStatus.connected) {
        _discoverServices();
      }
    });

    _mtuSub = session.mtuStream.listen((int mtu) {
      _currentMtu = mtu;
      updateLog(_statusKey, '连接状态: connected, MTU=$_currentMtu');
    });
  }

  Future<void> _connectDevice() async {
    final BleSession? session = _session;
    if (session == null) {
      appendLog('✗ [Connect] 请先扫描并选择设备');
      return;
    }
    final BleDeviceItem? device = _targetDevice;
    appendLog(
      '→ [Connect] 正在连接 ${device?.name ?? session.deviceId} ...',
    );
    try {
      await session.connect(
        timeout: _connectTimeout,
        autoConnect: false,
      );
      appendLog('✓ [Connect] 连接指令已发出');
    } catch (error) {
      appendLog('✗ [Connect] 连接异常: $error');
    }
  }

  Future<void> _disconnectDevice() async {
    final BleSession? session = _readySession;
    if (session == null) return;
    appendLog('→ [Disconnect] 正在断开连接...');
    try {
      await session.disconnect();
      appendLog('✓ [Disconnect] 设备已断开');
    } catch (error) {
      appendLog('✗ [Disconnect] 断开异常: $error');
    }
  }

  Future<void> _discoverServices() async {
    final BleSession? session = _readySession;
    if (session == null) return;
    appendLog('→ [Discover] 正在发起服务发现 (discoverServices)...');
    try {
      final List<BleServiceInfo> services = await session.discoverServices();
      _services = services;
      appendLog('✓ [Discover] 发现 ${services.length} 个 GATT 服务');
      for (final BleServiceInfo service in services) {
        appendLog('  → Service ${service.uuid} chars=${service.characteristics.length}');
        for (final BleCharInfo char in service.characteristics) {
          final List<String> props = <String>[
            if (char.canRead) 'Read',
            if (char.canWrite) 'Write',
            if (char.canWriteWithoutResponse) 'WriteNoResp',
            if (char.canNotify) 'Notify',
            if (char.canIndicate) 'Indicate',
          ];
          appendLog('    · Char ${char.uuid} [${props.join('/')}]');
        }
      }
    } catch (error) {
      appendLog('✗ [Discover] 服务发现失败: $error');
    }
  }

  Future<void> _requestMtu() async {
    final BleSession? session = _readySession;
    if (session == null) return;
    appendLog('→ [MTU] 正在请求扩展 MTU 至 $_desiredMtu 字节...');
    try {
      final int mtu = await session.requestMtu(_desiredMtu);
      _currentMtu = mtu;
      appendLog('✓ [MTU] 协商成功: $mtu 字节');
    } catch (error) {
      appendLog('✗ [MTU] 请求失败: $error');
    }
  }

  Future<void> _readCharacteristic() async {
    final BleSession? session = _readySession;
    if (session == null) return;
    final BleCharInfo? char = _pickChar((BleCharInfo c) => c.canRead);
    if (char == null) {
      appendLog('✗ [Read] 未找到可读特征值，请先发现服务');
      return;
    }
    appendLog('→ [Read] 正在读取特征值: ${char.uuid}...');
    try {
      final List<int> value =
          await session.read(characteristicUuid: char.uuid);
      final String hex = BleUtils.bytesToHex(value);
      appendLog('✓ [Read] Hex=[$hex]（长度: ${value.length} 字节）');
    } catch (error) {
      appendLog('✗ [Read] 读取失败: $error');
    }
  }

  Future<void> _writeCharacteristic() async {
    final BleSession? session = _readySession;
    if (session == null) return;
    final BleCharInfo? char = _pickChar(
      (BleCharInfo c) => c.canWrite || c.canWriteWithoutResponse,
    );
    if (char == null) {
      appendLog('✗ [Write] 未找到可写特征值，请先发现服务');
      return;
    }
    final List<int> data = 'Hello BLE from lib_bluetooth!'.codeUnits;
    appendLog(
      '→ [Write] 正在写入 ${data.length} 字节到特征值: ${char.uuid}...',
    );
    try {
      await session.write(
        characteristicUuid: char.uuid,
        data: data,
        withoutResponse: !char.canWrite,
      );
      appendLog('✓ [Write] 写入成功');
    } catch (error) {
      appendLog('✗ [Write] 写入失败: $error');
    }
  }

  void _toggleNotification() {
    final BleSession? session = _readySession;
    if (session == null) return;
    final BleCharInfo? char = _pickChar(
      (BleCharInfo c) => c.canNotify || c.canIndicate,
    );
    if (char == null) {
      appendLog('✗ [Notify] 未找到可订阅特征值，请先发现服务');
      return;
    }
    appendLog('→ [Notify] 正在监听 Notify: ${char.uuid}...');
    try {
      final Stream<List<int>> stream = session.listenNotification(
        characteristicUuid: char.uuid,
        enable: true,
      );
      final StreamSubscription<List<int>>? oldNotifySub = _notifySub;
      _notifySub = null;
      if (oldNotifySub != null) {
        unawaited(oldNotifySub.cancel());
      }
      _notifySub = stream.listen((List<int> value) {
        if (!mounted) return;
        final String hex = BleUtils.bytesToHex(value);
        updateLog(_notifyKey, 'Notify ${char.uuid}: Hex=[$hex]');
      });
      appendLog('✓ [Notify] 数据流已就绪');
    } catch (error) {
      appendLog('✗ [Notify] 设置失败: $error');
    }
  }
}
