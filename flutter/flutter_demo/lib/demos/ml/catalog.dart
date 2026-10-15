import 'package:flutter/widgets.dart';
import 'package:flutter_demo/catalog/models/catalog_entry.dart';
import 'package:flutter_demo/catalog/models/catalog_section.dart';
import 'package:flutter_demo/demos/ml/ml_digit_classifier_demo.dart';
import 'package:flutter_demo/demos/ml/ml_image_classification_demo.dart';

/// 机器学习 / 端侧 AI 模块（TensorFlow Lite / LiteRT 实战演练）
///
/// 官方文档: https://ai.google.dev/edge/litert
///
/// 说明：本模块与 Android 侧 `:modules:module_ml` 是两套独立演进的实现，
/// 不与 Android 严格同步。Android 侧已整体迁移到 LiteRT 2.x 的 CompiledModel API，
/// 覆盖官方 `samples/litert/` 9 个「Android + Kotlin」任务的全部
/// （视觉 3 / 文本 2 / 语音 4）；本侧仍基于 `tflite_flutter` 插件
/// （TensorFlow Lite 运行时），故目前只覆盖手写数字识别与图像分类两页。
class MlCatalog extends CatalogSection {
  const MlCatalog._();

  @override
  String get path => 'ml';

  @override
  String get title => 'Machine Learning';

  @override
  String get subtitle => 'TensorFlow Lite 端侧推理（手写数字识别与图像分类）';

  @override
  List<CatalogEntry> get items => _items;

  static final List<CatalogEntry> _items = <CatalogEntry>[
    CatalogEntry.page(
      path: 'digit_classifier',
      title: 'MNIST 手写数字实时识别',
      subtitle: '自定义手写板涂鸦、28x28 灰度提取与 Softmax 置信度分布',
      pageBuilder: (BuildContext context) => const MlDigitClassifierDemoPage(
        title: 'MNIST 手写数字实时识别',
      ),
    ),
    CatalogEntry.page(
      path: 'image_classification',
      title: 'MobileNet 图像物体分类',
      subtitle: '4:3 无黑边容器、Center-Crop 等比裁剪与 1000 类纯中文 Top-5 排序',
      pageBuilder: (BuildContext context) => const MlImageClassificationDemoPage(
        title: 'MobileNet 图像物体分类',
      ),
    ),
  ];
}

/// 单例实例
const MlCatalog mlCatalog = MlCatalog._();
