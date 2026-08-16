import 'package:flutter/material.dart';
import 'package:flutter_staggered_grid_view/flutter_staggered_grid_view.dart';

/// Flutter Staggered Grid View
/// https://pub.dev/packages/flutter_staggered_grid_view
class StaggeredGridViewDemoPage extends StatelessWidget {
  const StaggeredGridViewDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return StaggeredGridViewDemoView(title: title);
  }
}

class StaggeredGridViewDemoView extends StatelessWidget {
  const StaggeredGridViewDemoView({super.key, required this.title});

  final String title;

  static const List<_GalleryItem> _items = <_GalleryItem>[
    _GalleryItem(
      title: 'Masonry feed',
      subtitle: '高度不固定',
      height: 220,
      color: Color(0xFFE0F2FE),
      icon: Icons.waterfall_chart_rounded,
    ),
    _GalleryItem(
      title: 'Catalog card',
      subtitle: '跨列布局',
      height: 150,
      color: Color(0xFFDCFCE7),
      icon: Icons.dashboard_customize_rounded,
    ),
    _GalleryItem(
      title: 'Mood board',
      subtitle: '拼贴样式',
      height: 260,
      color: Color(0xFFFCE7F3),
      icon: Icons.auto_awesome_mosaic_rounded,
    ),
    _GalleryItem(
      title: 'Photo note',
      subtitle: '列表瀑布流',
      height: 180,
      color: Color(0xFFFEF3C7),
      icon: Icons.photo_library_rounded,
    ),
    _GalleryItem(
      title: 'Metrics tile',
      subtitle: '信息卡片',
      height: 132,
      color: Color(0xFFEDE9FE),
      icon: Icons.query_stats_rounded,
    ),
    _GalleryItem(
      title: 'Travel plan',
      subtitle: '内容自适应',
      height: 240,
      color: Color(0xFFFFEDD5),
      icon: Icons.map_rounded,
    ),
    _GalleryItem(
      title: 'Recipe board',
      subtitle: '不同高度',
      height: 172,
      color: Color(0xFFCCFBF1),
      icon: Icons.restaurant_menu_rounded,
    ),
    _GalleryItem(
      title: 'Reading list',
      subtitle: '紧凑排布',
      height: 208,
      color: Color(0xFFE5E7EB),
      icon: Icons.menu_book_rounded,
    ),
    _GalleryItem(
      title: 'Product wall',
      subtitle: '响应式列数',
      height: 148,
      color: Color(0xFFDBEAFE),
      icon: Icons.widgets_rounded,
    ),
  ];

  static const List<_StaggeredTileSpec> _tileSpecs = <_StaggeredTileSpec>[
    _StaggeredTileSpec(2, 2, 0),
    _StaggeredTileSpec(2, 1, 1),
    _StaggeredTileSpec(1, 1, 2),
    _StaggeredTileSpec(1, 1, 3),
    _StaggeredTileSpec(2, 1, 4),
    _StaggeredTileSpec(1, 2, 5),
    _StaggeredTileSpec(1, 1, 6),
    _StaggeredTileSpec(2, 1, 7),
  ];

  @override
  Widget build(BuildContext context) {
    return DefaultTabController(
      length: 3,
      child: Scaffold(
        appBar: AppBar(
          title: Text(title),
          bottom: const TabBar(
            tabs: <Widget>[
              Tab(text: 'Masonry'),
              Tab(text: 'Staggered'),
              Tab(text: 'Quilted'),
            ],
          ),
        ),
        body: TabBarView(
          children: <Widget>[
            _buildMasonryTab(),
            _buildStaggeredTab(),
            _buildQuiltedTab(),
          ],
        ),
      ),
    );
  }

