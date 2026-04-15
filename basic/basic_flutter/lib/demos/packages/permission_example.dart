import 'package:flutter/material.dart';
import 'package:permission_handler/permission_handler.dart';

const List<_PermissionModel> _permissionDefinitions = <_PermissionModel>[
  _PermissionModel(
    permission: Permission.notification,
    title: '通知权限',
    subtitle: '消息提醒和系统通知',
  ),
  _PermissionModel(
    permission: Permission.locationWhenInUse,
    title: '定位权限',
    subtitle: '使用期间获取位置信息',
  ),
  _PermissionModel(
    permission: Permission.camera,
    title: '相机权限',
    subtitle: '拍照、扫码和图像采集',
  ),
  _PermissionModel(
    permission: Permission.photos,
    title: '相册权限',
    subtitle: '读取照片和媒体文件',
  ),
];

/// Permission Handler
/// https://pub.dev/packages/permission_handler
class PermissionExample extends StatelessWidget {
  const PermissionExample({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return PermissionRoute(title: title);
  }
}

class PermissionRoute extends StatefulWidget {
  const PermissionRoute({super.key, required this.title});

  final String title;

  @override
  State<PermissionRoute> createState() => _PermissionRouteState();
}

class _PermissionRouteState extends State<PermissionRoute> {
  Map<Permission, PermissionStatus> _statuses =
      <Permission, PermissionStatus>{};

  @override
  void initState() {
    super.initState();
    _refreshAllPermissions();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(widget.title)),
      body: getBody(),
    );
  }

  Widget getBody() {
    final ThemeData theme = Theme.of(context);

    return ListView(
      padding: const EdgeInsets.all(16),
      children: <Widget>[
        for (final _PermissionModel definition in _permissionDefinitions)
          _buildPermissionCard(definition, theme),
      ],
    );
  }

  Widget _buildPermissionCard(_PermissionModel definition, ThemeData theme) {
    final PermissionStatus status = _statusOf(definition.permission);

    return _PermissionCard(
      title: definition.title,
      subtitle: definition.subtitle,
      accentColor: _accentColor(status, theme),
      buttonLabel: _actionLabel(status),
      description: _statusDescription(status),
      onPressed: () => _handlePermissionAction(definition),
    );
  }

  Future<void> _refreshAllPermissions() async {
    final Map<Permission, PermissionStatus> nextStatuses =
        <Permission, PermissionStatus>{};

    for (final _PermissionModel definition in _permissionDefinitions) {
      nextStatuses[definition.permission] = await definition.permission.status;
    }

    if (!mounted) {
      return;
    }

    setState(() {
      _statuses = nextStatuses;
    });
  }

  PermissionStatus _statusOf(Permission permission) {
    return _statuses[permission] ?? PermissionStatus.denied;
  }

  Future<void> _handlePermissionAction(_PermissionModel definition) async {
    final PermissionStatus currentStatus = _statusOf(definition.permission);

    if (currentStatus.isPermanentlyDenied) {
      await openAppSettings();
      return;
    }

    final PermissionStatus nextStatus = await definition.permission.request();

    if (!mounted) {
      return;
    }

    setState(() {
      _statuses[definition.permission] = nextStatus;
    });
  }

  String _statusText(PermissionStatus status) {
    switch (status) {
      case PermissionStatus.denied:
        return '未授权';
      case PermissionStatus.granted:
        return '已授权';
      case PermissionStatus.restricted:
        return '受限制';
      case PermissionStatus.limited:
        return '部分授权';
      case PermissionStatus.permanentlyDenied:
        return '已永久拒绝';
      case PermissionStatus.provisional:
        return '临时授权';
    }
  }

  String _statusDescription(PermissionStatus status) {
    return '申请状态：${_statusText(status)}';
  }

  String _actionLabel(PermissionStatus status) {
    return status.isPermanentlyDenied ? '设置' : '申请';
  }

  Color _accentColor(PermissionStatus status, ThemeData theme) {
    if (status.isGranted || status.isLimited || status.isProvisional) {
      return const Color(0xFF1C8A63);
    }
    if (status.isPermanentlyDenied) {
      return theme.colorScheme.error;
    }
    if (status.isRestricted) {
      return const Color(0xFFE08A00);
    }
    return const Color(0xFF0F5DAA);
  }
}

class _PermissionCard extends StatelessWidget {
  const _PermissionCard({
    required this.title,
    required this.subtitle,
    required this.accentColor,
    required this.buttonLabel,
    required this.description,
    required this.onPressed,
  });

  final String title;
  final String subtitle;
  final Color accentColor;
  final String buttonLabel;
  final String description;
  final VoidCallback onPressed;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: Container(
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(20),
          border: Border.all(color: accentColor.withValues(alpha: 0.16)),
          boxShadow: const <BoxShadow>[
            BoxShadow(
              color: Color(0x120A2533),
              blurRadius: 18,
              offset: Offset(0, 8),
            ),
          ],
        ),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: <Widget>[
              Row(
                children: <Widget>[
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: <Widget>[
                        Text(
                          title,
                          style: theme.textTheme.titleMedium?.copyWith(
                            fontWeight: FontWeight.w700,
                          ),
                        ),
                        const SizedBox(height: 4),
                        Text(
                          subtitle,
                          style: theme.textTheme.bodySmall?.copyWith(
                            color: theme.colorScheme.onSurfaceVariant,
                          ),
                        ),
                      ],
                    ),
                  ),
                  FilledButton(onPressed: onPressed, child: Text(buttonLabel)),
                ],
              ),
              const SizedBox(height: 14),
              Text(
                description,
                style: theme.textTheme.bodyMedium?.copyWith(
                  color: theme.colorScheme.onSurfaceVariant,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _PermissionModel {
  const _PermissionModel({
    required this.permission,
    required this.title,
    required this.subtitle,
  });

  final Permission permission;
  final String title;
  final String subtitle;
}
