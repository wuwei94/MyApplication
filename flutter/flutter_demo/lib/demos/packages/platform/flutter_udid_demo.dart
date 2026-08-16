import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_demo/core/utils/logger/logger.dart';
import 'package:flutter_udid/flutter_udid.dart';

/// Flutter UDID
/// https://pub.dev/packages/flutter_udid
class FlutterUdidDemoPage extends StatelessWidget {
  const FlutterUdidDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return FlutterUdidDemoView(title: title);
  }
}

class FlutterUdidDemoView extends StatefulWidget {
  const FlutterUdidDemoView({super.key, required this.title});

  final String title;

  @override
  State<FlutterUdidDemoView> createState() => _FlutterUdidDemoViewState();
}

class _FlutterUdidDemoViewState extends State<FlutterUdidDemoView> {
  _FlutterUdidSnapshot? _snapshot;
  String? _statusMessage;
  bool _isRefreshing = false;
  bool _isPluginAvailable = true;
  bool _isPlatformSupported = true;

  @override
  void initState() {
    super.initState();
    unawaited(_refreshUdid(source: '页面初始化'));
  }

  Future<void> _refreshUdid({required String source}) async {
    if (_isRefreshing) {
      return;
    }

    if (kIsWeb || defaultTargetPlatform == TargetPlatform.fuchsia) {
      setState(() {
        _snapshot = null;
        _isRefreshing = false;
        _isPluginAvailable = true;
        _isPlatformSupported = false;
        _statusMessage = 'flutter_udid 当前不提供 Web/Fuchsia 实现。';
      });
      return;
    }

    setState(() {
      _isRefreshing = true;
      _isPluginAvailable = true;
      _isPlatformSupported = true;
      _statusMessage = null;
    });

    try {
      final List<String> values = await Future.wait<String>(<Future<String>>[
        FlutterUdid.udid,
        FlutterUdid.consistentUdid,
      ]);
      final _FlutterUdidSnapshot snapshot = _FlutterUdidSnapshot(
        platformLabel: _platformLabel(),
        udid: values[0],
        consistentUdid: values[1],
        loadedAt: DateTime.now(),
      );
      logInfo('Flutter UDID refreshed [$source]: ${snapshot.platformLabel}');

      if (!mounted) {
        return;
      }

      setState(() {
        _snapshot = snapshot;
        _isRefreshing = false;
        _statusMessage = '已成功读取平台 UDID 和统一格式 UDID。';
      });
    } on MissingPluginException catch (error, stackTrace) {
      logError('flutter_udid plugin is not registered.', error, stackTrace);

      if (!mounted) {
        return;
      }

      setState(() {
        _snapshot = null;
        _isRefreshing = false;
        _isPluginAvailable = false;
        _statusMessage =
            '当前运行环境未注册 flutter_udid 插件。通常需要冷启动 App；'
            'widget test 环境里看到这个提示也属于正常现象。';
      });
    } on PlatformException catch (error, stackTrace) {
      logError('Failed to load flutter_udid.', error, stackTrace);

      if (!mounted) {
        return;
      }

      setState(() {
        _snapshot = null;
        _isRefreshing = false;
        _statusMessage = '读取 UDID 失败：${error.message ?? error.code}';
      });
    } catch (error, stackTrace) {
      logError('Failed to load flutter_udid.', error, stackTrace);

      if (!mounted) {
        return;
      }

      setState(() {
        _snapshot = null;
        _isRefreshing = false;
        _statusMessage = '读取 UDID 失败：$error';
      });
    }
  }

