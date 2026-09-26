import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:flutter_demo/core/basic/basic.dart';
import 'package:flutter_udid/flutter_udid.dart';

/// Flutter UDID — 跨平台持久设备标识
///
/// 核心机制与避坑点：
/// 1. 双值语义：`FlutterUdid.udid` 平台原始格式，`consistentUdid` 统一格式。
/// 2. 平台边界：Web / Fuchsia 无实现；插件未注册时抛 `MissingPluginException`。
///
/// 官方参考：
/// https://pub.dev/packages/flutter_udid
class FlutterUdidDemoPage extends BasicResponsePage {
  const FlutterUdidDemoPage({super.key, required super.title});

  @override
  BasicResponsePageState<FlutterUdidDemoPage> createState() =>
      _FlutterUdidDemoPageState();
}

class _FlutterUdidDemoPageState
    extends BasicResponsePageState<FlutterUdidDemoPage> {
  @override
  void initState() {
    super.initState();
    showDescription('flutter_udid 示例：读取平台 UDID 与统一格式 UDID');
  }

  @override
  List<String> buildList() => const <String>[
        '1. 读取平台 UDID',
        '2. 读取统一格式 UDID',
        '3. 同时读取两组 UDID',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        _readUdid();
      case 1:
        _readConsistentUdid();
      case 2:
        _readBoth();
    }
  }

  bool _ensurePlatform() {
    if (kIsWeb || defaultTargetPlatform == TargetPlatform.fuchsia) {
      appendLog('✗ flutter_udid 当前不提供 Web/Fuchsia 实现');
      return false;
    }
    return true;
  }

  Future<void> _readUdid() async {
    if (!_ensurePlatform()) {
      return;
    }
    appendLog('→ [udid] FlutterUdid.udid');
    try {
      final String value = await FlutterUdid.udid;
      appendLog('✓ udid=$value');
    } on MissingPluginException catch (error) {
      appendLog('✗ MissingPluginException: ${error.message}');
    } on PlatformException catch (error) {
      appendLog('✗ PlatformException: ${error.message ?? error.code}');
    } catch (error) {
      appendLog('✗ unexpected: $error');
    }
  }

  Future<void> _readConsistentUdid() async {
    if (!_ensurePlatform()) {
      return;
    }
    appendLog('→ [consistentUdid] FlutterUdid.consistentUdid');
    try {
      final String value = await FlutterUdid.consistentUdid;
      appendLog('✓ consistentUdid=$value');
    } on MissingPluginException catch (error) {
      appendLog('✗ MissingPluginException: ${error.message}');
    } on PlatformException catch (error) {
      appendLog('✗ PlatformException: ${error.message ?? error.code}');
    } catch (error) {
      appendLog('✗ unexpected: $error');
    }
  }

  Future<void> _readBoth() async {
    if (!_ensurePlatform()) {
      return;
    }
    appendLog('→ [both] FlutterUdid.udid + consistentUdid');
    try {
      final List<String> values = await Future.wait<String>(
        <Future<String>>[FlutterUdid.udid, FlutterUdid.consistentUdid],
      );
      appendLog('✓ udid=${values[0]}');
      appendLog('✓ consistentUdid=${values[1]}');
    } on MissingPluginException catch (error) {
      appendLog('✗ MissingPluginException: ${error.message}');
    } on PlatformException catch (error) {
      appendLog('✗ PlatformException: ${error.message ?? error.code}');
    } catch (error) {
      appendLog('✗ unexpected: $error');
    }
  }
}
