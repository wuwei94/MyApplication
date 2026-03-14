import 'package:flutter/material.dart';
import 'package:permission_handler/permission_handler.dart';

/// Permission Handler
/// https://pub.dev/packages/permission_handler
class PermissionExample extends StatelessWidget {
  const PermissionExample({super.key});

  @override
  Widget build(BuildContext context) {
    return const PermissionRoute(title: 'Permission Example');
  }
}

class PermissionRoute extends StatelessWidget {
  const PermissionRoute({super.key, required this.title});

  final String title;

  Future<void> _requestPermission(
    BuildContext context,
    _PermissionItem item,
  ) async {
    final PermissionStatus status = await item.permission.request();
    final String result = _statusLabel(status);

    if (!context.mounted) {
      return;
    }

    ScaffoldMessenger.of(
      context,
    ).showSnackBar(SnackBar(content: Text('${item.title}: $result')));
  }

  String _statusLabel(PermissionStatus status) {
    return switch (status) {
      PermissionStatus.granted => '已授权',
      PermissionStatus.denied => '已拒绝',
      PermissionStatus.permanentlyDenied => '永久拒绝',
      PermissionStatus.restricted => '受限制',
      PermissionStatus.limited => '部分授权',
      PermissionStatus.provisional => '临时授权',
    };
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(title)),
      body: ListView.separated(
        padding: const EdgeInsets.all(16),
        itemCount: _permissionItems.length,
        separatorBuilder: (_, index) => const SizedBox(height: 12),
        itemBuilder: (BuildContext context, int index) {
          final _PermissionItem item = _permissionItems[index];

          return Card(
            child: ListTile(
              title: Text(item.title),
              subtitle: Text(item.subtitle),
              trailing: const Icon(Icons.chevron_right),
              onTap: () => _requestPermission(context, item),
            ),
          );
        },
      ),
    );
  }
}

class _PermissionItem {
  const _PermissionItem({
    required this.title,
    required this.subtitle,
    required this.permission,
  });

  final String title;
  final String subtitle;
  final Permission permission;
}

const List<_PermissionItem> _permissionItems = <_PermissionItem>[
  _PermissionItem(
    title: 'Camera',
    subtitle: '申请相机权限',
    permission: Permission.camera,
  ),
  _PermissionItem(
    title: 'Microphone',
    subtitle: '申请麦克风权限',
    permission: Permission.microphone,
  ),
  _PermissionItem(
    title: 'Location',
    subtitle: '申请定位权限',
    permission: Permission.locationWhenInUse,
  ),
  _PermissionItem(
    title: 'Notification',
    subtitle: '申请通知权限',
    permission: Permission.notification,
  ),
  _PermissionItem(
    title: 'Contacts',
    subtitle: '申请联系人权限',
    permission: Permission.contacts,
  ),
];
