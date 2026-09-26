import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';

/// NestedScrollView
/// Demonstrates nested scrolling with header and tab bar
class NestedScrollViewDemoPage extends BasicLayoutPage {
  const NestedScrollViewDemoPage({super.key, required super.title});

  @override
  BasicLayoutPageState<NestedScrollViewDemoPage> createState() =>
      _NestedScrollViewDemoPageState();
}

class _NestedScrollViewDemoPageState
    extends BasicLayoutPageState<NestedScrollViewDemoPage> {
  @override
  Widget buildPreview() {
    return DefaultTabController(
      length: 3,
      child: NestedScrollView(
        headerSliverBuilder: (BuildContext context, bool innerBoxIsScrolled) {
          return <Widget>[
            SliverAppBar(
              expandedHeight: 200,
              floating: false,
              pinned: true,
              flexibleSpace: FlexibleSpaceBar(
                title: Text(widget.title),
                background: Container(
                  decoration: BoxDecoration(
                    gradient: LinearGradient(
                      begin: Alignment.topLeft,
                      end: Alignment.bottomRight,
                      colors: <Color>[
                        Colors.blue.shade400,
                        Colors.purple.shade400,
                      ],
                    ),
                  ),
                  child: const Center(
                    child: Icon(
                      Icons.landscape,
                      size: 80,
                      color: Colors.white54,
                    ),
                  ),
                ),
              ),
            ),
            SliverPersistentHeader(
              delegate: _SliverTabBarDelegate(
                const TabBar(
                  tabs: <Widget>[
                    Tab(text: 'Tab 1'),
                    Tab(text: 'Tab 2'),
                    Tab(text: 'Tab 3'),
                  ],
                ),
              ),
              pinned: true,
            ),
          ];
        },
        body: TabBarView(
          children: <Widget>[
            _buildList('List 1'),
            _buildList('List 2'),
            _buildList('List 3'),
          ],
        ),
      ),
    );
  }

  Widget _buildList(String label) {
    return ListView.builder(
      padding: const EdgeInsets.all(16),
      itemCount: 30,
      itemBuilder: (BuildContext context, int index) {
        return Card(
          child: ListTile(
            title: Text('$label - Item $index'),
            leading: CircleAvatar(child: Text('$index')),
          ),
        );
      },
    );
  }
}

class _SliverTabBarDelegate extends SliverPersistentHeaderDelegate {
  _SliverTabBarDelegate(this.tabBar);

  final TabBar tabBar;

  @override
  double get minExtent => tabBar.preferredSize.height;

  @override
  double get maxExtent => tabBar.preferredSize.height;

  @override
  Widget build(
    BuildContext context,
    double shrinkOffset,
    bool overlapsContent,
  ) {
    return Container(color: Theme.of(context).cardColor, child: tabBar);
  }

  @override
  bool shouldRebuild(covariant SliverPersistentHeaderDelegate oldDelegate) {
    return false;
  }
}