  Widget _buildMasonryTab() {
    return LayoutBuilder(
      builder: (BuildContext context, BoxConstraints constraints) {
        final int crossAxisCount = constraints.maxWidth >= 720 ? 3 : 2;

        return MasonryGridView.count(
          padding: const EdgeInsets.fromLTRB(16, 16, 16, 24),
          crossAxisCount: crossAxisCount,
          mainAxisSpacing: 12,
          crossAxisSpacing: 12,
          itemCount: _items.length,
          itemBuilder: (BuildContext context, int index) {
            final _GalleryItem item = _items[index];

            return SizedBox(
              height: item.height,
              child: _GalleryCard(item: item, index: index),
            );
          },
        );
      },
    );
  }

  Widget _buildStaggeredTab() {
    return ListView(
      padding: const EdgeInsets.fromLTRB(16, 16, 16, 24),
      children: <Widget>[
        StaggeredGrid.count(
          crossAxisCount: 4,
          mainAxisSpacing: 12,
          crossAxisSpacing: 12,
          children: _tileSpecs
              .map((_StaggeredTileSpec spec) {
                final _GalleryItem item = _items[spec.itemIndex];

                return StaggeredGridTile.count(
                  crossAxisCellCount: spec.crossAxisCellCount,
                  mainAxisCellCount: spec.mainAxisCellCount,
                  child: _GalleryCard(item: item, index: spec.itemIndex),
                );
              })
              .toList(growable: false),
        ),
      ],
    );
  }

  Widget _buildQuiltedTab() {
    return GridView.custom(
      padding: const EdgeInsets.fromLTRB(16, 16, 16, 24),
      gridDelegate: SliverQuiltedGridDelegate(
        crossAxisCount: 4,
        mainAxisSpacing: 12,
        crossAxisSpacing: 12,
        repeatPattern: QuiltedGridRepeatPattern.inverted,
        pattern: const <QuiltedGridTile>[
          QuiltedGridTile(2, 2),
          QuiltedGridTile(1, 1),
          QuiltedGridTile(1, 1),
          QuiltedGridTile(1, 2),
        ],
      ),
      childrenDelegate: SliverChildBuilderDelegate((
        BuildContext context,
        int index,
      ) {
        final _GalleryItem item = _items[index % _items.length];

        return _GalleryCard(item: item, index: index);
      }, childCount: 16),
    );
  }
}

class _GalleryCard extends StatelessWidget {
  const _GalleryCard({required this.item, required this.index});

  final _GalleryItem item;
  final int index;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return DecoratedBox(
      decoration: BoxDecoration(
        color: item.color,
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: Colors.black12),
      ),
      child: Padding(
        padding: const EdgeInsets.all(14),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Row(
              children: <Widget>[
                Container(
                  width: 36,
                  height: 36,
                  decoration: BoxDecoration(
                    color: Colors.white,
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: Icon(item.icon, size: 20, color: Colors.black87),
                ),
                const Spacer(),
                Text(
                  '#${index + 1}',
                  style: theme.textTheme.labelMedium?.copyWith(
                    color: Colors.black54,
                    fontWeight: FontWeight.w700,
                  ),
                ),
              ],
            ),
            const Spacer(),
            Text(
              item.title,
              maxLines: 2,
              overflow: TextOverflow.ellipsis,
              style: theme.textTheme.titleMedium?.copyWith(
                fontWeight: FontWeight.w800,
                color: Colors.black87,
              ),
            ),
            const SizedBox(height: 6),
            Text(
              item.subtitle,
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
              style: theme.textTheme.bodySmall?.copyWith(
                color: Colors.black54,
                fontWeight: FontWeight.w600,
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _GalleryItem {
  const _GalleryItem({
    required this.title,
    required this.subtitle,
    required this.height,
    required this.color,
    required this.icon,
  });

  final String title;
  final String subtitle;
  final double height;
  final Color color;
  final IconData icon;
}

class _StaggeredTileSpec {
  const _StaggeredTileSpec(
    this.crossAxisCellCount,
    this.mainAxisCellCount,
    this.itemIndex,
  );

  final int crossAxisCellCount;
  final int mainAxisCellCount;
  final int itemIndex;
}
