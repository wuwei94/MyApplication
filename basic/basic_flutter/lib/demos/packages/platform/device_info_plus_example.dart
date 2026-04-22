import 'dart:async';
import 'dart:convert';

import 'package:basic_flutter/core/utils/logger/logger.dart';
import 'package:device_info_plus/device_info_plus.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

/// Device Info Plus
/// https://pub.dev/packages/device_info_plus
class DeviceInfoPlusDemoPage extends StatelessWidget {
  const DeviceInfoPlusDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return DeviceInfoPlusDemoView(title: title);
  }
}

class DeviceInfoPlusDemoView extends StatefulWidget {
  const DeviceInfoPlusDemoView({super.key, required this.title});

  final String title;

  @override
  State<DeviceInfoPlusDemoView> createState() => _DeviceInfoPlusDemoViewState();
}

class _DeviceInfoPlusDemoViewState extends State<DeviceInfoPlusDemoView> {
  final DeviceInfoPlugin _deviceInfoPlugin = DeviceInfoPlugin();

  _DeviceInfoSnapshot? _snapshot;
  String? _errorMessage;
  bool _isRefreshing = false;

  @override
  void initState() {
    super.initState();
    unawaited(_refreshDeviceInfo(source: '页面初始化'));
  }

  Future<void> _refreshDeviceInfo({required String source}) async {
    if (_isRefreshing) {
      return;
    }

    setState(() {
      _isRefreshing = true;
      _errorMessage = null;
    });

    try {
      final _DeviceInfoSnapshot snapshot = await _loadSnapshot();
      logInfo('Device info refreshed [$source]: ${snapshot.platformLabel}');

      if (!mounted) {
        return;
      }

      setState(() {
        _snapshot = snapshot;
        _isRefreshing = false;
      });
    } on PlatformException catch (error, stackTrace) {
      logError('Failed to load device info.', error, stackTrace);

      if (!mounted) {
        return;
      }

      setState(() {
        _errorMessage = '读取设备信息失败：${error.message ?? error.code}';
        _isRefreshing = false;
      });
    } catch (error, stackTrace) {
      logError('Failed to load device info.', error, stackTrace);

      if (!mounted) {
        return;
      }

      setState(() {
        _errorMessage = '读取设备信息失败：$error';
        _isRefreshing = false;
      });
    }
  }

  Future<_DeviceInfoSnapshot> _loadSnapshot() async {
    if (kIsWeb) {
      final WebBrowserInfo info = await _deviceInfoPlugin.webBrowserInfo;
      return _DeviceInfoSnapshot.web(
        info: info,
        fields: _readWebBrowserInfo(info),
      );
    }

    switch (defaultTargetPlatform) {
      case TargetPlatform.android:
        final AndroidDeviceInfo info = await _deviceInfoPlugin.androidInfo;
        return _DeviceInfoSnapshot.android(
          info: info,
          fields: _readAndroidBuildData(info),
        );
      case TargetPlatform.iOS:
        final IosDeviceInfo info = await _deviceInfoPlugin.iosInfo;
        return _DeviceInfoSnapshot.ios(
          info: info,
          fields: _readIosDeviceInfo(info),
        );
      case TargetPlatform.macOS:
        final MacOsDeviceInfo info = await _deviceInfoPlugin.macOsInfo;
        return _DeviceInfoSnapshot.macOs(
          info: info,
          fields: _readMacOsDeviceInfo(info),
        );
      case TargetPlatform.linux:
        final LinuxDeviceInfo info = await _deviceInfoPlugin.linuxInfo;
        return _DeviceInfoSnapshot.linux(
          info: info,
          fields: _readLinuxDeviceInfo(info),
        );
      case TargetPlatform.windows:
        final WindowsDeviceInfo info = await _deviceInfoPlugin.windowsInfo;
        return _DeviceInfoSnapshot.windows(
          info: info,
          fields: _readWindowsDeviceInfo(info),
        );
      case TargetPlatform.fuchsia:
        return _DeviceInfoSnapshot.unsupported(platformLabel: 'Fuchsia');
    }
  }

