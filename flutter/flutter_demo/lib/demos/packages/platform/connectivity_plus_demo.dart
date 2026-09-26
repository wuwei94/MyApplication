import 'dart:async';

import 'package:connectivity_plus/connectivity_plus.dart';
import 'package:flutter/services.dart';
import 'package:flutter_demo/core/basic/basic.dart';
import 'package:flutter_demo/core/utils/network/connectivity_service.dart';

/// connectivity_plus — 网络类型读取与变化监听
///
/// 核心机制与避坑点：
/// 1. 双通道：`checkConnectivity()` 读当前态，`onConnectivityChanged` 监听后续变化。
/// 2. 结果语义：7.x 可能同时返回多个网络类型；无网返回 `none`，接口态不等于外网可达。
///
/// 官方参考：
/// https://pub.dev/packages/connectivity_plus
class ConnectivityPlusDemoPage extends BasicResponsePage {
  const ConnectivityPlusDemoPage({super.key, required super.title});

  @override
  BasicResponsePageState<ConnectivityPlusDemoPage> createState() =>
      _ConnectivityPlusDemoPageState();
}

class _ConnectivityPlusDemoPageState
    extends BasicResponsePageState<ConnectivityPlusDemoPage> {
  final ConnectivityService _connectivityService = ConnectivityService.instance;
  StreamSubscription<List<ConnectivityResult>>? _subscription;

  @override
  void initState() {
    super.initState();
    showDescription('connectivity_plus 示例：读取当前网络类型并监听变化');
    _subscription = _connectivityService.onConnectivityChanged.listen(
      _handleConnectivityChanged,
    );
  }

  @override
  void dispose() {
    _subscription?.cancel();
    super.dispose();
  }

  @override
  List<String> buildList() => const <String>[
        '1. 读取当前网络状态',
        '2. 开始监听网络变化',
        '3. 停止监听网络变化',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        _checkConnectivity();
      case 1:
        _startListen();
      case 2:
        _stopListen();
    }
  }

  void _handleConnectivityChanged(List<ConnectivityResult> results) {
    final String summary = _connectivityService.summaryOf(results);
    appendLog('✓ [onConnectivityChanged] $summary');
  }

  Future<void> _checkConnectivity() async {
    appendLog('→ [check] ConnectivityService.checkConnectivity()');
    try {
      final List<ConnectivityResult> results = await _connectivityService
          .checkConnectivity();
      final String summary = _connectivityService.summaryOf(results);
      appendLog('✓ summary=$summary');
    } on PlatformException catch (error) {
      appendLog('✗ PlatformException: ${error.message ?? error.code}');
    } catch (error) {
      appendLog('✗ unexpected: $error');
    }
  }

  void _startListen() {
    if (_subscription != null) {
      appendLog('✗ 监听已在进行中');
      return;
    }
    _subscription = _connectivityService.onConnectivityChanged.listen(
      _handleConnectivityChanged,
    );
    appendLog('✓ [onConnectivityChanged] 监听已开始');
  }

  void _stopListen() {
    _subscription?.cancel();
    _subscription = null;
    appendLog('✓ 监听已停止');
  }
}
