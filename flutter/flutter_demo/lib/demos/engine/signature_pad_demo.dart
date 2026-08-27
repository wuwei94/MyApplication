import 'dart:async';
import 'dart:io';
import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:path_provider/path_provider.dart';

/// 手写签名板示例
///
/// 使用 [CustomPainter] 与二次贝塞尔曲线平滑笔迹，
/// 支持撤销/重做，并通过 [RenderRepaintBoundary.toImage] 离屏导出 PNG。
class SignaturePadDemoPage extends StatelessWidget {
  const SignaturePadDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return SignaturePadDemoView(title: title);
  }
}

class SignaturePadDemoView extends StatefulWidget {
  const SignaturePadDemoView({super.key, required this.title});

  final String title;

  @override
  State<SignaturePadDemoView> createState() => _SignaturePadDemoViewState();
}

class _SignaturePadDemoViewState extends State<SignaturePadDemoView> {
  final List<List<Offset>> _strokes = <List<Offset>>[];
  final List<List<Offset>> _redoStack = <List<Offset>>[];
  final GlobalKey _canvasKey = GlobalKey();
  List<Offset>? _activeStroke;

  bool get _canUndo => _strokes.isNotEmpty;

  bool get _canRedo => _redoStack.isNotEmpty;

  void _onPanStart(Offset position) {
    setState(() {
      _redoStack.clear();
      _activeStroke = <Offset>[position];
    });
  }

  void _onPanUpdate(Offset position) {
    final List<Offset>? active = _activeStroke;
    if (active == null) {
      return;
    }
    setState(() {
      active.add(position);
    });
  }

  void _onPanEnd() {
    final List<Offset>? active = _activeStroke;
    if (active == null) {
      return;
    }
    setState(() {
      _strokes.add(active);
      _activeStroke = null;
    });
  }

  void _undo() {
    setState(() {
      if (_strokes.isNotEmpty) {
        _redoStack.add(_strokes.removeLast());
      }
    });
  }

  void _redo() {
    setState(() {
      if (_redoStack.isNotEmpty) {
        _strokes.add(_redoStack.removeLast());
      }
    });
  }

  void _clear() {
    setState(() {
      _strokes.clear();
      _redoStack.clear();
      _activeStroke = null;
    });
  }

  Future<void> _exportPng() async {
    final RenderObject? renderObject =
        _canvasKey.currentContext?.findRenderObject();
    if (renderObject is! RenderRepaintBoundary) {
      return;
    }
    try {
      final ui.Image image = await renderObject.toImage(pixelRatio: 3);
      final ByteData? byteData =
          await image.toByteData(format: ui.ImageByteFormat.png);
      image.dispose();
      if (byteData == null) {
        return;
      }
      final Directory dir = await getTemporaryDirectory();
      final File file = File(
        '${dir.path}/signature_'
        '${DateTime.now().millisecondsSinceEpoch}.png',
      );
      await file.writeAsBytes(byteData.buffer.asUint8List());
      if (!mounted) {
        return;
      }
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('已导出 PNG：${file.path}')),
      );
    } catch (_) {
      if (!mounted) {
        return;
      }
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('导出失败')),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final ColorScheme colorScheme = Theme.of(context).colorScheme;
    return Scaffold(
      appBar: AppBar(title: Text(widget.title)),
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.all(16),
            child: Text(
              '在下方画布上手写签名，支持撤销、重做与导出 PNG。',
              style: Theme.of(context).textTheme.bodyMedium,
            ),
          ),
          Expanded(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: ClipRRect(
                borderRadius: BorderRadius.circular(16),
                child: DecoratedBox(
                  decoration: BoxDecoration(
                    color: colorScheme.surfaceContainerHighest,
                  ),
                  child: RepaintBoundary(
                    key: _canvasKey,
                    child: GestureDetector(
                      behavior: HitTestBehavior.opaque,
                      onPanStart: (DragStartDetails details) =>
                          _onPanStart(details.localPosition),
                      onPanUpdate: (DragUpdateDetails details) =>
                          _onPanUpdate(details.localPosition),
                      onPanEnd: (DragEndDetails details) => _onPanEnd(),
                      child: SizedBox.expand(
                        child: CustomPaint(
                          painter: _SignaturePainter(
                            strokes: _strokes,
                            activeStroke: _activeStroke,
                            color: colorScheme.onSurface,
                          ),
                        ),
                      ),
                    ),
                  ),
                ),
              ),
            ),
          ),
          Padding(
            padding: const EdgeInsets.all(16),
            child: Row(
              children: [
                Expanded(
                  child: OutlinedButton.icon(
                    onPressed: _canUndo ? _undo : null,
                    icon: const Icon(Icons.undo),
                    label: const Text('撤销'),
                  ),
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: OutlinedButton.icon(
                    onPressed: _canRedo ? _redo : null,
                    icon: const Icon(Icons.redo),
                    label: const Text('重做'),
                  ),
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: OutlinedButton.icon(
                    onPressed: _clear,
                    icon: const Icon(Icons.delete_outline),
                    label: const Text('清空'),
                  ),
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: FilledButton.icon(
                    onPressed: () => unawaited(_exportPng()),
                    icon: const Icon(Icons.save_alt),
                    label: const Text('导出'),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class _SignaturePainter extends CustomPainter {
  const _SignaturePainter({
    required this.strokes,
    required this.activeStroke,
    required this.color,
  });

  final List<List<Offset>> strokes;
  final List<Offset>? activeStroke;
  final Color color;

  @override
  void paint(Canvas canvas, Size size) {
    final Paint paint = Paint()
      ..color = color
      ..strokeWidth = 3
      ..strokeCap = StrokeCap.round
      ..strokeJoin = StrokeJoin.round
      ..style = PaintingStyle.stroke;

    for (final List<Offset> stroke in strokes) {
      _drawStroke(canvas, stroke, paint);
    }
    final List<Offset>? active = activeStroke;
    if (active != null) {
      _drawStroke(canvas, active, paint);
    }
  }

  void _drawStroke(Canvas canvas, List<Offset> points, Paint paint) {
    if (points.isEmpty) {
      return;
    }
    if (points.length == 1) {
      canvas.drawCircle(points.first, paint.strokeWidth / 2, paint);
      return;
    }
    final Path path = Path()..moveTo(points.first.dx, points.first.dy);
    for (int i = 1; i < points.length - 1; i++) {
      final Offset mid = Offset(
        (points[i].dx + points[i + 1].dx) / 2,
        (points[i].dy + points[i + 1].dy) / 2,
      );
      path.quadraticBezierTo(
        points[i].dx,
        points[i].dy,
        mid.dx,
        mid.dy,
      );
    }
    path.lineTo(points.last.dx, points.last.dy);
    canvas.drawPath(path, paint);
  }

  @override
  bool shouldRepaint(covariant _SignaturePainter oldDelegate) => true;
}
