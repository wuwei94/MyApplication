import 'dart:io';

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart' show rootBundle;
import 'package:flutter_demo/core/basic/basic.dart';
import 'package:flutter_demo/demos/ml/engines/ml_model_download.dart';
import 'package:flutter_demo/demos/ml/engines/ml_tflite_helper.dart';
import 'package:image/image.dart' as img;
import 'package:image_picker/image_picker.dart';
import 'package:tflite_flutter/tflite_flutter.dart';

/// EfficientNet-Lite — 图像物体分类 Top-5（LiteRT / TFLite 端侧推理）
///
/// 与 Android `:modules:module_ml` 的 `TFLiteImageClassificationActivity` 一一对应：
/// 1. 两个模型可切换：`efficientnet_lite0`（随包，输入 224x224）与
///    `efficientnet_lite2`（首次选中时下载 24 MB，输入 **260x260** —— 两个模型的输入
///    尺寸不同，官方样例写死 224，切到 Lite2 后数组长度与输入张量不符，本页按模型取尺寸）；
/// 2. 归一化到 [-1, 1]（`(v - 127.5) / 127.5`），**整图拉伸**重采样
///    （官方 `createScaledBitmap` 口径）；
/// 3. 标签表 `efficientnet_labels.txt`（1000 行 ImageNet 类、无背景类）。两个模型的
///    `TFLITE_METADATA` 实测都只有 1088 字节、不含官方注释所称的
///    `labels_without_background.txt`，因此标签以独立资产分发、两模型共用
///    （三个样本实测：两模型 top-1 索引完全一致，类别顺序相同）；
/// 4. 输出即概率（末算子 SOFTMAX），**没有背景类**，不需要反量化也不需要 Softmax；
/// 5. CPU / GPU 切换即重建引擎。
///
/// 官方参考：
/// https://github.com/google-ai-edge/litert-samples/tree/main/samples/litert/image_classification
class MlImageClassificationDemoPage extends BasicImagePage {
  const MlImageClassificationDemoPage({super.key, required super.title});

  @override
  BasicImagePageState<MlImageClassificationDemoPage> createState() =>
      _MlImageClassificationDemoPageState();
}

/// 页面可选的模型
///
/// [remote] 为 null 表示随包资产；否则先确保下载完成，再用文件加载解释器。
/// [defaultInputEdge] 是该模型输入张量的实测边长。
enum _ImageModel {
  lite0('Lite0', 'assets/ml/efficientnet_lite0.tflite', null, 224),
  lite2('Lite2', 'efficientnet_lite2.tflite', MlRemoteModels.efficientnetLite2, 260);

  const _ImageModel(this.displayName, this.path, this.remote, this.defaultInputEdge);

  final String displayName;
  final String path;
  final MlRemoteModel? remote;
  final int defaultInputEdge;
}

