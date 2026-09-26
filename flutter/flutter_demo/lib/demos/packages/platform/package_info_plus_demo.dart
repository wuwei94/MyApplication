import 'package:flutter_demo/core/basic/basic.dart';
import 'package:package_info_plus/package_info_plus.dart';

/// Package Info Plus — 读取应用包信息
///
/// 核心机制与避坑点：
/// 1. 缓存语义：`PackageInfo.fromPlatform()` 会缓存结果，适合启动流程一次性读取。
/// 2. 构建时机：改 pubspec 版本后 iOS / macOS 可能需重新构建才能看到新值。
///
/// 官方参考：
/// https://pub.dev/packages/package_info_plus
class PackageInfoPlusDemoPage extends BasicResponsePage {
  const PackageInfoPlusDemoPage({super.key, required super.title});

  @override
  BasicResponsePageState<PackageInfoPlusDemoPage> createState() =>
      _PackageInfoPlusDemoPageState();
}

class _PackageInfoPlusDemoPageState
    extends BasicResponsePageState<PackageInfoPlusDemoPage> {
  @override
  void initState() {
    super.initState();
    showDescription('package_info_plus 示例：读取包名 / 版本 / 安装来源');
  }

  @override
  List<String> buildList() => const <String>[
        '1. 读取完整包信息',
        '2. 读取包名与版本',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        _readFullInfo();
      case 1:
        _readSummary();
    }
  }

  Future<void> _readFullInfo() async {
    appendLog('→ [fromPlatform] PackageInfo.fromPlatform()');
    try {
      final PackageInfo info = await PackageInfo.fromPlatform();
      appendLog('✓ appName=${info.appName}');
      appendLog('✓ packageName=${info.packageName}');
      appendLog('✓ version=${info.version}+${info.buildNumber}');
      appendLog('✓ installerStore=${info.installerStore ?? 'null'}');
      appendLog('✓ buildSignature=${info.buildSignature}');
    } catch (error) {
      appendLog('✗ unexpected: $error');
    }
  }

  Future<void> _readSummary() async {
    appendLog('→ [summary] PackageInfo.fromPlatform()');
    try {
      final PackageInfo info = await PackageInfo.fromPlatform();
      appendLog('✓ ${info.packageName} ${info.version}+${info.buildNumber}');
    } catch (error) {
      appendLog('✗ unexpected: $error');
    }
  }
}