  Map<String, Object?> _readAndroidBuildData(AndroidDeviceInfo data) {
    return <String, Object?>{
      'version.securityPatch': data.version.securityPatch,
      'version.sdkInt': data.version.sdkInt,
      'version.release': data.version.release,
      'version.previewSdkInt': data.version.previewSdkInt,
      'version.incremental': data.version.incremental,
      'version.codename': data.version.codename,
      'version.baseOS': data.version.baseOS,
      'board': data.board,
      'bootloader': data.bootloader,
      'brand': data.brand,
      'device': data.device,
      'display': data.display,
      'fingerprint': data.fingerprint,
      'hardware': data.hardware,
      'host': data.host,
      'id': data.id,
      'manufacturer': data.manufacturer,
      'model': data.model,
      'product': data.product,
      'name': data.name,
      'supported32BitAbis': data.supported32BitAbis,
      'supported64BitAbis': data.supported64BitAbis,
      'supportedAbis': data.supportedAbis,
      'tags': data.tags,
      'type': data.type,
      'isPhysicalDevice': data.isPhysicalDevice,
      'freeDiskSize': data.freeDiskSize,
      'totalDiskSize': data.totalDiskSize,
      'systemFeatures': data.systemFeatures,
      'isLowRamDevice': data.isLowRamDevice,
      'physicalRamSize': data.physicalRamSize,
      'availableRamSize': data.availableRamSize,
    };
  }

  Map<String, Object?> _readIosDeviceInfo(IosDeviceInfo data) {
    return <String, Object?>{
      'name': data.name,
      'systemName': data.systemName,
      'systemVersion': data.systemVersion,
      'model': data.model,
      'modelName': data.modelName,
      'localizedModel': data.localizedModel,
      'identifierForVendor': data.identifierForVendor,
      'isPhysicalDevice': data.isPhysicalDevice,
      'isiOSAppOnMac': data.isiOSAppOnMac,
      'isiOSAppOnVision': data.isiOSAppOnVision,
      'freeDiskSize': data.freeDiskSize,
      'totalDiskSize': data.totalDiskSize,
      'physicalRamSize': data.physicalRamSize,
      'availableRamSize': data.availableRamSize,
      'utsname.sysname': data.utsname.sysname,
      'utsname.nodename': data.utsname.nodename,
      'utsname.release': data.utsname.release,
      'utsname.version': data.utsname.version,
      'utsname.machine': data.utsname.machine,
    };
  }

  Map<String, Object?> _readLinuxDeviceInfo(LinuxDeviceInfo data) {
    return <String, Object?>{
      'name': data.name,
      'version': data.version,
      'id': data.id,
      'idLike': data.idLike,
      'versionCodename': data.versionCodename,
      'versionId': data.versionId,
      'prettyName': data.prettyName,
      'buildId': data.buildId,
      'variant': data.variant,
      'variantId': data.variantId,
      'machineId': data.machineId,
    };
  }

  Map<String, Object?> _readWebBrowserInfo(WebBrowserInfo data) {
    return <String, Object?>{
      'browserName': data.browserName.name,
      'appCodeName': data.appCodeName,
      'appName': data.appName,
      'appVersion': data.appVersion,
      'deviceMemory': data.deviceMemory,
      'language': data.language,
      'languages': data.languages,
      'platform': data.platform,
      'product': data.product,
      'productSub': data.productSub,
      'userAgent': data.userAgent,
      'vendor': data.vendor,
      'vendorSub': data.vendorSub,
      'hardwareConcurrency': data.hardwareConcurrency,
      'maxTouchPoints': data.maxTouchPoints,
    };
  }

