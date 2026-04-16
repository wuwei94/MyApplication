import 'package:basic_flutter/core/utils/image/extended_image_loader.dart';
import 'package:flutter/material.dart';

/// extended_image
/// https://pub.dev/packages/extended_image
/// 在常规网络图加载能力之外，还额外提供更丰富的图片交互能力。
class ExtendedImageDemoPage extends StatelessWidget {
  const ExtendedImageDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return ExtendedImageDemoView(title: title);
  }
}

class ExtendedImageDemoView extends StatefulWidget {
  const ExtendedImageDemoView({super.key, required this.title});

  final String title;

  @override
  State<ExtendedImageDemoView> createState() => _ExtendedImageDemoViewState();
}

class _ExtendedImageDemoViewState extends State<ExtendedImageDemoView> {
  static const Color _accentColor = Color(0xFF0F766E);
  static const List<String> _capabilities = <String>[
    '网络缓存',
    '占位图',
    '错误态',
    '手势缩放',
  ];
  static const String _basicImageUrl =
      'https://picsum.photos/seed/extended-basic/900/520';
  static const String _roundedImageUrl =
      'https://picsum.photos/seed/extended-rounded/900/520';
  static const String _avatarImageUrl =
      'https://picsum.photos/seed/extended-avatar/240/240';
  static const String _gestureImageUrl =
      'https://picsum.photos/seed/extended-gesture/1400/960';

  bool _isClearing = false;
  String _statusMessage =
      '当前 ExtendedImageLoader 基于 extended_image，在统一加载 API 之外还提供了手势预览能力。';

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
          packageName: 'extended_image',
          description: '除了网络图缓存和错误态处理，这套封装还可以自然扩展到大图预览、缩放和平移等交互场景。',
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
        _buildCapabilitySection(),
        const SizedBox(height: 16),
        _buildCacheSection(),
      ],
    );
  }

  Widget _buildBasicSection() {
    return _ImageDemoSectionCard(
      title: '基础用法',
      subtitle: '基础加载方式适合常规封面图、列表图和详情头图，默认已经包含缓存、占位图和错误态。',
      child: _ImageDemoPreviewFrame(
        child: ExtendedImageLoader.load(
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
        child: ExtendedImageLoader.radius(
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
        child: ExtendedImageLoader.round(url: _avatarImageUrl, size: 120),
      ),
    );
  }

  Widget _buildCapabilitySection() {
    return _ImageDemoSectionCard(
      title: '扩展能力',
      subtitle: '在与 cached 版一致的公共 API 之外，这套封装还额外提供手势缩放和平移能力。',
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          const _ImageDemoHintText(text: '下面这张图支持双击放大、双指缩放和拖拽平移，更适合大图预览场景。'),
          const SizedBox(height: 12),
          _ImageDemoPreviewFrame(
            backgroundColor: const Color(0xFF101828),
            child: SizedBox(
              width: double.infinity,
              height: 260,
              child: ExtendedImageLoader.gesture(url: _gestureImageUrl),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildCacheSection() {
    return _ImageDemoSectionCard(
      title: '缓存控制',
      subtitle: '清理当前示例相关缓存后，重新进入页面会再次请求网络资源，方便验证缓存是否生效。',
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
      _statusMessage = '正在清理 extended_image 缓存...';
    });

    try {
      await ExtendedImageLoader.clear(_basicImageUrl);
      await ExtendedImageLoader.clear(_roundedImageUrl);
      await ExtendedImageLoader.clear(_avatarImageUrl);
      await ExtendedImageLoader.clear(_gestureImageUrl);
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
  const _ImageDemoPreviewFrame({required this.child, this.backgroundColor});

  final Widget child;
  final Color? backgroundColor;

  @override
  Widget build(BuildContext context) {
    final BorderRadius borderRadius = BorderRadius.circular(20);

    return DecoratedBox(
      decoration: BoxDecoration(
        color: backgroundColor ?? Colors.white,
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
