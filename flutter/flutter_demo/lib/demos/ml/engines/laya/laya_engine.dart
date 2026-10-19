/// 单行多语 Laya 执行 —— 与 Android `LayaEngine` 对应。
///
/// 结构对应（三张图 + 两套数据文件）：
/// - 主图 `laya_ml_s256_embeds_wfp16.tflite`：吃 `inputs_embeds` / `attention_mask` /
///   `qtype_onehot`，产出 `token_logits`（逐位置选项打分）与 `pooled_cls`；
/// - 动作头 `laya_ml_act_head_fp32.tflite`：吃 `pooled_cls` + 由 marker 位置推出的 `feats`，
///   产出 `act_logits`。官方把选项置信度拆成「主图打分 + 小头校准」两步；
/// - 词表 `tokenizer.json`（约 33 MB，BPE）+ 嵌入表 `token_embeddings_fp16.bin`（约 375 MB）。
///
/// 与 Android 实现的差异（Flutter 侧 API 限定）：
/// - 命名张量寻址：`tflite_flutter` 的 `Tensor.name` 可用，因此在构造时按名字建
///   「名字 → 索引」映射，规避「签名顺序与 FlatBuffer 张量顺序不一致」；
/// - 串行性：Flutter 侧没有 `limitedParallelism(1)` 的引擎队列，页面自行保证
///   同一时刻只有一次推理（与现有 4 个页面同一约定）；
/// - GPU 精度：官方要求 GPU 显式 FP32（`isPrecisionLossAllowed: false`）；
///   构建失败时按页面既有约定自动重试一次 CPU，并把原因如实写进状态栏。
library;

import 'dart:io';
import 'dart:typed_data';

import 'package:flutter_demo/demos/ml/engines/laya/laya_calibration.dart';
import 'package:flutter_demo/demos/ml/engines/laya/laya_decoder.dart';
import 'package:flutter_demo/demos/ml/engines/laya/laya_embeddings.dart';
import 'package:flutter_demo/demos/ml/engines/laya/laya_prompt_builder.dart';
import 'package:flutter_demo/demos/ml/engines/laya/laya_tokenizer.dart';
import 'package:flutter_demo/demos/ml/engines/ml_model_download.dart';
import 'package:flutter_demo/demos/ml/engines/ml_tflite_helper.dart';
import 'package:tflite_flutter/tflite_flutter.dart';

/// 单行多语 Laya 执行；GPU 显式请求 FP32。
class LayaEngine {
  LayaEngine({this.window = 256}) {
    if (window != 256 && window != 512) {
      throw ArgumentError('Only multilingual windows 256 and 512 are supported');
    }
  }

  /// 只支持 256 与 512 两个多语窗口。
  final int window;

  /// 模型文件名清单（无论图存哪种方式都需要的共享文件）
  static const List<String> requiredFiles = <String>[
    'laya_ml_act_head_fp32.tflite',
    'laya_ml_calibration.json',
    'tokenizer.json',
    'token_embeddings_fp16.bin',
    'token_embeddings.json',
  ];

  late final LayaTokenizer tokenizer;
  late final LayaCalibration calibration;
  late final LayaEmbeddings embeddings;

  /// 分词器初始化耗时（毫秒）。
  late final double tokenizerLoadMs;

  /// BPE 词表条目数；仅用于状态栏展示，不参与推理。
  late final int tokenizerSize;

  /// 元数据解析与表映射耗时（毫秒）。
  late final double embeddingLoadMs;

  /// 主图与动作图（按后端缓存；Flutter 侧 delegate 挂在实例上，切后端=重建解释器）
  final Map<String, _LayaGraph> _mainGraphs = <String, _LayaGraph>{};
  final Map<String, _LayaGraph> _actGraphs = <String, _LayaGraph>{};

  /// 嵌入查找的复用目标缓冲
  Float32List? _embeddingBuffer;

  bool _closed = false;

  /// 主图文件名（固定 wfp16 存储变体）
  String get mainFilename => 'laya_ml_s${window}_embeds_wfp16.tflite';

  /// 加载分词器 / 标定 / 嵌入表元数据（不含编译计算图）。
  ///
  /// 对应 Android 的 `init {}` 块；图编译留给 [initialize]（与计时分开，
  /// 这样首次图调用能保持「冷启动」观测）。
  Future<void> loadTables() async {
    _checkOpen();
    final Stopwatch watch = Stopwatch()..start();
    tokenizer = LayaTokenizer(await _requireFile('tokenizer.json'));
    watch.stop();
    tokenizerLoadMs = watch.elapsedMicroseconds / 1000.0;
    tokenizerSize = tokenizer.vocabularySize;

    calibration = await LayaCalibration.load(await _requireFile('laya_ml_calibration.json'));

    final Stopwatch embeddingWatch = Stopwatch()..start();
    embeddings = LayaEmbeddings(
      await _requireFile('token_embeddings_fp16.bin'),
      await _requireFile('token_embeddings.json'),
    );
    embeddingWatch.stop();
    embeddingLoadMs = embeddingWatch.elapsedMicroseconds / 1000.0;
  }

