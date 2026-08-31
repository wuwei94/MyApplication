import 'dart:math' as math;
import 'dart:typed_data';
import 'package:flutter/material.dart';
import 'package:flutter_demo/demos/ml/widgets/finger_draw_widget.dart';
import 'package:tflite_flutter/tflite_flutter.dart';

/// MNIST 手写数字实时识别演示
///
/// 官方文档: https://www.tensorflow.org/lite/examples/digit_classification/overview
class MlDigitClassifierDemoPage extends StatefulWidget {
  final String title;

  const MlDigitClassifierDemoPage({
    super.key,
    required this.title,
  });

  @override
  State<MlDigitClassifierDemoPage> createState() =>
      _MlDigitClassifierDemoPageState();
}

class _MlDigitClassifierDemoPageState extends State<MlDigitClassifierDemoPage> {
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

  Future<void> _loadModel() async {
    try {
      final Interpreter interpreter = await Interpreter.fromAsset(
        'assets/ml/mnist.tflite',
        options: InterpreterOptions()..threads = 2,
      );
      interpreter.allocateTensors();
      if (mounted) {
        setState(() {
          _interpreter = interpreter;
          _isModelLoaded = true;
        });
      }
    } catch (e, stack) {
      debugPrint('加载 MNIST 模型失败: $e\n$stack');
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

      // 执行 Native Direct Memory 零拷贝推理 (Float32 灰度)
      interpreter.getInputTensor(0).data = inputBuffer.buffer.asUint8List();
      interpreter.invoke();
      final Float32List rawScores =
          interpreter.getOutputTensor(0).data.buffer.asFloat32List();
      stopwatch.stop();

      // Softmax 概率归一化
      final double maxScore = rawScores.reduce(math.max);
      final List<double> expScores =
          rawScores.map((double s) => math.exp(s - maxScore)).toList();
      final double sumExp = expScores.reduce((double a, double b) => a + b);
      final List<double> probs =
          expScores.map((double e) => e / (sumExp == 0 ? 1.0 : sumExp)).toList();

    int maxIdx = 0;
    double maxProb = probs[0];
    for (int i = 1; i < 10; i++) {
      if (probs[i] > maxProb) {
        maxProb = probs[i];
        maxIdx = i;
      }
    }

      if (mounted) {
        setState(() {
          _predictedDigit = maxIdx;
          _confidence = maxProb;
          _latencyMs = stopwatch.elapsedMicroseconds / 1000.0;
          _probabilities = probs;
        });
      }
    } catch (e, stack) {
      debugPrint('手写识别异常: $e\n$stack');
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

  @override
  void dispose() {
    _interpreter?.close();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
      ),
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(16.0),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: <Widget>[
              const Text(
                '在下方黑色区域用手指绘制 0 ~ 9 的任意数字：',
                style: TextStyle(fontSize: 14, fontWeight: FontWeight.w500),
              ),
              const SizedBox(height: 10),

              // 涂鸦手写板 (1:1 正方形)
              AspectRatio(
                aspectRatio: 1.0,
                child: Container(
                  decoration: BoxDecoration(
                    color: const Color(0xFF1E1E1E),
                    borderRadius: BorderRadius.circular(12),
                    boxShadow: const <BoxShadow>[
                      BoxShadow(
                        color: Colors.black12,
                        blurRadius: 6,
                        offset: Offset(0, 3),
                      ),
                    ],
                  ),
                  child: FingerDrawWidget(
                    key: _drawKey,
                    onStrokeFinished: _runClassification,
                  ),
                ),
              ),

              const SizedBox(height: 10),

              // 操作按钮
              Row(
                children: <Widget>[
                  Expanded(
                    child: OutlinedButton.icon(
                      onPressed: _clearCanvas,
                      icon: const Icon(Icons.refresh),
                      label: const Text('清空画板'),
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: ElevatedButton.icon(
                      onPressed: _runClassification,
                      icon: const Icon(Icons.psychology),
                      label: const Text('手动识别'),
                    ),
                  ),
                ],
              ),

              const SizedBox(height: 12),

              // 识别结果展示面板
              Expanded(
                child: Card(
                  elevation: 2,
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(10),
                  ),
                  child: Padding(
                    padding: const EdgeInsets.all(12.0),
                    child: Row(
                      children: <Widget>[
                        // 左侧：大号预测结果
                        Container(
                          width: 110,
                          padding: const EdgeInsets.all(8),
                          decoration: BoxDecoration(
                            color: Colors.blue.shade50,
                            borderRadius: BorderRadius.circular(8),
                          ),
                          child: Column(
                            mainAxisAlignment: MainAxisAlignment.center,
                            children: <Widget>[
                              const Text(
                                '识别数字',
                                style: TextStyle(
                                    fontSize: 13, color: Colors.blueGrey),
                              ),
                              const SizedBox(height: 4),
                              Text(
                                _predictedDigit >= 0 ? '$_predictedDigit' : '?',
                                style: TextStyle(
                                  fontSize: 48,
                                  fontWeight: FontWeight.bold,
                                  color: _predictedDigit >= 0
                                      ? Colors.blue.shade800
                                      : Colors.grey,
                                ),
                              ),
                              const SizedBox(height: 4),
                              Text(
                                _predictedDigit >= 0
                                    ? '置信度 ${( _confidence * 100).toStringAsFixed(1)}%'
                                    : '等待书写',
                                style: const TextStyle(
                                    fontSize: 11, color: Colors.blueGrey),
                              ),
                              if (_latencyMs > 0)
                                Text(
                                  '${_latencyMs.toStringAsFixed(2)} ms',
                                  style: const TextStyle(
                                      fontSize: 10, color: Colors.green),
                                ),
                            ],
                          ),
                        ),

                        const SizedBox(width: 12),

                        // 右侧：0~9 置信度水平分布条形图
                        Expanded(
                          child: ListView.builder(
                            itemCount: 10,
                            physics: const NeverScrollableScrollPhysics(),
                            itemBuilder: (BuildContext context, int digit) {
                              final double prob = _probabilities[digit];
                              final bool isTop = digit == _predictedDigit &&
                                  _predictedDigit >= 0;

                              return Padding(
                                padding:
                                    const EdgeInsets.symmetric(vertical: 2.0),
                                child: Row(
                                  children: <Widget>[
                                    SizedBox(
                                      width: 16,
                                      child: Text(
                                        '$digit:',
                                        style: TextStyle(
                                          fontSize: 11,
                                          fontWeight: isTop
                                              ? FontWeight.bold
                                              : FontWeight.normal,
                                          color: isTop
                                              ? Colors.blue.shade700
                                              : Colors.black87,
                                        ),
                                      ),
                                    ),
                                    const SizedBox(width: 4),
                                    Expanded(
                                      child: ClipRRect(
                                        borderRadius: BorderRadius.circular(4),
                                        child: LinearProgressIndicator(
                                          value: prob,
                                          minHeight: 8,
                                          backgroundColor: Colors.grey.shade200,
                                          valueColor:
                                              AlwaysStoppedAnimation<Color>(
                                            isTop
                                                ? Colors.blue
                                                : Colors.blue.shade200,
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
                                          fontSize: 10,
                                          fontFamily: 'monospace',
                                          fontWeight: isTop
                                              ? FontWeight.bold
                                              : FontWeight.normal,
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
                    ),
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
