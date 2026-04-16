import 'package:flutter/material.dart';

/// TabBarView Example
/// Demonstrates tab navigation with TabBarView
class TabBarViewDemoPage extends StatelessWidget {
  const TabBarViewDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return TabBarViewDemoView(title: title);
  }
}

class TabBarViewDemoView extends StatelessWidget {
  const TabBarViewDemoView({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return DefaultTabController(
      length: 4,
      child: Scaffold(
        appBar: AppBar(
          title: Text(title),
          bottom: const TabBar(
            tabs: [
              Tab(icon: Icon(Icons.home), text: 'Home'),
              Tab(icon: Icon(Icons.search), text: 'Search'),
              Tab(icon: Icon(Icons.favorite), text: 'Likes'),
              Tab(icon: Icon(Icons.person), text: 'Profile'),
            ],
          ),
        ),
        body: TabBarView(
          children: [
            _buildTabContent('Home', Colors.blue),
            _buildTabContent('Search', Colors.green),
            _buildTabContent('Likes', Colors.red),
            _buildTabContent('Profile', Colors.purple),
          ],
        ),
      ),
    );
  }

  Widget _buildTabContent(String label, Color color) {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
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
