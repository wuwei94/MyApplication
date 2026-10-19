import 'dart:ui' as ui;

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart' show rootBundle;
import 'package:flutter_demo/core/basic/basic.dart';
import 'package:flutter_demo/demos/ml/engines/ml_tflite_helper.dart';
import 'package:image/image.dart' as img;
import 'package:image_picker/image_picker.dart';
import 'package:tflite_flutter/tflite_flutter.dart';

/// MediaPipe Selfie Multiclass — 人像多类语义分割（LiteRT / TFLite 端侧推理）
///
/// 与 Android `:modules:module_ml` 的 `TFLiteImageSegmentationActivity` 一一对应：
/// 1. 输入 [1, 256, 256, 3] FP32，归一化到 [-1, 1]（`(v - 127.5) / 127.5`），
///    **整图拉伸** —— 掩码相对整幅输入图逐像素输出，裁剪会让掩码与原图整体错位；
/// 2. 输出 [1, 256, 256, 6] 为**平坦** Float32（通道交错），按 `像素序号 * 类别数 + 类别`
///    取通道做 argmax；
/// 3. 输出实测是未归一化的 logits，只做 argmax —— argmax 在 Softmax 前后不变，
///    不需要也不能再叠加 Softmax；
/// 4. 掩码由预览区按底图相同显示矩形拉伸叠加；类别集合是**人像专属**的，
///    非人像场景几乎全部判为背景 / 其他。
///
/// 官方参考：
/// https://github.com/google-ai-edge/litert-samples/tree/main/samples/litert/image_segmentation
class MlImageSegmentationDemoPage extends BasicImagePage {
  const MlImageSegmentationDemoPage({super.key, required super.title});

  @override
  BasicImagePageState<MlImageSegmentationDemoPage> createState() =>
      _MlImageSegmentationDemoPageState();
}

