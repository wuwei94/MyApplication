# 端侧机器学习（TensorFlow Lite / LiteRT）开发指南

> 本文档系统梳理 `:modules:module_ml` 模块的架构设计、端侧 AI 推理流程、模型量化、图像预处理与防失真规范、CPU 多核 / XNNPACK / GPU Delegate 硬件加速矩阵以及 Native 内存生命周期管理。

---

## 一、 端侧 AI 核心技术体系

端侧机器学习（On-Device Machine Learning）开发主要包含以下 **5 个核心技术环节**：

```
┌────────────────────────────────────────────────────────┐
│ 1. 模型加载 (FlatBuffers 零拷贝 mmap 映射到 Native 内存) │
└──────────────────────────┬─────────────────────────────┘
                           ▼
┌────────────────────────────────────────────────────────┐
│ 2. 输入构造 (Center-Crop 中心等比裁剪 + Direct ByteBuffer) │
└──────────────────────────┬─────────────────────────────┘
                           ▼
┌────────────────────────────────────────────────────────┐
│ 3. 硬件加速 (CPU 单核 / 多核 / XNNPACK 汇编 / GPU 显卡)  │
└──────────────────────────┬─────────────────────────────┘
                           ▼
┌────────────────────────────────────────────────────────┐
│ 4. 前向推理与后处理 (Softmax 概率归一化 + Top-K 排序)    │
└──────────────────────────┬─────────────────────────────┘
                           ▼
┌────────────────────────────────────────────────────────┐
│ 5. 资源释放 (Interpreter / GpuDelegate / Bitmap 回收)  │
└────────────────────────────────────────────────────────┘
```

---

## 二、 4 大核心功能模块详解（原生 Android & Flutter 双端对照）

| 功能模块 | Android 原生实现 (`:modules:module_ml`) | Flutter 跨平台实现 (`flutter_demo/demos/ml`) | 核心技术点 |
| :--- | :--- | :--- | :--- |
| **MNIST 手写识别** | `TFLiteDigitClassifierActivity.kt` (`/Ml/DigitClassifier`) | `ml_digit_classifier_demo.dart` (`ml/digit_classifier`) | • 自定义 `FingerDrawView` / `CustomPainter` 画板<br>• 28×28 灰度通道归一化 (`FP32`)<br>• Softmax 概率分布与柱状图 |
| **MobileNet 图像分类** | `TFLiteImageClassificationActivity.kt` (`/Ml/ImageClassification`) | `ml_image_classification_demo.dart` (`ml/image_classification`) | • 4:3 黄金比例无黑边自适应容器<br>• Center-Crop 居中等比裁剪防拉伸<br>• ImageNet 1000 类别纯中文标签<br>• Top-5 Monospace 严格对齐排版 |
| **硬件加速跑分实测** | `TFLiteGpuDelegateActivity.kt` (`/Ml/GpuDelegate`) | `ml_gpu_benchmark_demo.dart` (`ml/gpu_benchmark`) | • `GpuDelegateV2` 硬件探测与加速<br>• 2×3 经典科学控制变量法对照矩阵<br>• 首帧 Warm-up、稳态均值、P95 与 FPS |
| **张量底层与内存架构** | `TFLiteTensorBasicsActivity.kt` (`/Ml/TensorBasics`) | `ml_tensor_basics_demo.dart` (`ml/tensor_basics`) | • FlatBuffers `mmap` 零拷贝文件映射<br>• Direct Memory 原生字节序排布<br>• 动态张量尺寸调整 (`resizeInput`)<br>• 多输入多输出 (MIMO) 调度 |

---

## 三、 图像预处理与视觉防失真规范

