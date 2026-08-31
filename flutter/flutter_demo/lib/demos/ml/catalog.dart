import 'package:flutter/widgets.dart';
import 'package:flutter_demo/catalog/models/catalog_entry.dart';
import 'package:flutter_demo/catalog/models/catalog_section.dart';
import 'package:flutter_demo/demos/ml/ml_digit_classifier_demo.dart';
import 'package:flutter_demo/demos/ml/ml_gpu_benchmark_demo.dart';
import 'package:flutter_demo/demos/ml/ml_image_classification_demo.dart';
import 'package:flutter_demo/demos/ml/ml_tensor_basics_demo.dart';

/// 机器学习 / 端侧 AI 模块（TensorFlow Lite / LiteRT 实战演练）
///
/// 官方文档: https://www.tensorflow.org/lite
class MlCatalog extends CatalogSection {
  const MlCatalog._();

  @override
  String get path => 'ml';

  @override
  String get title => 'Machine Learning';

  @override
  String get subtitle => 'TensorFlow Lite 端侧推理、图像分类与 GPU 硬件加速';

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
    CatalogEntry.page(
      path: 'gpu_benchmark',
      title: 'CPU 多核 vs GPU 硬件加速跑分',
      subtitle: '2×3 科学对照矩阵（单核 1T、单核+XNN、多核 4T、多核+XNN、GPU Delegate）',
      pageBuilder: (BuildContext context) => const MlGpuBenchmarkDemoPage(
        title: 'CPU 多核 vs GPU 硬件加速跑分',
      ),
    ),
    CatalogEntry.page(
      path: 'tensor_basics',
      title: 'TFLite 张量底层操作与内存架构',
      subtitle: 'FlatBuffers 零拷贝、Direct ByteData、动态 Reshape 与 MIMO 调度',
      pageBuilder: (BuildContext context) => const MlTensorBasicsDemoPage(
        title: 'TFLite 张量底层操作与内存架构',
      ),
    ),
  ];
}

/// 单例实例
const MlCatalog mlCatalog = MlCatalog._();
