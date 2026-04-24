import 'dart:async';

import 'package:basic_flutter/core/utils/logger/logger.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:package_info_plus/package_info_plus.dart';

/// Package Info Plus
/// https://pub.dev/packages/package_info_plus
class PackageInfoPlusDemoPage extends StatelessWidget {
  const PackageInfoPlusDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return PackageInfoPlusDemoView(title: title);
  }
}

class PackageInfoPlusDemoView extends StatefulWidget {
  const PackageInfoPlusDemoView({super.key, required this.title});

  final String title;

  @override
  State<PackageInfoPlusDemoView> createState() =>
      _PackageInfoPlusDemoViewState();
}

class _PackageInfoPlusDemoViewState extends State<PackageInfoPlusDemoView> {
  PackageInfo? _packageInfo;
  String? _errorMessage;
  DateTime? _loadedAt;
  bool _isLoading = false;
  bool _isPluginAvailable = true;

  @override
  void initState() {
    super.initState();
    unawaited(_loadPackageInfo(source: '页面初始化'));
  }

  Future<void> _loadPackageInfo({required String source}) async {
    if (_isLoading) {
      return;
    }

    setState(() {
      _isLoading = true;
      _errorMessage = null;
      _isPluginAvailable = true;
    });

    try {
      final PackageInfo packageInfo = await PackageInfo.fromPlatform();
      logInfo(
        'Package info loaded [$source]: '
        '${packageInfo.packageName} ${packageInfo.version}'
        '+${packageInfo.buildNumber}',
      );

      if (!mounted) {
        return;
      }

      setState(() {
        _packageInfo = packageInfo;
        _loadedAt = DateTime.now();
        _isLoading = false;
      });
    } on MissingPluginException catch (error, stackTrace) {
      logError(
        'package_info_plus plugin is not registered.',
        error,
        stackTrace,
      );

      if (!mounted) {
        return;
      }

      setState(() {
        _packageInfo = null;
        _isLoading = false;
        _isPluginAvailable = false;
        _errorMessage =
            '当前运行环境未注册 package_info_plus 插件。通常需要冷启动 App；'
            'widget test 环境里看到这个提示也属于正常现象。';
      });
    } on PlatformException catch (error, stackTrace) {
      logError('Failed to load package info.', error, stackTrace);

      if (!mounted) {
        return;
      }

      setState(() {
        _packageInfo = null;
        _isLoading = false;
        _errorMessage = '读取应用信息失败：${error.message ?? error.code}';
      });
    } catch (error, stackTrace) {
      logError('Failed to load package info.', error, stackTrace);

      if (!mounted) {
        return;
      }

      setState(() {
        _packageInfo = null;
        _isLoading = false;
        _errorMessage = '读取应用信息失败：$error';
      });
    }
  }

  String _platformLabel() {
    if (kIsWeb) {
      return 'Web';
    }

    return switch (defaultTargetPlatform) {
      TargetPlatform.android => 'Android',
      TargetPlatform.iOS => 'iOS',
      TargetPlatform.macOS => 'macOS',
      TargetPlatform.linux => 'Linux',
      TargetPlatform.windows => 'Windows',
      TargetPlatform.fuchsia => 'Fuchsia',
    };
  }

  Color _accentColor(ThemeData theme) {
    if (_packageInfo != null) {
      return const Color(0xFF1C8A63);
    }
    if (!_isPluginAvailable) {
      return theme.colorScheme.error;
    }
    if (_errorMessage != null) {
      return const Color(0xFFE08A00);
    }
    return const Color(0xFF0F5DAA);
  }

  String _statusTitle() {
    if (_packageInfo != null) {
      return '已成功读取应用包信息';
    }
    if (!_isPluginAvailable) {
      return '插件尚未注册';
    }
    if (_errorMessage != null) {
      return '读取应用信息失败';
    }
    return '准备读取应用包信息';
  }

  String _statusMessage() {
    if (_packageInfo != null) {
      return '这个页面演示 PackageInfo.fromPlatform() 的核心字段，'
          '适合快速查看当前 App 的包名、版本号、构建号与安装来源。';
    }
    if (_errorMessage != null) {
      return _errorMessage!;
    }
    return '页面进入后会自动读取当前 App 的包信息。'
        '如果你想在 runApp() 之前调用它，需要先确保 WidgetsFlutterBinding'
        '.ensureInitialized() 已执行。';
  }

  String _statusBadgeLabel() {
    if (_isLoading) {
      return 'LOADING';
    }
    if (_packageInfo != null) {
      return 'READY';
    }
    if (_errorMessage != null) {
      return 'ERROR';
    }
    return 'IDLE';
  }

  String _displayValue(String? value, {String fallback = 'Unavailable'}) {
    if (value == null || value.isEmpty) {
      return fallback;
    }
    return value;
  }

  String _formatClock(DateTime dateTime) {
    final String hour = dateTime.hour.toString().padLeft(2, '0');
    final String minute = dateTime.minute.toString().padLeft(2, '0');
    final String second = dateTime.second.toString().padLeft(2, '0');
    return '$hour:$minute:$second';
  }

