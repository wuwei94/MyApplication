/// 一个 ASR 模型完整的推理侧组合体：分词器 + 预处理 + 主图（含解码器）。
///
/// 与 Android `:modules:module_ml` 的 `AsrEngine` 一一对应。
///
/// 官方把这三样散在 `MainActivity` 的字段里各自 new / close，本模块收成一个
/// 可整体释放的组合体，理由有二：
/// 1. 引擎构建必须整块成功或整块失败（页面侧只有一个「就绪 / 失败」状态）；
/// 2. 「构建到一半就抛异常」时能就地回滚 —— 官方在 `preparePipeline` 的 catch 里
///    调 `resetPipeline()` 兜底，这里靠 [open] 内部补偿关闭达到同样效果。
///
/// 与 Android 的差异只有一处：Android 的后端是编译期选项
/// （`TFLiteModelHelper.createFallbackOptions(accelerator)`），
/// 而经典 C API 的加速后端是**运行时挂的 delegate**（XNNPACK / GPU），
/// 因此「切后端」在两端都是重建引擎，但失败回落点在 [open] 里。
library;

import 'dart:io';

import 'package:flutter/foundation.dart';
import 'package:flutter_demo/demos/ml/engines/asr/asr_decoder.dart';
import 'package:flutter_demo/demos/ml/engines/asr/asr_model_catalog.dart';
import 'package:flutter_demo/demos/ml/engines/asr/asr_preprocess.dart';
import 'package:flutter_demo/demos/ml/engines/asr/asr_tokenizer.dart';
import 'package:flutter_demo/demos/ml/engines/ml_signature.dart';
import 'package:flutter_demo/demos/ml/engines/ml_tflite_helper.dart';

/// ASR 推理组合体
///
/// 生成实例**必须**整体释放（[close]）：主图的原生句柄由 [MlSignatureGraph] 持有，
/// 关闭期间并发 `invoke()` 会踩已释放内存。页面侧统一由后台 isolate 串行调度，
/// 因此「构建 / 推理 / 释放」天然排在同一条消息队列上。
class AsrEngine {
  AsrEngine._(
    this.graph,
    this.tokenizer,
    this.audioPreprocessor,
    this.decoder,
    this.backendLabel,
  );

  /// 官方全链路统一按 16 kHz 单声道处理
  static const int sampleRate = 16000;

  final AsrSignatureGraph graph;

  /// 分词器只做「id → 文本」一个方向，因此整张词表常驻即可
  final AsrHfTokenizer tokenizer;

  /// `logMelSpectro` 为 null 的模型（Moonshine）直接吃原始波形
  final AsrPreprocessor audioPreprocessor;

  final AsrDecoder decoder;

  /// 实际生效的后端描述（GPU 建不出来时如实写着「已回退 CPU」）
  final String backendLabel;

  /// 该模型一次能吃多长的音频（与音频源的 `interval` 对齐）
  int get inputMilliseconds => graph.config.inputMilliseconds;

  /// 跑一次识别：`encode` → 解码器
  List<AsrStep> recognize(Float32List features) {
    graph.runEncode(features);
    return decoder.decode();
  }

  /// 释放整条链（幂等）
  ///
  /// 分词器与预处理都是纯 Dart 对象，无需释放；唯一要回收的是原生图。
  void close() => graph.graph.close();

  /// 从本地文件构建：模型图 + 分词器 + 预处理 + 解码器
  ///
  /// [useGpu] 为真时先试 GPU delegate，建不出来（设备无可用 GPU 后端、驱动异常）
  /// 就地回落 CPU，并把实际后端写进 [backendLabel] —— 与 Android 页
  /// 「不静默降级」的语义一致：界面上必须看得出真正跑在哪。
  static AsrEngine open({
    required File modelFile,
    required File tokenizerFile,
    required AsrModelConfig config,
    bool useGpu = false,
  }) {
    final AsrHfTokenizer tokenizer = AsrHfTokenizer.fromFile(tokenizerFile);
    final AsrLogMelConfig? logMel = config.logMelSpectro;
    final AsrPreprocessor preprocessor = logMel == null
        ? const AsrDummyPreprocessor()
        : AsrMelSpectroProcessor(sampleRate, logMel);

    final int threads = MlTfliteHelper.cpuThreads;
    final String cpuLabel = 'CPU ($threads 线程 + XNNPACK)';

    MlSignatureGraph? signatureLayer;
    String backendLabel = cpuLabel;
    if (useGpu) {
      try {
        signatureLayer = MlSignatureGraph.open(
          modelFile.path,
          numThreads: threads,
          gpu: true,
          // 与官方 LiteRtRunner 同参：清单里 gpuEnforceFloat32 默认 true，
          // 即 GPU 上强制 fp32，不允许精度损失
          gpuAllowPrecisionLoss: !config.gpuEnforceFloat32,
        );
        backendLabel = 'GPU 硬件加速 (GpuDelegateV2)';
      } catch (error) {
        // GPU 建不出来不是错误，是设备能力问题；如实回落并记在标签里
        debugPrint('ASR 的 GPU delegate 创建失败，回退 CPU: $error');
        backendLabel = 'GPU 不可用，已回退 CPU ($threads 线程 + XNNPACK)';
      }
    }
    final MlSignatureGraph graph = signatureLayer ??
        MlSignatureGraph.open(modelFile.path, numThreads: threads);

    try {
      final AsrSignatureGraph signatureGraph = AsrSignatureGraph(graph, config);
      final AsrDecoder decoder = asrDecoderFor(signatureGraph, tokenizer);
      return AsrEngine._(
        signatureGraph,
        tokenizer,
        preprocessor,
        decoder,
        backendLabel,
      );
    } catch (_) {
      // 解码器构造失败时主图已经建好了，就地关掉避免泄漏
      graph.close();
      rethrow;
    }
  }
}
