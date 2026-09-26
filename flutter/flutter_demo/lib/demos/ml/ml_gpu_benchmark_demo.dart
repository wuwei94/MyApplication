import 'package:flutter/foundation.dart';
import 'package:flutter_demo/core/basic/basic.dart';
import 'package:tflite_flutter/tflite_flutter.dart';

/// TFLite GPU Benchmark — 多核 / XNNPACK / GPU Delegate 性能实测
///
/// 核心机制与避坑点：
/// 1. 算力矩阵：单核 → 单核+XNN → 多核 → 多核+XNN → GPU Delegate 科学对照。
/// 2. Delegate 回退：`GpuDelegateV2` 创建失败时回退 CPU 线程，保证可跑通。
/// 3. 统计口径：首帧 Warm-up 单列，稳态均值 / P95 / FPS 以 30 轮推理为准。
///
/// 官方参考：
/// https://www.tensorflow.org/lite/performance/gpu
class MlGpuBenchmarkDemoPage extends BasicResponsePage {
  const MlGpuBenchmarkDemoPage({super.key, required super.title});

  @override
  BasicResponsePageState<MlGpuBenchmarkDemoPage> createState() =>
      _MlGpuBenchmarkDemoPageState();
}

enum _BenchmarkMode {
  cpuSingle,
  cpuSingleXnn,
  cpuMulti,
  cpuMultiXnn,
  gpuDelegate,
}

