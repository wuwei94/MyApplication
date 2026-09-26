import 'package:flutter_demo/core/basic/basic.dart';
import 'package:tflite_flutter/tflite_flutter.dart';

/// TFLite Tensor — 张量元数据反射与内存生命周期
///
/// 核心机制与避坑点：
/// 1. 元数据反射：`getInputTensors` / `getOutputTensors` 读取名称、形状与类型。
/// 2. 动态 Reshape：`resizeInputTensor` + `allocateTensors` 调整 Batch 等维度。
/// 3. 资源释放：`interpreter.close()` 显式销毁 Native 句柄，避免长时间驻留 OOM。
///
/// 官方参考：
/// https://www.tensorflow.org/lite/guide
class MlTensorBasicsDemoPage extends BasicResponsePage {
  const MlTensorBasicsDemoPage({super.key, required super.title});

  @override
  BasicResponsePageState<MlTensorBasicsDemoPage> createState() =>
      _MlTensorBasicsDemoPageState();
}

class _MlTensorBasicsDemoPageState
    extends BasicResponsePageState<MlTensorBasicsDemoPage> {
  Interpreter? _interpreter;

  @override
  void initState() {
    super.initState();
    showDescription('TFLite 张量示例：元数据反射 / Direct 内存 / 动态 Reshape / MIMO / 释放');
  }

  @override
  void dispose() {
    _interpreter?.close();
    super.dispose();
  }

  @override
  List<String> buildList() => const <String>[
        '1. 反射张量元数据',
        '2. 解释 Direct 内存机制',
        '3. 动态调整输入形状',
        '4. 演示 MIMO 调度',
        '5. 释放 Native 内存',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        _inspectMetadata();
      case 1:
        _explainDirectMemory();
      case 2:
        _testDynamicResizing();
      case 3:
        _testMimoExecution();
      case 4:
        _demonstrateSafeRelease();
    }
  }

  Future<void> _inspectMetadata() async {
    appendLog('→ [Metadata] Interpreter.fromAsset(mobilenet_v1_1.0_224_quant)');
    try {
      _interpreter?.close();
      final Interpreter interpreter = await Interpreter.fromAsset(
        'assets/ml/mobilenet_v1_1.0_224_quant.tflite',
      );
      _interpreter = interpreter;

      final List<Tensor> inputTensors = interpreter.getInputTensors();
      final List<Tensor> outputTensors = interpreter.getOutputTensors();

      appendLog('✓ 模型加载成功 (FlatBuffers 零拷贝内存映射)');
      appendLog('• 输入张量数: ${inputTensors.length}');
      for (int i = 0; i < inputTensors.length; i++) {
        final Tensor tensor = inputTensors[i];
        appendLog(
          '  [Input #$i] 名称: ${tensor.name}, 形状: ${tensor.shape}, 类型: ${tensor.type}',
        );
      }

      appendLog('• 输出张量数: ${outputTensors.length}');
      for (int i = 0; i < outputTensors.length; i++) {
        final Tensor tensor = outputTensors[i];
        appendLog(
          '  [Output #$i] 名称: ${tensor.name}, 形状: ${tensor.shape}, 类型: ${tensor.type}',
        );
      }
    } catch (error) {
      appendLog('✗ 反射失败: $error');
    }
  }

  void _explainDirectMemory() {
    appendLog('→ [Memory] Direct ByteData 内存排布与字节序');
    appendLog('• Dart FFI 与 Native C++ 共享物理内存（Zero-Copy Direct Buffer）');
    appendLog('• 字节序规范: 必须使用 Endian.host 原生主机字节序，避免高低位颠倒');
    appendLog('• FP32 浮点模型: 4 字节/通道，28x28x1 = 3,136 字节');
    appendLog('• UINT8 量化模型: 1 字节/通道，224x224x3 = 150,528 字节');
  }

  Future<void> _testDynamicResizing() async {
    appendLog('→ [Reshape] resizeInputTensor(0, [2, 224, 224, 3])');
    try {
      _interpreter?.close();
      final Interpreter interpreter = await Interpreter.fromAsset(
        'assets/ml/mobilenet_v1_1.0_224_quant.tflite',
      );
      _interpreter = interpreter;

      final Tensor inputTensor = interpreter.getInputTensor(0);
      appendLog('• 初始输入形状: ${inputTensor.shape}');

      interpreter.resizeInputTensor(0, <int>[2, 224, 224, 3]);
      interpreter.allocateTensors();

      final Tensor updatedTensor = interpreter.getInputTensor(0);
      appendLog('✓ 重新分配张量成功，新形状: ${updatedTensor.shape}');
    } catch (error) {
      appendLog('✗ 动态调整失败: $error');
    }
  }

  void _testMimoExecution() {
    appendLog('→ [MIMO] runForMultipleInputs 多输入多输出调度');
    appendLog('• 检测模型常见多输出：边界框 + 置信度 + 类别');
    appendLog('  final inputs = [inputBuffer1, inputBuffer2];');
    appendLog('  final outputs = {0: outputBoxes, 1: outputScores};');
    appendLog('  interpreter.runForMultipleInputs(inputs, outputs);');
    appendLog('✓ 一次前向传播同时填充多个输出张量');
  }

  void _demonstrateSafeRelease() {
    appendLog('→ [Release] interpreter.close()');
    final Interpreter? interpreter = _interpreter;
    if (interpreter != null) {
      interpreter.close();
      _interpreter = null;
      appendLog('✓ Native Runtime / GPU 显存 / 张量分配池已销毁');
    } else {
      appendLog('• 当前 Interpreter 已处于释放状态');
    }
  }
}
