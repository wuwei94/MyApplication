/// ASR 模型清单 —— 与 Android `:modules:module_ml` 的 `AsrModelCatalog` /
/// `AsrModelConfig` 一一对应。
///
/// 解析的是 asset 里的 `asr_model_metadata.json`：**结构与官方同源**，只做了两处
/// 必要改动 —— 下载地址换成可达的镜像站，以及补上官方 schema 里没有的
/// `approxBytes*` / `supportsGpu` 两个字段（用于界面上「首次需下载约 xx MB」提示
/// 与后端下拉框的可用性）。
///
/// 与官方一样是**纯 JSON 驱动**：新增模型只需往 JSON 里加一项，代码零改动。
library;

import 'dart:convert';

import 'package:flutter/services.dart' show rootBundle;
import 'package:flutter_demo/demos/ml/engines/asr/asr_preprocess.dart';
import 'package:flutter_demo/demos/ml/engines/ml_model_download.dart';

/// 一个 ASR 模型 + 分词器的完整配置（对应官方 `ModelConfig`）。
///
/// 与官方 `ModelConfig` 的差异有两处，都是**去掉本模块不支持的维度**：
/// - 去掉 `npuModelPath` / `npuModelRemoteUrlPattern` / `npuHighPerformance`。
///   官方默认就把 NPU 运行时库注释掉了（`build.gradle.kts` 里
///   `dynamicFeatures.add(":litert_npu_runtime_libraries:…")` 整块是注释），
///   且需要按 SoC 走不同的动态特性模块；本模块没有 NPU 设备可验证，故不保留这条路。
/// - 去掉 `hasDecoder`：官方算了它却从没被读过（解码器分派是按模型名里的
///   `-ctc` / `-tdt` 走的），这个字段在官方代码里是死代码，不迁移。
class AsrModelConfig {
  const AsrModelConfig({
    required this.key,
    required this.modelFileName,
    required this.modelUrl,
    required this.tokenizerFileName,
    required this.tokenizerUrl,
    required this.inputMilliseconds,
    required this.logMelSpectro,
    required this.decodeStartTokenId,
    required this.decodeStopTokenId,
    required this.decodeSkipUntilTokenId,
    required this.gpuEnforceFloat32,
    required this.gpuShareConstantTensors,
    required this.supportsGpu,
    required this.approxModelBytes,
    required this.approxTokenizerBytes,
  });

  /// 模型名，同时也是元数据 JSON 的键与下拉框显示值
  final String key;
  final String modelFileName;
  final String modelUrl;
  final String tokenizerFileName;
  final String tokenizerUrl;
  final int inputMilliseconds;

  /// `null` 表示该模型**吃原始波形**（Moonshine），此时走 [AsrDummyPreprocessor]
  final AsrLogMelConfig? logMelSpectro;
  final int decodeStartTokenId;
  final int decodeStopTokenId;
  final int decodeSkipUntilTokenId;

  /// GPU 上是否强制 fp32（官方清单字段 `gpuEnforceFloat32`）
  ///
  /// 官方 `LiteRtRunner` 在 GPU 上按此字段选 `Precision.FP32` / `Precision.DEFAULT`，
  /// schema 默认值是 **true**。本侧对应 `GpuDelegateV2` 的
  /// `isPrecisionLossAllowed`（取反后传入，见 `MlSignatureGraph.open`）。
  final bool gpuEnforceFloat32;

  /// 是否允许 GPU 共享常量张量（官方 `gpuShareConstantTensors`，官方默认 true）
  ///
  /// 1.4.0 的 `GpuDelegateV2` 没有对应开关（2.x 的 `CompiledModel.GpuOptions` 才有），
  /// 这里只为与官方 schema 对齐而保留字段，不参与本侧配置。
  final bool gpuShareConstantTensors;

  /// 是否支持 GPU 后端（官方 README 的能力表里 Qwen3-ASR 只标了 CPU）
  final bool supportsGpu;
  final int approxModelBytes;
  final int approxTokenizerBytes;

  /// 该配置需要下载的总字节数（仅用于「首次需下载约 xx MB」提示）
  int get approxBytes => approxModelBytes + approxTokenizerBytes;

  /// 该配置需要下载的文件（模型 + 分词器）
  List<MlRemoteModel> remoteModels() => <MlRemoteModel>[
        _remoteModelOf(modelFileName, modelUrl, approxModelBytes),
        _remoteModelOf(tokenizerFileName, tokenizerUrl, approxTokenizerBytes),
      ];

