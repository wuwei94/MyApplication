import 'dart:typed_data';
import 'dart:ui' as ui;
import 'package:flutter/material.dart';

/// 触摸涂鸦手写板组件
///
/// 支持平滑笔迹绘制、一键清屏与自动渲染导出 28x28 灰度张量数据
class FingerDrawWidget extends StatefulWidget {
  final VoidCallback? onStrokeFinished;

  const FingerDrawWidget({
    super.key,
    this.onStrokeFinished,
  });

  @override
  FingerDrawWidgetState createState() => FingerDrawWidgetState();
}

class FingerDrawWidgetState extends State<FingerDrawWidget> {
  final List<List<Offset>> _strokes = <List<Offset>>[];
  List<Offset>? _currentStroke;

  /// 清空画板
  void clear() {
    setState(() {
      _strokes.clear();
      _currentStroke = null;
    });
  }

  /// 是否有笔迹
  bool get hasStrokes => _strokes.isNotEmpty;

  /// 将当前手写笔迹光栅化并缩放到 28x28 灰度 Float32 数组 [1, 28, 28, 1]
  Future<Float32List?> export28x28Grayscale() async {
    if (_strokes.isEmpty) return null;

    final ui.PictureRecorder recorder = ui.PictureRecorder();
    final Canvas canvas = Canvas(recorder, const Rect.fromLTWH(0, 0, 28, 28));

    // 背景全黑
    final Paint bgPaint = Paint()..color = Colors.black;
    canvas.drawRect(const Rect.fromLTWH(0, 0, 28, 28), bgPaint);

    // 计算当前笔迹的外接矩形（Bounding Box）并居中等比缩放到 20x20（留 4px 边距，符合 MNIST 标准）
    double minX = double.infinity, minY = double.infinity;
    double maxX = -double.infinity, maxY = -double.infinity;

    for (final List<Offset> stroke in _strokes) {
      for (final Offset point in stroke) {
        if (point.dx < minX) minX = point.dx;
        if (point.dy < minY) minY = point.dy;
        if (point.dx > maxX) maxX = point.dx;
        if (point.dy > maxY) maxY = point.dy;
      }
    }

    final double strokeWidth = maxX - minX;
    final double strokeHeight = maxY - minY;

    if (strokeWidth > 0 && strokeHeight > 0) {
      final double maxDim = strokeWidth > strokeHeight ? strokeWidth : strokeHeight;
      final double scale = 20.0 / maxDim;
      final double targetCenterX = 14.0;
      final double targetCenterY = 14.0;
      final double currentCenterX = (minX + maxX) / 2.0;
      final double currentCenterY = (minY + maxY) / 2.0;

      canvas.save();
      canvas.translate(
        targetCenterX - currentCenterX * scale,
        targetCenterY - currentCenterY * scale,
      );
      canvas.scale(scale, scale);

      final Paint strokePaint = Paint()
        ..color = Colors.white
        ..strokeCap = StrokeCap.round
        ..strokeJoin = StrokeJoin.round
        ..strokeWidth = (2.4 / scale).clamp(1.5, 3.5)
        ..style = PaintingStyle.stroke;

      for (final List<Offset> stroke in _strokes) {
        if (stroke.length == 1) {
          canvas.drawCircle(stroke.first, 1.2 / scale, strokePaint..style = PaintingStyle.fill);
          strokePaint.style = PaintingStyle.stroke;
        } else {
          final Path path = Path()..moveTo(stroke.first.dx, stroke.first.dy);
          for (int i = 1; i < stroke.length; i++) {
            path.lineTo(stroke[i].dx, stroke[i].dy);
          }
          canvas.drawPath(path, strokePaint);
        }
      }
      canvas.restore();
    }

    final ui.Picture picture = recorder.endRecording();
    final ui.Image img = await picture.toImage(28, 28);
    final ByteData? byteData = await img.toByteData(format: ui.ImageByteFormat.rawRgba);

    if (byteData == null) return null;

    final Float32List buffer = Float32List(28 * 28);
    for (int i = 0; i < 28 * 28; i++) {
      final int offset = i * 4;
      final int r = byteData.getUint8(offset);
      final int g = byteData.getUint8(offset + 1);
      final int b = byteData.getUint8(offset + 2);
      // 灰度公式 Y = 0.299R + 0.587G + 0.114B，并归一化到 [0.0, 1.0]
      buffer[i] = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0;
    }

    return buffer;
  }

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onPanStart: (DragStartDetails details) {
        setState(() {
          _currentStroke = <Offset>[details.localPosition];
          _strokes.add(_currentStroke!);
        });
      },
      onPanUpdate: (DragUpdateDetails details) {
        setState(() {
          _currentStroke?.add(details.localPosition);
        });
      },
      onPanEnd: (DragEndDetails details) {
        _currentStroke = null;
        widget.onStrokeFinished?.call();
      },
      child: ClipRRect(
        borderRadius: BorderRadius.circular(12),
        child: CustomPaint(
          size: Size.infinite,
          painter: _CanvasPainter(_strokes),
        ),
      ),
    );
  }
}

class _CanvasPainter extends CustomPainter {
  final List<List<Offset>> strokes;

  _CanvasPainter(this.strokes);

  @override
  void paint(Canvas canvas, Size size) {
    // 黑色背景
    canvas.drawRect(
      Offset.zero & size,
      Paint()..color = const Color(0xFF1E1E1E),
    );

    final Paint paint = Paint()
      ..color = Colors.white
      ..strokeCap = StrokeCap.round
      ..strokeJoin = StrokeJoin.round
      ..strokeWidth = 18.0
      ..style = PaintingStyle.stroke;

    for (final List<Offset> stroke in strokes) {
      if (stroke.length == 1) {
        canvas.drawCircle(stroke.first, 9.0, paint..style = PaintingStyle.fill);
        paint.style = PaintingStyle.stroke;
      } else {
        final Path path = Path()..moveTo(stroke.first.dx, stroke.first.dy);
        for (int i = 1; i < stroke.length; i++) {
          path.lineTo(stroke[i].dx, stroke[i].dy);
        }
        canvas.drawPath(path, paint);
      }
    }
  }

  @override
  bool shouldRepaint(covariant _CanvasPainter oldDelegate) => true;
}
