import 'dart:async';

import 'package:flutter_demo/core/basic/basic.dart';
import 'package:lib_bluetooth/lib_bluetooth.dart';

/// BLE 扫描 — 适配器状态与设备发现
///
/// 核心机制与避坑点：
/// 1. 前置条件：适配器须为 `on`，Android 还需定位权限才能发现设备。
/// 2. 超时停扫：`startScan(timeout)` 到期自动停止，结果持续由 `scanResults` 流推送。
/// 3. 过滤扫描：可用 `withServices` 缩小广播过滤范围，降低功耗与干扰。
///
/// 官方参考：
/// https://pub.dev/packages/flutter_blue_plus
class BleScanDemoPage extends BasicResponsePage {
  const BleScanDemoPage({super.key, required super.title});

  @override
  BasicResponsePageState<BleScanDemoPage> createState() =>
      _BleScanDemoPageState();
}

class _BleScanDemoPageState extends BasicResponsePageState<BleScanDemoPage> {
  static const String _adapterKey = 'adapter';
  static const String _scanKey = 'scan';

  BleAdapterState _adapterState = BleAdapterState.unknown;
  bool _isScanning = false;
  List<BleDeviceItem> _scanResults = <BleDeviceItem>[];

  StreamSubscription<BleAdapterState>? _adapterStateSub;
  StreamSubscription<bool>? _isScanningSub;
  StreamSubscription<List<BleDeviceItem>>? _scanResultsSub;

  @override
  void initState() {
    super.initState();
    showDescription('BLE 扫描示例：监听适配器状态，启停扫描并实时汇总设备列表');
    _bindStreams();
  }

  @override
  void dispose() {
    _adapterStateSub?.cancel();
    _isScanningSub?.cancel();
    _scanResultsSub?.cancel();
    super.dispose();
  }

  void _bindStreams() {
    _adapterStateSub =
        BleClient.instance.adapterState.listen((BleAdapterState state) {
      _adapterState = state;
      if (!mounted) return;
      updateLog(_adapterKey, '蓝牙适配器: ${state.name}');
    });

    _isScanningSub = BleClient.instance.isScanning.listen((bool scanning) {
      _isScanning = scanning;
      if (!mounted) return;
      updateLog(
        _scanKey,
        scanning ? '扫描中...' : '扫描已停止，共 ${_scanResults.length} 台',
      );
    });

    _scanResultsSub =
        BleClient.instance.scanResults.listen((List<BleDeviceItem> results) {
      _scanResults = results;
      if (!mounted) return;
      final StringBuffer buffer = StringBuffer('设备数=${results.length}');
      for (final BleDeviceItem item in results) {
        buffer.write('\n  ${item.name} (${item.id}) rssi=${item.rssi}');
      }
      updateLog(_scanKey, buffer.toString());
    });
  }

  @override
  List<String> buildList() => const <String>[
        '1. 开始扫描周边设备',
        '2. 停止扫描',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        _startScan();
      case 1:
        _stopScan();
    }
  }

  Future<void> _startScan() async {
    if (_adapterState != BleAdapterState.on) {
      appendLog('✗ [Scan] 请先开启蓝牙（当前: ${_adapterState.name}）');
      return;
    }
    appendLog('→ [Scan] 开始扫描（15s 超时）');
    try {
      await BleClient.instance.startScan(
        timeout: const Duration(seconds: 15),
        androidUsesFineLocation: true,
      );
    } catch (error) {
      appendLog('✗ [Scan] 启动扫描失败: $error');
    }
  }

  Future<void> _stopScan() async {
    if (!_isScanning) {
      appendLog('→ [Scan] 当前未在扫描');
      return;
    }
    appendLog('→ [Scan] 停止扫描');
    try {
      await BleClient.instance.stopScan();
    } catch (error) {
      appendLog('✗ [Scan] 停止扫描失败: $error');
    }
  }
}