  Future<void> _copyText(String label, String value) async {
    if (value.isEmpty || value == _unavailableText) {
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
    if (!_isPlatformSupported) {
      return const Color(0xFFE08A00);
    }
    if (!_isPluginAvailable) {
      return theme.colorScheme.error;
    }
    if (_snapshot != null) {
      return const Color(0xFF1C8A63);
    }
    return const Color(0xFF0F5DAA);
  }

  String _statusTitle() {
    if (!_isPlatformSupported) {
      return '当前平台不支持';
    }
    if (!_isPluginAvailable) {
      return '插件尚未注册';
    }
    if (_snapshot != null) {
      return '已成功读取 Flutter UDID';
    }
    return '准备读取 Flutter UDID';
  }

  String _statusMessageText() {
    if (_statusMessage != null) {
      return _statusMessage!;
    }
    return '页面进入后会自动调用 FlutterUdid.udid 和 '
        'FlutterUdid.consistentUdid。';
  }

  String _statusBadgeLabel() {
    if (_isRefreshing) {
      return 'LOADING';
    }
    if (_snapshot != null) {
      return 'READY';
    }
    if (_statusMessage != null) {
      return 'NOTICE';
    }
    return 'IDLE';
  }

  String _displayValue(String? value) {
    if (value == null || value.isEmpty) {
      return _unavailableText;
    }
    return value;
  }

  String _formatDateTime(DateTime? dateTime) {
    if (dateTime == null) {
      return _unavailableText;
    }

    final String year = dateTime.year.toString().padLeft(4, '0');
    final String month = dateTime.month.toString().padLeft(2, '0');
    final String day = dateTime.day.toString().padLeft(2, '0');
    final String hour = dateTime.hour.toString().padLeft(2, '0');
    final String minute = dateTime.minute.toString().padLeft(2, '0');
    final String second = dateTime.second.toString().padLeft(2, '0');
    return '$year-$month-$day $hour:$minute:$second';
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
        actions: <Widget>[
          IconButton(
            onPressed: _isRefreshing
                ? null
                : () => _refreshUdid(source: '手动刷新'),
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
    final Color accentColor = _accentColor(theme);
    final _FlutterUdidSnapshot? snapshot = _snapshot;

    return ListView(
      padding: const EdgeInsets.all(16),
      children: <Widget>[
        const _InfoCard(
          title: 'flutter_udid 示例',
          subtitle: '跨平台持久设备标识读取',
          accentColor: Color(0xFF0F5DAA),
          child: Wrap(
            spacing: 8,
            runSpacing: 8,
            children: <Widget>[
              _FeatureChip(label: 'FlutterUdid.udid'),
              _FeatureChip(label: 'consistentUdid'),
              _FeatureChip(label: 'MethodChannel'),
            ],
          ),
        ),
        const SizedBox(height: 16),
        _InfoCard(
          title: _statusTitle(),
          subtitle: _statusMessageText(),
          accentColor: accentColor,
          child: Row(
            children: <Widget>[
              Icon(Icons.fingerprint_rounded, color: accentColor, size: 30),
              const SizedBox(width: 12),
              Expanded(
                child: Text(
                  '当前平台：${snapshot?.platformLabel ?? _platformLabel()}',
                  style: theme.textTheme.titleMedium?.copyWith(
                    fontWeight: FontWeight.w700,
                  ),
                ),
              ),
              _StatusBadge(label: _statusBadgeLabel(), color: accentColor),
            ],
          ),
        ),
        const SizedBox(height: 16),
        _ValueCard(
          title: 'Platform UDID',
          subtitle: '平台原始格式，由 FlutterUdid.udid 返回。',
          value: _displayValue(snapshot?.udid),
          accentColor: accentColor,
          onCopy: () =>
              _copyText('Platform UDID', _displayValue(snapshot?.udid)),
        ),
        const SizedBox(height: 16),
        _ValueCard(
          title: 'Consistent UDID',
          subtitle: '统一格式，由 FlutterUdid.consistentUdid 返回。',
          value: _displayValue(snapshot?.consistentUdid),
          accentColor: accentColor,
          onCopy: () => _copyText(
            'Consistent UDID',
            _displayValue(snapshot?.consistentUdid),
          ),
        ),
        const SizedBox(height: 16),
        _InfoCard(
          title: '读取信息',
          subtitle: '最近一次调用结果',
          accentColor: accentColor,
          child: Column(
            children: <Widget>[
              _MetaRow(
                label: 'Platform',
                value: snapshot?.platformLabel ?? _platformLabel(),
              ),
              const SizedBox(height: 10),
              _MetaRow(
                label: 'Loaded at',
                value: _formatDateTime(snapshot?.loadedAt),
              ),
              const SizedBox(height: 10),
              _MetaRow(
                label: 'Plugin',
                value: _isPluginAvailable ? 'Registered' : 'Missing',
              ),
            ],
          ),
        ),
        const SizedBox(height: 16),
        const _InfoCard(
          title: '稳定性说明',
          subtitle: 'UDID 适合演示设备标识读取，不适合作为唯一安全凭证。',
          accentColor: Color(0xFFE08A00),
          child: Text(
            '设备重置、系统升级、签名证书变化、Root/Jailbreak 等情况都可能让 UDID 发生变化。'
            '正式业务中建议结合账号体系、服务端风控和隐私合规策略使用。',
          ),
        ),
      ],
    );
  }
}

const String _unavailableText = 'Unavailable';

class _FlutterUdidSnapshot {
  const _FlutterUdidSnapshot({
    required this.platformLabel,
    required this.udid,
    required this.consistentUdid,
    required this.loadedAt,
  });

  final String platformLabel;
  final String udid;
  final String consistentUdid;
  final DateTime loadedAt;
}

class _InfoCard extends StatelessWidget {
  const _InfoCard({
    required this.title,
    required this.subtitle,
    required this.accentColor,
    required this.child,
  });

  final String title;
  final String subtitle;
  final Color accentColor;
  final Widget child;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return Card(
      clipBehavior: Clip.antiAlias,
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: <Widget>[
                Container(
                  width: 4,
                  height: 42,
                  decoration: BoxDecoration(
                    color: accentColor,
                    borderRadius: BorderRadius.circular(2),
                  ),
                ),
                const SizedBox(width: 12),
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
                      Text(subtitle, style: theme.textTheme.bodyMedium),
                    ],
                  ),
                ),
              ],
            ),
            const SizedBox(height: 16),
            child,
          ],
        ),
      ),
    );
  }
}

