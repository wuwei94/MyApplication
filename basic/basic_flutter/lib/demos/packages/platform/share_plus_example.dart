import 'package:basic_flutter/core/utils/logger/logger.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:share_plus/share_plus.dart';

/// Share Plus
/// https://pub.dev/packages/share_plus
class SharePlusDemoPage extends StatelessWidget {
  const SharePlusDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return SharePlusDemoView(title: title);
  }
}

class SharePlusDemoView extends StatefulWidget {
  const SharePlusDemoView({super.key, required this.title});

  final String title;

  @override
  State<SharePlusDemoView> createState() => _SharePlusDemoViewState();
}

class _SharePlusDemoViewState extends State<SharePlusDemoView> {
  static const String _sampleTitle = 'basic_flutter share_plus 示例';
  static const String _sampleText =
      '来自 basic_flutter 的 share_plus 分享内容，'
      '用于演示系统分享面板、分享结果和平台能力接入。';
  static final Uri _packageUri = Uri.parse(
    'https://pub.dev/packages/share_plus',
  );

  String _statusTitle = '尚未发起分享';
  String _statusMessage = '点击下方按钮，体验系统分享面板的文本、链接与组合分享。';
  ShareResultStatus? _lastStatus;
  bool _isSharing = false;

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
        const _SectionCard(
          title: 'share_plus 示例',
          subtitle: '这个页面演示 SharePlus.instance.share() 与 ShareParams 的常见用法。',
          accentColor: Color(0xFF0F5DAA),
          child: Wrap(
            spacing: 8,
            runSpacing: 8,
            children: <Widget>[
              _FeatureChip(label: 'text'),
              _FeatureChip(label: 'uri'),
              _FeatureChip(label: 'result status'),
            ],
          ),
        ),
        const SizedBox(height: 12),
        _StatusCard(
          title: _statusTitle,
          message: _statusMessage,
          status: _lastStatus,
        ),
        const SizedBox(height: 16),
        _ShareActionCard(
          icon: Icons.notes_rounded,
          title: '分享文本',
          subtitle: '只传入 text，适合普通文案或复制到其他 App。',
          buttonLabel: '分享文本',
          isBusy: _isSharing,
          onPressed: () => _shareText(context),
        ),
        const SizedBox(height: 12),
        _ShareActionCard(
          icon: Icons.link_rounded,
          title: '分享链接',
          subtitle: '传入 uri，系统会按平台能力展示可接收链接的 App。',
          buttonLabel: '分享链接',
          isBusy: _isSharing,
          onPressed: () => _shareUri(context),
        ),
        const SizedBox(height: 12),
        _ShareActionCard(
          icon: Icons.ios_share_rounded,
          title: '分享文本和链接',
          subtitle: '同时传入 title、subject、text 与 uri。',
          buttonLabel: '组合分享',
          isBusy: _isSharing,
          onPressed: () => _shareTextAndUri(context),
        ),
        const SizedBox(height: 12),
        _SectionCard(
          title: '平台提示',
          subtitle:
              'iPad 与桌面平台需要提供分享弹层的锚点位置；'
              '示例会从当前页面 context 自动计算 sharePositionOrigin。',
          accentColor: const Color(0xFF1C8A63),
          child: Text(
            '如果在新增插件后遇到 MissingPluginException，通常需要完全停止并冷启动 App。'
            'widget test 环境中未注册原生插件也可能看到该提示。',
            style: theme.textTheme.bodySmall?.copyWith(
              color: theme.colorScheme.onSurfaceVariant,
            ),
          ),
        ),
      ],
    );
  }

  Future<void> _shareText(BuildContext context) async {
    await _share(
      context: context,
      label: '分享文本',
      params: ShareParams(
        title: _sampleTitle,
        subject: _sampleTitle,
        text: _sampleText,
        sharePositionOrigin: _sharePositionOrigin(context),
      ),
    );
  }

  Future<void> _shareUri(BuildContext context) async {
    await _share(
      context: context,
      label: '分享链接',
      params: ShareParams(
        title: _sampleTitle,
        uri: _packageUri,
        sharePositionOrigin: _sharePositionOrigin(context),
      ),
    );
  }

  Future<void> _shareTextAndUri(BuildContext context) async {
    await _share(
      context: context,
      label: '分享文本和链接',
      params: ShareParams(
        title: _sampleTitle,
        subject: 'share_plus package',
        text: _sampleText,
        uri: _packageUri,
        sharePositionOrigin: _sharePositionOrigin(context),
      ),
    );
  }

  Future<void> _share({
    required BuildContext context,
    required String label,
    required ShareParams params,
  }) async {
    if (_isSharing) {
      return;
    }

    setState(() {
      _isSharing = true;
      _statusTitle = '$label 已发起';
      _statusMessage = '系统分享面板打开中，等待用户选择目标应用。';
      _lastStatus = null;
    });

    try {
      final ShareResult result = await SharePlus.instance.share(params);
      logInfo(
        'Share result [$label]: ${result.status.name}, raw: ${result.raw}',
      );

      if (!mounted) {
        return;
      }

      setState(() {
        _isSharing = false;
        _lastStatus = result.status;
        _statusTitle = _statusTitleOf(result.status);
        _statusMessage = _statusMessageOf(result);
      });
    } on MissingPluginException catch (error, stackTrace) {
      logError('share_plus plugin is not registered.', error, stackTrace);

      if (!mounted) {
        return;
      }

      setState(() {
        _isSharing = false;
        _lastStatus = null;
        _statusTitle = '插件尚未注册';
        _statusMessage = '当前运行环境未注册 share_plus 插件。添加插件后通常需要冷启动 App。';
      });
    } on PlatformException catch (error, stackTrace) {
      logError('Failed to share content.', error, stackTrace);

      if (!mounted) {
        return;
      }

      setState(() {
        _isSharing = false;
        _lastStatus = null;
        _statusTitle = '分享失败';
        _statusMessage = '平台分享能力返回错误：${error.message ?? error.code}';
      });
    } catch (error, stackTrace) {
      logError('Failed to share content.', error, stackTrace);

      if (!mounted) {
        return;
      }

      setState(() {
        _isSharing = false;
        _lastStatus = null;
        _statusTitle = '分享失败';
        _statusMessage = '分享调用失败：$error';
      });
    }
  }

  Rect? _sharePositionOrigin(BuildContext context) {
    final RenderObject? renderObject = context.findRenderObject();
    if (renderObject is! RenderBox || !renderObject.hasSize) {
      return null;
    }

    return renderObject.localToGlobal(Offset.zero) & renderObject.size;
  }

  String _statusTitleOf(ShareResultStatus status) {
    return switch (status) {
      ShareResultStatus.success => '分享已完成',
      ShareResultStatus.dismissed => '分享面板已关闭',
      ShareResultStatus.unavailable => '当前平台不可用',
    };
  }

  String _statusMessageOf(ShareResult result) {
    final String target = result.raw.isEmpty ? '未返回目标应用' : result.raw;

    return switch (result.status) {
      ShareResultStatus.success => '用户完成了分享，平台返回：$target。',
      ShareResultStatus.dismissed => '用户关闭了分享面板，没有选择目标应用。',
      ShareResultStatus.unavailable => '当前平台没有可用的系统分享能力或目标应用。',
    };
  }
}

