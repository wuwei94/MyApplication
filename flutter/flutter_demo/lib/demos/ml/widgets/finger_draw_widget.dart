import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';

/// 触摸涂鸦手写板组件
///
/// 支持平滑笔迹绘制、一键清屏与自动渲染导出 28x28 RGB 张量数据。
///
/// 导出契约与 Android `FingerDrawView.exportScaledBitmap` 对齐：
/// **整图拉伸**到 28x28，不做外接框裁剪 / 居中重排（与官方 sample 一致）；
/// 取值 0~255 不归一化（`digit_classifier.tflite` 的输入规格）。
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

  /// 将当前手写笔迹光栅化并**整图拉伸**到 28x28，导出交错 RGB Float32 数组
  ///
  /// 长度为 `28 * 28 * 3`，取值 0~255（不除 255）—— 与 Android 页
  /// `InputNormalize.RAW_0_255` 的写入口径一致。画布为白字黑底，
  /// 三个通道取值相同，仍按 RGB 交错写入以匹配模型 `[1, 28, 28, 3]` 的输入签名。
  Future<Float32List?> export28x28Rgb() async {
    if (_strokes.isEmpty) return null;
    final Size? size = context.size;
    if (size == null || size.width <= 0 || size.height <= 0) return null;

    final ui.PictureRecorder recorder = ui.PictureRecorder();
    final Canvas canvas = Canvas(recorder);

    // 整图拉伸：把整个画布坐标空间等比映射到 28x28（不裁剪、不居中重排）
    canvas.scale(28.0 / size.width, 28.0 / size.height);

    // 背景全黑
    final Paint bgPaint = Paint()..color = Colors.black;
    canvas.drawRect(Offset.zero & size, bgPaint);

    final Paint strokePaint = Paint()
      ..color = Colors.white
      ..strokeCap = StrokeCap.round
      ..strokeJoin = StrokeJoin.round
      ..strokeWidth = 18.0
      ..style = PaintingStyle.stroke;

    for (final List<Offset> stroke in _strokes) {
      if (stroke.length == 1) {
        canvas.drawCircle(
          stroke.first,
          9.0,
          strokePaint..style = PaintingStyle.fill,
        );
        strokePaint.style = PaintingStyle.stroke;
      } else {
        final Path path = Path()..moveTo(stroke.first.dx, stroke.first.dy);
        for (int i = 1; i < stroke.length; i++) {
          path.lineTo(stroke[i].dx, stroke[i].dy);
        }
        canvas.drawPath(path, strokePaint);
      }
    }

    final ui.Picture picture = recorder.endRecording();
    final ui.Image image = await picture.toImage(28, 28);
    final ByteData? byteData =
        await image.toByteData(format: ui.ImageByteFormat.rawRgba);
    image.dispose();
    if (byteData == null) return null;

    // 白字黑底 → R = G = B，取 R 通道按 RGB 交错展开
    final Float32List buffer = Float32List(28 * 28 * 3);
    for (int i = 0; i < 28 * 28; i++) {
      final double value = byteData.getUint8(i * 4).toDouble();
      buffer[i * 3] = value;
      buffer[i * 3 + 1] = value;
      buffer[i * 3 + 2] = value;
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
