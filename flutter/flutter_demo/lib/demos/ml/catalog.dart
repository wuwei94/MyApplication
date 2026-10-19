import 'package:flutter/widgets.dart';
import 'package:flutter_demo/catalog/models/catalog_entry.dart';
import 'package:flutter_demo/catalog/models/catalog_section.dart';
import 'package:flutter_demo/demos/ml/ml_digit_classifier_demo.dart';
import 'package:flutter_demo/demos/ml/ml_image_classification_demo.dart';
import 'package:flutter_demo/demos/ml/ml_image_segmentation_demo.dart';
import 'package:flutter_demo/demos/ml/ml_speech_recognition_demo.dart';
import 'package:flutter_demo/demos/ml/ml_text_classification_demo.dart';
import 'package:flutter_demo/demos/ml/ml_text_to_speech_demo.dart';
import 'package:flutter_demo/demos/ml/ml_text_to_speech_lm_demo.dart';
import 'package:flutter_demo/demos/ml/ml_text_to_speech_streaming_demo.dart';
import 'package:flutter_demo/demos/ml/ml_zero_shot_classification_demo.dart';

/// 机器学习 / 端侧 AI 模块（LiteRT / TensorFlow Lite 实战演练）
///
/// 官方文档: https://ai.google.dev/edge/litert
///
/// 说明：本模块与 Android 侧 `:modules:module_ml` 是两套独立演进的实现，
/// 但**示例集合同步对齐**官方 `litert-samples/samples/litert` 的任务清单。
/// Android 侧已整体迁移到 LiteRT 2.x 的 CompiledModel API，覆盖官方 `samples/litert/`
/// 顶层 9 个通用任务（视觉 3 / 文本 2 / 语音 4）；本侧基于
/// `tflite_flutter` 插件（经典 Interpreter API），按同一批模型与同一套
/// 预处理契约逐页对应。
///
/// 本侧的原生推理运行时由 `tools/android/apply_android_tflite_flutter.dart`
/// 补丁升到 **LiteRT 2.3.0**（`libLiteRt.so`，与 Android 侧同版本）—— 1.4.0
/// 不解析「外置权重」布局，会让 Matcha / Laya / Qwen3-TTS / whisper 这类模型
/// 的 `invoke` 全部失败。详见 `docs/dependencies.md`。
///
/// 当前进度：
///
/// - 视觉 3 / 3：digit_classifier / image_classification / image_segmentation
/// - 文本 2 / 2：text_classification / zero_shot_classification
/// - 语音 4 / 4：text_to_speech / text_to_speech_streaming / text_to_speech_lm /
///   speech_recognition
///
/// **9 / 9 全部到位** —— 官方 `samples/litert` 顶层 9 个通用任务已无缺口
/// （不含 `qualcomm/*`、`phototalk_sample_app` / `google/sample_app_tpu`
/// 这类需要 NPU / TPU 或 LiteRT-LM 运行时的应用，以及 C++ / iOS 任务）。
class MlCatalog extends CatalogSection {
  const MlCatalog._();

  @override
  String get path => 'ml';

  @override
  String get title => 'Machine Learning';

  @override
  String get subtitle => 'LiteRT / TensorFlow Lite 端侧推理（视觉与文本任务）';

  @override
  List<CatalogEntry> get items => _items;

  static final List<CatalogEntry> _items = <CatalogEntry>[
    CatalogEntry.page(
      path: 'digit_classifier',
      title: 'MNIST 手写数字实时识别',
      subtitle: '画板涂鸦、整图拉伸 28x28、0~255 不归一化、输出即概率',
      pageBuilder: (BuildContext context) => const MlDigitClassifierDemoPage(
        title: 'MNIST 手写数字实时识别',
      ),
    ),
    CatalogEntry.page(
      path: 'image_classification',
      title: 'EfficientNet-Lite 图像物体分类',
      subtitle: 'Lite0 224x224 / Lite2 260x260 可切换、(v-127.5)/127.5 归一化、1000 类 Top-5',
      pageBuilder: (BuildContext context) => const MlImageClassificationDemoPage(
        title: 'EfficientNet-Lite 图像物体分类',
      ),
    ),
    CatalogEntry.page(
      path: 'image_segmentation',
      title: 'Selfie Multiclass 人像语义分割',
      subtitle: '256x256 逐像素 6 类 argmax + 半透明色块叠加回原图',
      pageBuilder: (BuildContext context) => const MlImageSegmentationDemoPage(
        title: 'Selfie Multiclass 人像语义分割',
      ),
    ),
    CatalogEntry.page(
      path: 'text_classification',
      title: '文本情感分类（MobileBERT / 平均词向量）',
      subtitle: '定长 INT32 分词输入、双模型切换、负面/正面二分类',
      pageBuilder: (BuildContext context) => const MlTextClassificationDemoPage(
        title: '文本情感分类',
      ),
    ),
    CatalogEntry.page(
      path: 'zero_shot_classification',
      title: 'Laya 多语零样本分类',
      subtitle: '提示词 + 候选标准直接作答，三题型 + 温度校准，模型约 679 MB 运行时下载',
      pageBuilder: (BuildContext context) =>
          const MlZeroShotClassificationDemoPage(
        title: 'Laya 多语零样本分类',
      ),
    ),
    CatalogEntry.page(
      path: 'text_to_speech',
      title: 'Matcha-TTS 端侧语音合成',
      subtitle: 'G2P + 文本编码 + 扩散 ODE + HiFi-GAN，约 88 MB 运行时下载，GPU/CPU 分工',
      pageBuilder: (BuildContext context) => const MlTextToSpeechDemoPage(
        title: 'Matcha-TTS 端侧语音合成',
      ),
    ),
    CatalogEntry.page(
      path: 'text_to_speech_streaming',
      title: 'KittenTTS 流式语音合成',
      subtitle: '三图动态形状 + 按句流式播放，首音延迟只有一句，约 32 MB 运行时下载',
      pageBuilder: (BuildContext context) =>
          const MlTextToSpeechStreamingDemoPage(
        title: 'KittenTTS 流式语音合成',
      ),
    ),
    CatalogEntry.page(
      path: 'text_to_speech_lm',
      title: 'Qwen3-TTS 语音语言模型合成',
      subtitle: 'talker + MTP + codec 三图逐帧解码，12.5 Hz · 24 kHz，约 1.67 GB 运行时下载',
      pageBuilder: (BuildContext context) => const MlTextToSpeechLmDemoPage(
        title: 'Qwen3-TTS 语音语言模型合成',
      ),
    ),
    CatalogEntry.page(
      path: 'speech_recognition',
      title: '语音识别（CTC / TDT / 自回归三策略）',
      subtitle: '重叠窗口拼接 + 未确认文本标灰，6 个模型按名分派解码策略，最大约 614 MB 运行时下载',
      pageBuilder: (BuildContext context) => const MlSpeechRecognitionDemoPage(
        title: '语音识别',
      ),
    ),
  ];
}

/// 单例实例
const MlCatalog mlCatalog = MlCatalog._();
