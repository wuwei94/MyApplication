import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_demo/core/utils/logger/logger.dart';
import 'package:geolocator/geolocator.dart';

const LocationSettings _currentPositionSettings = LocationSettings(
  accuracy: LocationAccuracy.medium,
  distanceFilter: 0,
  timeLimit: Duration(seconds: 30),
);

/// Geolocator
/// https://pub.dev/packages/geolocator
class GeolocatorDemoPage extends StatelessWidget {
  const GeolocatorDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return GeolocatorDemoView(title: title);
  }
}

class GeolocatorDemoView extends StatefulWidget {
  const GeolocatorDemoView({super.key, required this.title});

  final String title;

  @override
  State<GeolocatorDemoView> createState() => _GeolocatorDemoViewState();
}

class _GeolocatorDemoViewState extends State<GeolocatorDemoView> {
  StreamSubscription<ServiceStatus>? _serviceStatusSubscription;

  bool? _serviceEnabled;
  LocationPermission? _permission;
  LocationAccuracyStatus? _accuracyStatus;
  Position? _currentPosition;
  Position? _lastKnownPosition;
  DateTime? _lastUpdatedAt;
  String? _errorMessage;
  bool _isLoading = false;

  @override
  void initState() {
    super.initState();
    _startServiceStatusListener();
    unawaited(_refreshLocation(source: '页面初始化', readCurrentPosition: false));
  }

  @override
  void dispose() {
    unawaited(_serviceStatusSubscription?.cancel() ?? Future<void>.value());
    super.dispose();
  }

  void _startServiceStatusListener() {
    if (kIsWeb) {
      return;
    }

    try {
      _serviceStatusSubscription = Geolocator.getServiceStatusStream().listen(
        _handleServiceStatusChanged,
        onError: (Object error) => _handleServiceStatusError(error),
      );
    } on UnsupportedError catch (error, stackTrace) {
      logError(
        'Geolocator service status stream is unsupported.',
        error,
        stackTrace,
      );
    }
  }

  void _handleServiceStatusChanged(ServiceStatus status) {
    if (!mounted) {
      return;
    }

    setState(() {
      _serviceEnabled = status == ServiceStatus.enabled;
      if (status == ServiceStatus.disabled) {
        _errorMessage = '系统定位服务已关闭。';
      }
    });
  }

  void _handleServiceStatusError(Object error) {
    logError('Geolocator service status stream failed.', error);
  }

