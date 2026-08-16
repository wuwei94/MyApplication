import 'package:flutter/material.dart';

/// ShaderMask
/// Demonstrates gradient masks and shader effects
class ShaderMaskDemoPage extends StatelessWidget {
  const ShaderMaskDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return ShaderMaskDemoView(title: title);
  }
}

class ShaderMaskDemoView extends StatelessWidget {
  const ShaderMaskDemoView({super.key, required this.title});

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
            _buildSectionTitle('Gradient Text'),
            _buildGradientText(),
            const SizedBox(height: 24),
            _buildSectionTitle('Gradient Image'),
            _buildGradientImage(),
            const SizedBox(height: 24),
            _buildSectionTitle('Fade Edge Effect'),
            _buildFadeEdge(),
            const SizedBox(height: 24),
            _buildSectionTitle('Mask with Icon'),
            _buildIconMask(),
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

  Widget _buildGradientText() {
    return const Center(
      child: ShaderMask(
        shaderCallback: _gradientTextShader,
        child: Text(
          'Gradient Text',
          style: TextStyle(
            fontSize: 40,
            fontWeight: FontWeight.bold,
            color: Colors.white,
          ),
        ),
      ),
    );
  }

  static Shader _gradientTextShader(Rect bounds) {
    return const LinearGradient(
      colors: [Colors.blue, Colors.purple, Colors.pink],
    ).createShader(bounds);
  }

  Widget _buildGradientImage() {
    return const Center(
      child: ShaderMask(
        shaderCallback: _gradientImageShader,
        blendMode: BlendMode.srcIn,
        child: Icon(Icons.flutter_dash, size: 120),
      ),
    );
  }

  static Shader _gradientImageShader(Rect bounds) {
    return const RadialGradient(
      colors: [Colors.yellow, Colors.orange, Colors.red],
      center: Alignment.center,
    ).createShader(bounds);
  }

  Widget _buildFadeEdge() {
    return Container(
      height: 100,
      decoration: BoxDecoration(
        color: Colors.blue.shade100,
        borderRadius: BorderRadius.circular(8),
      ),
      child: ShaderMask(
        shaderCallback: (bounds) {
          return const LinearGradient(
            colors: [
              Colors.transparent,
              Colors.black,
              Colors.black,
              Colors.transparent,
            ],
            stops: [0.0, 0.2, 0.8, 1.0],
          ).createShader(bounds);
        },
        blendMode: BlendMode.dstIn,
        child: ListView(
          scrollDirection: Axis.horizontal,
          children: List.generate(
            10,
            (index) => Container(
              width: 80,
              margin: const EdgeInsets.all(8),
              color: Colors.blue,
              child: Center(
                child: Text(
                  'Item $index',
                  style: const TextStyle(color: Colors.white),
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildIconMask() {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceAround,
      children: [
        ShaderMask(
          shaderCallback: (bounds) {
            return const LinearGradient(
              colors: [Colors.green, Colors.blue],
            ).createShader(bounds);
          },
          child: const Icon(Icons.favorite, size: 60, color: Colors.white),
        ),
        ShaderMask(
          shaderCallback: (bounds) {
            return const SweepGradient(
              colors: [
                Colors.red,
                Colors.orange,
                Colors.yellow,
                Colors.green,
                Colors.blue,
                Colors.purple,
                Colors.red,
              ],
            ).createShader(bounds);
          },
          child: const Icon(Icons.star, size: 60, color: Colors.white),
        ),
        ShaderMask(
          shaderCallback: (bounds) {
            return const LinearGradient(
              colors: [Colors.purple, Colors.pink],
              begin: Alignment.topLeft,
              end: Alignment.bottomRight,
            ).createShader(bounds);
          },
          child: const Icon(Icons.music_note, size: 60, color: Colors.white),
        ),
      ],
    );
  }
}
