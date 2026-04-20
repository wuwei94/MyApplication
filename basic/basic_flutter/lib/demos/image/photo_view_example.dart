import 'package:flutter/material.dart';
import 'package:photo_view/photo_view.dart';
import 'package:photo_view/photo_view_gallery.dart';

/// photo_view
/// https://pub.dev/packages/photo_view
/// 适合大图查看、双击放大、拖拽平移和图库浏览。
class PhotoViewDemoPage extends StatelessWidget {
  const PhotoViewDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return PhotoViewDemoView(title: title);
  }
}

class PhotoViewDemoView extends StatefulWidget {
  const PhotoViewDemoView({super.key, required this.title});

  final String title;

  @override
  State<PhotoViewDemoView> createState() => _PhotoViewDemoViewState();
}

class _PhotoViewDemoViewState extends State<PhotoViewDemoView> {
  static const Color _accentColor = Color(0xFF0284C7);
  static const List<String> _capabilities = <String>[
    '双击放大',
    '拖拽平移',
    '捏合缩放',
    '图库浏览',
  ];
  static const List<String> _scenarios = <String>[
    '商品详情图',
    '长图/海报预览',
    '相册大图浏览',
    '头像或封面查看',
  ];
  static const List<_PhotoSample> _samples = <_PhotoSample>[
    _PhotoSample(
      title: 'Winter Portrait',
      subtitle: '适合查看人物细节，体验双击放大与拖拽平移。',
      assetPath: 'assets/images/pic0.jpg',
      heroTag: 'photo-view-sample-0',
    ),
    _PhotoSample(
      title: 'Shadow On Rock',
      subtitle: '适合观察光影和构图，也适合做图库左右切换示例。',
      assetPath: 'assets/images/pic1.jpg',
      heroTag: 'photo-view-sample-1',
    ),
    _PhotoSample(
      title: 'Surfing Snapshot',
      subtitle: '画面主体较小，更容易感受到 PhotoView 的放大收益。',
      assetPath: 'assets/images/pic2.jpg',
      heroTag: 'photo-view-sample-2',
    ),
    _PhotoSample(
      title: 'Long Shadow Street',
      subtitle: '拖拽查看画面边缘时，能更直观体会到平移交互。',
      assetPath: 'assets/images/pic3.jpg',
      heroTag: 'photo-view-sample-3',
    ),
  ];

