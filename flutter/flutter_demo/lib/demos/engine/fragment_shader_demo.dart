import 'dart:async';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter/scheduler.dart';

/// Fragment Shader 示例
///
/// 通过 [ui.FragmentProgram] 运行时编译 GLSL 片段着色器，
/// 用 [Ticker] 驱动 uniform 时间参数，实现波纹扩散效果。
class FragmentShaderDemoPage extends StatelessWidget {
  const FragmentShaderDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return FragmentShaderDemoView(title: title);
  }
}

class FragmentShaderDemoView extends StatefulWidget {
  const FragmentShaderDemoView({super.key, required this.title});

  final String title;

  @override
  State<FragmentShaderDemoView> createState() => _FragmentShaderDemoViewState();
}

class _FragmentShaderDemoViewState extends State<FragmentShaderDemoView>
    with SingleTickerProviderStateMixin {
  late final Ticker _ticker;
  ui.FragmentShader? _shader;
  String? _error;
  double _time = 0;

  @override
  void initState() {
    super.initState();
    _ticker = createTicker(_onTick)..start();
    unawaited(_loadShader());
  }

  @override
  void dispose() {
    _shader?.dispose();
    _ticker.dispose();
    super.dispose();
  }

  void _onTick(Duration elapsed) {
    _time = elapsed.inMilliseconds / 1000;
    if (mounted) {
      setState(() {});
    }
  }

  Future<void> _loadShader() async {
    try {
      final ui.FragmentProgram program = await ui.FragmentProgram.fromAsset(
        'assets/shaders/water_ripple.frag',
      );
      if (!mounted) {
        return;
      }
      setState(() {
        _shader = program.fragmentShader();
      });
    } catch (error) {
      if (!mounted) {
        return;
      }
      setState(() {
        _error = error.toString();
      });
    }
  }

  Widget _buildPlaceholder() {
    final String? error = _error;
    if (error == null) {
      return const Center(child: CircularProgressIndicator());
    }
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: SelectableText(
          'Shader 加载失败：\n$error',
          style: Theme.of(context).textTheme.bodySmall,
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final ui.FragmentShader? shader = _shader;
    return Scaffold(
      appBar: AppBar(title: Text(widget.title)),
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.all(16),
            child: Text(
              '运行时编译 GLSL 片段着色器，用 Ticker 驱动 uTime 实现波纹扩散。',
              style: Theme.of(context).textTheme.bodyMedium,
            ),
          ),
          Expanded(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: ClipRRect(
                borderRadius: BorderRadius.circular(16),
                child: shader == null
                    ? _buildPlaceholder()
                    : SizedBox.expand(
                        child: CustomPaint(
                          painter: _ShaderPainter(shader: shader, time: _time),
                        ),
                      ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _ShaderPainter extends CustomPainter {
  const _ShaderPainter({required this.shader, required this.time});

  final ui.FragmentShader shader;
  final double time;

  @override
  void paint(Canvas canvas, Size size) {
    shader
      ..setFloat(0, size.width)
      ..setFloat(1, size.height)
      ..setFloat(2, time);
    canvas.drawRect(Offset.zero & size, Paint()..shader = shader);
  }

  @override
  bool shouldRepaint(covariant _ShaderPainter oldDelegate) =>
      oldDelegate.time != time;
}
