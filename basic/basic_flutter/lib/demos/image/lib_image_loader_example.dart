import 'package:flutter/material.dart';
import 'package:lib_image_loader/image_loader.dart';

/// lib_image_loader
/// 本地 package：../basic_flutter_libs/lib_image_loader
/// 演示 IImageLoader 统一接口与 ImageLoader 门面的常规网络图加载与缓存清理。
class LibImageLoaderDemoPage extends StatelessWidget {
  const LibImageLoaderDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return LibImageLoaderDemoView(title: title);
  }
}

class LibImageLoaderDemoView extends StatefulWidget {
  const LibImageLoaderDemoView({super.key, required this.title});

  final String title;

  @override
  State<LibImageLoaderDemoView> createState() => _LibImageLoaderDemoViewState();
}

class _LibImageLoaderDemoViewState extends State<LibImageLoaderDemoView> {
  static const String _imageUrl =
      'https://picsum.photos/seed/lib-image-loader/900/520';
  static const String _avatarUrl =
      'https://picsum.photos/seed/lib-image-loader-avatar/240/240';

  Future<void> _clearCache() async {
    await ImageLoader.clear(_imageUrl);
    await ImageLoader.clear(_avatarUrl);

    if (!mounted) return;
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text('缓存已清理，重新进入页面会重新下载')),
    );
  }

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return Scaffold(
      appBar: AppBar(title: Text(widget.title)),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: <Widget>[
          const _KernelCard(),
          const SizedBox(height: 16),
          _PreviewCard(
            title: 'ImageLoader.load（基础图）',
            subtitle: '默认包含缓存、占位图与错误态，内核可整体替换。',
            child: ImageLoader.load(
              url: _imageUrl,
              width: double.infinity,
              height: 220,
            ),
          ),
          const SizedBox(height: 16),
          _PreviewCard(
            title: 'ImageLoader.radius（圆角图）',
            subtitle: '圆角场景沿用统一调用方式，适合 Banner、卡片头图。',
            child: ImageLoader.radius(
              url: _imageUrl,
              width: double.infinity,
              height: 220,
              borderRadius: 24,
            ),
          ),
          const SizedBox(height: 16),
          _PreviewCard(
            title: 'ImageLoader.round（圆形图）',
            subtitle: '固定尺寸小图可直接用于头像、群组缩略图。',
            child: Center(child: ImageLoader.round(url: _avatarUrl, size: 120)),
          ),
          const SizedBox(height: 16),
          _PreviewCard(
            title: 'ImageLoader.provider（ImageProvider）',
            subtitle: '返回 ImageProvider，可直接用于 CircleAvatar、FadeInImage、Hero 等组件。',
            child: Center(
              child: CircleAvatar(
                radius: 48,
                backgroundImage: ImageLoader.provider(_avatarUrl),
              ),
            ),
          ),
          const SizedBox(height: 16),
          Card(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: <Widget>[
                  Text(
                    'ImageLoader.clear（缓存清理）',
                    style: theme.textTheme.titleMedium?.copyWith(
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                  const SizedBox(height: 12),
                  OutlinedButton.icon(
                    onPressed: _clearCache,
                    icon: const Icon(Icons.delete_outline_rounded),
                    label: const Text('清除本页图片缓存'),
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _KernelCard extends StatelessWidget {
  const _KernelCard();

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Row(
          children: <Widget>[
            const Icon(Icons.image_outlined),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: <Widget>[
                  const Text('当前内核：CachedNetworkImageLoader'),
                  const SizedBox(height: 4),
                  Text(
                    'ImageLoader 门面默认基于 cached_network_image，'
                    '负责常规网络图加载、缓存、占位图与错误态；'
                    '大图查看场景不属于本包范围。',
                    style: Theme.of(context).textTheme.bodySmall,
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _PreviewCard extends StatelessWidget {
  const _PreviewCard({
    required this.title,
    required this.subtitle,
    required this.child,
  });

  final String title;
  final String subtitle;
  final Widget child;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return Card(
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
            Text(subtitle, style: theme.textTheme.bodySmall),
            const SizedBox(height: 12),
            ClipRRect(
              borderRadius: BorderRadius.circular(16),
              child: child,
            ),
          ],
        ),
      ),
    );
  }
}
