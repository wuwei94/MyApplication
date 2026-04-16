import 'package:basic_flutter/core/utils/network/cached_network_image_loader.dart';
import 'package:flutter/material.dart';

/// cached_network_image
/// https://pub.dev/packages/cached_network_image
/// 更适合普通网络图加载、缓存、占位图和错误图场景。
class CachedNetworkImageDemoPage extends StatelessWidget {
  const CachedNetworkImageDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return CachedNetworkImageDemoView(title: title);
  }
}

class CachedNetworkImageDemoView extends StatefulWidget {
  const CachedNetworkImageDemoView({super.key, required this.title});

  final String title;

  @override
  State<CachedNetworkImageDemoView> createState() =>
      _CachedNetworkImageDemoViewState();
}

class _CachedNetworkImageDemoViewState
    extends State<CachedNetworkImageDemoView> {
  static const Color _accentColor = Color(0xFF2563EB);
  static const List<String> _capabilities = <String>[
    '网络缓存',
    '占位图',
    '错误态',
  ];
  static const String _basicImageUrl =
      'https://picsum.photos/seed/cached-basic/900/520';
  static const String _roundedImageUrl =
      'https://picsum.photos/seed/cached-rounded/900/520';
  static const String _avatarImageUrl =
      'https://picsum.photos/seed/cached-avatar/240/240';

  bool _isClearing = false;
  String _statusMessage =
      '当前 CachedNetworkImageLoader 基于 cached_network_image，适合普通封面图、列表图和头像加载场景。';

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(widget.title)),
      body: getBody(),
    );
  }

  Widget getBody() {
    return ListView(
      padding: const EdgeInsets.all(16),
      children: <Widget>[
        _ImageDemoHeroCard(
          packageName: 'cached_network_image',
          description: '更偏向轻量、稳定的网络图加载封装，常见场景是列表封面、卡片缩略图和用户头像。',
          statusMessage: _statusMessage,
          capabilities: _capabilities,
          accentColor: _accentColor,
        ),
        const SizedBox(height: 16),
        _buildBasicSection(),
        const SizedBox(height: 16),
        _buildRoundedSection(),
        const SizedBox(height: 16),
        _buildAvatarSection(),
        const SizedBox(height: 16),
        _buildCacheControlSection(),
      ],
    );
  }

  Widget _buildBasicSection() {
    return _ImageDemoSectionCard(
      title: '基础用法',
      subtitle: '基础加载方式适合常规封面图、列表图和详情头图，默认已经包含缓存、占位图和错误态。',
      child: _ImageDemoPreviewFrame(
        child: CachedNetworkImageLoader.load(
          url: _basicImageUrl,
          width: double.infinity,
          height: 220,
        ),
      ),
    );
  }

  Widget _buildRoundedSection() {
    return _ImageDemoSectionCard(
      title: '圆角图片',
      subtitle: '圆角场景也沿用同一套调用方式，更适合 Banner、卡片头图这类常见展示场景。',
      child: _ImageDemoPreviewFrame(
        child: CachedNetworkImageLoader.radius(
          url: _roundedImageUrl,
          width: double.infinity,
          height: 220,
          borderRadius: 24,
        ),
      ),
    );
  }

  Widget _buildAvatarSection() {
    return _ImageDemoSectionCard(
      title: '圆形头像',
      subtitle: '头像、群组缩略图等固定尺寸小图可以继续复用统一的圆形方法。',
      child: Center(
        child: CachedNetworkImageLoader.round(url: _avatarImageUrl, size: 120),
      ),
    );
  }

  Widget _buildCacheControlSection() {
    return _ImageDemoSectionCard(
      title: '缓存控制',
      subtitle: '清理后再次进入页面会重新请求网络资源，方便验证缓存是否生效。',
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          _ImageDemoHintText(text: _statusMessage),
          const SizedBox(height: 12),
          ElevatedButton.icon(
            onPressed: _isClearing ? null : _clearCache,
            icon: _isClearing
                ? const SizedBox(
                    width: 18,
                    height: 18,
                    child: CircularProgressIndicator(strokeWidth: 2),
                  )
                : const Icon(Icons.delete_outline),
            label: Text(_isClearing ? '清理中...' : '清除缓存'),
          ),
        ],
      ),
    );
  }

  Future<void> _clearCache() async {
    _setStateIfMounted(() {
      _isClearing = true;
      _statusMessage = '正在清理 cached_network_image 缓存...';
    });

    try {
      await CachedNetworkImageLoader.clear(_basicImageUrl);
      await CachedNetworkImageLoader.clear(_roundedImageUrl);
      await CachedNetworkImageLoader.clear(_avatarImageUrl);
      _setStateIfMounted(() {
        _statusMessage = '缓存已清理完成，重新进入页面后会重新下载图片资源。';
      });
    } catch (error) {
      _setStateIfMounted(() {
        _statusMessage = '清理缓存失败：$error';
      });
    } finally {
      _setStateIfMounted(() {
        _isClearing = false;
      });
    }
  }

  void _setStateIfMounted(VoidCallback fn) {
    if (!mounted) {
      return;
    }

    setState(fn);
  }
}