  Map<String, Object?> _readMacOsDeviceInfo(MacOsDeviceInfo data) {
    return <String, Object?>{
      'computerName': data.computerName,
      'hostName': data.hostName,
      'arch': data.arch,
      'model': data.model,
      'modelName': data.modelName,
      'kernelVersion': data.kernelVersion,
      'majorVersion': data.majorVersion,
      'minorVersion': data.minorVersion,
      'patchVersion': data.patchVersion,
      'osRelease': data.osRelease,
      'activeCPUs': data.activeCPUs,
      'memorySize': data.memorySize,
      'cpuFrequency': data.cpuFrequency,
      'systemGUID': data.systemGUID,
    };
  }

  Map<String, Object?> _readWindowsDeviceInfo(WindowsDeviceInfo data) {
    return <String, Object?>{
      'numberOfCores': data.numberOfCores,
      'computerName': data.computerName,
      'systemMemoryInMegabytes': data.systemMemoryInMegabytes,
      'userName': data.userName,
      'majorVersion': data.majorVersion,
      'minorVersion': data.minorVersion,
      'buildNumber': data.buildNumber,
      'platformId': data.platformId,
      'csdVersion': data.csdVersion,
      'servicePackMajor': data.servicePackMajor,
      'servicePackMinor': data.servicePackMinor,
      'suitMask': data.suitMask,
      'productType': data.productType,
      'reserved': data.reserved,
      'buildLab': data.buildLab,
      'buildLabEx': data.buildLabEx,
      'digitalProductId': data.digitalProductId,
      'displayVersion': data.displayVersion,
      'editionId': data.editionId,
      'installDate': data.installDate,
      'productId': data.productId,
      'productName': data.productName,
      'registeredOwner': data.registeredOwner,
      'releaseId': data.releaseId,
      'deviceId': data.deviceId,
    };
  }

