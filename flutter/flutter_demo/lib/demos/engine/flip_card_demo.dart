import 'dart:math' as math;

import 'package:flutter/material.dart';

/// 3D 翻转卡片示例
///
/// 使用 [Matrix4] 透视投影 + 绕 Y 轴旋转，实现点击翻转的正反面卡片，
/// 展示 Flutter 的矩阵变换能力。
class FlipCardDemoPage extends StatelessWidget {
  const FlipCardDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return FlipCardDemoView(title: title);
  }
}

class FlipCardDemoView extends StatefulWidget {
  const FlipCardDemoView({super.key, required this.title});

  final String title;

  @override
  State<FlipCardDemoView> createState() => _FlipCardDemoViewState();
}

class _FlipCardDemoViewState extends State<FlipCardDemoView>
    with SingleTickerProviderStateMixin {
  late final AnimationController _controller = AnimationController(
    vsync: this,
    duration: const Duration(milliseconds: 600),
  );

  bool _showingFront = true;

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  void _toggle() {
    if (_showingFront) {
      _controller.forward();
    } else {
      _controller.reverse();
    }
    _showingFront = !_showingFront;
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(widget.title)),
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.all(16),
            child: Text(
              'Matrix4 透视投影 + rotateY，卡片绕 Y 轴 3D 翻转，点击切换正反面。',
              style: Theme.of(context).textTheme.bodyMedium,
            ),
          ),
          Expanded(
            child: Center(
              child: GestureDetector(
                onTap: _toggle,
                child: AnimatedBuilder(
                  animation: _controller,
                  builder: (BuildContext context, Widget? child) {
                    final double angle = _controller.value * math.pi;
                    final bool showBack = _controller.value >= 0.5;
                    return Transform(
                      transform: Matrix4.identity()
                        ..setEntry(3, 2, 0.001)
                        ..rotateY(angle),
                      alignment: Alignment.center,
                      child: showBack
                          ? Transform(
                              transform: Matrix4.identity()..rotateY(math.pi),
                              alignment: Alignment.center,
                              child: const _Card(back: true),
                            )
                          : const _Card(back: false),
                    );
                  },
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _Card extends StatelessWidget {
  const _Card({required this.back});

  final bool back;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 200,
      height: 280,
      decoration: BoxDecoration(
        color: back ? Colors.teal : Colors.indigo,
        borderRadius: BorderRadius.circular(20),
        boxShadow: const [
          BoxShadow(
            color: Colors.black26,
            blurRadius: 16,
            offset: Offset(0, 8),
          ),
        ],
      ),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(
            back ? Icons.check_circle : Icons.touch_app,
            size: 56,
            color: Colors.white,
          ),
          const SizedBox(height: 16),
          Text(
            back ? '背面' : '正面',
            style: const TextStyle(
              color: Colors.white,
              fontSize: 24,
              fontWeight: FontWeight.bold,
            ),
          ),
          const SizedBox(height: 8),
          const Text('点击翻转', style: TextStyle(color: Colors.white70)),
        ],
      ),
    );
  }
}
