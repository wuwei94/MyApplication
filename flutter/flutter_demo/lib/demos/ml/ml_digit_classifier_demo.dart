import 'dart:math' as math;

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';
import 'package:flutter_demo/demos/ml/widgets/finger_draw_widget.dart';
import 'package:tflite_flutter/tflite_flutter.dart';

/// MNIST — 手写数字识别
///
/// 核心机制与避坑点：
/// 1. 手写预处理：`export28x28Grayscale` 外接框居中缩放为 MNIST 标准 28x28 灰度。
/// 2. 推理契约：Float32 张量直写后 `invoke`，Softmax 归一化得到 0~9 置信度。
/// 3. 画布交互：预览区仅承载手写绘制，识别 / 清空一律走下方操作列表。
///
/// 官方参考：
/// https://www.tensorflow.org/lite/examples/digit_classification/overview
class MlDigitClassifierDemoPage extends BasicImagePage {
  const MlDigitClassifierDemoPage({super.key, required super.title});

  @override
  BasicImagePageState<MlDigitClassifierDemoPage> createState() =>
      _MlDigitClassifierDemoPageState();
}

class _MlDigitClassifierDemoPageState
    extends BasicImagePageState<MlDigitClassifierDemoPage> {
  final GlobalKey<FingerDrawWidgetState> _drawKey =
      GlobalKey<FingerDrawWidgetState>();

  Interpreter? _interpreter;
  bool _isModelLoaded = false;
  int _predictedDigit = -1;
  double _confidence = 0.0;
  double _latencyMs = 0.0;
  List<double> _probabilities = List<double>.filled(10, 0.0);

  @override
  void initState() {
    super.initState();
    _loadModel();
  }

  @override
  void dispose() {
    _interpreter?.close();
    super.dispose();
  }

  @override
  List<String> buildList() => const <String>[
        '1. 识别手写数字',
        '2. 清空画板',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        _runClassification();
      case 1:
        _clearCanvas();
    }
  }

  @override
  Widget buildPreview() {
    return Padding(
      padding: const EdgeInsets.all(BasicDemoDimens.pagePadding),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: <Widget>[
          const Text(
            '在黑色区域绘制 0~9，抬笔后自动识别，也可点击下方操作项手动识别',
            style: TextStyle(fontSize: BasicDemoDimens.fontTitle),
          ),
          const SizedBox(height: BasicDemoDimens.itemPadding),
          AspectRatio(
            aspectRatio: 1,
            child: ClipRRect(
              borderRadius: BorderRadius.circular(BasicDemoDimens.cornerCard),
              child: FingerDrawWidget(
                key: _drawKey,
                onStrokeFinished: _runClassification,
              ),
            ),
          ),
          const SizedBox(height: BasicDemoDimens.itemPadding),
          Expanded(child: _buildResultPanel()),
        ],
      ),
    );
  }

  Widget _buildResultPanel() {
    final String digitLabel = _predictedDigit >= 0 ? '$_predictedDigit' : '?';
    final String confidenceLabel = _predictedDigit >= 0
        ? '置信度 ${(_confidence * 100).toStringAsFixed(1)}%'
        : '等待书写';

    return Row(
      children: <Widget>[
        SizedBox(
          width: 96,
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: <Widget>[
              Text(
                digitLabel,
                style: TextStyle(
                  fontSize: 40,
                  fontWeight: FontWeight.bold,
                  color: _predictedDigit >= 0
                      ? Colors.blue.shade800
                      : Colors.grey,
                ),
              ),
              Text(
                confidenceLabel,
                style: const TextStyle(
                  fontSize: BasicDemoDimens.fontBody,
                  color: Colors.blueGrey,
                ),
              ),
              if (_latencyMs > 0)
                Text(
                  '${_latencyMs.toStringAsFixed(2)} ms',
                  style: const TextStyle(
                    fontSize: BasicDemoDimens.fontBody,
                    color: Colors.green,
                  ),
                ),
            ],
          ),
        ),
        const SizedBox(width: BasicDemoDimens.itemPadding),
        Expanded(
          child: ListView.builder(
            itemCount: 10,
            itemBuilder: (BuildContext context, int digit) {
              final double prob = _probabilities[digit];
              final bool isTop =
                  digit == _predictedDigit && _predictedDigit >= 0;
              return Padding(
                padding: const EdgeInsets.symmetric(vertical: 1),
                child: Row(
                  children: <Widget>[
                    SizedBox(
                      width: 18,
                      child: Text(
                        '$digit',
                        style: TextStyle(
                          fontSize: BasicDemoDimens.fontBody,
                          fontWeight:
                              isTop ? FontWeight.bold : FontWeight.normal,
                        ),
                      ),
                    ),
                    const SizedBox(width: 4),
                    Expanded(
                      child: ClipRRect(
                        borderRadius: BorderRadius.circular(4),
                        child: LinearProgressIndicator(
                          value: prob,
                          minHeight: 6,
                          backgroundColor: Colors.grey.shade200,
                          valueColor: AlwaysStoppedAnimation<Color>(
                            isTop ? Colors.blue : Colors.blue.shade200,
                          ),
                        ),
                      ),
                    ),
                    const SizedBox(width: 6),
                    SizedBox(
                      width: 44,
                      child: Text(
                        '${(prob * 100).toStringAsFixed(1)}%',
                        textAlign: TextAlign.end,
                        style: TextStyle(
                          fontSize: BasicDemoDimens.fontBody,
                          fontFamily: 'monospace',
                          fontWeight:
                              isTop ? FontWeight.bold : FontWeight.normal,
                        ),
                      ),
                    ),
                  ],
                ),
              );
            },
          ),
        ),
      ],
    );
  }

  Future<void> _loadModel() async {
    try {
      final Interpreter interpreter = await Interpreter.fromAsset(
        'assets/ml/mnist.tflite',
        options: InterpreterOptions()..threads = 2,
      );
      interpreter.allocateTensors();
      if (!mounted) return;
      setState(() {
        _interpreter = interpreter;
        _isModelLoaded = true;
      });
    } catch (error) {
      debugPrint('加载 MNIST 模型失败: $error');
    }
  }

  Future<void> _runClassification() async {
    final Interpreter? interpreter = _interpreter;
    if (interpreter == null || !_isModelLoaded) return;

    final Float32List? inputBuffer =
        await _drawKey.currentState?.export28x28Grayscale();
    if (inputBuffer == null) return;

    try {
      final Stopwatch stopwatch = Stopwatch()..start();
      interpreter.getInputTensor(0).data = inputBuffer.buffer.asUint8List();
      interpreter.invoke();
      final Uint8List rawOutput = interpreter.getOutputTensor(0).data;
      final Float32List rawScores = rawOutput.buffer.asFloat32List();
      stopwatch.stop();

      final double maxScore = rawScores.reduce(math.max);
      final List<double> expScores =
          rawScores.map((double s) => math.exp(s - maxScore)).toList();
      final double sumExp = expScores.reduce((double a, double b) => a + b);
      final List<double> probs = expScores
          .map((double e) => e / (sumExp == 0 ? 1.0 : sumExp))
          .toList();

      int maxIdx = 0;
      double maxProb = probs[0];
      for (int i = 1; i < probs.length; i++) {
        if (probs[i] > maxProb) {
          maxProb = probs[i];
          maxIdx = i;
        }
      }

      if (!mounted) return;
      setState(() {
        _predictedDigit = maxIdx;
        _confidence = maxProb;
        _latencyMs = stopwatch.elapsedMicroseconds / 1000.0;
        _probabilities = probs;
      });
    } catch (error) {
      debugPrint('手写识别异常: $error');
    }
  }

  void _clearCanvas() {
    _drawKey.currentState?.clear();
    setState(() {
      _predictedDigit = -1;
      _confidence = 0.0;
      _latencyMs = 0.0;
      _probabilities = List<double>.filled(10, 0.0);
    });
  }
}
