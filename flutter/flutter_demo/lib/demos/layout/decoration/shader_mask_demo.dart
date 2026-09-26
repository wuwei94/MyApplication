import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';

/// ShaderMask
/// Demonstrates gradient masks and shader effects
class ShaderMaskDemoPage extends BasicLayoutPage {
  const ShaderMaskDemoPage({super.key, required super.title});

  @override
  BasicLayoutPageState<ShaderMaskDemoPage> createState() =>
      _ShaderMaskDemoPageState();
}

class _ShaderMaskDemoPageState extends BasicLayoutPageState<ShaderMaskDemoPage> {
  @override
  Widget buildPreview() {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(BasicDemoDimens.pagePadding),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          _buildSectionTitle('Gradient Text'),
          _buildGradientText(),
          const SizedBox(height: BasicDemoDimens.sectionGap),
          _buildSectionTitle('Gradient Image'),
          _buildGradientImage(),
          const SizedBox(height: BasicDemoDimens.sectionGap),
          _buildSectionTitle('Fade Edge Effect'),
          _buildFadeEdge(),
          const SizedBox(height: BasicDemoDimens.sectionGap),
          _buildSectionTitle('Mask with Icon'),
          _buildIconMask(),
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
      colors: <Color>[Colors.blue, Colors.purple, Colors.pink],
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
      colors: <Color>[Colors.yellow, Colors.orange, Colors.red],
      center: Alignment.center,
    ).createShader(bounds);
  }

  Widget _buildFadeEdge() {
    return Container(
      height: 100,
      decoration: BoxDecoration(
        color: Colors.blue.shade100,
        borderRadius: BorderRadius.circular(BasicDemoDimens.cornerSmall),
      ),
      child: ShaderMask(
        shaderCallback: (Rect bounds) {
          return const LinearGradient(
            colors: <Color>[
              Colors.transparent,
              Colors.black,
              Colors.black,
              Colors.transparent,
            ],
            stops: <double>[0.0, 0.2, 0.8, 1.0],
          ).createShader(bounds);
        },
        blendMode: BlendMode.dstIn,
        child: ListView(
          scrollDirection: Axis.horizontal,
          children: List<Widget>.generate(
            10,
            (int index) => Container(
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
      children: <Widget>[
        ShaderMask(
          shaderCallback: (Rect bounds) {
            return const LinearGradient(
              colors: <Color>[Colors.green, Colors.blue],
            ).createShader(bounds);
          },
          child: const Icon(Icons.favorite, size: 60, color: Colors.white),
        ),
        ShaderMask(
          shaderCallback: (Rect bounds) {
            return const SweepGradient(
              colors: <Color>[
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
          shaderCallback: (Rect bounds) {
            return const LinearGradient(
              colors: <Color>[Colors.purple, Colors.pink],
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
