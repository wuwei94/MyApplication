import 'dart:math' as math;

import 'package:flutter/material.dart';
import 'package:flutter/scheduler.dart';

/// 粒子系统示例
///
/// 基于 [Ticker] 帧循环驱动、[CustomPainter] 自绘渲染，
/// 演示触摸飞溅、重力下落、边界反弹与生命周期淡出。
class ParticleSystemDemoPage extends StatelessWidget {
  const ParticleSystemDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return ParticleSystemDemoView(title: title);
  }
}

/// 单个粒子的运动状态与视觉属性。
class _Particle {
  _Particle({
    required this.position,
    required this.velocity,
    required this.color,
    required this.radius,
    required this.maxLife,
  }) : life = maxLife;

  Offset position;
  Offset velocity;
  final Color color;
  final double radius;
  final double maxLife;
  double life;
}

class ParticleSystemDemoView extends StatefulWidget {
  const ParticleSystemDemoView({super.key, required this.title});

  final String title;

  @override
  State<ParticleSystemDemoView> createState() =>
      _ParticleSystemDemoViewState();
}

class _ParticleSystemDemoViewState extends State<ParticleSystemDemoView>
    with SingleTickerProviderStateMixin {
  static const double _gravity = 520;
  static const int _maxParticles = 600;

  final List<_Particle> _particles = <_Particle>[];
  final math.Random _random = math.Random();

  late final Ticker _ticker;
  Duration _lastElapsed = Duration.zero;
  Size _canvasSize = Size.zero;

  @override
  void initState() {
    super.initState();
    _ticker = createTicker(_onTick)..start();
  }

  @override
  void dispose() {
    _ticker.dispose();
    super.dispose();
  }

  void _onTick(Duration elapsed) {
    final double dt = ((elapsed - _lastElapsed).inMicroseconds / 1e6)
        .clamp(0.0, 1 / 30)
        .toDouble();
    _lastElapsed = elapsed;
    _updateParticles(dt);
    if (mounted) {
      setState(() {});
    }
  }

  void _updateParticles(double dt) {
    if (_canvasSize == Size.zero) {
      return;
    }
    for (int i = _particles.length - 1; i >= 0; i--) {
      final _Particle particle = _particles[i];
      particle.velocity = Offset(
        particle.velocity.dx,
        particle.velocity.dy + _gravity * dt,
      );
      particle.position = particle.position + particle.velocity * dt;
      particle.life -= dt;

      final double minX = particle.radius;
      final double maxX = _canvasSize.width - particle.radius;
      if (particle.position.dx < minX || particle.position.dx > maxX) {
        particle.velocity = Offset(
          -particle.velocity.dx * 0.82,
          particle.velocity.dy,
        );
        particle.position = Offset(
          particle.position.dx.clamp(minX, maxX).toDouble(),
          particle.position.dy,
        );
      }
      final double maxY = _canvasSize.height - particle.radius;
      if (particle.position.dy > maxY) {
        particle.velocity = Offset(
          particle.velocity.dx,
          -particle.velocity.dy * 0.7,
        );
        particle.position = Offset(particle.position.dx, maxY);
      }

      if (particle.life <= 0) {
        _particles.removeAt(i);
      }
    }
  }

  void _spawnBurst(Offset at, {required int count, required double maxSpeed}) {
    for (int i = 0; i < count; i++) {
      if (_particles.length >= _maxParticles) {
        _particles.removeAt(0);
      }
      final double angle = _random.nextDouble() * 2 * math.pi;
      final double speed = 40 + _random.nextDouble() * maxSpeed;
      final double life = 1.2 + _random.nextDouble() * 1.6;
      _particles.add(
        _Particle(
          position: at,
          velocity: Offset(math.cos(angle), math.sin(angle)) * speed,
          color: HSLColor.fromAHSL(
            1.0,
            _random.nextDouble() * 360,
            0.85,
            0.6,
          ).toColor(),
          radius: 1.5 + _random.nextDouble() * 3.2,
          maxLife: life,
        ),
      );
    }
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
              '在画布上点击或滑动，粒子会飞溅、受重力下落并在边界反弹。',
              style: Theme.of(context).textTheme.bodyMedium,
            ),
          ),
          Expanded(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: LayoutBuilder(
                builder: (BuildContext context, BoxConstraints constraints) {
                  _canvasSize = constraints.biggest;
                  return ClipRRect(
                    borderRadius: BorderRadius.circular(16),
                    child: DecoratedBox(
                      decoration: BoxDecoration(
                        color: Theme.of(context)
                            .colorScheme
                            .surfaceContainerHighest,
                      ),
                      child: RepaintBoundary(
                        child: GestureDetector(
                          behavior: HitTestBehavior.opaque,
                          onPanDown: (DragDownDetails details) {
                            _spawnBurst(
                              details.localPosition,
                              count: 26,
                              maxSpeed: 340,
                            );
                          },
                          onPanUpdate: (DragUpdateDetails details) {
                            _spawnBurst(
                              details.localPosition,
                              count: 4,
                              maxSpeed: 140,
                            );
                          },
                          child: SizedBox.expand(
                            child: CustomPaint(
                              painter: _ParticlePainter(particles: _particles),
                            ),
                          ),
                        ),
                      ),
                    ),
                  );
                },
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _ParticlePainter extends CustomPainter {
  const _ParticlePainter({required this.particles});

  final List<_Particle> particles;

  @override
  void paint(Canvas canvas, Size size) {
    final Paint paint = Paint();
    for (final _Particle particle in particles) {
      final double alpha =
          (particle.life / particle.maxLife).clamp(0.0, 1.0).toDouble();
      paint.color = particle.color.withValues(alpha: alpha);
      canvas.drawCircle(particle.position, particle.radius, paint);
    }
  }

  @override
  bool shouldRepaint(covariant _ParticlePainter oldDelegate) => true;
}