  Future<void> _handleRefresh() async {
    await _refreshDeviceInfo(source: '手动刷新');
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
        actions: <Widget>[
          IconButton(
            onPressed: _isRefreshing ? null : _handleRefresh,
            icon: const Icon(Icons.refresh_rounded),
            tooltip: 'Refresh',
          ),
        ],
      ),
      body: getBody(),
    );
  }

  Widget getBody() {
    final ThemeData theme = Theme.of(context);
    final _DeviceInfoSnapshot? snapshot = _snapshot;

    return ListView(
      padding: const EdgeInsets.all(16),
      children: <Widget>[
        const _SectionCard(
          title: 'device_info_plus 示例',
          subtitle: '读取当前设备或浏览器的系统信息，并展示各平台返回的关键字段。',
          accentColor: Color(0xFF0F5DAA),
          child: Wrap(
            spacing: 8,
            runSpacing: 8,
            children: <Widget>[
              _FeatureChip(label: 'androidInfo'),
              _FeatureChip(label: 'iosInfo'),
              _FeatureChip(label: 'webBrowserInfo'),
              _FeatureChip(label: 'macOsInfo'),
              _FeatureChip(label: 'linuxInfo'),
              _FeatureChip(label: 'windowsInfo'),
            ],
          ),
        ),
        const SizedBox(height: 16),
        if (_errorMessage != null)
          _SectionCard(
            title: '读取失败',
            subtitle: _errorMessage!,
            accentColor: theme.colorScheme.error,
            child: Align(
              alignment: Alignment.centerLeft,
              child: FilledButton.icon(
                onPressed: _isRefreshing ? null : _handleRefresh,
                icon: const Icon(Icons.refresh_rounded),
                label: const Text('重试'),
              ),
            ),
          ),
        if (_snapshot == null && _isRefreshing)
          const Padding(
            padding: EdgeInsets.symmetric(vertical: 48),
            child: Center(child: CircularProgressIndicator()),
          ),
        if (snapshot != null) ...<Widget>[
          _SectionCard(
            title: snapshot.title,
            subtitle: snapshot.subtitle,
            accentColor: snapshot.accentColor,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: <Widget>[
                Row(
                  children: <Widget>[
                    Icon(
                      snapshot.iconData,
                      color: snapshot.accentColor,
                      size: 28,
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: Text(
                        snapshot.platformLabel,
                        style: theme.textTheme.titleMedium?.copyWith(
                          fontWeight: FontWeight.w700,
                        ),
                      ),
                    ),
                    _StatusBadge(
                      label: _isRefreshing ? 'REFRESHING' : 'READY',
                      color: snapshot.accentColor,
                    ),
                  ],
                ),
                const SizedBox(height: 12),
                Text(
                  '最近读取：${_formatDateTime(snapshot.loadedAt)}',
                  style: theme.textTheme.bodySmall?.copyWith(
                    color: theme.colorScheme.onSurfaceVariant,
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: 16),
          _SectionCard(
            title: '关键信息',
            subtitle: '不同平台字段不完全一致，这里优先展示最常用的几项。',
            accentColor: snapshot.accentColor,
            child: Wrap(
              spacing: 12,
              runSpacing: 12,
              children: snapshot.metrics
                  .map((metric) => _MetricCard(metric: metric))
                  .toList(growable: false),
            ),
          ),
          const SizedBox(height: 16),
          _SectionCard(
            title: '平台提示',
            subtitle: snapshot.note,
            accentColor: const Color(0xFF1C8A63),
            child: Text(
              '这个页面展示的是按平台整理后的只读字段。'
              '如果你后续要把这些信息上传日志或埋点，'
              '建议先做白名单筛选，不要直接整包上报。',
              style: theme.textTheme.bodyMedium?.copyWith(
                color: theme.colorScheme.onSurfaceVariant,
              ),
            ),
          ),
          const SizedBox(height: 16),
          _SectionCard(
            title: '原始字段',
            subtitle: '字段名和返回内容参考官方示例做了扁平化处理，方便浏览与调试。',
            accentColor: const Color(0xFFE08A00),
            child: SelectionArea(
              child: Column(
                children: snapshot.fields.entries
                    .map(
                      (entry) => _FieldRow(
                        label: entry.key,
                        value: _formatValue(entry.value),
                      ),
                    )
                    .toList(growable: false),
              ),
            ),
          ),
        ],
      ],
    );
  }

  String _formatDateTime(DateTime dateTime) {
    final String hour = dateTime.hour.toString().padLeft(2, '0');
    final String minute = dateTime.minute.toString().padLeft(2, '0');
    final String second = dateTime.second.toString().padLeft(2, '0');
    return '$hour:$minute:$second';
  }

  String _formatValue(Object? value) {
    final Object? normalizedValue = _normalizeValue(value);

    if (normalizedValue == null) {
      return '-';
    }
    if (normalizedValue is String) {
      return normalizedValue;
    }
    if (normalizedValue is num || normalizedValue is bool) {
      return '$normalizedValue';
    }

    return const JsonEncoder.withIndent('  ').convert(normalizedValue);
  }

  Object? _normalizeValue(Object? value) {
    if (value == null || value is String || value is num || value is bool) {
      return value;
    }
    if (value is DateTime) {
      return value.toIso8601String();
    }
    if (value is Uint8List) {
      return value.toList(growable: false);
    }
    if (value is Enum) {
      return value.name;
    }
    if (value is Iterable<Object?>) {
      return value.map(_normalizeValue).toList(growable: false);
    }
    if (value is Map<Object?, Object?>) {
      final Map<String, Object?> normalizedMap = <String, Object?>{};
      for (final MapEntry<Object?, Object?> entry in value.entries) {
        normalizedMap['${entry.key}'] = _normalizeValue(entry.value);
      }
      return normalizedMap;
    }
    return '$value';
  }
}

class _DeviceInfoSnapshot {
  const _DeviceInfoSnapshot({
    required this.platformLabel,
    required this.title,
    required this.subtitle,
    required this.iconData,
    required this.accentColor,
    required this.metrics,
    required this.fields,
    required this.note,
    required this.loadedAt,
  });

  factory _DeviceInfoSnapshot.android({
    required AndroidDeviceInfo info,
    required Map<String, Object?> fields,
  }) {
    return _DeviceInfoSnapshot(
      platformLabel: 'Android Device Info',
      title: '${info.brand} ${info.model}'.trim(),
      subtitle: 'Android ${info.version.release} · SDK ${info.version.sdkInt}',
      iconData: Icons.phone_android_rounded,
      accentColor: const Color(0xFF0F5DAA),
      metrics: <_DeviceMetric>[
        _DeviceMetric(label: 'Manufacturer', value: info.manufacturer),
        _DeviceMetric(label: 'Device', value: info.device),
        _DeviceMetric(
          label: 'Physical',
          value: info.isPhysicalDevice ? 'Yes' : 'Simulator',
        ),
        _DeviceMetric(label: 'RAM', value: '${info.availableRamSize} MB free'),
      ],
      fields: _sortFields(fields),
      note:
          'Android 的 serial 只有在满足官方权限与设备条件时才会返回真实值，'
          '否则通常是 unknown。',
      loadedAt: DateTime.now(),
    );
  }

  factory _DeviceInfoSnapshot.ios({
    required IosDeviceInfo info,
    required Map<String, Object?> fields,
  }) {
    return _DeviceInfoSnapshot(
      platformLabel: 'iOS Device Info',
      title: info.modelName,
      subtitle: '${info.systemName} ${info.systemVersion}',
      iconData: Icons.phone_iphone_rounded,
      accentColor: const Color(0xFF1C8A63),
      metrics: <_DeviceMetric>[
        _DeviceMetric(label: 'Model', value: info.model),
        _DeviceMetric(label: 'Machine', value: info.utsname.machine),
        _DeviceMetric(
          label: 'Physical',
          value: info.isPhysicalDevice ? 'Yes' : 'Simulator',
        ),
        _DeviceMetric(
          label: 'Identifier',
          value: info.identifierForVendor ?? 'Unavailable',
        ),
      ],
      fields: _sortFields(fields),
      note:
          'iOS 16 及以上如果没有额外 entitlement，'
          '`name` 字段通常只会返回 iPhone 或 iPad 这类通用名称。',
      loadedAt: DateTime.now(),
    );
  }

  factory _DeviceInfoSnapshot.web({
    required WebBrowserInfo info,
    required Map<String, Object?> fields,
  }) {
    return _DeviceInfoSnapshot(
      platformLabel: 'Web Browser Info',
      title: _browserNameLabel(info.browserName),
      subtitle: '${info.platform ?? 'Web'} · ${info.language ?? 'unknown'}',
      iconData: Icons.language_rounded,
      accentColor: const Color(0xFFE08A00),
      metrics: <_DeviceMetric>[
        _DeviceMetric(label: 'Vendor', value: info.vendor ?? 'Unknown'),
        _DeviceMetric(
          label: 'Device Memory',
          value: info.deviceMemory == null ? '-' : '${info.deviceMemory} GB',
        ),
        _DeviceMetric(
          label: 'CPU Cores',
          value: '${info.hardwareConcurrency ?? '-'}',
        ),
        _DeviceMetric(
          label: 'Touch Points',
          value: '${info.maxTouchPoints ?? 0}',
        ),
      ],
      fields: _sortFields(fields),
      note:
          'Web 端拿到的是浏览器 Navigator 暴露的信息，不等同于完整设备指纹，'
          '很多字段会被浏览器主动弱化或隐藏。',
      loadedAt: DateTime.now(),
    );
  }

  factory _DeviceInfoSnapshot.macOs({
    required MacOsDeviceInfo info,
    required Map<String, Object?> fields,
  }) {
    return _DeviceInfoSnapshot(
      platformLabel: 'macOS Device Info',
      title: info.modelName,
      subtitle:
          'macOS ${info.majorVersion}.${info.minorVersion}.${info.patchVersion}',
      iconData: Icons.laptop_mac_rounded,
      accentColor: const Color(0xFF6D4C41),
      metrics: <_DeviceMetric>[
        _DeviceMetric(label: 'Model', value: info.model),
        _DeviceMetric(label: 'Arch', value: info.arch),
        _DeviceMetric(label: 'CPUs', value: '${info.activeCPUs}'),
        _DeviceMetric(
          label: 'Memory',
          value:
              '${(info.memorySize / (1024 * 1024 * 1024)).toStringAsFixed(1)} GB',
        ),
      ],
      fields: _sortFields(fields),
      note:
          'Apple Silicon 设备如果当前进程通过 Rosetta 运行，'
          '`arch` 可能显示为 x86_64。',
      loadedAt: DateTime.now(),
    );
  }

  factory _DeviceInfoSnapshot.linux({
    required LinuxDeviceInfo info,
    required Map<String, Object?> fields,
  }) {
    return _DeviceInfoSnapshot(
      platformLabel: 'Linux Device Info',
      title: info.prettyName,
      subtitle: 'Linux ${info.versionId ?? info.version ?? 'Unknown'}',
      iconData: Icons.desktop_windows_outlined,
      accentColor: const Color(0xFF455A64),
      metrics: <_DeviceMetric>[
        _DeviceMetric(label: 'ID', value: info.id),
        _DeviceMetric(
          label: 'Version',
          value: info.versionId ?? info.version ?? '-',
        ),
        _DeviceMetric(
          label: 'Variant',
          value: info.variant ?? info.variantId ?? '-',
        ),
        _DeviceMetric(
          label: 'Machine ID',
          value: info.machineId ?? 'Unavailable',
        ),
      ],
      fields: _sortFields(fields),
      note: 'Linux 返回值主要来自 `/etc/os-release` 与 machine-id 文件。',
      loadedAt: DateTime.now(),
    );
  }

  factory _DeviceInfoSnapshot.windows({
    required WindowsDeviceInfo info,
    required Map<String, Object?> fields,
  }) {
    return _DeviceInfoSnapshot(
      platformLabel: 'Windows Device Info',
      title: info.productName,
      subtitle:
          'Windows ${info.majorVersion}.${info.minorVersion}.${info.buildNumber}',
      iconData: Icons.desktop_windows_rounded,
      accentColor: const Color(0xFF1565C0),
      metrics: <_DeviceMetric>[
        _DeviceMetric(label: 'Computer', value: info.computerName),
        _DeviceMetric(label: 'Edition', value: info.editionId),
        _DeviceMetric(label: 'CPU Cores', value: '${info.numberOfCores}'),
        _DeviceMetric(
          label: 'Memory',
          value: '${info.systemMemoryInMegabytes} MB',
        ),
      ],
      fields: _sortFields(fields),
      note:
          'Windows 返回值里包含 installDate、digitalProductId 等偏系统级字段，'
          '用于展示和诊断比直接上报更合适。',
      loadedAt: DateTime.now(),
    );
  }

  factory _DeviceInfoSnapshot.unsupported({required String platformLabel}) {
    return _DeviceInfoSnapshot(
      platformLabel: '$platformLabel Device Info',
      title: '$platformLabel 暂不支持',
      subtitle: '当前平台没有可读取的示例数据',
      iconData: Icons.device_unknown_rounded,
      accentColor: const Color(0xFF8E8E93),
      metrics: const <_DeviceMetric>[
        _DeviceMetric(label: 'Status', value: 'Unsupported'),
      ],
      fields: const <String, Object?>{
        'error': 'Current platform is not supported by this demo.',
      },
      note: '当前 demo 没有为这个平台提供额外适配。',
      loadedAt: DateTime.now(),
    );
  }

  final String platformLabel;
  final String title;
  final String subtitle;
  final IconData iconData;
  final Color accentColor;
  final List<_DeviceMetric> metrics;
  final Map<String, Object?> fields;
  final String note;
  final DateTime loadedAt;

  static String _browserNameLabel(BrowserName browserName) {
    return switch (browserName) {
      BrowserName.chrome => 'Chrome Browser',
      BrowserName.firefox => 'Firefox Browser',
      BrowserName.edge => 'Edge Browser',
      BrowserName.opera => 'Opera Browser',
      BrowserName.safari => 'Safari Browser',
      BrowserName.samsungInternet => 'Samsung Internet',
      BrowserName.msie => 'Internet Explorer',
      BrowserName.unknown => 'Unknown Browser',
    };
  }

  static Map<String, Object?> _sortFields(Map<String, Object?> fields) {
    final List<MapEntry<String, Object?>> entries = fields.entries.toList()
      ..sort(
        (MapEntry<String, Object?> left, MapEntry<String, Object?> right) =>
            left.key.compareTo(right.key),
      );

    return Map<String, Object?>.fromEntries(entries);
  }
}

class _DeviceMetric {
  const _DeviceMetric({required this.label, required this.value});

  final String label;
  final String value;
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
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: accentColor.withValues(alpha: 0.14)),
        boxShadow: const <BoxShadow>[
          BoxShadow(
            color: Color(0x120A2533),
            blurRadius: 18,
            offset: Offset(0, 8),
          ),
        ],
      ),
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
              color: theme.colorScheme.onSurfaceVariant,
            ),
          ),
          const SizedBox(height: 16),
          child,
        ],
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
        color: const Color(0xFFF1F6FB),
        borderRadius: BorderRadius.circular(999),
      ),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
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

