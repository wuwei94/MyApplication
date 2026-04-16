import 'package:flutter/material.dart';

/// Opacity Example
/// Demonstrates the usage of Opacity widget
class OpacityDemoPage extends StatelessWidget {
  const OpacityDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return OpacityDemoView(title: title);
  }
}

class OpacityDemoView extends StatefulWidget {
  const OpacityDemoView({super.key, required this.title});

  final String title;

  @override
  State<OpacityDemoView> createState() => _OpacityDemoViewState();
}

class _OpacityDemoViewState extends State<OpacityDemoView> {
  double _opacity = 1.0;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(widget.title)),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _buildSectionTitle('Opacity Slider'),
            Slider(
              value: _opacity,
              min: 0.0,
              max: 1.0,
              onChanged: (value) {
                setState(() {
                  _opacity = value;
                });
              },
            ),
            Text('Opacity: ${(_opacity * 100).toInt()}%'),
            const SizedBox(height: 24),
            _buildSectionTitle('Opacity Widget'),
            Opacity(
              opacity: _opacity,
              child: Container(
                width: double.infinity,
                height: 100,
                color: Colors.blue,
                child: const Center(
                  child: Text(
                    'Fading Container',
                    style: TextStyle(color: Colors.white, fontSize: 20),
                  ),
                ),
              ),
            ),
            const SizedBox(height: 24),
            _buildSectionTitle('Opacity with Child'),
            Opacity(
              opacity: _opacity,
              child: Card(
                child: Padding(
                  padding: const EdgeInsets.all(16),
                  child: Column(
                    children: [
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
            const SizedBox(height: 24),
            _buildSectionTitle('Different Opacity Values'),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceAround,
              children: [
                _buildOpacityBox(1.0, '100%'),
                _buildOpacityBox(0.7, '70%'),
                _buildOpacityBox(0.4, '40%'),
                _buildOpacityBox(0.1, '10%'),
              ],
            ),
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

  Widget _buildOpacityBox(double opacity, String label) {
    return Column(
      children: [
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