  String _formatDateTime(DateTime? dateTime) {
    if (dateTime == null) {
      return 'Unavailable';
    }

    final String year = dateTime.year.toString().padLeft(4, '0');
    final String month = dateTime.month.toString().padLeft(2, '0');
    final String day = dateTime.day.toString().padLeft(2, '0');
    final String hour = dateTime.hour.toString().padLeft(2, '0');
    final String minute = dateTime.minute.toString().padLeft(2, '0');
    final String second = dateTime.second.toString().padLeft(2, '0');
    return '$year-$month-$day $hour:$minute:$second';
  }

  String _buildVersionLabel(PackageInfo packageInfo) {
    return '${packageInfo.version}+${packageInfo.buildNumber}';
  }

  Future<void> _copyText(String label, String value) async {
    if (value.isEmpty || value == 'Unavailable') {
      return;
    }

    await Clipboard.setData(ClipboardData(text: value));
    if (!mounted) {
      return;
    }

    ScaffoldMessenger.of(
      context,
    ).showSnackBar(SnackBar(content: Text('$label 已复制到剪贴板')));
  }

  List<_PackageInfoField> _buildInfoFields() {
    final PackageInfo? packageInfo = _packageInfo;
    if (packageInfo == null) {
      return const <_PackageInfoField>[];
    }

    final String installerStore = _displayValue(
      packageInfo.installerStore,
      fallback: 'Unavailable / Manual install',
    );
    final String buildSignature = _displayValue(packageInfo.buildSignature);

    return <_PackageInfoField>[
      _PackageInfoField(
        label: 'Platform',
        value: _platformLabel(),
        helper: '当前运行平台',
      ),
      _PackageInfoField(
        label: 'App Name',
        value: _displayValue(packageInfo.appName),
        helper: '应用展示名',
        copyable: packageInfo.appName.isNotEmpty,
      ),
      _PackageInfoField(
        label: 'Package Name',
        value: _displayValue(packageInfo.packageName),
        helper: 'Android applicationId / iOS bundleIdentifier',
        copyable: packageInfo.packageName.isNotEmpty,
      ),
      _PackageInfoField(
        label: 'Version',
        value: _displayValue(packageInfo.version),
        helper: 'pubspec.yaml 里的主版本号',
        copyable: packageInfo.version.isNotEmpty,
      ),
      _PackageInfoField(
        label: 'Build Number',
        value: _displayValue(packageInfo.buildNumber),
        helper: 'pubspec.yaml 里的构建号',
        copyable: packageInfo.buildNumber.isNotEmpty,
      ),
      _PackageInfoField(
        label: 'Version Label',
        value: _buildVersionLabel(packageInfo),
        helper: '页面里常用的 version + buildNumber 拼接值',
        copyable: true,
      ),
      _PackageInfoField(
        label: 'Installer Store',
        value: installerStore,
        helper: '记录应用由哪个安装源安装；手动安装时通常为空',
        copyable: packageInfo.installerStore?.isNotEmpty ?? false,
      ),
      _PackageInfoField(
        label: 'Build Signature',
        value: buildSignature,
        helper: 'Android 上可能返回签名摘要，其它平台通常为空字符串',
        copyable: packageInfo.buildSignature.isNotEmpty,
      ),
      _PackageInfoField(
        label: 'Install Time',
        value: _formatDateTime(packageInfo.installTime),
        helper: '首次安装时间；部分平台可能返回空值',
      ),
      _PackageInfoField(
        label: 'Update Time',
        value: _formatDateTime(packageInfo.updateTime),
        helper: '最近更新时间；部分平台可能返回空值',
      ),
    ];
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
        actions: <Widget>[
          IconButton(
            onPressed: _isLoading
                ? null
                : () => _loadPackageInfo(source: '手动读取'),
            icon: const Icon(Icons.refresh_rounded),
            tooltip: 'Reload',
          ),
        ],
      ),
      body: getBody(),
    );
  }

  Widget getBody() {
    final ThemeData theme = Theme.of(context);
    final Color accentColor = _accentColor(theme);
    final PackageInfo? packageInfo = _packageInfo;
    final List<_PackageInfoField> infoFields = _buildInfoFields();

    return ListView(
      padding: const EdgeInsets.all(16),
      children: <Widget>[
        const _SectionCard(
          title: 'package_info_plus 示例',
          subtitle:
              '读取应用名、包名、版本号、构建号与安装来源。'
              '如果你计划在 runApp() 前调用 PackageInfo.fromPlatform()，'
              '要先执行 WidgetsFlutterBinding.ensureInitialized()。',
          child: Wrap(
            spacing: 8,
            runSpacing: 8,
            children: <Widget>[
              _FeatureChip(label: 'PackageInfo.fromPlatform()'),
              _FeatureChip(label: 'installerStore'),
              _FeatureChip(label: 'buildSignature'),
              _FeatureChip(label: 'installTime / updateTime'),
            ],
          ),
        ),
        const SizedBox(height: 16),
        _SectionCard(
          title: _statusTitle(),
          subtitle: _statusMessage(),
          accentColor: accentColor,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: <Widget>[
              Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: <Widget>[
                  Icon(
                    packageInfo != null
                        ? Icons.inventory_2_rounded
                        : _errorMessage != null
                        ? Icons.warning_amber_rounded
                        : Icons.info_outline_rounded,
                    color: accentColor,
                    size: 28,
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Text(
                      packageInfo == null
                          ? '等待首次读取'
                          : '${packageInfo.appName} · ${_buildVersionLabel(packageInfo)}',
                      style: theme.textTheme.titleMedium?.copyWith(
                        fontWeight: FontWeight.w700,
                      ),
                    ),
                  ),
                  const SizedBox(width: 12),
                  _StatusBadge(label: _statusBadgeLabel(), color: accentColor),
                ],
              ),
              const SizedBox(height: 12),
              Text(
                _loadedAt == null
                    ? '最近读取：尚未完成'
                    : '最近读取：${_formatClock(_loadedAt!)}',
                style: theme.textTheme.bodySmall?.copyWith(
                  color: theme.colorScheme.onSurfaceVariant,
                ),
              ),
              const SizedBox(height: 12),
              FilledButton.icon(
                onPressed: _isLoading
                    ? null
                    : () => _loadPackageInfo(source: '手动读取'),
                icon: _isLoading
                    ? const SizedBox(
                        width: 16,
                        height: 16,
                        child: CircularProgressIndicator(strokeWidth: 2),
                      )
                    : const Icon(Icons.refresh_rounded),
                label: Text(_isLoading ? '读取中...' : '重新读取应用信息'),
              ),
            ],
          ),
        ),
        const SizedBox(height: 16),
        _SectionCard(
          title: '字段预览',
          subtitle: packageInfo == null
              ? '读取成功后，这里会展示 PackageInfo.fromPlatform() 的返回内容。'
              : '下面这些值来自当前平台实现，你可以直接对照 pubspec 版本配置观察变化。',
          child: infoFields.isEmpty
              ? Text('暂无可展示数据。', style: theme.textTheme.bodyMedium)
              : Column(
                  children: infoFields
                      .map(
                        (_PackageInfoField field) => Padding(
                          padding: const EdgeInsets.only(bottom: 12),
                          child: _InfoTile(
                            field: field,
                            onCopy: field.copyable
                                ? () => _copyText(field.label, field.value)
                                : null,
                          ),
                        ),
                      )
                      .toList(),
                ),
        ),
        const SizedBox(height: 16),
        const _SectionCard(
          title: '使用提示',
          subtitle:
              'PackageInfo.fromPlatform() 会缓存结果，'
              '适合在页面或启动流程里做一次性读取。'
              '如果你修改了 pubspec.yaml 里的版本号，'
              'iOS / macOS 端有时需要重新构建后才能看到最新值。',
          child: SizedBox.shrink(),
        ),
      ],
    );
  }
}