class _MetricCard extends StatelessWidget {
  const _MetricCard({required this.metric});

  final _DeviceMetric metric;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return SizedBox(
      width: 156,
      child: DecoratedBox(
        decoration: BoxDecoration(
          color: const Color(0xFFF8FAFC),
          borderRadius: BorderRadius.circular(18),
          border: Border.all(color: const Color(0xFFE4EBF3)),
        ),
        child: Padding(
          padding: const EdgeInsets.all(14),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: <Widget>[
              Text(
                metric.label,
                style: theme.textTheme.labelMedium?.copyWith(
                  color: theme.colorScheme.onSurfaceVariant,
                ),
              ),
              const SizedBox(height: 8),
              Text(
                metric.value,
                style: theme.textTheme.titleSmall?.copyWith(
                  fontWeight: FontWeight.w700,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _FieldRow extends StatelessWidget {
  const _FieldRow({required this.label, required this.value});

  final String label;
  final String value;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: Container(
        width: double.infinity,
        decoration: BoxDecoration(
          color: const Color(0xFFF8FAFC),
          borderRadius: BorderRadius.circular(16),
          border: Border.all(color: const Color(0xFFE4EBF3)),
        ),
        padding: const EdgeInsets.all(12),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Text(
              label,
              style: theme.textTheme.labelMedium?.copyWith(
                color: theme.colorScheme.primary,
                fontWeight: FontWeight.w700,
              ),
            ),
            const SizedBox(height: 6),
            SelectableText(
              value,
              style: theme.textTheme.bodyMedium?.copyWith(height: 1.4),
            ),
          ],
        ),
      ),
    );
  }
}

class _StatusBadge extends StatelessWidget {
  const _StatusBadge({required this.label, required this.color});

  final String label;
  final Color color;

  @override
  Widget build(BuildContext context) {
    return DecoratedBox(
      decoration: BoxDecoration(
        color: color.withValues(alpha: 0.12),
        borderRadius: BorderRadius.circular(999),
      ),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
        child: Text(
          label,
          style: Theme.of(context).textTheme.labelSmall?.copyWith(
            color: color,
            fontWeight: FontWeight.w700,
            letterSpacing: 0.6,
          ),
        ),
      ),
    );
  }
}
