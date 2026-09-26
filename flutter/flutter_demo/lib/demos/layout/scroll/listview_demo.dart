import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';

/// ListView
/// Demonstrates the usage of ListView widget
class ListViewDemoPage extends BasicLayoutPage {
  const ListViewDemoPage({super.key, required super.title});

  @override
  BasicLayoutPageState<ListViewDemoPage> createState() =>
      _ListViewDemoPageState();
}

class _ListViewDemoPageState extends BasicLayoutPageState<ListViewDemoPage> {
  @override
  Widget buildPreview() {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(BasicDemoDimens.pagePadding),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          _buildSectionTitle('ListView.builder'),
          SizedBox(height: 200, child: _buildListViewBuilder()),
          const SizedBox(height: BasicDemoDimens.sectionGap),
          _buildSectionTitle('ListView.separated'),
          SizedBox(height: 200, child: _buildListViewSeparated()),
          const SizedBox(height: BasicDemoDimens.sectionGap),
          _buildSectionTitle('Horizontal ListView'),
          SizedBox(height: 120, child: _buildHorizontalListView()),
        ],
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
      itemBuilder: (BuildContext context, int index) {
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
      separatorBuilder: (BuildContext context, int index) =>
          const Divider(height: 1),
      itemBuilder: (BuildContext context, int index) {
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
      itemBuilder: (BuildContext context, int index) {
        return Container(
          width: 100,
          margin: const EdgeInsets.symmetric(horizontal: 8),
          decoration: BoxDecoration(
            color: Colors.blue.shade100,
            borderRadius: BorderRadius.circular(BasicDemoDimens.cornerSmall),
          ),
          child: Center(child: Text('Card $index')),
        );
      },
    );
  }
}