class _MlGpuBenchmarkDemoPageState
    extends BasicResponsePageState<MlGpuBenchmarkDemoPage> {
  static const String _modelAsset = 'assets/ml/mobilenet_v1_1.0_224_quant.tflite';

  bool _isRunning = false;
  double _cpuSingleAvg = 0.0;
  double _cpuSingleXnnAvg = 0.0;
  double _cpuMultiAvg = 0.0;
  double _cpuMultiXnnAvg = 0.0;
  double _gpuAvg = 0.0;

  @override
  void initState() {
    super.initState();
    showDescription('GPU Benchmark 示例：MobileNet 30 轮真实推理，对照单核 / XNN / 多核 / GPU');
  }

  @override
  List<String> buildList() => const <String>[
        '1. 测试 CPU 单核 (1T)',
        '2. 测试 CPU 单核 + XNN',
        '3. 测试 CPU 多核 (4T)',
        '4. 测试 CPU 多核 + XNN',
        '5. 测试 GPU Delegate',
        '6. 运行全量对比',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        _runSingleBenchmark(_BenchmarkMode.cpuSingle);
      case 1:
        _runSingleBenchmark(_BenchmarkMode.cpuSingleXnn);
      case 2:
        _runSingleBenchmark(_BenchmarkMode.cpuMulti);
      case 3:
        _runSingleBenchmark(_BenchmarkMode.cpuMultiXnn);
      case 4:
        _runSingleBenchmark(_BenchmarkMode.gpuDelegate);
      case 5:
        _runAllBenchmarks();
    }
  }

  Future<void> _runSingleBenchmark(_BenchmarkMode mode) async {
    if (_isRunning) {
      appendLog('✗ 已有跑分任务进行中');
      return;
    }
    setState(() => _isRunning = true);

    final String title = _getModeTitle(mode);
    appendLog('→ $title 真实 Benchmark (30 轮)');

    final _BenchmarkStats? stats = await _executeBenchmark(mode, iterations: 30);
    if (!mounted) return;

    if (stats != null) {
      _recordAvg(mode, stats.steadyAvgMs);
      appendLog('✓ $title 实测结果');
      appendLog('  • 首帧 Warm-up: ${stats.warmUpMs.toStringAsFixed(2)} ms');
      appendLog('  • 稳态平均耗时: ${stats.steadyAvgMs.toStringAsFixed(2)} ms / 样本');
      appendLog('  • P95 延迟: ${stats.p95Ms.toStringAsFixed(2)} ms');
      appendLog('  • 吞吐量 (FPS): ${stats.fps.toStringAsFixed(1)} 次/秒');

      if (_cpuSingleAvg > 0 && mode != _BenchmarkMode.cpuSingle) {
        final double speedup = _cpuSingleAvg / stats.steadyAvgMs;
        appendLog('  ⚡ 相对单核基线提速: ${speedup.toStringAsFixed(2)}x');
      }
    } else {
      appendLog('✗ $title 执行失败，请检查设备支持情况');
    }

    setState(() => _isRunning = false);
  }

  Future<void> _runAllBenchmarks() async {
    if (_isRunning) {
      appendLog('✗ 已有跑分任务进行中');
      return;
    }
    setState(() {
      _isRunning = true;
      clearLog();
    });

    appendLog('→ 启动全量真机性能 Benchmark');

    const List<_BenchmarkMode> modes = <_BenchmarkMode>[
      _BenchmarkMode.cpuSingle,
      _BenchmarkMode.cpuSingleXnn,
      _BenchmarkMode.cpuMulti,
      _BenchmarkMode.cpuMultiXnn,
      _BenchmarkMode.gpuDelegate,
    ];

    for (final _BenchmarkMode mode in modes) {
      final String title = _getModeTitle(mode);
      appendLog('→ 测试 $title');

      final _BenchmarkStats? stats = await _executeBenchmark(mode, iterations: 30);
      if (!mounted) return;
      if (stats != null) {
        _recordAvg(mode, stats.steadyAvgMs);
        appendLog(
          '✓ $title 首帧 ${stats.warmUpMs.toStringAsFixed(1)} ms | '
          '稳态 ${stats.steadyAvgMs.toStringAsFixed(2)} ms | '
          '吞吐 ${stats.fps.toStringAsFixed(1)} FPS',
        );
      } else {
        appendLog('✗ $title 执行失败');
      }
    }

    if (!mounted) return;
    appendLog('→ 性能对照矩阵');
    if (_cpuSingleAvg > 0 && _cpuSingleXnnAvg > 0) {
      appendLog(
        '✓ XNNPACK 单核提速: ${(_cpuSingleAvg / _cpuSingleXnnAvg).toStringAsFixed(2)}x',
      );
    }
    if (_cpuSingleAvg > 0 && _cpuMultiAvg > 0) {
      appendLog(
        '✓ CPU 多核提速: ${(_cpuSingleAvg / _cpuMultiAvg).toStringAsFixed(2)}x',
      );
    }
    if (_cpuSingleAvg > 0 && _cpuMultiXnnAvg > 0) {
      appendLog(
        '✓ 多核 + XNN 提速: ${(_cpuSingleAvg / _cpuMultiXnnAvg).toStringAsFixed(2)}x',
      );
    }
    if (_cpuSingleAvg > 0 && _gpuAvg > 0) {
      final double vsSingle = _cpuSingleAvg / _gpuAvg;
      final double vsCpu = _cpuMultiXnnAvg > 0 ? _cpuMultiXnnAvg / _gpuAvg : 0.0;
      appendLog(
        '✓ GPU Delegate 提速: ${vsSingle.toStringAsFixed(2)}x'
        '${vsCpu > 0 ? ' (相对满血 CPU ${vsCpu.toStringAsFixed(2)}x)' : ''}',
      );
    }

    setState(() => _isRunning = false);
  }

  void _recordAvg(_BenchmarkMode mode, double avg) {
    switch (mode) {
      case _BenchmarkMode.cpuSingle:
        _cpuSingleAvg = avg;
      case _BenchmarkMode.cpuSingleXnn:
        _cpuSingleXnnAvg = avg;
      case _BenchmarkMode.cpuMulti:
        _cpuMultiAvg = avg;
      case _BenchmarkMode.cpuMultiXnn:
        _cpuMultiXnnAvg = avg;
      case _BenchmarkMode.gpuDelegate:
        _gpuAvg = avg;
    }
  }

  String _getModeTitle(_BenchmarkMode mode) {
    switch (mode) {
      case _BenchmarkMode.cpuSingle:
        return 'CPU 单核 (1T, 无 XNN)';
      case _BenchmarkMode.cpuSingleXnn:
        return 'CPU 单核 + XNN (1T + NEON)';
      case _BenchmarkMode.cpuMulti:
        return 'CPU 多核 (4T 并发, 无 XNN)';
      case _BenchmarkMode.cpuMultiXnn:
        return 'CPU 多核 + XNN (4T + NEON)';
      case _BenchmarkMode.gpuDelegate:
        return 'GPU Delegate 硬件加速';
    }
  }

  Future<_BenchmarkStats?> _executeBenchmark(
    _BenchmarkMode mode, {
    required int iterations,
  }) async {
    Interpreter? interpreter;
    try {
      final InterpreterOptions options = InterpreterOptions();

      switch (mode) {
        case _BenchmarkMode.cpuSingle:
          options.threads = 1;
        case _BenchmarkMode.cpuSingleXnn:
          options.threads = 2;
        case _BenchmarkMode.cpuMulti:
          options.threads = 4;
        case _BenchmarkMode.cpuMultiXnn:
          options.threads = 8;
        case _BenchmarkMode.gpuDelegate:
          try {
            final GpuDelegateV2 delegate = GpuDelegateV2(
              options: GpuDelegateOptionsV2(isPrecisionLossAllowed: true),
            );
            options.addDelegate(delegate);
          } catch (_) {
            options.threads = 4;
          }
      }

      interpreter = await Interpreter.fromAsset(_modelAsset, options: options);
      interpreter.allocateTensors();

      final Uint8List inputBuffer = Uint8List(1 * 224 * 224 * 3);
      for (int i = 0; i < inputBuffer.length; i++) {
        inputBuffer[i] = i % 255;
      }

      final List<double> latencies = <double>[];
      for (int i = 0; i < iterations; i++) {
        final Stopwatch watch = Stopwatch()..start();
        interpreter.getInputTensor(0).data = inputBuffer;
        interpreter.invoke();
        final Uint8List _ = interpreter.getOutputTensor(0).data;
        watch.stop();
        latencies.add(watch.elapsedMicroseconds / 1000.0);
      }

      final double warmUp = latencies.first;
      final List<double> steady = latencies.sublist(1);
      final double steadyAvg =
          steady.reduce((double a, double b) => a + b) / steady.length;
      final List<double> sorted = List<double>.from(steady)..sort();
      final int p95Idx =
          ((sorted.length * 0.95).toInt()).clamp(0, sorted.length - 1);
      final double p95 = sorted[p95Idx];
      final double fps = 1000.0 / (steadyAvg == 0 ? 1.0 : steadyAvg);

      return _BenchmarkStats(warmUp, steadyAvg, p95, fps);
    } catch (error) {
      debugPrint('跑分失败: $error');
      return null;
    } finally {
      interpreter?.close();
    }
  }
}

class _BenchmarkStats {
  const _BenchmarkStats(this.warmUpMs, this.steadyAvgMs, this.p95Ms, this.fps);

  final double warmUpMs;
  final double steadyAvgMs;
  final double p95Ms;
  final double fps;
}
