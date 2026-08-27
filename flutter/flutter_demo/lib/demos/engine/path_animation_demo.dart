import 'dart:math' as math;
import 'dart:ui' as ui;

import 'package:flutter/material.dart';

/// 沿路径动画示例
///
/// 使用 [ui.PathMetric] 测量路径长度，小球沿贝塞尔曲线运动，
/// 通过 [ui.Tangent] 获取切线角度决定朝向，并高亮已走过的轨迹。
class PathAnimationDemoPage extends StatelessWidget {
  const PathAnimationDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return PathAnimationDemoView(title: title);
  }
}

class PathAnimationDemoView extends StatefulWidget {
  const PathAnimationDemoView({super.key, required this.title});

  final String title;

  @override
  State<PathAnimationDemoView> createState() => _PathAnimationDemoViewState();
}

class _PathAnimationDemoViewState extends State<PathAnimationDemoView>
    with SingleTickerProviderStateMixin {
  late final AnimationController _controller = AnimationController(
    vsync: this,
    duration: const Duration(seconds: 3),
  );

  @override
  void initState() {
    super.initState();
    _controller.repeat();
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
        actions: [
          IconButton(
            onPressed: () => _controller.repeat(),
            icon: const Icon(Icons.replay),
            tooltip: '重播',
          ),
        ],
      ),
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.all(16),
            child: Text(
              'PathMetric 测量路径长度，小球沿贝塞尔曲线运动，'
              '切线决定其朝向，已走过的轨迹被高亮。',
              style: Theme.of(context).textTheme.bodyMedium,
            ),
          ),
          Expanded(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: SizedBox.expand(
                child: AnimatedBuilder(
                  animation: _controller,
                  builder: (BuildContext context, Widget? child) {
                    return CustomPaint(
                      painter: _PathPainter(progress: _controller.value),
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

class _PathPainter extends CustomPainter {
  const _PathPainter({required this.progress});

  final double progress;

  @override
  void paint(Canvas canvas, Size size) {
    final Path path = Path()
      ..moveTo(size.width * 0.1, size.height * 0.8)
      ..cubicTo(
        size.width * 0.25,
        size.height * 0.15,
        size.width * 0.6,
        size.height * 0.95,
        size.width * 0.9,
        size.height * 0.3,
      );

    final Paint basePaint = Paint()
      ..color = Colors.blueGrey.shade300
      ..style = PaintingStyle.stroke
      ..strokeWidth = 3;

    final Paint progressPaint = Paint()
      ..color = Colors.deepPurple
      ..style = PaintingStyle.stroke
      ..strokeWidth = 5
      ..strokeCap = StrokeCap.round;

    final ui.PathMetric metric = path.computeMetrics().first;
    final double distance = metric.length * progress;

    canvas.drawPath(path, basePaint);
    canvas.drawPath(metric.extractPath(0, distance), progressPaint);

    final ui.Tangent? tangent = metric.getTangentForOffset(distance);
    if (tangent != null) {
      final Offset position = tangent.position;
      final Offset direction = Offset(
        math.cos(tangent.angle),
        math.sin(tangent.angle),
      );

      canvas.drawCircle(position, 10, Paint()..color = Colors.redAccent);
      canvas.drawLine(
        position,
        position + direction * 24,
        Paint()
          ..color = Colors.redAccent
          ..strokeWidth = 3
          ..strokeCap = StrokeCap.round,
      );
    }
  }

  @override
  bool shouldRepaint(covariant _PathPainter oldDelegate) =>
      oldDelegate.progress != progress;
}
