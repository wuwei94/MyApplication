import 'dart:io';
import 'dart:typed_data';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart' show rootBundle;
import 'package:image/image.dart' as img;
import 'package:image_picker/image_picker.dart';
import 'package:tflite_flutter/tflite_flutter.dart';

/// MobileNet 图像物体分类实战 (ImageNet 1000 类别)
///
/// 官方文档: https://www.tensorflow.org/lite/examples/image_classification/overview
class MlImageClassificationDemoPage extends StatefulWidget {
  final String title;

  const MlImageClassificationDemoPage({
    super.key,
    required this.title,
  });

  @override
  State<MlImageClassificationDemoPage> createState() =>
      _MlImageClassificationDemoPageState();
}

class _MlImageClassificationDemoPageState
    extends State<MlImageClassificationDemoPage> {
  Interpreter? _interpreter;
  List<String> _labels = <String>[];
  bool _isGpuEnabled = false;

  Uint8List? _previewImageBytes;
  List<MapEntry<String, double>> _top5Results = <MapEntry<String, double>>[];
  double _preprocessMs = 0.0;
  double _inferenceMs = 0.0;
  bool _isProcessing = false;

  final ImagePicker _picker = ImagePicker();

  @override
  void initState() {
    super.initState();
    _initModel();
  }

  Future<void> _initModel({bool useGpu = false}) async {
    _interpreter?.close();
    _interpreter = null;

    try {
      // 1. 加载分类标签
      if (_labels.isEmpty) {
        final String labelData =
            await rootBundle.loadString('assets/ml/labels.txt');
        _labels = labelData
            .split('\n')
            .map((String s) => s.trim())
            .where((String s) => s.isNotEmpty)
            .toList();
      }

      // 2. 配置 Interpreter (CPU 多线程已内置 NEON / XNNPACK 优化)
      final InterpreterOptions options = InterpreterOptions()..threads = 4;
      if (useGpu) {
        try {
          final GpuDelegateV2 gpuDelegate = GpuDelegateV2(
            options: GpuDelegateOptionsV2(isPrecisionLossAllowed: true),
          );
          options.addDelegate(gpuDelegate);
        } catch (e) {
          debugPrint('GPU Delegate 创建失败，回退到 CPU: $e');
        }
      }

      final Interpreter interpreter = await Interpreter.fromAsset(
        'assets/ml/mobilenet_v1_1.0_224_quant.tflite',
        options: options,
      );
      interpreter.allocateTensors();

      if (mounted) {
        setState(() {
          _interpreter = interpreter;
          _isGpuEnabled = useGpu;
        });
      }

      // 默认加载金毛犬样本
      if (_previewImageBytes == null) {
        await _loadSampleAsset('assets/ml/sample_dog.jpg');
      }
    } catch (e, stack) {
      debugPrint('初始化模型失败: $e\n$stack');
    }
  }

  Future<void> _loadSampleAsset(String assetPath) async {
    try {
      final ByteData data = await rootBundle.load(assetPath);
      final Uint8List bytes = data.buffer.asUint8List();
      if (mounted) {
        setState(() {
          _previewImageBytes = bytes;
        });
      }
      await _runClassification(bytes);
    } catch (e) {
      debugPrint('加载样本图失败: $e');
    }
  }

  Future<void> _pickImageFromGallery() async {
    try {
      final XFile? file = await _picker.pickImage(source: ImageSource.gallery);
      if (file != null) {
        final Uint8List bytes = await File(file.path).readAsBytes();
        if (mounted) {
          setState(() {
            _previewImageBytes = bytes;
          });
        }
        await _runClassification(bytes);
      }
    } catch (e) {
      debugPrint('选择相册图片失败: $e');
    }
  }

  Future<void> _runClassification(Uint8List rawBytes) async {
    final Interpreter? interpreter = _interpreter;
    if (interpreter == null || _isProcessing) return;

    if (mounted) {
      setState(() {
        _isProcessing = true;
      });
    }

    try {
      final Stopwatch preprocessWatch = Stopwatch()..start();

      // 1. 解码图片
      final img.Image? decoded = img.decodeImage(rawBytes);
      if (decoded == null) {
        if (mounted) setState(() => _isProcessing = false);
        return;
      }

      // 2. Center-Crop 中心正方形等比裁剪，避免拉伸变形
      final int minDim =
          decoded.width < decoded.height ? decoded.width : decoded.height;
      final int xOffset = (decoded.width - minDim) ~/ 2;
      final int yOffset = (decoded.height - minDim) ~/ 2;
      final img.Image cropped = img.copyCrop(
        decoded,
        x: xOffset,
        y: yOffset,
        width: minDim,
        height: minDim,
      );

      // 3. 缩放到目标 224x224
      final img.Image resized =
          img.copyResize(cropped, width: 224, height: 224);

      // 4. 构建 UINT8 RGB 原始字节张量 (224 * 224 * 3 = 150,528 字节)
      final Uint8List inputBuffer = Uint8List(1 * 224 * 224 * 3);
      int bufferIdx = 0;
      for (int y = 0; y < 224; y++) {
        for (int x = 0; x < 224; x++) {
          final img.Pixel pixel = resized.getPixel(x, y);
          inputBuffer[bufferIdx++] = pixel.r.toInt();
          inputBuffer[bufferIdx++] = pixel.g.toInt();
          inputBuffer[bufferIdx++] = pixel.b.toInt();
        }
      }

      preprocessWatch.stop();

      // 5. 执行 Native Direct Memory 零拷贝推理
      final Stopwatch inferenceWatch = Stopwatch()..start();
      interpreter.getInputTensor(0).data = inputBuffer;
      interpreter.invoke();
      final Uint8List rawOutput = interpreter.getOutputTensor(0).data;
      inferenceWatch.stop();

      // 6. 解析 Top-5 结果
      final List<MapEntry<int, double>> scoredIndices =
          <MapEntry<int, double>>[];

      for (int i = 0; i < rawOutput.length; i++) {
        final double prob = (rawOutput[i] & 0xFF) / 255.0;
        scoredIndices.add(MapEntry<int, double>(i, prob));
      }

      scoredIndices.sort((MapEntry<int, double> a, MapEntry<int, double> b) =>
          b.value.compareTo(a.value));

      final List<MapEntry<String, double>> top5 = scoredIndices
          .take(5)
          .map((MapEntry<int, double> e) {
            final String label =
                (e.key < _labels.length) ? _labels[e.key] : '类别 #${e.key}';
            return MapEntry<String, double>(label, e.value);
          })
          .toList();

      if (mounted) {
        setState(() {
          _top5Results = top5;
          _preprocessMs = preprocessWatch.elapsedMicroseconds / 1000.0;
          _inferenceMs = inferenceWatch.elapsedMicroseconds / 1000.0;
          _isProcessing = false;
        });
      }
    } catch (e, stack) {
      debugPrint('图像识别异常: $e\n$stack');
      if (mounted) {
        setState(() {
          _isProcessing = false;
        });
      }
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
              // GPU 硬件加速开关
              Container(
                padding:
                    const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                decoration: BoxDecoration(
                  color: Colors.grey.shade100,
                  borderRadius: BorderRadius.circular(8),
                ),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: <Widget>[
                    const Text(
                      '启用 GPU 硬件加速 (Delegate):',
                      style:
                          TextStyle(fontSize: 14, fontWeight: FontWeight.w500),
                    ),
                    Switch(
                      value: _isGpuEnabled,
                      onChanged: (bool value) {
                        _initModel(useGpu: value);
                      },
                    ),
                  ],
                ),
              ),

              const SizedBox(height: 10),

              // 图像预览卡片 (4:3 黄金比例无黑边)
              AspectRatio(
                aspectRatio: 4 / 3,
                child: Container(
                  decoration: BoxDecoration(
                    color: const Color(0xFF212121),
                    borderRadius: BorderRadius.circular(10),
                    boxShadow: const <BoxShadow>[
                      BoxShadow(
                        color: Colors.black12,
                        blurRadius: 6,
                        offset: Offset(0, 3),
                      ),
                    ],
                  ),
                  child: ClipRRect(
                    borderRadius: BorderRadius.circular(10),
                    child: _previewImageBytes != null
                        ? Image.memory(
                            _previewImageBytes!,
                            fit: BoxFit.cover,
                          )
                        : const Center(
                            child: Icon(Icons.image,
                                color: Colors.white24, size: 48),
                          ),
                  ),
                ),
              ),

              const SizedBox(height: 10),

              // 示例按钮与相册选图
              Row(
                children: <Widget>[
                  Expanded(
                    child: OutlinedButton(
                      style: OutlinedButton.styleFrom(
                        padding: EdgeInsets.zero,
                        visualDensity: VisualDensity.compact,
                      ),
                      onPressed: () =>
                          _loadSampleAsset('assets/ml/sample_dog.jpg'),
                      child: const Text('金毛犬', style: TextStyle(fontSize: 12)),
                    ),
                  ),
                  const SizedBox(width: 6),
                  Expanded(
                    child: OutlinedButton(
                      style: OutlinedButton.styleFrom(
                        padding: EdgeInsets.zero,
                        visualDensity: VisualDensity.compact,
                      ),
                      onPressed: () =>
                          _loadSampleAsset('assets/ml/sample_car.jpg'),
                      child: const Text('跑车', style: TextStyle(fontSize: 12)),
                    ),
                  ),
                  const SizedBox(width: 6),
                  Expanded(
                    child: OutlinedButton(
                      style: OutlinedButton.styleFrom(
                        padding: EdgeInsets.zero,
                        visualDensity: VisualDensity.compact,
                      ),
                      onPressed: () =>
                          _loadSampleAsset('assets/ml/sample_mug.jpg'),
                      child: const Text('咖啡杯', style: TextStyle(fontSize: 12)),
                    ),
                  ),
                  const SizedBox(width: 6),
                  Expanded(
                    flex: 1,
                    child: ElevatedButton.icon(
                      style: ElevatedButton.styleFrom(
                        padding: EdgeInsets.zero,
                        visualDensity: VisualDensity.compact,
                      ),
                      onPressed: _pickImageFromGallery,
                      icon: const Icon(Icons.photo_library, size: 14),
                      label:
                          const Text('相册选图', style: TextStyle(fontSize: 12)),
                    ),
                  ),
                ],
              ),

              const SizedBox(height: 10),

              // 识别结果面板
              Expanded(
                child: Card(
                  elevation: 2,
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(10),
                  ),
                  child: Padding(
                    padding: const EdgeInsets.all(12.0),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: <Widget>[
                        Text(
                          _isGpuEnabled
                              ? '推理引擎: GPU Delegate (显卡并行)'
                              : '推理引擎: CPU (4 线程 + XNNPACK NEON)',
                          style: TextStyle(
                            fontSize: 13,
                            fontWeight: FontWeight.bold,
                            color: _isGpuEnabled
                                ? Colors.deepPurple
                                : Colors.blue.shade700,
                          ),
                        ),
                        const SizedBox(height: 2),
                        Text(
                          '预处理耗时: ${_preprocessMs.toStringAsFixed(1)} ms | 模型推理耗时: ${_inferenceMs.toStringAsFixed(1)} ms',
                          style: TextStyle(
                              fontSize: 11, color: Colors.grey.shade600),
                        ),
                        const Divider(height: 14),
                        const Text(
                          'Top-5 识别结果 (置信度最高标签):',
                          style: TextStyle(
                              fontSize: 13, fontWeight: FontWeight.bold),
                        ),
                        const SizedBox(height: 6),
                        Expanded(
                          child: _top5Results.isEmpty
                              ? const Center(child: CircularProgressIndicator())
                              : ListView.builder(
                                  itemCount: _top5Results.length,
                                  itemBuilder:
                                      (BuildContext context, int index) {
                                    final MapEntry<String, double> item =
                                        _top5Results[index];
                                    final String percent =
                                        '${(item.value * 100).toStringAsFixed(2).padLeft(5)}%';
                                    return Padding(
                                      padding: const EdgeInsets.symmetric(
                                          vertical: 3.0),
                                      child: Row(
                                        children: <Widget>[
                                          Text(
                                            'Top ${index + 1}:',
                                            style: const TextStyle(
                                              fontSize: 12,
                                              fontFamily: 'monospace',
                                              fontWeight: FontWeight.w600,
                                            ),
                                          ),
                                          const SizedBox(width: 8),
                                          Container(
                                            padding: const EdgeInsets.symmetric(
                                                horizontal: 6, vertical: 1),
                                            decoration: BoxDecoration(
                                              color: index == 0
                                                  ? Colors.amber.shade100
                                                  : Colors.grey.shade200,
                                              borderRadius:
                                                  BorderRadius.circular(4),
                                            ),
                                            child: Text(
                                              percent,
                                              style: TextStyle(
                                                fontSize: 11,
                                                fontFamily: 'monospace',
                                                fontWeight: FontWeight.bold,
                                                color: index == 0
                                                    ? Colors.amber.shade900
                                                    : Colors.black87,
                                              ),
                                            ),
                                          ),
                                          const SizedBox(width: 8),
                                          Expanded(
                                            child: Text(
                                              '──  ${item.key}',
                                              style: TextStyle(
                                                fontSize: 12,
                                                fontWeight: index == 0
                                                    ? FontWeight.bold
                                                    : FontWeight.normal,
                                              ),
                                              maxLines: 1,
                                              overflow: TextOverflow.ellipsis,
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
