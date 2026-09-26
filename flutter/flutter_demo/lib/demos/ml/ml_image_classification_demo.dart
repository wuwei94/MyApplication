import 'dart:io';

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart' show rootBundle;
import 'package:flutter_demo/core/basic/basic.dart';
import 'package:image/image.dart' as img;
import 'package:image_picker/image_picker.dart';
import 'package:tflite_flutter/tflite_flutter.dart';

/// MobileNet — 图像物体分类 Top-5
///
/// 核心机制与避坑点：
/// 1. 预处理：Center-Crop 正方形等比裁剪后缩放到 224x224，避免拉伸变形。
/// 2. 推理契约：UINT8 RGB 张量直写 `getInputTensor(0).data` 后 `invoke`。
/// 3. Delegate：`GpuDelegateV2` 失败时回退 CPU 多线程，保证可跑通。
///
/// 官方参考：
/// https://www.tensorflow.org/lite/examples/image_classification/overview
class MlImageClassificationDemoPage extends BasicImagePage {
  const MlImageClassificationDemoPage({super.key, required super.title});

  @override
  BasicImagePageState<MlImageClassificationDemoPage> createState() =>
      _MlImageClassificationDemoPageState();
}

class _MlImageClassificationDemoPageState
    extends BasicImagePageState<MlImageClassificationDemoPage> {
  Interpreter? _interpreter;
  List<String> _labels = <String>[];
  bool _isGpuEnabled = false;
  bool _isProcessing = false;

  Uint8List? _previewImageBytes;
  List<MapEntry<String, double>> _top5Results = <MapEntry<String, double>>[];
  double _preprocessMs = 0.0;
  double _inferenceMs = 0.0;

  final ImagePicker _picker = ImagePicker();

  @override
  void initState() {
    super.initState();
    _initModel();
  }

  @override
  void dispose() {
    _interpreter?.close();
    super.dispose();
  }

  @override
  List<String> buildList() => const <String>[
        '1. 加载金毛犬样本',
        '2. 加载跑车样本',
        '3. 加载咖啡杯样本',
        '4. 相册选图识别',
        '5. 切换 GPU 加速',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        _loadSampleAsset('assets/ml/sample_dog.jpg');
      case 1:
        _loadSampleAsset('assets/ml/sample_car.jpg');
      case 2:
        _loadSampleAsset('assets/ml/sample_mug.jpg');
      case 3:
        _pickImageFromGallery();
      case 4:
        _initModel(useGpu: !_isGpuEnabled);
    }
  }

  @override
  Widget buildPreview() {
    final Uint8List? bytes = _previewImageBytes;
    return Padding(
      padding: const EdgeInsets.all(BasicDemoDimens.pagePadding),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: <Widget>[
          Expanded(
            flex: 3,
            child: ClipRRect(
              borderRadius: BorderRadius.circular(BasicDemoDimens.cornerCard),
              child: bytes == null
                  ? const ColoredBox(
                      color: Color(0xFF212121),
                      child: Center(
                        child: Icon(Icons.image, color: Colors.white24, size: 48),
                      ),
                    )
                  : Image.memory(bytes, fit: BoxFit.contain),
            ),
          ),
          const SizedBox(height: BasicDemoDimens.itemPadding),
          Expanded(flex: 2, child: _buildResultPanel()),
        ],
      ),
    );
  }

  Widget _buildResultPanel() {
    final String engine = _isGpuEnabled
        ? 'GPU Delegate (显卡并行)'
        : 'CPU (4 线程 + XNNPACK NEON)';
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: <Widget>[
        Text(
          '推理引擎: $engine',
          style: TextStyle(
            fontSize: BasicDemoDimens.fontTitle,
            fontWeight: FontWeight.bold,
            color: _isGpuEnabled ? Colors.deepPurple : Colors.blue.shade700,
          ),
        ),
        const SizedBox(height: 2),
        Text(
          '预处理 ${_preprocessMs.toStringAsFixed(1)} ms | '
          '推理 ${_inferenceMs.toStringAsFixed(1)} ms',
          style: TextStyle(fontSize: BasicDemoDimens.fontBody, color: Colors.grey.shade600),
        ),
        const SizedBox(height: BasicDemoDimens.itemPadding),
        Expanded(
          child: _top5Results.isEmpty
              ? const Center(child: Text('选择下方样本开始识别'))
              : ListView.builder(
                  itemCount: _top5Results.length,
                  itemBuilder: (BuildContext context, int index) {
                    final MapEntry<String, double> item = _top5Results[index];
                    final String percent =
                        '${(item.value * 100).toStringAsFixed(2)}%';
                    return Padding(
                      padding: const EdgeInsets.symmetric(vertical: 2),
                      child: Row(
                        children: <Widget>[
                          SizedBox(
                            width: 48,
                            child: Text(
                              'Top${index + 1}',
                              style: TextStyle(
                                fontSize: BasicDemoDimens.fontBody,
                                fontFamily: 'monospace',
                                fontWeight: index == 0
                                    ? FontWeight.bold
                                    : FontWeight.normal,
                              ),
                            ),
                          ),
                          SizedBox(
                            width: 56,
                            child: Text(
                              percent,
                              style: const TextStyle(
                                fontSize: BasicDemoDimens.fontBody,
                                fontFamily: 'monospace',
                              ),
                            ),
                          ),
                          Expanded(
                            child: Text(
                              item.key,
                              maxLines: 1,
                              overflow: TextOverflow.ellipsis,
                              style: TextStyle(
                                fontSize: BasicDemoDimens.fontBody,
                                fontWeight: index == 0
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
    );
  }

  Future<void> _initModel({bool useGpu = false}) async {
    _interpreter?.close();
    _interpreter = null;

    try {
      if (_labels.isEmpty) {
        final String labelData =
            await rootBundle.loadString('assets/ml/labels.txt');
        _labels = labelData
            .split('\n')
            .map((String s) => s.trim())
            .where((String s) => s.isNotEmpty)
            .toList();
      }

      final InterpreterOptions options = InterpreterOptions()..threads = 4;
      if (useGpu) {
        try {
          final GpuDelegateV2 gpuDelegate = GpuDelegateV2(
            options: GpuDelegateOptionsV2(isPrecisionLossAllowed: true),
          );
          options.addDelegate(gpuDelegate);
        } catch (error) {
          debugPrint('GPU Delegate 创建失败，回退到 CPU: $error');
        }
      }

      final Interpreter interpreter = await Interpreter.fromAsset(
        'assets/ml/mobilenet_v1_1.0_224_quant.tflite',
        options: options,
      );
      interpreter.allocateTensors();

      if (!mounted) return;
      setState(() {
        _interpreter = interpreter;
        _isGpuEnabled = useGpu;
      });

      if (_previewImageBytes == null) {
        await _loadSampleAsset('assets/ml/sample_dog.jpg');
      }
    } catch (error) {
      debugPrint('初始化模型失败: $error');
    }
  }

  Future<void> _loadSampleAsset(String assetPath) async {
    try {
      final ByteData data = await rootBundle.load(assetPath);
      final Uint8List bytes = data.buffer.asUint8List();
      if (!mounted) return;
      setState(() {
        _previewImageBytes = bytes;
      });
      await _runClassification(bytes);
    } catch (error) {
      debugPrint('加载样本图失败: $error');
    }
  }

  Future<void> _pickImageFromGallery() async {
    try {
      final XFile? file = await _picker.pickImage(source: ImageSource.gallery);
      if (file == null) return;
      final Uint8List bytes = await File(file.path).readAsBytes();
      if (!mounted) return;
      setState(() {
        _previewImageBytes = bytes;
      });
      await _runClassification(bytes);
    } catch (error) {
      debugPrint('选择相册图片失败: $error');
    }
  }

  Future<void> _runClassification(Uint8List rawBytes) async {
    final Interpreter? interpreter = _interpreter;
    if (interpreter == null || _isProcessing) return;

    setState(() {
      _isProcessing = true;
    });

    try {
      final Stopwatch preprocessWatch = Stopwatch()..start();

      final img.Image? decoded = img.decodeImage(rawBytes);
      if (decoded == null) {
        if (mounted) {
          setState(() => _isProcessing = false);
        }
        return;
      }

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
      final img.Image resized =
          img.copyResize(cropped, width: 224, height: 224);

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

      final Stopwatch inferenceWatch = Stopwatch()..start();
      interpreter.getInputTensor(0).data = inputBuffer;
      interpreter.invoke();
      final Uint8List rawOutput = interpreter.getOutputTensor(0).data;
      inferenceWatch.stop();

      final List<MapEntry<int, double>> scoredIndices =
          <MapEntry<int, double>>[];
      for (int i = 0; i < rawOutput.length; i++) {
        final double prob = (rawOutput[i] & 0xFF) / 255.0;
        scoredIndices.add(MapEntry<int, double>(i, prob));
      }
      scoredIndices.sort(
        (MapEntry<int, double> a, MapEntry<int, double> b) =>
            b.value.compareTo(a.value),
      );

      final List<MapEntry<String, double>> top5 = scoredIndices
          .take(5)
          .map((MapEntry<int, double> e) {
            final String label =
                (e.key < _labels.length) ? _labels[e.key] : '类别 #${e.key}';
            return MapEntry<String, double>(label, e.value);
          })
          .toList();

      if (!mounted) return;
      setState(() {
        _top5Results = top5;
        _preprocessMs = preprocessWatch.elapsedMicroseconds / 1000.0;
        _inferenceMs = inferenceWatch.elapsedMicroseconds / 1000.0;
        _isProcessing = false;
      });
    } catch (error) {
      debugPrint('图像识别异常: $error');
      if (mounted) {
        setState(() {
          _isProcessing = false;
        });
      }
    }
  }
}
