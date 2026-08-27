import 'package:flutter/material.dart';

/// 交错动画示例
///
/// 使用 [AnimationController] + [Interval] + [CurvedAnimation]
/// 将总时长切分为多段，让多个组件错峰进场。
class StaggeredAnimationDemoPage extends StatelessWidget {
  const StaggeredAnimationDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return StaggeredAnimationDemoView(title: title);
  }
}

class StaggeredAnimationDemoView extends StatefulWidget {
  const StaggeredAnimationDemoView({super.key, required this.title});

  final String title;

  @override
  State<StaggeredAnimationDemoView> createState() =>
      _StaggeredAnimationDemoViewState();
}

/// 单个色块所需的动画集合，便于统一创建与释放。
class _TileAnimation {
  const _TileAnimation({
    required this.slide,
    required this.fade,
    required this.scale,
    required this.curved,
  });

  final Animation<Offset> slide;
  final Animation<double> fade;
  final Animation<double> scale;
  final CurvedAnimation curved;
}

class _StaggeredAnimationDemoViewState extends State<StaggeredAnimationDemoView>
    with SingleTickerProviderStateMixin {
  static const int _tileCount = 6;

  late final AnimationController _controller;
  late final List<_TileAnimation> _tiles;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 1800),
    );
    _tiles = _createTiles();
    _controller.forward();
  }

  @override
  void dispose() {
    for (final _TileAnimation tile in _tiles) {
      tile.curved.dispose();
    }
    _controller.dispose();
    super.dispose();
  }

  List<_TileAnimation> _createTiles() {
    return List<_TileAnimation>.generate(_tileCount, (int index) {
      final double start = index / _tileCount;
      final double end = start + 1 / _tileCount;
      final CurvedAnimation curved = CurvedAnimation(
        parent: _controller,
        curve: Interval(start, end, curve: Curves.easeOutCubic),
      );
      return _TileAnimation(
        slide: Tween<Offset>(
          begin: Offset(index.isEven ? 0.6 : -0.6, 0),
          end: Offset.zero,
        ).animate(curved),
        fade: curved,
        scale: Tween<double>(begin: 0.7, end: 1).animate(curved),
        curved: curved,
      );
    });
  }

  void _replay() {
    _controller.forward(from: 0);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
        actions: [
          IconButton(
            onPressed: _replay,
            icon: const Icon(Icons.replay),
            tooltip: '重播',
          ),
        ],
      ),
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.all(16),
            child: Text(
              '通过 Interval 将总时长切分为 $_tileCount 段，'
              '每个色块错峰完成位移、淡入与缩放。',
              style: Theme.of(context).textTheme.bodyMedium,
            ),
          ),
          Expanded(
            child: Column(
              children: List<Widget>.generate(
                _tileCount,
                (int index) => Expanded(child: _buildTile(index)),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildTile(int index) {
    final _TileAnimation tile = _tiles[index];
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 6),
      child: FadeTransition(
        opacity: tile.fade,
        child: SlideTransition(
          position: tile.slide,
          child: ScaleTransition(
            scale: tile.scale,
            child: Container(
              alignment: Alignment.center,
              decoration: BoxDecoration(
                color: Colors.primaries[index % Colors.primaries.length],
                borderRadius: BorderRadius.circular(16),
              ),
              child: Text(
                'Tile ${index + 1}',
                style: const TextStyle(
                  color: Colors.white,
                  fontWeight: FontWeight.bold,
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }
}
