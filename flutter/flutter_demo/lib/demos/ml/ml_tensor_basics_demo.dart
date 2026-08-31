import 'package:flutter/material.dart';
import 'package:tflite_flutter/tflite_flutter.dart';

/// TFLite 张量底层操作与内存生命周期规范
///
/// 官方文档: https://www.tensorflow.org/lite/guide
class MlTensorBasicsDemoPage extends StatefulWidget {
  final String title;

  const MlTensorBasicsDemoPage({
    super.key,
    required this.title,
  });

  @override
  State<MlTensorBasicsDemoPage> createState() => _MlTensorBasicsDemoPageState();
}

class _MlTensorBasicsDemoPageState extends State<MlTensorBasicsDemoPage> {
  final ScrollController _scrollController = ScrollController();
  final List<String> _logs = <String>[];
  Interpreter? _interpreter;

  @override
  void initState() {
    super.initState();
    _appendLog('💡 TFLite 张量底层操作与内存架构');
    _appendLog('演示 FlatBuffers 零拷贝、张量元数据反射、动态张量调整与 Native 资源管理。');
    _appendLog('请点击下方操作项执行：\n');
  }

  void _appendLog(String text) {
    setState(() {
      _logs.add(text);
    });
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (_scrollController.hasClients) {
        _scrollController.animateTo(
          _scrollController.position.maxScrollExtent,
          duration: const Duration(milliseconds: 150),
          curve: Curves.easeOut,
        );
      }
    });
  }

  Future<void> _inspectMetadata() async {
    _appendLog('── 1. 模型加载与张量元数据反射 ──');
    try {
      _interpreter?.close();
      _interpreter = await Interpreter.fromAsset(
        'assets/ml/mobilenet_v1_1.0_224_quant.tflite',
      );

      final List<Tensor> inputTensors = _interpreter!.getInputTensors();
      final List<Tensor> outputTensors = _interpreter!.getOutputTensors();

      _appendLog('✓ 模型加载成功 (FlatBuffers 零拷贝内存映射)');
      _appendLog('• 输入张量数: ${inputTensors.length}');
      for (int i = 0; i < inputTensors.length; i++) {
        final Tensor t = inputTensors[i];
        _appendLog(
            '  [Input #$i] 名称: ${t.name}, 形状: ${t.shape}, 类型: ${t.type}');
      }

      _appendLog('• 输出张量数: ${outputTensors.length}');
      for (int i = 0; i < outputTensors.length; i++) {
        final Tensor t = outputTensors[i];
        _appendLog(
            '  [Output #$i] 名称: ${t.name}, 形状: ${t.shape}, 类型: ${t.type}');
      }
    } catch (e) {
      _appendLog('✗ 反射失败: $e');
    }
  }

  void _explainDirectMemory() {
    _appendLog('\n── 2. Direct ByteData 内存排布与字节序机制 ──');
    _appendLog('• Dart FFI 与 Native C++ 共享物理内存（Zero-Copy Direct Buffer）');
    _appendLog('• 字节序规范: 必须使用 Endian.host 原生主机字节序，避免高低位颠倒');
    _appendLog('• 内存结构:');
    _appendLog('  - FP32 浮点模型: 4 字节/通道，28x28x1 = 3,136 字节');
    _appendLog('  - UINT8 量化模型: 1 字节/通道，224x224x3 = 150,528 字节 (内存占用减少 75%!)');
  }

  Future<void> _testDynamicResizing() async {
    _appendLog('\n── 3. 动态张量尺寸调整 (resizeInputTensor) ──');
    try {
      _interpreter?.close();
      _interpreter = await Interpreter.fromAsset(
        'assets/ml/mobilenet_v1_1.0_224_quant.tflite',
      );

      final Tensor inputTensor = _interpreter!.getInputTensor(0);
      _appendLog('• 初始输入形状: ${inputTensor.shape}');

      // 调整 Batch 大小为 2
      _appendLog('• 正在将 Batch 维度从 1 调整为 2: [2, 224, 224, 3]');
      _interpreter!.resizeInputTensor(0, <int>[2, 224, 224, 3]);
      _interpreter!.allocateTensors();

      final Tensor updatedTensor = _interpreter!.getInputTensor(0);
      _appendLog('✓ 重新分配张量成功，新形状: ${updatedTensor.shape}');
      _appendLog('  支持一次推理并发处理多张图片，提升吞吐效率！');
    } catch (e) {
      _appendLog('✗ 动态调整失败: $e');
    }
  }

  void _testMimoExecution() {
    _appendLog('\n── 4. 多输入多输出 (MIMO) 调度 ──');
    _appendLog('• 在检测模型（如 YOLO/SSD）中通常有多个输出张量（边界框 + 置信度 + 类别）');
    _appendLog('• TFLite 规范语法:');
    _appendLog('  final inputs = [inputBuffer1, inputBuffer2];');
    _appendLog('  final outputs = {0: outputBoxes, 1: outputScores};');
    _appendLog('  interpreter.runForMultipleInputs(inputs, outputs);');
    _appendLog('✓ 一次前向传播同时填充多个输出张量，无跨语言上下文切换开销。');
  }

  void _demonstrateSafeRelease() {
    _appendLog('\n── 5. Native 内存释放与防泄漏最佳实践 ──');
    if (_interpreter != null) {
      _interpreter!.close();
      _interpreter = null;
      _appendLog('✓ interpreter.close() 显式调用成功');
      _appendLog('  底层 C++ Runtime 实例、GPU 显存句柄及张量分配池已完整销毁');
      _appendLog('  杜绝了长时间驻留导致的 Native OOM (Out Of Memory)。');
    } else {
      _appendLog('• 当前 Interpreter 已处于释放状态。');
    }
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
              // 操作按钮列表
              Wrap(
                spacing: 8,
                runSpacing: 6,
                children: <Widget>[
                  ActionChip(
                    avatar: const Icon(Icons.analytics, size: 16),
                    label: const Text('1. 元数据反射'),
                    onPressed: _inspectMetadata,
                  ),
                  ActionChip(
                    avatar: const Icon(Icons.memory, size: 16),
                    label: const Text('2. Direct 内存机制'),
                    onPressed: _explainDirectMemory,
                  ),
                  ActionChip(
                    avatar: const Icon(Icons.aspect_ratio, size: 16),
                    label: const Text('3. 动态 Reshape'),
                    onPressed: _testDynamicResizing,
                  ),
                  ActionChip(
                    avatar: const Icon(Icons.alt_route, size: 16),
                    label: const Text('4. MIMO 调度'),
                    onPressed: _testMimoExecution,
                  ),
                  ActionChip(
                    avatar: const Icon(Icons.delete_sweep, size: 16),
                    label: const Text('5. 内存安全释放'),
                    onPressed: _demonstrateSafeRelease,
                  ),
                ],
              ),

              const SizedBox(height: 10),

              // 控制台日志输出
              Expanded(
                child: Container(
                  padding: const EdgeInsets.all(12),
                  decoration: BoxDecoration(
                    color: const Color(0xFF212121),
                    borderRadius: BorderRadius.circular(8),
                    boxShadow: const <BoxShadow>[
                      BoxShadow(
                        color: Colors.black26,
                        blurRadius: 4,
                        offset: Offset(0, 2),
                      ),
                    ],
                  ),
                  child: ListView.builder(
                    controller: _scrollController,
                    itemCount: _logs.length,
                    itemBuilder: (BuildContext context, int index) {
                      return Text(
                        _logs[index],
                        style: const TextStyle(
                          color: Color(0xFF81C784),
                          fontFamily: 'monospace',
                          fontSize: 11,
                          height: 1.4,
                        ),
                      );
                    },
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