class _ImageDemoHeroCard extends StatelessWidget {
  const _ImageDemoHeroCard({
    required this.packageName,
    required this.description,
    required this.statusMessage,
    required this.capabilities,
    required this.accentColor,
  });

  final String packageName;
  final String description;
  final String statusMessage;
  final List<String> capabilities;
  final Color accentColor;

  @override
  Widget build(BuildContext context) {
    final Color borderColor = Color.lerp(Colors.white, accentColor, 0.28)!;
    final Color topColor = Color.lerp(Colors.white, accentColor, 0.14)!;
    final Color bottomColor = Color.lerp(Colors.white, accentColor, 0.05)!;

    return DecoratedBox(
      decoration: BoxDecoration(
        gradient: LinearGradient(
          colors: <Color>[topColor, bottomColor],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: borderColor),
      ),
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            _ImageDemoPackageBadge(
              packageName: packageName,
              accentColor: accentColor,
            ),
            const SizedBox(height: 16),
            Text(
              description,
              style: const TextStyle(fontSize: 15, height: 1.6),
            ),
            const SizedBox(height: 12),
            Text(
              statusMessage,
              style: TextStyle(
                fontSize: 13,
                height: 1.5,
                color: Colors.blueGrey.shade700,
              ),
            ),
            const SizedBox(height: 16),
            Wrap(
              spacing: 8,
              runSpacing: 8,
              children: capabilities
                  .map(
                    (String capability) => _ImageDemoCapabilityChip(
                      label: capability,
                      accentColor: accentColor,
                    ),
                  )
                  .toList(),
            ),
          ],
        ),
      ),
    );
  }
}

class _ImageDemoSectionCard extends StatelessWidget {
  const _ImageDemoSectionCard({
    required this.title,
    required this.subtitle,
    required this.child,
  });

  final String title;
  final String subtitle;
  final Widget child;

  @override
  Widget build(BuildContext context) {
    return DecoratedBox(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: Colors.grey.shade300),
      ),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Text(
              title,
              style: const TextStyle(fontSize: 16, fontWeight: FontWeight.w700),
            ),
            const SizedBox(height: 6),
            Text(
              subtitle,
              style: TextStyle(
                fontSize: 13,
                height: 1.5,
                color: Colors.blueGrey.shade700,
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

class _ImageDemoPreviewFrame extends StatelessWidget {
  const _ImageDemoPreviewFrame({required this.child});

  final Widget child;

  @override
  Widget build(BuildContext context) {
    final BorderRadius borderRadius = BorderRadius.circular(20);

    return DecoratedBox(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: borderRadius,
        border: Border.all(color: Colors.grey.shade300),
      ),
      child: ClipRRect(borderRadius: borderRadius, child: child),
    );
  }
}

class _ImageDemoHintText extends StatelessWidget {
  const _ImageDemoHintText({required this.text});

  final String text;

  @override
  Widget build(BuildContext context) {
    return Text(
      text,
      style: TextStyle(
        fontSize: 13,
        height: 1.5,
        color: Colors.blueGrey.shade700,
      ),
    );
  }
}

class _ImageDemoPackageBadge extends StatelessWidget {
  const _ImageDemoPackageBadge({
    required this.packageName,
    required this.accentColor,
  });

  final String packageName;
  final Color accentColor;

  @override
  Widget build(BuildContext context) {
    final Color backgroundColor = Color.lerp(Colors.white, accentColor, 0.18)!;
    final Color borderColor = Color.lerp(Colors.white, accentColor, 0.36)!;

    return DecoratedBox(
      decoration: BoxDecoration(
        color: backgroundColor,
        borderRadius: BorderRadius.circular(999),
        border: Border.all(color: borderColor),
      ),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
        child: Text(
          packageName,
          style: const TextStyle(fontSize: 12, fontWeight: FontWeight.w700),
        ),
      ),
    );
  }
}

class _ImageDemoCapabilityChip extends StatelessWidget {
  const _ImageDemoCapabilityChip({
    required this.label,
    required this.accentColor,
  });

  final String label;
  final Color accentColor;

  @override
  Widget build(BuildContext context) {
    final Color backgroundColor = Color.lerp(Colors.white, accentColor, 0.12)!;

    return DecoratedBox(
      decoration: BoxDecoration(
        color: backgroundColor,
        borderRadius: BorderRadius.circular(999),
      ),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
        child: Text(
          label,
          style: const TextStyle(fontSize: 12, fontWeight: FontWeight.w600),
        ),
      ),
    );
  }
}