class _PackageInfoField {
  const _PackageInfoField({
    required this.label,
    required this.value,
    required this.helper,
    this.copyable = false,
  });

  final String label;
  final String value;
  final String helper;
  final bool copyable;
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

    return DecoratedBox(
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
            const SizedBox(height: 8),
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
        color: const Color(0xFFE8F1FB),
        borderRadius: BorderRadius.circular(999),
      ),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
        child: Text(label),
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
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
        child: Text(
          label,
          style: TextStyle(color: color, fontWeight: FontWeight.w700),
        ),
      ),
    );
  }
}

class _InfoTile extends StatelessWidget {
  const _InfoTile({required this.field, this.onCopy});

  final _PackageInfoField field;
  final VoidCallback? onCopy;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return DecoratedBox(
      decoration: BoxDecoration(
        color: const Color(0xFFF6F8FB),
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: const Color(0xFFDCE4EF)),
      ),
      child: Padding(
        padding: const EdgeInsets.all(12),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: <Widget>[
                Expanded(
                  child: Text(
                    field.label,
                    style: theme.textTheme.bodySmall?.copyWith(
                      color: theme.colorScheme.onSurfaceVariant,
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                ),
                if (onCopy != null)
                  IconButton(
                    onPressed: onCopy,
                    icon: const Icon(Icons.copy_rounded, size: 18),
                    visualDensity: VisualDensity.compact,
                    tooltip: 'Copy',
                  ),
              ],
            ),
            const SizedBox(height: 4),
            Text(
              field.value,
              style: theme.textTheme.bodyLarge?.copyWith(
                fontWeight: FontWeight.w600,
              ),
            ),
            const SizedBox(height: 6),
            Text(
              field.helper,
              style: theme.textTheme.bodySmall?.copyWith(
                color: theme.colorScheme.onSurfaceVariant,
                height: 1.4,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
