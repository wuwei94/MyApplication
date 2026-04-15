import 'package:flutter/material.dart';

/// ListView Example
/// Demonstrates the usage of ListView widget
class ListViewExample extends StatelessWidget {
  const ListViewExample({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return ListViewRoute(title: title);
  }
}

class ListViewRoute extends StatelessWidget {
  const ListViewRoute({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(title)),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _buildSectionTitle('ListView.builder'),
            SizedBox(height: 200, child: _buildListViewBuilder()),
            const SizedBox(height: 24),
            _buildSectionTitle('ListView.separated'),
            SizedBox(height: 200, child: _buildListViewSeparated()),
            const SizedBox(height: 24),
            _buildSectionTitle('Horizontal ListView'),
            SizedBox(height: 120, child: _buildHorizontalListView()),
          ],
        ),
      ),
    );
  }

  Widget _buildSectionTitle(String text) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: Text(
        text,
        style: const TextStyle(
          fontSize: 18,
          fontWeight: FontWeight.bold,
          color: Colors.blue,
        ),
      ),
    );
  }

  Widget _buildListViewBuilder() {
    return ListView.builder(
      itemCount: 20,
      itemBuilder: (context, index) {
        return ListTile(
          leading: CircleAvatar(child: Text('$index')),
          title: Text('Item $index'),
          subtitle: Text('Subtitle for item $index'),
        );
      },
    );
  }

  Widget _buildListViewSeparated() {
    return ListView.separated(
      itemCount: 20,
      separatorBuilder: (context, index) => const Divider(height: 1),
      itemBuilder: (context, index) {
        return ListTile(
          leading: CircleAvatar(
            backgroundColor: Colors.blue.shade100,
            child: Text('$index'),
          ),
          title: Text('Item $index'),
          trailing: const Icon(Icons.chevron_right),
        );
      },
    );
  }

  Widget _buildHorizontalListView() {
    return ListView.builder(
      scrollDirection: Axis.horizontal,
      itemCount: 10,
      itemBuilder: (context, index) {
        return Container(
          width: 100,
          margin: const EdgeInsets.symmetric(horizontal: 8),
          decoration: BoxDecoration(
            color: Colors.blue.shade100,
            borderRadius: BorderRadius.circular(8),
          ),
          child: Center(child: Text('Card $index')),
        );
      },
    );
  }
}