class _MlImageClassificationDemoPageState
    extends BasicImagePageState<MlImageClassificationDemoPage> {
  static const String _labelsAsset = 'assets/ml/efficientnet_labels.txt';

  Interpreter? _interpreter;
  List<String> _labels = <String>[];
  bool _useGpu = false;
  _ImageModel _model = _ImageModel.lite0;

  /// 当前解释器实际对应的模型；null 表示引擎尚未建好。
  /// 加载失败时用它把选择器拨回去，避免「界面显示 Lite2、实际没有引擎」。
  _ImageModel? _activeModel;

  int _inputEdge = _ImageModel.lite0.defaultInputEdge;
  String _engineLabel = '初始化中';
  bool _isProcessing = false;

  /// 首次选中 Lite2 时的下载进度（0..1）；非下载期保持上次值即可
  bool _downloading = false;
  double _downloadProgress = 0.0;
  MlModelDownloader? _downloader;

  /// 初始化代次：每次 [_initModel] 自增，await 之后用它判断本轮是否已被取代
  int _initGeneration = 0;

  /// 正在进行的下载：同一时刻只允许一个，后到者复用它而不是再发一遍请求
  ///
  /// 否则「下载中切 GPU / 切模型」会让两个协程同时写同一个 `.part`，
  /// 落地一个长度正确但内容交错的坏模型。
  Future<void>? _ensureFuture;

  Uint8List? _previewImageBytes;
  List<({String label, double score})> _top5Results =
      <({String label, double score})>[];
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
    // 页面销毁时中断下载，保留 .part 供下次续传
    _downloader?.cancel();
    _interpreter?.close();
    super.dispose();
  }

  @override
  List<String> buildList() => <String>[
        '1. 加载金毛犬样本',
        '2. 加载跑车样本',
        '3. 加载咖啡杯样本',
        '4. 相册选图识别',
        '5. 切换 GPU 加速',
        _model == _ImageModel.lite0 ? '6. 切换 Lite2 (首次需下载)' : '6. 切回 Lite0',
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
        _initModel(model: _model, useGpu: _useGpu);
      case 5:
        final _ImageModel target =
            _model == _ImageModel.lite0 ? _ImageModel.lite2 : _ImageModel.lite0;
        _model = target;
        if (mounted) setState(() {});
        _initModel(model: target, useGpu: _useGpu);
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
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: <Widget>[
        Text(
          '推理引擎: $_engineLabel',
          style: TextStyle(
            fontSize: BasicDemoDimens.fontTitle,
            fontWeight: FontWeight.bold,
            color: _useGpu ? Colors.deepPurple : Colors.blue.shade700,
          ),
        ),
        if (_downloading) ...<Widget>[
          const SizedBox(height: 6),
          LinearProgressIndicator(
            value: _downloadProgress > 0 ? _downloadProgress : null,
          ),
        ],
        const SizedBox(height: 2),
        Text(
          '预处理 ${_preprocessMs.toStringAsFixed(1)} ms | '
          '推理 ${_inferenceMs.toStringAsFixed(1)} ms',
          style: TextStyle(
            fontSize: BasicDemoDimens.fontBody,
            color: Colors.grey.shade600,
          ),
        ),
        const SizedBox(height: BasicDemoDimens.itemPadding),
        Expanded(
          child: _top5Results.isEmpty
              ? const Center(child: Text('选择下方样本开始识别'))
              : ListView.builder(
                  itemCount: _top5Results.length,
                  itemBuilder: (BuildContext context, int index) {
                    final ({String label, double score}) item =
                        _top5Results[index];
                    final String percent =
                        '${(item.score * 100).toStringAsFixed(2)}%';
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
                              item.label,
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

  /// 异步有序初始化：加载标签 -> 确保模型就绪（必要时下载）-> 构建引擎
  ///
  /// 用代次号（[_initGeneration]）而不是「比较 `_model`」来判定是否让位：连点两次切换时，
  /// 两次调用的 target 可能相同（切走再切回），只比模型名会让前一轮继续覆盖后一轮的解释器，
  /// 造成引擎泄漏与状态错乱。每个 await 之后都要重新校验代次。
  Future<void> _initModel({_ImageModel? model, bool useGpu = false}) async {
    final int generation = ++_initGeneration;
    final _ImageModel target = model ?? _model;
    final Interpreter? previous = _interpreter;
    _interpreter = null;
    setState(() {
      _engineLabel = '初始化中';
    });

    try {
      if (_labels.isEmpty) {
        _labels = await MlTfliteHelper.loadLines(_labelsAsset);
      }

      await _ensureModel(target);
      if (generation != _initGeneration) {
        previous?.close();
        return;
      }

      final MlEngineBundle built = await _createEngine(target, useGpu);
      if (generation != _initGeneration) {
        // 本轮已被取代：刚建好的解释器没人接手，就地释放后再让位
        built.interpreter.close();
        previous?.close();
        return;
      }
      // 先取尺寸再接管解释器：这一步一旦抛异常，`_interpreter` 还没指向新引擎，
      // 失败分支把旧引擎还回去时不会把新引擎漏掉
      final int edge = _readInputEdge(built.interpreter, target.defaultInputEdge);
      _interpreter = built.interpreter;
      _activeModel = target;
      _inputEdge = edge;
      _engineLabel = '${built.engineLabel} · ${target.displayName} · '
          '输入 $_inputEdge×$_inputEdge';
      if (mounted) setState(() {});

      previous?.close();

      // 重推理当前图片（无图时加载默认样本）
      if (_previewImageBytes != null) {
        await _runClassification(_previewImageBytes!);
      } else {
        await _loadSampleAsset('assets/ml/sample_dog.jpg');
      }
    } catch (error) {
      debugPrint('初始化模型失败: $error');
      if (generation != _initGeneration) {
        // 本轮已被后一次初始化取代：旧引擎没人接手，就地释放，别让它只等 GC
        previous?.close();
        return;
      }
      // 与 Android 同语义：构建失败时旧引擎仍然有效，把它还回字段并复位选择，
      // 否则页面会停在「显示 Lite2、实际没有引擎」的半死状态
      _interpreter = previous;
      final _ImageModel? fallback = _activeModel;
      if (fallback != null) _model = fallback;
      _engineLabel = fallback == null
          ? '加载 ${target.displayName} 失败: $error'
          : '加载 ${target.displayName} 失败: $error（仍使用 ${fallback.displayName}）';
      if (mounted) setState(() {});
    }
  }

  /// 读取解释器输入张量的正方形边长；形状不可用时回退到枚举里的实测值。
  ///
  /// 与 Android `TFLiteModelHelper.readInputEdge` 同口径：读不到不是错误，
  /// 两个模型的已知值（224 / 260）本来就能兜住。
  int _readInputEdge(Interpreter interpreter, int fallback) {
    try {
      final List<int> shape = interpreter.getInputTensor(0).shape;
      return shape.length == 4 && shape[1] > 0 && shape[1] == shape[2]
          ? shape[1]
          : fallback;
    } catch (error) {
      debugPrint('读取输入张量形状失败，回退 $fallback: $error');
      return fallback;
    }
  }

  /// 确保模型文件已就绪：Lite0 随包、无需处理；Lite2 首次选中时下载
  ///
  /// 同一时刻只允许一个下载：已有下载在跑时直接复用它的 Future。这里**不能**用
  /// 「先判 pending、再发请求」的写法 —— `pendingBytes` 与实际开始写 `.part` 之间
  /// 还有 await，两个调用都会看到 pending > 0，于是并发写同一个文件。
  Future<void> _ensureModel(_ImageModel target) {
    final Future<void>? running = _ensureFuture;
    if (running != null) return running;

    final Future<void> future = _downloadModel(target)
        .whenComplete(() => _ensureFuture = null);
    _ensureFuture = future;
    return future;
  }

  Future<void> _downloadModel(_ImageModel target) async {
    final MlRemoteModel? remote = target.remote;
    if (remote == null) return;

    final int pending = await MlModelStore.pendingBytes(<MlRemoteModel>[remote]);
    if (pending <= 0) return;

    _downloading = true;
    _downloadProgress = 0.0;
    if (mounted) {
      setState(() {
        _engineLabel =
            '首次使用，需下载 ${target.displayName} 约 ${mlFormatBytes(pending)}…';
      });
    }

    try {
      final MlModelDownloader downloader = _downloader ??= MlModelDownloader();
      await downloader.ensureAll(
        <MlRemoteModel>[remote],
        onProgress: (int index, int downloaded, int total, int overall,
            int overallTotal) {
          if (!mounted) return;
          setState(() {
            final int percent = overallTotal > 0
                ? (overall * 100 / overallTotal).round().clamp(0, 100)
                : 0;
            _downloadProgress = overallTotal > 0 ? overall / overallTotal : 0.0;
            _engineLabel = '下载 ${target.displayName} · $percent% '
                '(${mlFormatBytes(overall)} / ${mlFormatBytes(overallTotal)})';
          });
        },
      );
    } finally {
      _downloading = false;
      if (mounted) setState(() {});
    }
  }

  /// 随包模型走 assets，下载模型走文件（`Interpreter.fromFile` 与生产路径一致）
  Future<MlEngineBundle> _createEngine(_ImageModel target, bool useGpu) async {
    final MlRemoteModel? remote = target.remote;
    if (remote == null) {
      return MlTfliteHelper.createInterpreter(target.path, useGpu: useGpu);
    }
    final File file = await MlModelStore.fileFor(remote);
    return MlTfliteHelper.createInterpreterFromFile(file, useGpu: useGpu);
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
        if (mounted) setState(() => _isProcessing = false);
        return;
      }

      // EfficientNet-Lite 输入 N×N×3 FP32，归一化到 [-1, 1]（整图拉伸）
      final Float32List input = MlTfliteHelper.encodeImage(
        decoded,
        _inputEdge,
        _inputEdge,
        MlInputNormalize.floatMinus1To1,
      );
      preprocessWatch.stop();

      final Stopwatch inferenceWatch = Stopwatch()..start();
      MlTfliteHelper.writeFloatInput(interpreter, 0, input);
      interpreter.invoke();
      final Float32List scores = MlTfliteHelper.readFloatOutput(interpreter);
      inferenceWatch.stop();

      // 输出 1000 类与标签表 1000 行严格一一对应：索引 i 就是 labels[i]，
      // 既没有背景类需要置零，也不需要做 -1 偏移
      final List<({int index, double score})> ranked =
          MlTfliteHelper.topK(scores, k: 5);
      final List<({String label, double score})> top5 = ranked
          .map((({int index, double score}) entry) => (
                label: entry.index < _labels.length
                    ? _labels[entry.index]
                    : '类别 #${entry.index}',
                score: entry.score,
              ))
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