class _MlImageSegmentationDemoPageState
    extends BasicImagePageState<MlImageSegmentationDemoPage> {
  static const String _modelAsset = 'assets/ml/selfie_multiclass.tflite';
  static const int _inputSize = 256;
  static const int _classCount = 6;
  static const int _backgroundIndex = 0;

  /// 模型固定类别名，索引与输出通道一一对应（模型未提供标签表 metadata）
  static const List<String> _classLabels = <String>[
    '背景',
    '头发',
    '身体皮肤',
    '面部皮肤',
    '衣物',
    '其他配饰',
  ];

  /// 与类别一一对应的叠加调色板，背景全透明由解码函数处理
  static const List<int> _classPalette = <int>[
    0xFF000000, // 背景（解码时置透明）
    0xFF7B4B2A, // 头发
    0xFFE8B58C, // 身体皮肤
    0xFFF2C9A8, // 面部皮肤
    0xFF3F6FB5, // 衣物
    0xFF9B59B6, // 其他配饰
  ];

  Interpreter? _interpreter;
  bool _useGpu = false;
  String _engineLabel = '初始化中';

  Uint8List? _previewImageBytes;
  Size? _previewImageSize;
  ui.Image? _maskImage;
  List<({String label, double ratio, int color})> _rankedClasses =
      <({String label, double ratio, int color})>[];

  double _preprocessMs = 0.0;
  double _inferenceMs = 0.0;
  double _postprocessMs = 0.0;

  final ImagePicker _picker = ImagePicker();

  @override
  void initState() {
    super.initState();
    _rebuildModel(useGpu: false);
  }

  @override
  void dispose() {
    _maskImage?.dispose();
    _interpreter?.close();
    super.dispose();
  }

  @override
  List<String> buildList() => <String>[
        '1. 加载金毛犬样本',
        '2. 加载跑车样本',
        '3. 加载咖啡杯样本',
        '4. 相册选图分割',
        '5. 切换 GPU 加速（当前：${_useGpu ? "GPU" : "CPU"}）',
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
          Text(
            '推理引擎: $_engineLabel | 输入 ${_inputSize}x$_inputSize | '
            '模型: selfie_multiclass.tflite',
            style: const TextStyle(fontSize: BasicDemoDimens.fontBody),
          ),
          const SizedBox(height: BasicDemoDimens.itemPadding),
          Expanded(flex: 3, child: _buildImageArea()),
          const SizedBox(height: BasicDemoDimens.itemPadding),
          Expanded(flex: 2, child: _buildStatsPanel()),
        ],
      ),
    );
  }

  Widget _buildImageArea() {
    final Uint8List? bytes = _previewImageBytes;
    final Size? imageSize = _previewImageSize;
    if (bytes == null || imageSize == null) {
      return const ColoredBox(
        color: Color(0xFF212121),
        child: Center(
          child: Icon(Icons.image, color: Colors.white24, size: 48),
        ),
      );
    }

    return ClipRRect(
      borderRadius: BorderRadius.circular(BasicDemoDimens.cornerCard),
      child: LayoutBuilder(
        builder: (BuildContext context, BoxConstraints constraints) {
          final Size areaSize = Size(constraints.maxWidth, constraints.maxHeight);
          // 底图按 BoxFit.contain 显示，掩码必须落在**同一个显示矩形**上才能对齐
          final Rect displayRect = _displayRect(imageSize, areaSize);
          final ui.Image? mask = _maskImage;
          return Stack(
            children: <Widget>[
              Positioned.fill(
                child: Image.memory(bytes, fit: BoxFit.contain),
              ),
              if (mask != null)
                Positioned.fromRect(
                  rect: displayRect,
                  child: IgnorePointer(
                    child: CustomPaint(
                      size: displayRect.size,
                      painter: _MaskOverlayPainter(mask),
                    ),
                  ),
                ),
            ],
          );
        },
      ),
    );
  }

  /// 底图在展示区内的实际矩形（与 Image.memory 的 BoxFit.contain 同算法）
  Rect _displayRect(Size imageSize, Size areaSize) {
    final FittedSizes sizes = applyBoxFit(
      BoxFit.contain,
      imageSize,
      areaSize,
    );
    final double dx = (areaSize.width - sizes.destination.width) / 2;
    final double dy = (areaSize.height - sizes.destination.height) / 2;
    return Rect.fromLTWH(dx, dy, sizes.destination.width, sizes.destination.height);
  }

  Widget _buildStatsPanel() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: <Widget>[
        Text(
          '预处理 ${_preprocessMs.toStringAsFixed(1)} ms | '
          '推理 ${_inferenceMs.toStringAsFixed(1)} ms | '
          '后处理 ${_postprocessMs.toStringAsFixed(1)} ms',
          style: TextStyle(
            fontSize: BasicDemoDimens.fontBody,
            color: Colors.grey.shade600,
          ),
        ),
        const SizedBox(height: BasicDemoDimens.itemPadding),
        Expanded(
          child: _rankedClasses.isEmpty
              ? const Center(child: Text('选择下方样本开始分割'))
              : ListView.builder(
                  itemCount: _rankedClasses.length,
                  itemBuilder: (BuildContext context, int index) {
                    final ({String label, double ratio, int color}) item =
                        _rankedClasses[index];
                    return Padding(
                      padding: const EdgeInsets.symmetric(vertical: 3),
                      child: Row(
                        children: <Widget>[
                          Container(
                            width: 14,
                            height: 14,
                            color: Color(item.color),
                          ),
                          const SizedBox(width: 8),
                          Text(
                            item.label,
                            style: const TextStyle(
                              fontSize: BasicDemoDimens.fontBody,
                            ),
                          ),
                          const Spacer(),
                          Text(
                            '${(item.ratio * 100).toStringAsFixed(1)}%',
                            style: const TextStyle(
                              fontSize: BasicDemoDimens.fontBody,
                              fontFamily: 'monospace',
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

      previous?.close();

      // 已有底图时用新后端重跑一次，避免掩码停留在旧后端的输出上
      final Uint8List? bytes = _previewImageBytes;
      if (bytes != null) {
        await _runSegmentation(bytes);
      }
    } catch (error) {
      debugPrint('初始化模型失败: $error');
      _engineLabel = '初始化失败: $error';
      if (mounted) setState(() {});
    }
  }

  Future<void> _loadSampleAsset(String assetPath) async {
    try {
      final ByteData data = await rootBundle.load(assetPath);
      await _swapPreview(data.buffer.asUint8List());
    } catch (error) {
      debugPrint('加载样本图失败: $error');
    }
  }

  Future<void> _pickImageFromGallery() async {
    try {
      final XFile? file = await _picker.pickImage(source: ImageSource.gallery);
      if (file == null) return;
      await _swapPreview(await file.readAsBytes());
    } catch (error) {
      debugPrint('选择相册图片失败: $error');
    }
  }

  Future<void> _swapPreview(Uint8List bytes) async {
    final img.Image? decoded = img.decodeImage(bytes);
    if (decoded == null || !mounted) return;
    setState(() {
      _previewImageBytes = bytes;
      _previewImageSize = Size(decoded.width.toDouble(), decoded.height.toDouble());
      _maskImage?.dispose();
      _maskImage = null;
      _rankedClasses = <({String label, double ratio, int color})>[];
    });
    await _runSegmentation(bytes);
  }

  Future<void> _runSegmentation(Uint8List rawBytes) async {
    final Interpreter? interpreter = _interpreter;
    if (interpreter == null) return;

    try {
      final Stopwatch preprocessWatch = Stopwatch()..start();

      final img.Image? decoded = img.decodeImage(rawBytes);
      if (decoded == null) return;

      // 输入 256x256x3 FP32，归一化到 [-1, 1]（整图拉伸）
      final Float32List input = MlTfliteHelper.encodeImage(
        decoded,
        _inputSize,
        _inputSize,
        MlInputNormalize.floatMinus1To1,
      );
      preprocessWatch.stop();

      final Stopwatch inferenceWatch = Stopwatch()..start();
      MlTfliteHelper.writeFloatInput(interpreter, 0, input);
      interpreter.invoke();
      final Float32List logits = MlTfliteHelper.readFloatOutput(interpreter);
      inferenceWatch.stop();

      final Stopwatch postprocessWatch = Stopwatch()..start();
      final Int32List mask = MlTfliteHelper.decodeSegmentationMask(
        logits,
        _inputSize,
        _inputSize,
        _classCount,
      );
      final Int32List counts = MlTfliteHelper.countClasses(mask, _classCount);
      final Int32List pixels = MlTfliteHelper.colorizeMask(
        mask,
        _classPalette,
        backgroundTransparent: true,
      );
      // 调色板是 ARGB 字节序，dart:ui 的 rgba8888 需要 R,G,B,A 顺序，这里转换
      final Uint8List rgba = Uint8List(pixels.length * 4);
      for (int i = 0; i < pixels.length; i++) {
        final int argb = pixels[i];
        rgba[i * 4] = (argb >> 16) & 0xFF;
        rgba[i * 4 + 1] = (argb >> 8) & 0xFF;
        rgba[i * 4 + 2] = argb & 0xFF;
        rgba[i * 4 + 3] = (argb >> 24) & 0xFF;
      }
      postprocessWatch.stop();

      final List<({String label, double ratio, int color})> ranked =
          _rankByCount(counts, mask.length);

      // decodeImageFromPixels 是回调式 API（返回 void），不是 Future
      ui.decodeImageFromPixels(
        rgba,
        _inputSize,
        _inputSize,
        ui.PixelFormat.rgba8888,
        (ui.Image image) {
          if (!mounted) {
            image.dispose();
            return;
          }
          setState(() {
            _maskImage?.dispose();
            _maskImage = image;
            _rankedClasses = ranked;
            _preprocessMs = preprocessWatch.elapsedMicroseconds / 1000.0;
            _inferenceMs = inferenceWatch.elapsedMicroseconds / 1000.0;
            _postprocessMs = postprocessWatch.elapsedMicroseconds / 1000.0;
          });
        },
      );
    } catch (error) {
      debugPrint('语义分割异常: $error');
    }
  }

  /// 按像素数排序的类别占比（排除背景，与 Android `buildLegend` 对齐）
  List<({String label, double ratio, int color})> _rankByCount(
    Int32List counts,
    int total,
  ) {
    final List<({String label, double ratio, int color})> ranked =
        <({String label, double ratio, int color})>[];
    for (int i = 0; i < counts.length; i++) {
      if (i == _backgroundIndex || counts[i] <= 0) continue;
      ranked.add((
        label: _classLabels[i],
        ratio: counts[i] / total,
        color: _classPalette[i],
      ));
    }
    ranked.sort(
      (({String label, double ratio, int color}) a,
              ({String label, double ratio, int color}) b) =>
          b.ratio.compareTo(a.ratio),
    );
    return ranked;
  }
}

/// 掩码叠加层：把 256x256 调色板图像拉伸铺满定位矩形
class _MaskOverlayPainter extends CustomPainter {
  final ui.Image mask;

  const _MaskOverlayPainter(this.mask);

  @override
  void paint(Canvas canvas, Size size) {
    paintImage(
      canvas: canvas,
      rect: Offset.zero & size,
      image: mask,
      fit: BoxFit.fill,
    );
  }

  @override
  bool shouldRepaint(covariant _MaskOverlayPainter oldDelegate) =>
      oldDelegate.mask != mask;
}
