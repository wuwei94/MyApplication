import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';
import 'package:flutter_demo/demos/ml/engines/ml_tflite_helper.dart';
import 'package:flutter_demo/demos/ml/widgets/finger_draw_widget.dart';
import 'package:tflite_flutter/tflite_flutter.dart';

/// MNIST — 手写数字识别（LiteRT / TFLite 端侧推理）
///
/// 与 Android `:modules:module_ml` 的 `TFLiteDigitClassifierActivity` 一一对应，
/// 预处理契约完全一致：
/// 1. 模型 `digit_classifier.tflite`，输入 `[1, 28, 28, 3]` FP32，取值 0~255 不归一化；
/// 2. 画布**整图拉伸**到 28x28（外接框裁剪 / 居中重排已撤掉，与官方 sample 一致）；
/// 3. 模型末算子即 SOFTMAX，**输出已是概率，不再二次归一化** —— 对概率再叠加
///    Softmax 会把 0.90 压到 ≈0.21，置信度与柱状图严重失真；
/// 4. CPU / GPU 切换即重建引擎（delegate 挂在解释器实例上，无法热替换）。
///
/// 官方参考：
/// https://github.com/google-ai-edge/litert-samples/tree/main/samples/litert/digit_classifier
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

  static const String _modelAsset = 'assets/ml/digit_classifier.tflite';

  Interpreter? _interpreter;
  bool _useGpu = false;
  String _engineLabel = '初始化中';
  int _predictedDigit = -1;
  double _confidence = 0.0;
  double _latencyMs = 0.0;
  List<double> _probabilities = List<double>.filled(10, 0.0);

  @override
  void initState() {
    super.initState();
    _rebuildModel(useGpu: false);
  }

  @override
  void dispose() {
    _interpreter?.close();
    super.dispose();
  }

  @override
  List<String> buildList() => <String>[
        '1. 识别手写数字',
        '2. 清空画板',
        '3. 切换 GPU 加速（当前：${_useGpu ? "GPU" : "CPU"}）',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        _runClassification();
      case 1:
        _clearCanvas();
      case 2:
        _useGpu = !_useGpu;
        _rebuildModel(useGpu: _useGpu);
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
          // 画板取剩余空间内最大的正方形：竖屏时预览区高度小于宽度，
          // 直接 AspectRatio(1) 会按宽度撑高而溢出 Column（实测溢出 597 px）
          Flexible(
            child: Center(
              child: AspectRatio(
                aspectRatio: 1,
                child: ClipRRect(
                  borderRadius: BorderRadius.circular(BasicDemoDimens.cornerCard),
                  child: FingerDrawWidget(
                    key: _drawKey,
                    onStrokeFinished: _runClassification,
                  ),
                ),
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
              Text(
                _engineLabel,
                textAlign: TextAlign.center,
                style: TextStyle(
                  fontSize: BasicDemoDimens.fontBody,
                  color: _useGpu ? Colors.deepPurple : Colors.blueGrey,
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

  /// 按当前加速开关重建引擎；GPU 构建失败时由 helper 回退 CPU 并如实写进引擎描述
  Future<void> _rebuildModel({required bool useGpu}) async {
    final Interpreter? previous = _interpreter;
    _interpreter = null;
    setState(() {
      _engineLabel = '初始化中';
    });

    try {
      final MlEngineBundle built =
          await MlTfliteHelper.createInterpreter(_modelAsset, useGpu: useGpu);
      _interpreter = built.interpreter;
      _engineLabel = built.engineLabel;
      if (mounted) setState(() {});

      // 旧引擎在新引擎就绪后再关：与 Android 页「先建新再关旧」同序，
      // 避免构建失败时页面退化成「引擎已置空」的半死状态
      previous?.close();

      // 画板上还有笔迹时用新后端重跑一次，避免结果停留在旧后端的输出上
      if (_drawKey.currentState?.hasStrokes ?? false) {
        await _runClassification();
      }
    } catch (error) {
      debugPrint('初始化模型失败: $error');
      _engineLabel = '初始化失败: $error';
      if (mounted) setState(() {});
    }
  }

  Future<void> _runClassification() async {
    final FingerDrawWidgetState? drawState = _drawKey.currentState;
    if (drawState == null || !drawState.hasStrokes) {
      setState(() {
        _predictedDigit = -1;
        _confidence = 0.0;
      });
      return;
    }

    final Float32List? input = await drawState.export28x28Rgb();
    if (input == null) return;

    // 引擎字段在每次 await 后都要重新校验：导出是异步的，期间用户可能已拨动
    // GPU 开关重建引擎，此刻字段里的实例已不是发起时那个
    final Interpreter? interpreter = _interpreter;
    if (interpreter == null) return;

    try {
      final Stopwatch stopwatch = Stopwatch()..start();
      MlTfliteHelper.writeFloatInput(interpreter, 0, input);
      interpreter.invoke();
      // 模型末算子即 SOFTMAX：输出已经是概率，直接使用
      final Float32List scores =
          MlTfliteHelper.readFloatOutput(interpreter);
      stopwatch.stop();

      final int predicted = _argMax(scores);

      if (!mounted) return;
      setState(() {
        _predictedDigit = predicted;
        _confidence = scores[predicted];
        _latencyMs = stopwatch.elapsedMicroseconds / 1000.0;
        _probabilities = scores;
      });
    } catch (error) {
      debugPrint('手写识别异常: $error');
    }
  }

  int _argMax(List<double> values) {
    int best = 0;
    for (int i = 1; i < values.length; i++) {
      if (values[i] > values[best]) best = i;
    }
    return best;
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
