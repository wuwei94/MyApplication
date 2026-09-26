import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';

/// Opacity
/// Demonstrates the usage of Opacity widget
class OpacityDemoPage extends BasicLayoutPage {
  const OpacityDemoPage({super.key, required super.title});

  @override
  BasicLayoutPageState<OpacityDemoPage> createState() =>
      _OpacityDemoPageState();
}

class _OpacityDemoPageState extends BasicLayoutPageState<OpacityDemoPage> {
  double _opacity = 1.0;

  @override
  List<String> buildList() => const <String>[
        '1. 不透明度 100%',
        '2. 不透明度 70%',
        '3. 不透明度 40%',
        '4. 不透明度 10%',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        setState(() => _opacity = 1.0);
      case 1:
        setState(() => _opacity = 0.7);
      case 2:
        setState(() => _opacity = 0.4);
      case 3:
        setState(() => _opacity = 0.1);
    }
  }

  @override
  Widget buildPreview() {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(BasicDemoDimens.pagePadding),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          _buildSectionTitle('Opacity Widget'),
          Opacity(
            opacity: _opacity,
            child: Container(
              width: double.infinity,
              height: 100,
              color: Colors.blue,
              child: Center(
                child: Text(
                  'Fading Container ${( _opacity * 100).toInt()}%',
                  style: const TextStyle(color: Colors.white, fontSize: 20),
                ),
              ),
            ),
          ),
          const SizedBox(height: BasicDemoDimens.sectionGap),
          _buildSectionTitle('Opacity with Child'),
          Opacity(
            opacity: _opacity,
            child: Card(
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Column(
                  children: <Widget>[
                    const Icon(Icons.image, size: 48),
                    const SizedBox(height: 8),
                    const Text('Card with Opacity'),
                    ElevatedButton(
                      onPressed: () {},
                      child: const Text('Button'),
                    ),
                  ],
                ),
              ),
            ),
          ),
          const SizedBox(height: BasicDemoDimens.sectionGap),
          _buildSectionTitle('Different Opacity Values'),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceAround,
            children: <Widget>[
              _buildOpacityBox(1.0, '100%'),
              _buildOpacityBox(0.7, '70%'),
              _buildOpacityBox(0.4, '40%'),
              _buildOpacityBox(0.1, '10%'),
            ],
          ),
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

  Widget _buildOpacityBox(double opacity, String label) {
    return Column(
      children: <Widget>[
        Opacity(
          opacity: opacity,
          child: Container(width: 60, height: 60, color: Colors.green),
        ),
        const SizedBox(height: 4),
        Text(label, style: const TextStyle(fontSize: 12)),
      ],
    );
  }
}
