import 'package:device_info_plus/device_info_plus.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:flutter_demo/core/basic/basic.dart';

/// Device Info Plus — 读取设备 / 浏览器系统信息
///
/// 核心机制与避坑点：
/// 1. 平台分支：`androidInfo` / `iosInfo` / `webBrowserInfo` 等按 `defaultTargetPlatform` 选择。
/// 2. 字段差异：各平台字段不完全一致，示例只打印平台主键字段，避免整包上报。
///
/// 官方参考：
/// https://pub.dev/packages/device_info_plus
class DeviceInfoPlusDemoPage extends BasicResponsePage {
  const DeviceInfoPlusDemoPage({super.key, required super.title});

  @override
  BasicResponsePageState<DeviceInfoPlusDemoPage> createState() =>
      _DeviceInfoPlusDemoPageState();
}

class _DeviceInfoPlusDemoPageState
    extends BasicResponsePageState<DeviceInfoPlusDemoPage> {
  final DeviceInfoPlugin _deviceInfoPlugin = DeviceInfoPlugin();

  @override
  void initState() {
    super.initState();
    showDescription('device_info_plus 示例：读取当前设备关键信息');
  }

  @override
  List<String> buildList() => const <String>[
        '1. 读取设备信息',
        '2. 读取 Android 构建信息',
        '3. 读取 iOS 设备信息',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        _readCurrentPlatform();
      case 1:
        _readAndroid();
      case 2:
        _readIos();
    }
  }

  Future<void> _readCurrentPlatform() async {
    appendLog('→ [info] DeviceInfoPlugin current platform');
    try {
      if (kIsWeb) {
        final WebBrowserInfo info = await _deviceInfoPlugin.webBrowserInfo;
        appendLog('✓ platform=Web browser=${info.browserName.name}');
        appendLog('✓ userAgent=${info.userAgent}');
        return;
      }

      switch (defaultTargetPlatform) {
        case TargetPlatform.android:
          await _readAndroid();
        case TargetPlatform.iOS:
          await _readIos();
        case TargetPlatform.macOS:
          final MacOsDeviceInfo info = await _deviceInfoPlugin.macOsInfo;
          appendLog('✓ platform=macOS model=${info.modelName}');
        case TargetPlatform.linux:
          final LinuxDeviceInfo info = await _deviceInfoPlugin.linuxInfo;
          appendLog('✓ platform=Linux prettyName=${info.prettyName}');
        case TargetPlatform.windows:
          final WindowsDeviceInfo info = await _deviceInfoPlugin.windowsInfo;
          appendLog('✓ platform=Windows product=${info.productName}');
        case TargetPlatform.fuchsia:
          appendLog('✗ Fuchsia 暂不支持');
      }
    } on PlatformException catch (error) {
      appendLog('✗ PlatformException: ${error.message ?? error.code}');
    } catch (error) {
      appendLog('✗ unexpected: $error');
    }
  }

  Future<void> _readAndroid() async {
    appendLog('→ [androidInfo] DeviceInfoPlugin.androidInfo');
    try {
      final AndroidDeviceInfo info = await _deviceInfoPlugin.androidInfo;
      appendLog('✓ brand=${info.brand} model=${info.model}');
      appendLog(
        '✓ android=${info.version.release} sdk=${info.version.sdkInt}',
      );
      appendLog('✓ isPhysicalDevice=${info.isPhysicalDevice}');
    } on PlatformException catch (error) {
      appendLog('✗ PlatformException: ${error.message ?? error.code}');
    } catch (error) {
      appendLog('✗ unexpected: $error');
    }
  }

  Future<void> _readIos() async {
    appendLog('→ [iosInfo] DeviceInfoPlugin.iosInfo');
    try {
      final IosDeviceInfo info = await _deviceInfoPlugin.iosInfo;
      appendLog('✓ modelName=${info.modelName}');
      appendLog('✓ system=${info.systemName} ${info.systemVersion}');
      appendLog('✓ isPhysicalDevice=${info.isPhysicalDevice}');
    } on PlatformException catch (error) {
      appendLog('✗ PlatformException: ${error.message ?? error.code}');
    } catch (error) {
      appendLog('✗ unexpected: $error');
    }
  }
}
