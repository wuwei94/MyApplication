import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';

/// GridView
/// Demonstrates the usage of GridView widget
class GridViewDemoPage extends BasicLayoutPage {
  const GridViewDemoPage({super.key, required super.title});

  @override
  BasicLayoutPageState<GridViewDemoPage> createState() =>
      _GridViewDemoPageState();
}

class _GridViewDemoPageState extends BasicLayoutPageState<GridViewDemoPage> {
  @override
  Widget buildPreview() {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(BasicDemoDimens.pagePadding),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          _buildSectionTitle('GridView.count'),
          SizedBox(height: 200, child: _buildGridViewCount()),
          const SizedBox(height: BasicDemoDimens.sectionGap),
          _buildSectionTitle('GridView.builder'),
          SizedBox(height: 200, child: _buildGridViewBuilder()),
          const SizedBox(height: BasicDemoDimens.sectionGap),
          _buildSectionTitle('GridView.extent'),
          SizedBox(height: 200, child: _buildGridViewExtent()),
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

  Widget _buildGridViewCount() {
    return GridView.count(
      crossAxisCount: 3,
      crossAxisSpacing: 8,
      mainAxisSpacing: 8,
      children: List<Widget>.generate(9, (int index) {
        return Container(
          decoration: BoxDecoration(
            color: Colors.blue.shade100,
            borderRadius: BorderRadius.circular(BasicDemoDimens.cornerSmall),
          ),
          child: Center(child: Text('Item $index')),
        );
      }),
    );
  }

  Widget _buildGridViewBuilder() {
    return GridView.builder(
      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: 4,
        crossAxisSpacing: 8,
        mainAxisSpacing: 8,
      ),
      itemCount: 12,
      itemBuilder: (BuildContext context, int index) {
        return Container(
          decoration: BoxDecoration(
            color: Colors.green.shade100,
            borderRadius: BorderRadius.circular(BasicDemoDimens.cornerSmall),
          ),
          child: Center(child: Text('$index')),
        );
      },
    );
  }

  Widget _buildGridViewExtent() {
    return GridView.extent(
      maxCrossAxisExtent: 80,
      crossAxisSpacing: 8,
      mainAxisSpacing: 8,
      children: List<Widget>.generate(12, (int index) {
        return Container(
          decoration: BoxDecoration(
            color: Colors.orange.shade100,
            borderRadius: BorderRadius.circular(BasicDemoDimens.cornerSmall),
          ),
          child: Center(child: Text('$index')),
        );
      }),
    );
  }
}