  /// 从 `asr_model_metadata.json` 里的一项解析
  static AsrModelConfig fromJson(String key, Map<String, Object?> json) {
    final String modelUrl = json['modelRemoteUrl'] as String? ?? '';
    final String tokenizerUrl = json['tokenizerUrl'] as String? ?? '';
    if (modelUrl.isEmpty || tokenizerUrl.isEmpty) {
      throw FormatException('$key 缺少模型或分词器地址');
    }
    return AsrModelConfig(
      key: key,
      modelFileName: modelUrl.substring(modelUrl.lastIndexOf('/') + 1),
      modelUrl: modelUrl,
      // 六个分词器的远端文件名**都叫 tokenizer.json**，扁平落盘会互相覆盖，
      // 因此落盘名带上模型名（与官方按 `<模型名>/tokenizer.json` 分目录等效）。
      tokenizerFileName: 'tokenizer-$key.json',
      tokenizerUrl: tokenizerUrl,
      inputMilliseconds: (json['inputMilliseconds'] as num?)?.toInt() ?? -1,
      logMelSpectro: json['logMelSpectro'] is Map<String, Object?>
          ? AsrLogMelConfig.fromJson(json['logMelSpectro']! as Map<String, Object?>)
          : null,
      decodeStartTokenId: (json['decodeStartTokenId'] as num?)?.toInt() ?? -1,
      decodeStopTokenId: (json['decodeStopTokenId'] as num?)?.toInt() ?? -1,
      decodeSkipUntilTokenId:
          (json['decodeSkipUntilTokenId'] as num?)?.toInt() ?? -1,
      // 这两个字段在官方 JSON 里通常省略，默认值与官方 `ModelConfigJson` 一致为 true
      gpuEnforceFloat32: json['gpuEnforceFloat32'] as bool? ?? true,
      gpuShareConstantTensors: json['gpuShareConstantTensors'] as bool? ?? true,
      supportsGpu: json['supportsGpu'] as bool? ?? true,
      approxModelBytes: (json['approxBytesModel'] as num?)?.toInt() ?? 0,
      approxTokenizerBytes: (json['approxBytesTokenizer'] as num?)?.toInt() ?? 0,
    );
  }

  /// 把 `https://hf-mirror.com/<owner>/<repo>/resolve/<rev>/<path>` 还原成 [MlRemoteModel]。
  ///
  /// 元数据 JSON 里存的是**完整 URL**（与官方 schema 一致），而下载层需要
  /// `repo` + `revision` + `remotePath` 三段，这里做一次转换。取不到 `/resolve/`
  /// 时直接报错而不是猜一个路径 —— 清单写错要立刻暴露。
  ///
  /// 下载地址由 [MlRemoteModel.url] 按 `hf-mirror.com` 重新拼出（与 Android 侧
  /// `RemoteModel.MIRROR_BASE` 同口径），因此这里必须把 `revision` 也解析出来：
  /// 官方清单里当前全是 `main`，但固定修订号一旦出现，丢掉它就等于换了权重。
  static MlRemoteModel _remoteModelOf(
    String fileName,
    String url,
    int approxBytes,
  ) {
    const String marker = '/resolve/';
    final int schemeEnd = url.indexOf('://');
    final int markerIndex = url.indexOf(marker);
    if (schemeEnd <= 0 || markerIndex <= marker.length) {
      throw FormatException('模型地址格式无法识别：$url');
    }
    final String withoutScheme = url.substring(schemeEnd + 3);
    final String repo = withoutScheme.substring(0, withoutScheme.indexOf(marker));
    final String afterMarker = url.substring(markerIndex + marker.length);
    final int slash = afterMarker.indexOf('/');
    if (repo.isEmpty || slash <= 0 || slash == afterMarker.length - 1) {
      throw FormatException('模型地址格式无法识别：$url');
    }
    return MlRemoteModel(
      fileName: fileName,
      repo: repo,
      revision: afterMarker.substring(0, slash),
      remotePath: afterMarker.substring(slash + 1),
      approxBytes: approxBytes.clamp(0, 1 << 62),
    );
  }
}

/// ASR 模型清单（对应官方 `ModelMetadataManager`）
class AsrModelCatalog {
  AsrModelCatalog._(this._configs, this.modelNames, this._raw);

  final Map<String, AsrModelConfig> _configs;
  final String _raw;

  /// 下拉框里的模型名，顺序**与 JSON 里书写的先后一致**
  final List<String> modelNames;

  /// 官方用的资源名，保持一致
  static const String assetName = 'assets/ml/asr_model_metadata.json';

  /// 从 asset 读取并解析
  static Future<AsrModelCatalog> load() async =>
      fromJson(await rootBundle.loadString(assetName));

  /// 从 JSON 文本解析（单测直接喂字符串）
  static AsrModelCatalog fromJson(String json) {
    final Map<String, Object?> root =
        jsonDecode(json) as Map<String, Object?>;
    final Map<String, AsrModelConfig> parsed = <String, AsrModelConfig>{};
    for (final MapEntry<String, Object?> entry in root.entries) {
      if (entry.value is! Map<String, Object?>) {
        continue;
      }
      parsed[entry.key] = AsrModelConfig.fromJson(
        entry.key,
        entry.value! as Map<String, Object?>,
      );
    }
    if (parsed.isEmpty) {
      throw const FormatException('ASR 模型清单为空');
    }
    // 下拉框顺序要跟清单里**书写的先后**一致（官方用 kotlinx.serialization 读进
    // LinkedHashMap，天然是声明顺序）。`jsonDecode` 返回的 Map 是**插入序**的
    // LinkedHashMap，本已一致，这里再按键在原始文本里首次出现的位置排一次，
    // 保证与 Android 侧 `org.json` 那条「按键在文本里出现位置重排」的补偿等价。
    final List<String> names = parsed.keys.toList()
      ..sort((String a, String b) =>
          json.indexOf('"$a"').compareTo(json.indexOf('"$b"')));
    return AsrModelCatalog._(parsed, names, json);
  }

  AsrModelConfig configOf(String modelName) {
    final AsrModelConfig? config = _configs[modelName];
    if (config == null) {
      throw StateError('模型清单里没有 $modelName');
    }
    return config;
  }

  /// 原始 JSON 文本（后台 isolate 里没有 asset 访问能力，由主 isolate 读好后传入）
  String get rawJson => _raw;
}
