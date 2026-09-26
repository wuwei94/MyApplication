import 'package:flutter_demo/core/basic/basic.dart';
import 'package:permission_handler/permission_handler.dart';

/// Permission Handler — 权限查询与申请
///
/// 核心机制与避坑点：
/// 1. 状态先行：先 `permission.status` 读当前态，再按需 `request()`。
/// 2. 永久拒绝：`permanentlyDenied` 只能 `openAppSettings()` 手动开启。
///
/// 官方参考：
/// https://pub.dev/packages/permission_handler
class PermissionDemoPage extends BasicResponsePage {
  const PermissionDemoPage({super.key, required super.title});

  @override
  BasicResponsePageState<PermissionDemoPage> createState() =>
      _PermissionDemoPageState();
}

class _PermissionDemoPageState extends BasicResponsePageState<PermissionDemoPage> {
  @override
  void initState() {
    super.initState();
    showDescription('permission_handler 示例：查询 / 申请运行时权限');
  }

  @override
  List<String> buildList() => const <String>[
        '1. 查询通知权限状态',
        '2. 申请通知权限',
        '3. 查询定位权限状态',
        '4. 申请定位权限',
        '5. 查询相机权限状态',
        '6. 申请相机权限',
        '7. 打开应用设置',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        _queryStatus(Permission.notification, '通知');
      case 1:
        _request(Permission.notification, '通知');
      case 2:
        _queryStatus(Permission.locationWhenInUse, '定位');
      case 3:
        _request(Permission.locationWhenInUse, '定位');
      case 4:
        _queryStatus(Permission.camera, '相机');
      case 5:
        _request(Permission.camera, '相机');
      case 6:
        _openSettings();
    }
  }

  Future<void> _queryStatus(Permission permission, String name) async {
    appendLog('→ [status] $name');
    try {
      final PermissionStatus status = await permission.status;
      appendLog('✓ $name=${status.name}');
    } catch (error) {
      appendLog('✗ unexpected: $error');
    }
  }

  Future<void> _request(Permission permission, String name) async {
    appendLog('→ [request] $name');
    try {
      final PermissionStatus current = await permission.status;
      if (current.isPermanentlyDenied) {
        appendLog('✗ 已永久拒绝，请到应用设置手动开启');
        return;
      }
      final PermissionStatus status = await permission.request();
      appendLog('✓ $name=${status.name}');
    } catch (error) {
      appendLog('✗ unexpected: $error');
    }
  }

  Future<void> _openSettings() async {
    appendLog('→ [openAppSettings] permission_handler.openAppSettings()');
    try {
      final bool opened = await openAppSettings();
      appendLog('✓ opened=$opened');
    } catch (error) {
      appendLog('✗ unexpected: $error');
    }
  }
}
