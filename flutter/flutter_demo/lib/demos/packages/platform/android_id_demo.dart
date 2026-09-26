import 'package:android_id/android_id.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:flutter_demo/core/basic/basic.dart';

/// Android ID — 读取 Android 设备标识
///
/// 核心机制与避坑点：
/// 1. 平台边界：`AndroidId().getId()` 仅 Android 有效，其它平台直接返回不可用。
/// 2. 插件缺失：冷启动前或测试环境可能抛 `MissingPluginException`，需单独兜底。
///
/// 官方参考：
/// https://pub.dev/packages/android_id
class AndroidIdDemoPage extends BasicResponsePage {
  const AndroidIdDemoPage({super.key, required super.title});

  @override
  BasicResponsePageState<AndroidIdDemoPage> createState() =>
      _AndroidIdDemoPageState();
}

class _AndroidIdDemoPageState extends BasicResponsePageState<AndroidIdDemoPage> {
  static const AndroidId _androidIdPlugin = AndroidId();

  @override
  void initState() {
    super.initState();
    showDescription('AndroidId 示例：读取 Android 设备标识');
  }

  @override
  List<String> buildList() => const <String>[
        '1. 读取 Android ID',
        '2. 读取并复制 Android ID',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        _readAndroidId();
      case 1:
        _readAndCopyAndroidId();
    }
  }

  Future<void> _readAndroidId() async {
    if (kIsWeb || defaultTargetPlatform != TargetPlatform.android) {
      appendLog('✗ 当前平台不是 Android，android_id 只在 Android 上返回值');
      return;
    }

    appendLog('→ [getId] AndroidId().getId()');
    try {
      final String? androidId = await _androidIdPlugin.getId();
      if (androidId == null || androidId.isEmpty) {
        appendLog('✗ 插件调用成功，但设备没有返回 Android ID');
        return;
      }
      appendLog('✓ androidId=$androidId');
    } on MissingPluginException catch (error) {
      appendLog('✗ MissingPluginException: ${error.message}');
    } on PlatformException catch (error) {
      appendLog('✗ PlatformException: ${error.message ?? error.code}');
    } catch (error) {
      appendLog('✗ unexpected: $error');
    }
  }

  Future<void> _readAndCopyAndroidId() async {
    if (kIsWeb || defaultTargetPlatform != TargetPlatform.android) {
      appendLog('✗ 当前平台不是 Android，无法读取');
      return;
    }

    appendLog('→ [getId] AndroidId().getId()');
    try {
      final String? androidId = await _androidIdPlugin.getId();
      if (androidId == null || androidId.isEmpty) {
        appendLog('✗ 设备没有返回 Android ID');
        return;
      }
      await Clipboard.setData(ClipboardData(text: androidId));
      appendLog('✓ androidId=$androidId');
      appendLog('✓ 已复制到剪贴板');
    } on MissingPluginException catch (error) {
      appendLog('✗ MissingPluginException: ${error.message}');
    } on PlatformException catch (error) {
      appendLog('✗ PlatformException: ${error.message ?? error.code}');
    } catch (error) {
      appendLog('✗ unexpected: $error');
    }
  }
}