  Future<void> _refreshLocation({
    required String source,
    bool requestPermission = false,
    bool readCurrentPosition = true,
  }) async {
    if (_isLoading) {
      return;
    }

    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      final bool serviceEnabled = await Geolocator.isLocationServiceEnabled();
      LocationPermission permission = await Geolocator.checkPermission();

      if (requestPermission && permission == LocationPermission.denied) {
        permission = await Geolocator.requestPermission();
      }

      final LocationAccuracyStatus? accuracyStatus =
          await _readAccuracyStatus();
      Position? lastKnownPosition = _lastKnownPosition;
      Position? currentPosition = _currentPosition;

      if (_canReadLocation(permission)) {
        lastKnownPosition = await _readLastKnownPosition();
      }

      String? positionMessage;
      if (readCurrentPosition &&
          serviceEnabled &&
          _canReadLocation(permission)) {
        final _PositionReadResult positionReadResult =
            await _readCurrentPosition(fallbackPosition: lastKnownPosition);
        currentPosition = positionReadResult.currentPosition ?? currentPosition;
        lastKnownPosition =
            positionReadResult.lastKnownPosition ?? lastKnownPosition;
        positionMessage = positionReadResult.message;
      }

      final String? errorMessage =
          _buildStatusMessage(
            serviceEnabled: serviceEnabled,
            permission: permission,
          ) ??
          positionMessage;

      logInfo(
        'Geolocator refresh [$source]: '
        'service=$serviceEnabled, permission=$permission',
      );

      if (!mounted) {
        return;
      }

      setState(() {
        _serviceEnabled = serviceEnabled;
        _permission = permission;
        _accuracyStatus = accuracyStatus;
        _lastKnownPosition = lastKnownPosition;
        _currentPosition = currentPosition;
        _lastUpdatedAt = DateTime.now();
        _errorMessage = errorMessage;
        _isLoading = false;
      });
    } on Exception catch (error, stackTrace) {
      logError('Failed to read geolocator position.', error, stackTrace);
      _applyError('读取定位失败：$error');
    }
  }

  Future<_PositionReadResult> _readCurrentPosition({
    required Position? fallbackPosition,
  }) async {
    try {
      final Position position = await Geolocator.getCurrentPosition(
        locationSettings: _currentPositionSettings,
      );
      return _PositionReadResult(currentPosition: position);
    } on TimeoutException catch (error, stackTrace) {
      logError('Geolocator current position timed out.', error, stackTrace);

      final Position? lastKnownPosition =
          fallbackPosition ?? await _readLastKnownPosition();
      if (lastKnownPosition != null) {
        return _PositionReadResult(
          currentPosition: lastKnownPosition,
          lastKnownPosition: lastKnownPosition,
          message: '实时定位超时，已显示系统缓存位置。模拟器请先注入坐标，真机可到开阔位置后重试。',
        );
      }

      return const _PositionReadResult(
        message: '实时定位超时：模拟器请先注入坐标，真机请确认定位服务已开启并到开阔位置后重试。',
      );
    }
  }

  Future<Position?> _readLastKnownPosition() async {
    if (kIsWeb) {
      return null;
    }

    try {
      return await Geolocator.getLastKnownPosition();
    } on UnsupportedError catch (error, stackTrace) {
      logError(
        'Geolocator last known position is unsupported.',
        error,
        stackTrace,
      );
      return null;
    }
  }

  Future<LocationAccuracyStatus?> _readAccuracyStatus() async {
    try {
      return await Geolocator.getLocationAccuracy();
    } on UnsupportedError catch (error, stackTrace) {
      logError('Geolocator accuracy status is unsupported.', error, stackTrace);
      return null;
    }
  }

  void _applyError(String message) {
    if (!mounted) {
      return;
    }

    setState(() {
      _errorMessage = message;
      _isLoading = false;
      _lastUpdatedAt = DateTime.now();
    });
  }

  bool _canReadLocation(LocationPermission permission) {
    return permission == LocationPermission.whileInUse ||
        permission == LocationPermission.always ||
        permission == LocationPermission.unableToDetermine;
  }

  String? _buildStatusMessage({
    required bool serviceEnabled,
    required LocationPermission permission,
  }) {
    if (!serviceEnabled) {
      return '系统定位服务未开启，当前位置读取会被跳过。';
    }
    if (permission == LocationPermission.denied) {
      return '定位权限尚未授权，点击“申请并定位”后会触发系统授权弹窗。';
    }
    if (permission == LocationPermission.deniedForever) {
      return '定位权限已被永久拒绝，需要进入应用设置手动开启。';
    }
    if (permission == LocationPermission.unableToDetermine) {
      return '当前平台无法确定定位权限状态，可以直接尝试读取当前位置。';
    }
    return null;
  }

  Future<void> _openAppSettings() async {
    if (kIsWeb) {
      _applyError('Web 平台不支持直接打开应用设置。');
      return;
    }

    try {
      await Geolocator.openAppSettings();
    } on UnsupportedError catch (error, stackTrace) {
      logError(
        'Geolocator open app settings is unsupported.',
        error,
        stackTrace,
      );
      _applyError('当前平台不支持打开应用设置。');
    }
  }

  Future<void> _openLocationSettings() async {
    if (kIsWeb) {
      _applyError('Web 平台不支持直接打开定位设置。');
      return;
    }

    try {
      await Geolocator.openLocationSettings();
    } on UnsupportedError catch (error, stackTrace) {
      logError(
        'Geolocator open location settings is unsupported.',
        error,
        stackTrace,
      );
      _applyError('当前平台不支持打开定位设置。');
    }
  }

  String _primaryActionLabel() {
    final LocationPermission? permission = _permission;
    if (permission == null || permission == LocationPermission.denied) {
      return '申请并定位';
    }
    return '重新定位';
  }

  Color _accentColor(ThemeData theme) {
    if (_errorMessage != null) {
      return theme.colorScheme.error;
    }
    if (_currentPosition != null) {
      return const Color(0xFF1C8A63);
    }
    return const Color(0xFF0F5DAA);
  }

  String _serviceLabel() {
    final bool? serviceEnabled = _serviceEnabled;
    if (serviceEnabled == null) {
      return '等待读取';
    }
    return serviceEnabled ? '已开启' : '已关闭';
  }

  String _permissionLabel() {
    final LocationPermission? permission = _permission;
    if (permission == null) {
      return '等待读取';
    }

    switch (permission) {
      case LocationPermission.denied:
        return '未授权';
      case LocationPermission.deniedForever:
        return '永久拒绝';
      case LocationPermission.whileInUse:
        return '使用期间';
      case LocationPermission.always:
        return '始终允许';
      case LocationPermission.unableToDetermine:
        return '无法确定';
    }
  }

  String _accuracyLabel() {
    final LocationAccuracyStatus? accuracyStatus = _accuracyStatus;
    if (accuracyStatus == null) {
      return '等待读取';
    }

    switch (accuracyStatus) {
      case LocationAccuracyStatus.precise:
        return '精确定位';
      case LocationAccuracyStatus.reduced:
        return '模糊定位';
      case LocationAccuracyStatus.unknown:
        return '未知';
    }
  }

  String _lastUpdatedLabel() {
    final DateTime? lastUpdatedAt = _lastUpdatedAt;
    if (lastUpdatedAt == null) {
      return '等待首次读取';
    }
    return '最近更新：${_formatDateTime(lastUpdatedAt)}';
  }

  String _locationSummary() {
    final Position? position = _currentPosition;
    if (position == null) {
      return '还没有当前位置数据';
    }
    return '${_formatCoordinate(position.latitude)}, '
        '${_formatCoordinate(position.longitude)}';
  }

  double? _distanceFromLastKnown() {
    final Position? currentPosition = _currentPosition;
    final Position? lastKnownPosition = _lastKnownPosition;
    if (currentPosition == null || lastKnownPosition == null) {
      return null;
    }

    return Geolocator.distanceBetween(
      lastKnownPosition.latitude,
      lastKnownPosition.longitude,
      currentPosition.latitude,
      currentPosition.longitude,
    );
  }

  String _formatCoordinate(double value) {
    return value.toStringAsFixed(6);
  }

  String _formatMeters(double value) {
    return '${value.toStringAsFixed(1)} m';
  }

  String _formatDateTime(DateTime dateTime) {
    final DateTime localDateTime = dateTime.toLocal();
    final String hour = localDateTime.hour.toString().padLeft(2, '0');
    final String minute = localDateTime.minute.toString().padLeft(2, '0');
    final String second = localDateTime.second.toString().padLeft(2, '0');
    return '$hour:$minute:$second';
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
    final Color accentColor = _accentColor(theme);
    final Position? currentPosition = _currentPosition;
    final double? distanceFromLastKnown = _distanceFromLastKnown();

    return ListView(
      padding: const EdgeInsets.all(16),
      children: <Widget>[
        const _SectionCard(
          title: 'geolocator 示例',
          subtitle: '读取定位服务状态、申请前台定位权限，并获取设备当前位置。',
          child: Wrap(
            spacing: 8,
            runSpacing: 8,
            children: <Widget>[
              _FeatureChip(label: 'checkPermission()'),
              _FeatureChip(label: 'requestPermission()'),
              _FeatureChip(label: 'getCurrentPosition()'),
              _FeatureChip(label: 'getLastKnownPosition()'),
              _FeatureChip(label: 'distanceBetween()'),
            ],
          ),
        ),
        const SizedBox(height: 16),
        _SectionCard(
          title: currentPosition == null ? '等待定位' : '当前位置',
          subtitle: _errorMessage ?? _lastUpdatedLabel(),
          accentColor: accentColor,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: <Widget>[
              Row(
                children: <Widget>[
                  Icon(Icons.my_location_rounded, color: accentColor, size: 28),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Text(
                      _locationSummary(),
                      style: theme.textTheme.titleMedium?.copyWith(
                        color: accentColor,
                        fontWeight: FontWeight.w700,
                      ),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 16),
              Wrap(
                spacing: 8,
                runSpacing: 8,
                children: <Widget>[
                  _StatusPill(label: '服务：${_serviceLabel()}'),
                  _StatusPill(label: '权限：${_permissionLabel()}'),
                  _StatusPill(label: '精度：${_accuracyLabel()}'),
                ],
              ),
              const SizedBox(height: 16),
              Wrap(
                spacing: 12,
                runSpacing: 12,
                children: <Widget>[
                  FilledButton.icon(
                    onPressed: _isLoading
                        ? null
                        : () => _refreshLocation(
                            source: '主按钮',
                            requestPermission: true,
                            readCurrentPosition: true,
                          ),
                    icon: _isLoading
                        ? const SizedBox.square(
                            dimension: 18,
                            child: CircularProgressIndicator(strokeWidth: 2),
                          )
                        : const Icon(Icons.near_me_rounded),
                    label: Text(_primaryActionLabel()),
                  ),
                  OutlinedButton.icon(
                    onPressed: _isLoading
                        ? null
                        : () => _refreshLocation(
                            source: '刷新状态',
                            readCurrentPosition: false,
                          ),
                    icon: const Icon(Icons.refresh_rounded),
                    label: const Text('刷新状态'),
                  ),
                ],
              ),
            ],
          ),
        ),
        const SizedBox(height: 16),
        _SectionCard(
          title: '定位数据',
          subtitle: currentPosition == null
              ? '授权并成功定位后，这里会展示 Position 的核心字段。'
              : 'Position 时间戳：${_formatDateTime(currentPosition.timestamp)}',
          child: currentPosition == null
              ? const _EmptyPosition()
              : _PositionDetails(position: currentPosition),
        ),
        const SizedBox(height: 16),
        _SectionCard(
          title: '辅助能力',
          subtitle: '结合最后一次缓存位置展示距离计算，并提供系统设置入口。',
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: <Widget>[
              _InfoRow(
                label: '缓存位置',
                value: _lastKnownPosition == null
                    ? '暂无缓存'
                    : '${_formatCoordinate(_lastKnownPosition!.latitude)}, '
                          '${_formatCoordinate(_lastKnownPosition!.longitude)}',
              ),
              _InfoRow(
                label: '缓存距离',
                value: distanceFromLastKnown == null
                    ? '等待两组坐标'
                    : _formatMeters(distanceFromLastKnown),
              ),
              const SizedBox(height: 12),
              Wrap(
                spacing: 12,
                runSpacing: 12,
                children: <Widget>[
                  OutlinedButton.icon(
                    onPressed: _openLocationSettings,
                    icon: const Icon(Icons.settings_suggest_rounded),
                    label: const Text('定位设置'),
                  ),
                  OutlinedButton.icon(
                    onPressed: _openAppSettings,
                    icon: const Icon(Icons.app_settings_alt_rounded),
                    label: const Text('应用设置'),
                  ),
                ],
              ),
            ],
          ),
        ),
      ],
    );
  }
}

