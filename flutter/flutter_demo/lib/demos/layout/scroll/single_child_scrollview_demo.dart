import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';

/// SingleChildScrollView
/// Demonstrates single child scrolling
class SingleChildScrollViewDemoPage extends BasicLayoutPage {
  const SingleChildScrollViewDemoPage({super.key, required super.title});

  @override
  BasicLayoutPageState<SingleChildScrollViewDemoPage> createState() =>
      _SingleChildScrollViewDemoPageState();
}

class _SingleChildScrollViewDemoPageState
    extends BasicLayoutPageState<SingleChildScrollViewDemoPage> {
  @override
  Widget buildPreview() {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(BasicDemoDimens.pagePadding),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          const Text(
            'SingleChildScrollView',
            style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 16),
          const Text(
            'This is a simple scrollable container that can hold a single child widget. '
            'It is useful when you have a widget that might overflow the screen.',
            style: TextStyle(fontSize: 16),
          ),
          const SizedBox(height: BasicDemoDimens.sectionGap),
          ...List<Widget>.generate(
            20,
            (int index) => Container(
              width: double.infinity,
              height: 80,
              margin: const EdgeInsets.only(bottom: 12),
              decoration: BoxDecoration(
                color: Colors.blue.shade100,
                borderRadius: BorderRadius.circular(BasicDemoDimens.cornerSmall),
              ),
              child: Center(
                child: Text(
                  'Item ${index + 1}',
                  style: const TextStyle(fontSize: 18),
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}