class _ShareActionCard extends StatelessWidget {
  const _ShareActionCard({
    required this.icon,
    required this.title,
    required this.subtitle,
    required this.buttonLabel,
    required this.isBusy,
    required this.onPressed,
  });

  final IconData icon;
  final String title;
  final String subtitle;
  final String buttonLabel;
  final bool isBusy;
  final VoidCallback onPressed;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return Card(
      elevation: 0,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(8),
        side: BorderSide(color: theme.colorScheme.outlineVariant),
      ),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Row(
              children: <Widget>[
                Icon(icon, color: theme.colorScheme.primary),
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
                      Text(
                        subtitle,
                        style: theme.textTheme.bodyMedium?.copyWith(
                          color: theme.colorScheme.onSurfaceVariant,
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
            const SizedBox(height: 12),
            Align(
              alignment: Alignment.centerRight,
              child: FilledButton.icon(
                onPressed: isBusy ? null : onPressed,
                icon: const Icon(Icons.ios_share_rounded),
                label: Text(buttonLabel),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _StatusCard extends StatelessWidget {
  const _StatusCard({
    required this.title,
    required this.message,
    required this.status,
  });

  final String title;
  final String message;
  final ShareResultStatus? status;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);
    final Color accentColor = _accentColor(theme);

    return _SectionCard(
      title: title,
      subtitle: message,
      accentColor: accentColor,
      child: Row(
        children: <Widget>[
          Icon(_statusIcon(), color: accentColor),
          const SizedBox(width: 12),
          _StatusBadge(label: _statusLabel(), color: accentColor),
        ],
      ),
    );
  }

  Color _accentColor(ThemeData theme) {
    return switch (status) {
      ShareResultStatus.success => const Color(0xFF1C8A63),
      ShareResultStatus.dismissed => const Color(0xFFE08A00),
      ShareResultStatus.unavailable => theme.colorScheme.error,
      null => const Color(0xFF0F5DAA),
    };
  }

  IconData _statusIcon() {
    return switch (status) {
      ShareResultStatus.success => Icons.check_circle_outline_rounded,
      ShareResultStatus.dismissed => Icons.remove_circle_outline_rounded,
      ShareResultStatus.unavailable => Icons.error_outline_rounded,
      null => Icons.pending_actions_rounded,
    };
  }

  String _statusLabel() {
    return switch (status) {
      ShareResultStatus.success => 'SUCCESS',
      ShareResultStatus.dismissed => 'DISMISSED',
      ShareResultStatus.unavailable => 'UNAVAILABLE',
      null => 'IDLE',
    };
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

    return Card(
      elevation: 0,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(8),
        side: BorderSide(color: theme.colorScheme.outlineVariant),
      ),
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
                  height: 44,
                  decoration: BoxDecoration(
                    color: accentColor,
                    borderRadius: BorderRadius.circular(999),
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
                      const SizedBox(height: 6),
                      Text(
                        subtitle,
                        style: theme.textTheme.bodyMedium?.copyWith(
                          color: theme.colorScheme.onSurfaceVariant,
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
            const SizedBox(height: 12),
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
    final ThemeData theme = Theme.of(context);

    return Chip(
      label: Text(label),
      avatar: Icon(
        Icons.check_rounded,
        size: 16,
        color: theme.colorScheme.primary,
      ),
      side: BorderSide(color: theme.colorScheme.outlineVariant),
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
        style: theme.textTheme.labelMedium?.copyWith(
          color: color,
          fontWeight: FontWeight.w700,
        ),
      ),
    );
  }
}
