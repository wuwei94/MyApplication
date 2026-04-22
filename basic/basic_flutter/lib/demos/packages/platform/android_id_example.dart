import 'dart:async';

import 'package:android_id/android_id.dart';
import 'package:basic_flutter/core/utils/logger/logger.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

/// Android ID
/// https://pub.dev/packages/android_id
class AndroidIdDemoPage extends StatelessWidget {
  const AndroidIdDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return AndroidIdDemoView(title: title);
  }
}

class AndroidIdDemoView extends StatefulWidget {
  const AndroidIdDemoView({super.key, required this.title});

  final String title;

  @override
  State<AndroidIdDemoView> createState() => _AndroidIdDemoViewState();
}

class _AndroidIdDemoViewState extends State<AndroidIdDemoView> {
  static const AndroidId _androidIdPlugin = AndroidId();

  String? _androidId;
  String? _statusMessage;
  bool _isRefreshing = false;
  bool _isAndroidSupported = false;
  bool _isPluginAvailable = true;

  @override
  void initState() {
    super.initState();
    unawaited(_refreshAndroidId(source: '页面初始化'));
  }

  Future<void> _refreshAndroidId({required String source}) async {
    if (_isRefreshing) {
      return;
    }

    if (kIsWeb || defaultTargetPlatform != TargetPlatform.android) {
      setState(() {
        _androidId = null;
        _isRefreshing = false;
        _isAndroidSupported = false;
        _isPluginAvailable = true;
        _statusMessage = '当前平台不是 Android，`android_id` 只在 Android 上返回值。';
      });
      return;
    }

    setState(() {
      _isRefreshing = true;
      _isAndroidSupported = true;
      _isPluginAvailable = true;
      _statusMessage = null;
    });

    try {
      final String? androidId = await _androidIdPlugin.getId();
      logInfo('Android ID refreshed [$source]: ${androidId ?? 'null'}');

      if (!mounted) {
        return;
      }

      setState(() {
        _androidId = androidId;
        _isRefreshing = false;
        _statusMessage = androidId == null
            ? '插件调用成功，但当前设备没有返回 Android ID。'
            : '已成功读取 Android ID，可用于演示平台标识符读取能力。';
      });
    } on MissingPluginException catch (error, stackTrace) {
      logError('android_id plugin is not registered.', error, stackTrace);

      if (!mounted) {
        return;
      }

      setState(() {
        _androidId = null;
        _isRefreshing = false;
        _isPluginAvailable = false;
        _statusMessage =
            '当前运行环境未注册 android_id 插件。通常需要冷启动 App 后再测试，'
            'widget test 环境下出现这个提示也属于正常现象。';
      });
    } on PlatformException catch (error, stackTrace) {
      logError('Failed to load Android ID.', error, stackTrace);

      if (!mounted) {
        return;
      }

      setState(() {
        _androidId = null;
        _isRefreshing = false;
        _statusMessage = '读取 Android ID 失败：${error.message ?? error.code}';
      });
    } catch (error, stackTrace) {
      logError('Failed to load Android ID.', error, stackTrace);

      if (!mounted) {
        return;
      }

      setState(() {
        _androidId = null;
        _isRefreshing = false;
        _statusMessage = '读取 Android ID 失败：$error';
      });
    }
  }

  Future<void> _copyAndroidId() async {
    final String? androidId = _androidId;
    if (androidId == null || androidId.isEmpty) {
      return;
    }

    await Clipboard.setData(ClipboardData(text: androidId));
    if (!mounted) {
      return;
    }

    ScaffoldMessenger.of(
      context,
    ).showSnackBar(const SnackBar(content: Text('Android ID 已复制到剪贴板')));
  }

  Color _accentColor(ThemeData theme) {
    if (!_isAndroidSupported) {
      return const Color(0xFFE08A00);
    }
    if (!_isPluginAvailable) {
      return theme.colorScheme.error;
    }
    if (_androidId != null && _androidId!.isNotEmpty) {
      return const Color(0xFF1C8A63);
    }
    return const Color(0xFF0F5DAA);
  }