  /// 编译指定后端的计算图。
  Future<void> initialize({required bool useGpu}) async {
    _checkOpen();
    await _mainGraph(useGpu);
    await _actGraph(useGpu);
  }

  /// 门禁与交互 App 使用完全相同的分词器与 prompt 构建器。
  LayaSequence prepare(Object? state, Map<String, Object?> question,
      {String questionId = ''}) {
    _checkOpen();
    return LayaPromptBuilder(tokenizer, maxLen: window, headMaxLen: 256)
        .build(state, question, questionId: questionId);
  }

  /// 一次主图调用 + 一次动作头调用。非有限的主图输出不会送入动作头。
  Future<({Float32List tokenLogits, Float32List pooledCls, Float32List markerLogits, Float32List actFeatures, Float32List actLogits, double embeddingLookupMs, double featureMs, double callMs})>
      runRaw(LayaSequence sequence, {required bool useGpu}) async {
    _checkOpen();
    if (sequence.ids.length > window) {
      throw ArgumentError('Sequence exceeds window $window');
    }
    if (sequence.markers.length != sequence.question.optionCount) {
      throw ArgumentError('Missing option marker');
    }
    final _LayaGraph main = await _mainGraph(useGpu);
    final _LayaGraph act = await _actGraph(useGpu);

    final Float32List attention = Float32List(window);
    for (int i = 0; i < window; i++) {
      attention[i] = i < sequence.ids.length ? 1.0 : 0.0;
    }
    final Float32List qtype = Float32List(3);
    qtype[sequence.question.qtype] = 1.0;

    final Stopwatch lookupWatch = Stopwatch()..start();
    final Float32List embeds =
        _embeddingBuffer ??= Float32List(window * LayaEmbeddings.width);
    await embeddings.gather(sequence.ids, window, embeds);
    lookupWatch.stop();

    final Stopwatch callWatch = Stopwatch()..start();
    _writeFloat(main, 'inputs_embeds', embeds);
    _writeFloat(main, 'attention_mask', attention);
    _writeFloat(main, 'qtype_onehot', qtype);
    main.interpreter.invoke();
    final Float32List tokens = _readFloat(main, 'token_logits');
    final Float32List pooled = _readFloat(main, 'pooled_cls');
    callWatch.stop();
    if (tokens.length != window || pooled.length != 768) {
      throw StateError('Unexpected main graph output shape: '
          'token_logits=${tokens.length}, pooled_cls=${pooled.length}');
    }

    final Float32List markers = LayaDecoder.gather(tokens, sequence.markers);
    final Float32List nanActFeatures = Float32List.fromList(<double>[
      double.nan,
      double.nan,
      double.nan,
      double.nan,
    ]);
    final Float32List nanActLogits = Float32List.fromList(<double>[double.nan, double.nan]);
    if (tokens.any((double value) => !value.isFinite) ||
        pooled.any((double value) => !value.isFinite)) {
      return (
        tokenLogits: tokens,
        pooledCls: pooled,
        markerLogits: markers,
        actFeatures: nanActFeatures,
        actLogits: nanActLogits,
        embeddingLookupMs: lookupWatch.elapsedMicroseconds / 1000.0,
        featureMs: 0.0,
        callMs: callWatch.elapsedMicroseconds / 1000.0,
      );
    }

    final Stopwatch featureWatch = Stopwatch()..start();
    final Float32List features = LayaDecoder.actFeatures(markers);
    featureWatch.stop();
    _writeFloat(act, 'pooled_cls', pooled);
    _writeFloat(act, 'feats', features);
    act.interpreter.invoke();
    final Float32List action = _readFloat(act, 'act_logits');
    if (action.length != 2) {
      throw StateError('Unexpected action graph output shape: ${action.length}');
    }
    return (
      tokenLogits: tokens,
      pooledCls: pooled,
      markerLogits: markers,
      actFeatures: features,
      actLogits: action,
      embeddingLookupMs: lookupWatch.elapsedMicroseconds / 1000.0,
      featureMs: featureWatch.elapsedMicroseconds / 1000.0,
      callMs: callWatch.elapsedMicroseconds / 1000.0,
    );
  }

