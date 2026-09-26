import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';
import 'package:flutter_staggered_grid_view/flutter_staggered_grid_view.dart';

/// StaggeredGridView — 瀑布 / 拼贴网格预览
///
/// 纯布局预览，无操作列表；画布直接展示 StaggeredGrid.count 关键形态。
///
/// 官方参考：
/// https://pub.dev/packages/flutter_staggered_grid_view
class StaggeredGridViewDemoPage extends BasicLayoutPage {
  const StaggeredGridViewDemoPage({super.key, required super.title});

  @override
  BasicLayoutPageState<StaggeredGridViewDemoPage> createState() =>
      _StaggeredGridViewDemoPageState();
}

class _StaggeredGridViewDemoPageState
    extends BasicLayoutPageState<StaggeredGridViewDemoPage> {
  static const List<_TileSpec> _tiles = <_TileSpec>[
    _TileSpec(cross: 2, main: 2, label: 'A 2×2'),
    _TileSpec(cross: 1, main: 1, label: 'B 1×1'),
    _TileSpec(cross: 1, main: 1, label: 'C 1×1'),
    _TileSpec(cross: 1, main: 2, label: 'D 1×2'),
    _TileSpec(cross: 1, main: 1, label: 'E 1×1'),
    _TileSpec(cross: 2, main: 1, label: 'F 2×1'),
    _TileSpec(cross: 1, main: 1, label: 'G 1×1'),
  ];

  @override
  Widget buildPreview() {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          const Text(
            'StaggeredGrid.count',
            style: TextStyle(
              fontSize: 18,
              fontWeight: FontWeight.bold,
              color: Colors.blue,
            ),
          ),
          const SizedBox(height: 12),
          StaggeredGrid.count(
            crossAxisCount: 4,
            mainAxisSpacing: 8,
            crossAxisSpacing: 8,
            children: <Widget>[
              for (final _TileSpec spec in _tiles)
                StaggeredGridTile.count(
                  crossAxisCellCount: spec.cross,
                  mainAxisCellCount: spec.main,
                  child: _buildTile(spec),
                ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildTile(_TileSpec spec) {
    final int seed = spec.label.hashCode & 0xff;
    return DecoratedBox(
      decoration: BoxDecoration(
        color: Color.fromARGB(255, 80 + seed, 120 + (seed % 80), 200 - seed % 50),
        borderRadius: BorderRadius.circular(12),
      ),
      child: Center(
        child: Text(
          spec.label,
          style: const TextStyle(
            color: Colors.white,
            fontWeight: FontWeight.w700,
          ),
        ),
      ),
    );
  }
}

class _TileSpec {
  const _TileSpec({
    required this.cross,
    required this.main,
    required this.label,
  });

  final int cross;
  final int main;
  final String label;
}