  int _selectedIndex = 0;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(widget.title)),
      body: getBody(),
    );
  }

  Widget getBody() {
    final _PhotoSample selectedSample = _samples[_selectedIndex];

    return ListView(
      padding: const EdgeInsets.all(16),
      children: <Widget>[
        const _PhotoViewHeroCard(
          accentColor: _accentColor,
          capabilities: _capabilities,
          description: 'photo_view 更偏向图片预览交互层，适合在详情页、相册页和全屏浏览器里提供缩放、平移与手势体验。',
          packageName: 'photo_view',
          statusMessage:
              '当前示例使用本地 assets 图片，实际项目里把 AssetImage 替换成 NetworkImage 也可以直接工作。',
        ),
        const SizedBox(height: 16),
        _buildInlinePreviewSection(selectedSample),
        const SizedBox(height: 16),
        _buildGallerySection(),
        const SizedBox(height: 16),
        _buildScenarioSection(),
      ],
    );
  }

  Widget _buildInlinePreviewSection(_PhotoSample sample) {
    return _PhotoViewSectionCard(
      subtitle: '下面这张图直接放在普通页面里，支持双击放大、双指缩放和拖拽查看局部细节。',
      title: '页面内联预览',
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          SizedBox(
            height: 320,
            child: ClipRRect(
              borderRadius: BorderRadius.circular(24),
              child: DecoratedBox(
                decoration: const BoxDecoration(color: Color(0xFF020617)),
                child: PhotoView(
                  backgroundDecoration: const BoxDecoration(
                    color: Color(0xFF020617),
                  ),
                  imageProvider: AssetImage(sample.assetPath),
                  initialScale: PhotoViewComputedScale.contained,
                  maxScale: PhotoViewComputedScale.covered * 3,
                  minScale: PhotoViewComputedScale.contained * 0.95,
                ),
              ),
            ),
          ),
          const SizedBox(height: 12),
          Text(
            sample.title,
            style: const TextStyle(fontSize: 16, fontWeight: FontWeight.w600),
          ),
          const SizedBox(height: 4),
          Text(
            sample.subtitle,
            style: TextStyle(
              color: Colors.blueGrey.shade700,
              fontSize: 13,
              height: 1.5,
            ),
          ),
          const SizedBox(height: 12),
          Wrap(
            spacing: 10,
            runSpacing: 10,
            children: <Widget>[
              ElevatedButton.icon(
                onPressed: () => _openGallery(initialIndex: _selectedIndex),
                icon: const Icon(Icons.open_in_full),
                label: const Text('全屏查看'),
              ),
              OutlinedButton.icon(
                onPressed: _selectNextSample,
                icon: const Icon(Icons.photo_library_outlined),
                label: const Text('下一张'),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildGallerySection() {
    return _PhotoViewSectionCard(
      subtitle: '缩略图先切换当前图片，再进入全屏图库；全屏状态下可左右滑动切换，并保留 Hero 动画过渡。',
      title: 'Gallery 浏览',
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          SizedBox(
            height: 108,
            child: ListView.separated(
              scrollDirection: Axis.horizontal,
              itemBuilder: (BuildContext context, int index) {
                final _PhotoSample sample = _samples[index];
                final bool isSelected = index == _selectedIndex;

                return _PhotoViewThumbnail(
                  isSelected: isSelected,
                  sample: sample,
                  onOpen: () => _openGallery(initialIndex: index),
                  onTap: () => _updateSelectedIndex(index),
                );
              },
              separatorBuilder: (BuildContext context, int index) {
                return const SizedBox(width: 12);
              },
              itemCount: _samples.length,
            ),
          ),
          const SizedBox(height: 12),
          Text(
            '点击缩略图切换当前预览，点击右上角放大按钮进入全屏图库。',
            style: TextStyle(
              color: Colors.blueGrey.shade700,
              fontSize: 13,
              height: 1.5,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildScenarioSection() {
    return _PhotoViewSectionCard(
      subtitle:
          '如果你的页面已经有图片列表或详情头图，一般只需要把图片组件替换成 PhotoView / PhotoViewGallery 即可。',
      title: '适用场景',
      child: Wrap(
        spacing: 10,
        runSpacing: 10,
        children: _scenarios
            .map(
              (String label) =>
                  _PhotoViewTagChip(accentColor: _accentColor, label: label),
            )
            .toList(),
      ),
    );
  }

  void _openGallery({required int initialIndex}) {
    Navigator.of(context).push(
      MaterialPageRoute<void>(
        builder: (BuildContext context) => _PhotoViewGalleryPage(
          initialIndex: initialIndex,
          samples: _samples,
        ),
      ),
    );
  }

  void _selectNextSample() {
    final int nextIndex = (_selectedIndex + 1) % _samples.length;
    _updateSelectedIndex(nextIndex);
  }

  void _updateSelectedIndex(int index) {
    setState(() {
      _selectedIndex = index;
    });
  }
}

class _PhotoViewHeroCard extends StatelessWidget {
  const _PhotoViewHeroCard({
    required this.accentColor,
    required this.capabilities,
    required this.description,
    required this.packageName,
    required this.statusMessage,
  });

  final Color accentColor;
  final List<String> capabilities;
  final String description;
  final String packageName;
  final String statusMessage;

  @override
  Widget build(BuildContext context) {
    final Color borderColor = Color.lerp(Colors.white, accentColor, 0.28)!;
    final Color topColor = Color.lerp(Colors.white, accentColor, 0.16)!;
    final Color bottomColor = Color.lerp(Colors.white, accentColor, 0.05)!;

    return DecoratedBox(
      decoration: BoxDecoration(
        gradient: LinearGradient(
          begin: Alignment.topLeft,
          colors: <Color>[topColor, bottomColor],
          end: Alignment.bottomRight,
        ),
        border: Border.all(color: borderColor),
        borderRadius: BorderRadius.circular(24),
      ),
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            _PhotoViewPackageBadge(
              accentColor: accentColor,
              packageName: packageName,
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
                color: Colors.blueGrey.shade700,
                fontSize: 13,
                height: 1.5,
              ),
            ),
            const SizedBox(height: 16),
            Wrap(
              spacing: 8,
              runSpacing: 8,
              children: capabilities
                  .map(
                    (String label) => _PhotoViewTagChip(
                      accentColor: accentColor,
                      label: label,
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

class _PhotoViewPackageBadge extends StatelessWidget {
  const _PhotoViewPackageBadge({
    required this.accentColor,
    required this.packageName,
  });

  final Color accentColor;
  final String packageName;

  @override
  Widget build(BuildContext context) {
    return DecoratedBox(
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(999),
        color: Color.lerp(Colors.white, accentColor, 0.18),
      ),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          children: <Widget>[
            Icon(Icons.zoom_in, color: accentColor, size: 18),
            const SizedBox(width: 8),
            Text(
              packageName,
              style: TextStyle(
                color: accentColor,
                fontWeight: FontWeight.w700,
                letterSpacing: 0.2,
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _PhotoViewSectionCard extends StatelessWidget {
  const _PhotoViewSectionCard({
    required this.subtitle,
    required this.title,
    required this.child,
  });

  final String subtitle;
  final String title;
  final Widget child;

  @override
  Widget build(BuildContext context) {
    return DecoratedBox(
      decoration: BoxDecoration(
        border: Border.all(color: const Color(0xFFE2E8F0)),
        borderRadius: BorderRadius.circular(24),
        color: Colors.white,
      ),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Text(
              title,
              style: const TextStyle(fontSize: 18, fontWeight: FontWeight.w700),
            ),
            const SizedBox(height: 6),
            Text(
              subtitle,
              style: TextStyle(
                color: Colors.blueGrey.shade700,
                fontSize: 13,
                height: 1.5,
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

class _PhotoViewTagChip extends StatelessWidget {
  const _PhotoViewTagChip({required this.accentColor, required this.label});

  final Color accentColor;
  final String label;

  @override
  Widget build(BuildContext context) {
    return DecoratedBox(
      decoration: BoxDecoration(
        border: Border.all(color: Color.lerp(Colors.white, accentColor, 0.32)!),
        borderRadius: BorderRadius.circular(999),
        color: Color.lerp(Colors.white, accentColor, 0.10),
      ),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
        child: Text(
          label,
          style: TextStyle(
            color: accentColor,
            fontSize: 13,
            fontWeight: FontWeight.w600,
          ),
        ),
      ),
    );
  }
}

class _PhotoViewThumbnail extends StatelessWidget {
  const _PhotoViewThumbnail({
    required this.isSelected,
    required this.sample,
    required this.onOpen,
    required this.onTap,
  });

  final bool isSelected;
  final VoidCallback onOpen;
  final VoidCallback onTap;
  final _PhotoSample sample;

  @override
  Widget build(BuildContext context) {
    final Color borderColor = isSelected
        ? const Color(0xFF0284C7)
        : const Color(0xFFE2E8F0);

    return SizedBox(
      width: 132,
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(20),
        child: DecoratedBox(
          decoration: BoxDecoration(
            border: Border.all(color: borderColor, width: isSelected ? 2 : 1),
            borderRadius: BorderRadius.circular(20),
            color: Colors.white,
          ),
          child: Padding(
            padding: const EdgeInsets.all(8),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: <Widget>[
                Expanded(
                  child: Stack(
                    children: <Widget>[
                      Positioned.fill(
                        child: ClipRRect(
                          borderRadius: BorderRadius.circular(14),
                          child: Hero(
                            tag: sample.heroTag,
                            child: Image.asset(
                              sample.assetPath,
                              fit: BoxFit.cover,
                            ),
                          ),
                        ),
                      ),
                      Positioned(
                        right: 6,
                        top: 6,
                        child: Material(
                          borderRadius: BorderRadius.circular(999),
                          color: Colors.black.withValues(alpha: 0.58),
                          child: InkWell(
                            onTap: onOpen,
                            borderRadius: BorderRadius.circular(999),
                            child: const Padding(
                              padding: EdgeInsets.all(6),
                              child: Icon(
                                Icons.zoom_out_map,
                                color: Colors.white,
                                size: 16,
                              ),
                            ),
                          ),
                        ),
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: 8),
                Text(
                  sample.title,
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: const TextStyle(
                    fontSize: 12,
                    fontWeight: FontWeight.w600,
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}

class _PhotoViewGalleryPage extends StatefulWidget {
  const _PhotoViewGalleryPage({
    required this.initialIndex,
    required this.samples,
  });

  final int initialIndex;
  final List<_PhotoSample> samples;

  @override
  State<_PhotoViewGalleryPage> createState() => _PhotoViewGalleryPageState();
}

class _PhotoViewGalleryPageState extends State<_PhotoViewGalleryPage> {
  late final PageController _pageController;
  late int _currentIndex;

  @override
  void initState() {
    super.initState();
    _currentIndex = widget.initialIndex;
    _pageController = PageController(initialPage: widget.initialIndex);
  }

  @override
  void dispose() {
    _pageController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final _PhotoSample currentSample = widget.samples[_currentIndex];

    return Scaffold(
      backgroundColor: const Color(0xFF020617),
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        foregroundColor: Colors.white,
        title: Text(currentSample.title),
      ),
      body: Stack(
        children: <Widget>[
          PhotoViewGallery.builder(
            backgroundDecoration: const BoxDecoration(color: Color(0xFF020617)),
            builder: (BuildContext context, int index) {
              final _PhotoSample sample = widget.samples[index];

              return PhotoViewGalleryPageOptions(
                heroAttributes: PhotoViewHeroAttributes(tag: sample.heroTag),
                imageProvider: AssetImage(sample.assetPath),
                initialScale: PhotoViewComputedScale.contained,
                maxScale: PhotoViewComputedScale.covered * 3,
                minScale: PhotoViewComputedScale.contained * 0.9,
              );
            },
            itemCount: widget.samples.length,
            onPageChanged: _handlePageChanged,
            pageController: _pageController,
            scrollPhysics: const BouncingScrollPhysics(),
          ),
          Positioned(
            bottom: 24,
            left: 16,
            right: 16,
            child: SafeArea(
              top: false,
              child: _PhotoViewGalleryFooter(
                currentIndex: _currentIndex,
                sample: currentSample,
                totalCount: widget.samples.length,
              ),
            ),
          ),
        ],
      ),
    );
  }

  void _handlePageChanged(int index) {
    setState(() {
      _currentIndex = index;
    });
  }
}

class _PhotoViewGalleryFooter extends StatelessWidget {
  const _PhotoViewGalleryFooter({
    required this.currentIndex,
    required this.sample,
    required this.totalCount,
  });

  final int currentIndex;
  final _PhotoSample sample;
  final int totalCount;

  @override
  Widget build(BuildContext context) {
    return DecoratedBox(
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(20),
        color: Colors.black.withValues(alpha: 0.50),
      ),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Row(
          children: <Widget>[
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                mainAxisSize: MainAxisSize.min,
                children: <Widget>[
                  Text(
                    sample.title,
                    style: const TextStyle(
                      color: Colors.white,
                      fontSize: 16,
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    sample.subtitle,
                    style: TextStyle(
                      color: Colors.white.withValues(alpha: 0.82),
                      fontSize: 13,
                      height: 1.5,
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(width: 16),
            Text(
              '${currentIndex + 1} / $totalCount',
              style: const TextStyle(
                color: Colors.white,
                fontWeight: FontWeight.w600,
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _PhotoSample {
  const _PhotoSample({
    required this.title,
    required this.subtitle,
    required this.assetPath,
    required this.heroTag,
  });

  final String title;
  final String subtitle;
  final String assetPath;
  final String heroTag;
}