  /// 运行构建器、两张图与标定解码，并记录各阶段耗时。
  Future<({LayaSequence sequence, Map<String, Object?> answer, Float32List markerLogits, Float32List actLogits, double prepareMs, double decodeMs, double totalMs})>
      answer(
    Object? state,
    Map<String, Object?> question, {
    required bool useGpu,
    LayaCalibration? calibrationOverride,
    String questionId = '',
  }) async {
    final Stopwatch started = Stopwatch()..start();
    final LayaSequence sequence =
        prepare(state, question, questionId: questionId);
    final Stopwatch prepared = Stopwatch()..start();
    final raw = await runRaw(sequence, useGpu: useGpu);
    if (raw.markerLogits.any((double value) => !value.isFinite) ||
        raw.pooledCls.any((double value) => !value.isFinite)) {
      throw StateError('Nonfinite model output; inspect the device gate report');
    }
    final Stopwatch decodeStarted = Stopwatch()..start();
    final Map<String, Object?> decoded = LayaDecoder.decodeQuestion(
      raw.markerLogits,
      raw.actLogits,
      sequence.question,
      calibrationOverride ?? calibration,
    );
    decodeStarted.stop();
    prepared.stop();
    started.stop();
    return (
      sequence: sequence,
      answer: decoded,
      markerLogits: raw.markerLogits,
      actLogits: raw.actLogits,
      prepareMs: (prepared.elapsedMicroseconds - started.elapsedMicroseconds) / 1000.0,
      decodeMs: decodeStarted.elapsedMicroseconds / 1000.0,
      totalMs: started.elapsedMicroseconds / 1000.0,
    );
  }

  Future<_LayaGraph> _mainGraph(bool useGpu) async {
    final String key = '$useGpu';
    final _LayaGraph? cached = _mainGraphs[key];
    if (cached != null) return cached;
    final _LayaGraph graph = await _createGraph(
      mainFilename,
      useGpu,
      <String>['attention_mask', 'inputs_embeds', 'qtype_onehot'],
      <String>['pooled_cls', 'token_logits'],
    );
    _mainGraphs[key] = graph;
    return graph;
  }

  Future<_LayaGraph> _actGraph(bool useGpu) async {
    final String key = '$useGpu';
    final _LayaGraph? cached = _actGraphs[key];
    if (cached != null) return cached;
    final _LayaGraph graph = await _createGraph(
      'laya_ml_act_head_fp32.tflite',
      useGpu,
      <String>['feats', 'pooled_cls'],
      <String>['act_logits'],
    );
    _actGraphs[key] = graph;
    return graph;
  }

  Future<_LayaGraph> _createGraph(
    String filename,
    bool useGpu,
    List<String> inputNames,
    List<String> outputNames,
  ) async {
    // Laya 主图要求 GPU 显式 FP32（官方样例明确指定）——这属于模型规格而非性能调优。
    final MlEngineBundle bundle =
        await MlTfliteHelper.createInterpreterFromFile(
      await _requireFile(filename),
      useGpu: useGpu,
      isPrecisionLossAllowed: false,
    );
    return _LayaGraph(bundle.interpreter, inputNames, outputNames);
  }

  void _writeFloat(_LayaGraph graph, String name, Float32List data) {
    final int index = graph.inputIndex(name);
    MlTfliteHelper.writeFloatInput(graph.interpreter, index, data);
  }

  Float32List _readFloat(_LayaGraph graph, String name) {
    final int index = graph.outputIndex(name);
    return MlTfliteHelper.readFloatOutput(graph.interpreter, tensorIndex: index);
  }

  Future<File> _requireFile(String fileName) async {
    final String path = await MlModelStore.pathIn(fileName);
    final File file = File(path);
    if (!file.existsSync() || file.lengthSync() == 0) {
      throw StateError('模型尚未下载: $fileName');
    }
    return file;
  }

  void _checkOpen() {
    if (_closed) {
      throw StateError('LayaEngine is closed');
    }
  }

  /// 关闭模型缓冲并释放表引用。
  Future<void> close() async {
    if (_closed) return;
    _closed = true;
    for (final _LayaGraph graph in _mainGraphs.values) {
      graph.interpreter.close();
    }
    for (final _LayaGraph graph in _actGraphs.values) {
      graph.interpreter.close();
    }
    _mainGraphs.clear();
    _actGraphs.clear();
    _embeddingBuffer = null;
    await embeddings.close();
  }
}

/// 一张图：解释器 + 「名字 → 索引」映射（签名顺序与 FlatBuffer 张量顺序不一致）。
class _LayaGraph {
  _LayaGraph(this.interpreter, List<String> inputNames, List<String> outputNames) {
    for (int i = 0; i < interpreter.getInputTensors().length; i++) {
      final String name = interpreter.getInputTensor(i).name;
      _inputIndexByName.putIfAbsent(name, () => i);
    }
    for (int i = 0; i < interpreter.getOutputTensors().length; i++) {
      final String name = interpreter.getOutputTensor(i).name;
      _outputIndexByName.putIfAbsent(name, () => i);
    }
    for (final String name in inputNames) {
      if (!_inputIndexByName.containsKey(name)) {
        throw StateError('图缺少输入张量: $name');
      }
    }
    for (final String name in outputNames) {
      if (!_outputIndexByName.containsKey(name)) {
        throw StateError('图缺少输出张量: $name');
      }
    }
  }

  final Interpreter interpreter;
  final Map<String, int> _inputIndexByName = <String, int>{};
  final Map<String, int> _outputIndexByName = <String, int>{};

  int inputIndex(String name) => _inputIndexByName[name]!;
  int outputIndex(String name) => _outputIndexByName[name]!;
}