  String _statusTitle() {
    if (!_isAndroidSupported) {
      return '当前平台不支持';
    }
    if (!_isPluginAvailable) {
      return '插件尚未注册';
    }
    if (_androidId != null && _androidId!.isNotEmpty) {
      return '已成功读取 Android ID';
    }
    return '等待读取 Android ID';
  }

  String _valueText() {
    final String? androidId = _androidId;
    if (androidId == null || androidId.isEmpty) {
      return 'Unavailable';
    }
    return androidId;
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

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
        actions: <Widget>[
          IconButton(
            onPressed: _isRefreshing
                ? null
                : () => _refreshAndroidId(source: '手动刷新'),
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

    return ListView(
      padding: const EdgeInsets.all(16),
      children: <Widget>[
        const _SectionCard(
          title: 'android_id 示例',
          subtitle: 'Android 设备标识符读取',
          accentColor: Color(0xFF0F5DAA),
          child: Wrap(
            spacing: 8,
            runSpacing: 8,
            children: <Widget>[
              _FeatureChip(label: 'AndroidId().getId()'),
              _FeatureChip(label: 'Platform guard'),
              _FeatureChip(label: 'MissingPluginException'),
            ],
          ),
        ),
        const SizedBox(height: 16),
        _SectionCard(
          title: _statusTitle(),
          subtitle: _statusMessage ?? '正在准备读取 Android ID。',
          accentColor: accentColor,
          child: Row(
            children: <Widget>[
              Icon(Icons.fingerprint_rounded, color: accentColor, size: 28),
              const SizedBox(width: 12),
              Expanded(
                child: Text(
                  '当前平台：${_platformLabel()}',
                  style: theme.textTheme.titleMedium?.copyWith(
                    fontWeight: FontWeight.w700,
                  ),
                ),
              ),
              _StatusBadge(
                label: _isRefreshing ? 'LOADING' : 'READY',
                color: accentColor,
              ),
            ],
          ),
        ),
        const SizedBox(height: 16),
        _SectionCard(
          title: 'Android ID',
          subtitle:
              '官方说明中，这个值在 Android 8+ 上会与签名 key、用户、设备组合相关，不再是简单的“全局设备唯一值”。',
          accentColor: accentColor,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: <Widget>[
              Container(
                width: double.infinity,
                decoration: BoxDecoration(
                  color: const Color(0xFFF8FAFC),
                  borderRadius: BorderRadius.circular(18),
                  border: Border.all(color: const Color(0xFFE4EBF3)),
                ),
                padding: const EdgeInsets.all(16),
                child: SelectionArea(
                  child: SelectableText(
                    _valueText(),
                    style: theme.textTheme.titleMedium?.copyWith(
                      fontWeight: FontWeight.w700,
                      letterSpacing: 0.3,
                    ),
                  ),
                ),
              ),
              const SizedBox(height: 12),
              Wrap(
                spacing: 12,
                runSpacing: 12,
                children: <Widget>[
                  FilledButton.icon(
                    onPressed: _androidId != null && _androidId!.isNotEmpty
                        ? _copyAndroidId
                        : null,
                    icon: const Icon(Icons.copy_rounded),
                    label: const Text('复制'),
                  ),
                  OutlinedButton.icon(
                    onPressed: _isRefreshing
                        ? null
                        : () => _refreshAndroidId(source: '手动刷新'),
                    icon: const Icon(Icons.refresh_rounded),
                    label: const Text('重新读取'),
                  ),
                ],
              ),
            ],
          ),
        ),
        const SizedBox(height: 16),
        const _SectionCard(
          title: '使用提示',
          subtitle:
              '这个示例更适合演示平台能力接入，不建议把 Android ID 当作长期稳定、跨应用不变的全局唯一标识。接入线上业务前，记得同时检查隐私合规和 Google Play 政策。',
          accentColor: Color(0xFFE08A00),
          child: SizedBox.shrink(),
        ),
      ],
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
