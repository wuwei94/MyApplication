import 'dart:math';

import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';

/// Clip
/// Demonstrates ClipRect, ClipRRect, ClipOval, ClipPath
class ClipDemoPage extends BasicLayoutPage {
  const ClipDemoPage({super.key, required super.title});

  @override
  BasicLayoutPageState<ClipDemoPage> createState() => _ClipDemoPageState();
}

class _ClipDemoPageState extends BasicLayoutPageState<ClipDemoPage> {
  @override
  Widget buildPreview() {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(BasicDemoDimens.pagePadding),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          _buildSectionTitle('ClipRRect - 圆角裁剪'),
          _buildClipRRect(),
          const SizedBox(height: BasicDemoDimens.sectionGap),
          _buildSectionTitle('ClipOval - 椭圆裁剪'),
          _buildClipOval(),
          const SizedBox(height: BasicDemoDimens.sectionGap),
          _buildSectionTitle('ClipRect - 矩形裁剪'),
          _buildClipRect(),
          const SizedBox(height: BasicDemoDimens.sectionGap),
          _buildSectionTitle('ClipPath - 路径裁剪'),
          _buildClipPath(),
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

  Widget _buildClipRRect() {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceAround,
      children: <Widget>[
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
      children: <Widget>[
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
      children: <Widget>[
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
    final Path path = Path();
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
    final Path path = Path();
    final double centerX = size.width / 2;
    final double centerY = size.height / 2;
    final double radius = size.width / 2;

    for (int i = 0; i < 5; i++) {
      final double angle = (i * 144 - 90) * 3.14159 / 180;
      final double x = centerX + radius * cos(angle);
      final double y = centerY + radius * sin(angle);
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