### 1. Center-Crop 中心等比正方形裁剪
* **问题背景**：MobileNet 模型输入要求固定为 `224 × 224` 正方形。如果手机长方形照片（如 4:3 或 16:9）直接强行缩放，会导致物体严重压扁变形，模型准确率骤降。
* **解决方案**：在 [`TFLiteModelHelper.kt`](file:///E:/StudioProjects/MyApplication/modules/module_ml/src/main/java/com/example/william/my/module/ml/helper/TFLiteModelHelper.kt) 中先按短边居中裁出 1:1 最大正方形，再平滑缩放到 224×224：
  ```kotlin
  val minDim = minOf(bitmap.width, bitmap.height)
  val xOffset = (bitmap.width - minDim) / 2
  val yOffset = (bitmap.height - minDim) / 2
  val cropped = Bitmap.createBitmap(bitmap, xOffset, yOffset, minDim, minDim)
  val scaled = Bitmap.createScaledBitmap(cropped, 224, 224, true)
  ```

### 2. 4:3 黄金比例卡片容器与 0 黑边呈现
* 手机相机传感器原生硬件比例全为 **`4:3`**；
* 布局采用 `ConstraintLayout` 的 `layout_constraintDimensionRatio="4:3"` 约束卡片容器，搭配打包在 assets 中的 `640x480 (4:3)` 标准实拍照片，实现全屏贴合、无黑边、无内容裁剪的优雅渲染。

---

## 四、 硬件加速技术与对照体系

### 1. 2 × 3 科学对照矩阵

```
┌─────────────────┬─────────────────┬─────────────────┐
│     单核 1T     │    单核+XNN     │     多核 4T     │
├─────────────────┼─────────────────┼─────────────────┤
│    多核+XNN     │     GPU加速     │    全量对比     │
└─────────────────┴─────────────────┴─────────────────┘
```

### 2. 核心加速机制对比

| 加速模式 | 底层配置 | 技术原理与性能表现 |
| :--- | :--- | :--- |
| **单核 1T** | `threads=1`, `useXNN=false` | **Baseline 对照基线**：单线程顺序执行，耗时最长（如 35ms）。 |
| **单核+XNN** | `threads=1`, `useXNN=true` | **纯指令集优化**：激活 ARM NEON 128位向量寄存器（SIMD），单核 1 周期并发计算 4 个浮点数，提速约 **1.8x**。 |
| **多核 4T** | `threads=4`, `useXNN=false` | **纯多核并发**：4 个 CPU 核心任务并行分担，提速约 **2.1x**。 |
| **多核+XNN** | `threads=4`, `useXNN=true` | **CPU 满血组合拳**：多核并发 + 汇编指令集双重叠加，提速约 **3.5x ~ 4x**。 |
| **GPU 加速** | `GpuDelegate(FP16)` | **显卡硬件并行**：调用 OpenCL / Vulkan 数百个 Shader 硬件流处理器，CPU 仅占 1 个调度线程（负载 <10%），提速 **8x ~ 10x**。 |

---

## 五、 Native C++ 内存防泄漏规范

1. **零拷贝模型映射 (`mmap`)**：
   使用 `AssetFileDescriptor` 配合 `FileChannel.MapMode.READ_ONLY` 直接映射为 `MappedByteBuffer`，不占用 JVM 堆内存，由系统虚拟内存按需分页加载。
2. **堆外直接内存 (`Direct ByteBuffer`)**：
   输入输出均使用 `ByteBuffer.allocateDirect()` 分配物理内存，并严格配置 `ByteOrder.nativeOrder()`，避免 JNI 跨语言调用时的双重内存拷贝。
3. **严格生命周期回收**：
   * 在 `Activity.onDestroy()` 中显式调用 `interpreter?.close()` 与 `gpuDelegate?.close()`，彻底销毁底层 C++ Runtime 实例与显存句柄。
   * 中间临时 `Bitmap` 及时调用 `.recycle()` 释放。

---

## 六、 官方文档与资源链接

* **TensorFlow Lite 官方门户**：[https://www.tensorflow.org/lite](https://www.tensorflow.org/lite)（包含 Android 快速入门与 API 规范）
* **GPU Delegate 硬件加速指南**：[https://www.tensorflow.org/lite/performance/gpu](https://www.tensorflow.org/lite/performance/gpu)
* **XNNPACK 汇编加速引擎**：[https://github.com/google/XNNPACK](https://github.com/google/XNNPACK)
* **Google 预训练模型中心（Kaggle Models）**：[https://www.kaggle.com/models?framework=tfLite](https://www.kaggle.com/models?framework=tfLite)