class _PositionDetails extends StatelessWidget {
  const _PositionDetails({required this.position});

  final Position position;

  @override
  Widget build(BuildContext context) {
    return Column(
      children: <Widget>[
        _InfoRow(label: '纬度', value: position.latitude.toStringAsFixed(6)),
        _InfoRow(label: '经度', value: position.longitude.toStringAsFixed(6)),
        _InfoRow(
          label: '水平精度',
          value: '${position.accuracy.toStringAsFixed(1)} m',
        ),
        _InfoRow(
          label: '海拔',
          value: '${position.altitude.toStringAsFixed(1)} m',
        ),
        _InfoRow(
          label: '速度',
          value: '${position.speed.toStringAsFixed(2)} m/s',
        ),
        _InfoRow(label: '方向', value: '${position.heading.toStringAsFixed(1)}°'),
        _InfoRow(label: '模拟定位', value: position.isMocked ? '是' : '否'),
        if (position.floor != null)
          _InfoRow(label: '楼层', value: position.floor.toString()),
      ],
    );
  }
}

class _PositionReadResult {
  const _PositionReadResult({
    this.currentPosition,
    this.lastKnownPosition,
    this.message,
  });

  final Position? currentPosition;
  final Position? lastKnownPosition;
  final String? message;
}

class _EmptyPosition extends StatelessWidget {
  const _EmptyPosition();

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: const Color(0xFFF6F8FA),
        borderRadius: BorderRadius.circular(14),
      ),
      child: Text(
        '当前没有可展示的坐标。',
        style: theme.textTheme.bodyMedium?.copyWith(
          color: const Color(0xFF5B6B76),
        ),
      ),
    );
  }
}