class _ValueCard extends StatelessWidget {
  const _ValueCard({
    required this.title,
    required this.subtitle,
    required this.value,
    required this.accentColor,
    required this.onCopy,
  });

  final String title;
  final String subtitle;
  final String value;
  final Color accentColor;
  final VoidCallback onCopy;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);
    final bool canCopy = value != _unavailableText;

    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Row(
              children: <Widget>[
                Icon(Icons.badge_outlined, color: accentColor),
                const SizedBox(width: 8),
                Expanded(
                  child: Text(
                    title,
                    style: theme.textTheme.titleMedium?.copyWith(
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                ),
                IconButton(
                  onPressed: canCopy ? onCopy : null,
                  icon: const Icon(Icons.copy_rounded),
                  tooltip: 'Copy',
                ),
              ],
            ),
            const SizedBox(height: 4),
            Text(subtitle, style: theme.textTheme.bodyMedium),
            const SizedBox(height: 12),
            Container(
              width: double.infinity,
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: theme.colorScheme.surfaceContainerHighest,
                borderRadius: BorderRadius.circular(8),
              ),
              child: SelectableText(
                value,
                style: theme.textTheme.bodyMedium?.copyWith(
                  fontFeatures: const <FontFeature>[
                    FontFeature.tabularFigures(),
                  ],
                  fontWeight: FontWeight.w600,
                ),
              ),
            ),
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
    return Chip(
      avatar: const Icon(Icons.check_circle_outline_rounded, size: 18),
      label: Text(label),
    );
  }
}

class _StatusBadge extends StatelessWidget {
  const _StatusBadge({required this.label, required this.color});

  final String label;
  final Color color;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
      decoration: BoxDecoration(
        color: color.withValues(alpha: 0.12),
        borderRadius: BorderRadius.circular(999),
      ),
      child: Text(
        label,
        style: theme.textTheme.labelSmall?.copyWith(
          color: color,
          fontWeight: FontWeight.w800,
        ),
      ),
    );
  }
}

class _MetaRow extends StatelessWidget {
  const _MetaRow({required this.label, required this.value});

  final String label;
  final String value;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: <Widget>[
        SizedBox(
          width: 92,
          child: Text(
            label,
            style: theme.textTheme.labelLarge?.copyWith(
              color: theme.colorScheme.onSurfaceVariant,
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
    );
  }
}
