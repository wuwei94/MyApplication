import 'dart:math';

import 'package:flutter/material.dart';

/// Clip Example
/// Demonstrates ClipRect, ClipRRect, ClipOval, ClipPath
class ClipDemoPage extends StatelessWidget {
  const ClipDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return ClipDemoView(title: title);
  }
}

class ClipDemoView extends StatelessWidget {
  const ClipDemoView({super.key, required this.title});

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
            _buildSectionTitle('ClipRRect - 圆角裁剪'),
            _buildClipRRect(),
            const SizedBox(height: 24),
            _buildSectionTitle('ClipOval - 椭圆裁剪'),
            _buildClipOval(),
            const SizedBox(height: 24),
            _buildSectionTitle('ClipRect - 矩形裁剪'),
            _buildClipRect(),
            const SizedBox(height: 24),
            _buildSectionTitle('ClipPath - 路径裁剪'),
            _buildClipPath(),
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

  Widget _buildClipRRect() {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceAround,
      children: [
        ClipRRect(
          borderRadius: BorderRadius.circular(16),
          child: Container(
            width: 100,
            height: 100,
            color: Colors.blue,
            child: const Icon(Icons.image, size: 50, color: Colors.white),
          ),
        ),
        ClipRRect(
          borderRadius: const BorderRadius.only(
            topLeft: Radius.circular(32),
            bottomRight: Radius.circular(32),
          ),
          child: Container(
            width: 100,
            height: 100,
            color: Colors.green,
            child: const Icon(Icons.crop_free, size: 50, color: Colors.white),
          ),
        ),
      ],
    );
  }

  Widget _buildClipOval() {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceAround,
      children: [
        ClipOval(
          child: Container(
            width: 100,
            height: 100,
            color: Colors.orange,
            child: const Icon(Icons.circle, size: 50, color: Colors.white),
          ),
        ),
        ClipOval(
          child: Container(
            width: 120,
            height: 80,
            color: Colors.purple,
            child: const Center(
              child: Text('Ellipse', style: TextStyle(color: Colors.white)),
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildClipRect() {
    return Center(
      child: ClipRect(
        child: Align(
          alignment: Alignment.center,
          heightFactor: 0.5,
          child: Container(
            width: 200,
            height: 200,
            color: Colors.teal,
            child: const Center(
              child: Text(
                'Half Clipped',
                style: TextStyle(color: Colors.white, fontSize: 20),
              ),
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildClipPath() {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceAround,
      children: [
        ClipPath(
          clipper: _TriangleClipper(),
          child: Container(
            width: 100,
            height: 100,
            color: Colors.red,
            child: const Center(
              child: Text('Triangle', style: TextStyle(color: Colors.white)),
            ),
          ),
        ),
        ClipPath(
          clipper: _StarClipper(),
          child: Container(
            width: 100,
            height: 100,
            color: Colors.amber,
            child: const Center(
              child: Text('Star', style: TextStyle(color: Colors.white)),
            ),
          ),
        ),
      ],
    );
  }
}

class _TriangleClipper extends CustomClipper<Path> {
  @override
  Path getClip(Size size) {
    final path = Path();
    path.moveTo(size.width / 2, 0);
    path.lineTo(size.width, size.height);
    path.lineTo(0, size.height);
    path.close();
    return path;
  }

  @override
  bool shouldReclip(covariant CustomClipper<Path> oldClipper) => false;
}

class _StarClipper extends CustomClipper<Path> {
  @override
  Path getClip(Size size) {
    final path = Path();
    final centerX = size.width / 2;
    final centerY = size.height / 2;
    final radius = size.width / 2;

    for (int i = 0; i < 5; i++) {
      final angle = (i * 144 - 90) * 3.14159 / 180;
      final x = centerX + radius * cos(angle);
      final y = centerY + radius * sin(angle);
      if (i == 0) {
        path.moveTo(x, y);
      } else {
        path.lineTo(x, y);
      }
    }
    path.close();
    return path;
  }

  @override
  bool shouldReclip(covariant CustomClipper<Path> oldClipper) => false;
}