class _SectionCard extends StatelessWidget {
  const _SectionCard({
    required this.title,
    required this.subtitle,
    required this.child,
    this.accentColor = const Color(0xFF0F5DAA),
  });

  final String title;
  final String subtitle;
  final Widget child;
  final Color accentColor;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return Container(
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
            Text(
              title,
              style: theme.textTheme.titleMedium?.copyWith(
                fontWeight: FontWeight.w700,
              ),
            ),
            const SizedBox(height: 6),
            Text(
              subtitle,
              style: theme.textTheme.bodyMedium?.copyWith(
                color: const Color(0xFF5B6B76),
              ),
            ),
            const SizedBox(height: 16),
            child,
          ],
        ),
      ),
    );
  }
}

class _FeatureChip extends StatelessWidget {
  const _FeatureChip({required this.label});

  final String label;

  @override
  Widget build(BuildContext context) {
    return DecoratedBox(
      decoration: BoxDecoration(
        color: const Color(0xFFF0F6FF),
        borderRadius: BorderRadius.circular(999),
        border: Border.all(color: const Color(0xFFD4E6FF)),
      ),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
        child: Text(
          label,
          style: Theme.of(context).textTheme.labelMedium?.copyWith(
            color: const Color(0xFF0F5DAA),
            fontWeight: FontWeight.w600,
          ),
        ),
      ),
    );
  }
}

class _StatusPill extends StatelessWidget {
  const _StatusPill({required this.label});

  final String label;

  @override
  Widget build(BuildContext context) {
    return DecoratedBox(
      decoration: BoxDecoration(
        color: const Color(0xFFF6F8FA),
        borderRadius: BorderRadius.circular(999),
      ),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
        child: Text(label, style: Theme.of(context).textTheme.labelMedium),
      ),
    );
  }
}

class _InfoRow extends StatelessWidget {
  const _InfoRow({required this.label, required this.value});

  final String label;
  final String value;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 8),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          SizedBox(
            width: 88,
            child: Text(
              label,
              style: theme.textTheme.bodyMedium?.copyWith(
                color: const Color(0xFF5B6B76),
              ),
            ),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Text(
              value,
              style: theme.textTheme.bodyMedium?.copyWith(
                fontWeight: FontWeight.w600,
              ),
            ),
          ),
        ],
      ),
    );
  }
}
