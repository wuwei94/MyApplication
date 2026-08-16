import 'package:flutter/material.dart';

/// SingleChildScrollView
/// Demonstrates single child scrolling
class SingleChildScrollViewDemoPage extends StatelessWidget {
  const SingleChildScrollViewDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return SingleChildScrollViewDemoView(title: title);
  }
}

class SingleChildScrollViewDemoView extends StatelessWidget {
  const SingleChildScrollViewDemoView({super.key, required this.title});

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
            const SizedBox(height: 24),
            ...List.generate(
              20,
              (index) => Container(
                width: double.infinity,
                height: 80,
                margin: const EdgeInsets.only(bottom: 12),
                decoration: BoxDecoration(
                  color: Colors.blue.shade100,
                  borderRadius: BorderRadius.circular(8),
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
      ),
    );
  }
}
