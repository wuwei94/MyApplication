import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';

/// TabBarView
/// Demonstrates tab navigation with TabBarView
class TabBarViewDemoPage extends BasicLayoutPage {
  const TabBarViewDemoPage({super.key, required super.title});

  @override
  BasicLayoutPageState<TabBarViewDemoPage> createState() =>
      _TabBarViewDemoPageState();
}

class _TabBarViewDemoPageState extends BasicLayoutPageState<TabBarViewDemoPage> {
  @override
  Widget buildPreview() {
    return DefaultTabController(
      length: 4,
      child: Column(
        children: <Widget>[
          const TabBar(
            tabs: <Widget>[
              Tab(icon: Icon(Icons.home), text: 'Home'),
              Tab(icon: Icon(Icons.search), text: 'Search'),
              Tab(icon: Icon(Icons.favorite), text: 'Likes'),
              Tab(icon: Icon(Icons.person), text: 'Profile'),
            ],
          ),
          Expanded(
            child: TabBarView(
              children: <Widget>[
                _buildTabContent('Home', Colors.blue),
                _buildTabContent('Search', Colors.green),
                _buildTabContent('Likes', Colors.red),
                _buildTabContent('Profile', Colors.purple),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildTabContent(String label, Color color) {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: <Widget>[
          Icon(Icons.tab, size: 100, color: color),
          const SizedBox(height: 20),
          Text(
            '$label Tab',
            style: TextStyle(
              fontSize: 32,
              color: color,
              fontWeight: FontWeight.bold,
            ),
          ),
          const SizedBox(height: 20),
          Text(
            'Swipe left or right to switch tabs',
            style: TextStyle(fontSize: 16, color: Colors.grey.shade600),
          ),
        ],
      ),
    );
  }
}
