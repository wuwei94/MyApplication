// 下面两个 src/ 导入是为 resetVariableTensors 的 FFI 直调服务的：
// tflite_flutter 0.12.1 的 Interpreter.resetVariableTensors() 有一处极性写反的
// 前置检查（对存活解释器必然抛 StateError，详见该方法注释），在官方修复发布前
// 只能走它自己导出的 C API 绑定。这是**有意**的窄面依赖，不是随手 import。
// ignore_for_file: implementation_imports
import 'dart:ffi';
import 'dart:io';
import 'dart:math' as math;

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart' show rootBundle;
import 'package:image/image.dart' as img;
import 'package:tflite_flutter/src/bindings/bindings.dart';
import 'package:tflite_flutter/src/bindings/tensorflow_lite_bindings_generated.dart';
import 'package:tflite_flutter/tflite_flutter.dart';

/// 位图到模型输入的归一化区间 —— 与 Android `InputNormalize` 一一对应。
///
/// 区间由**模型规格**决定，必须以实测为准（与 Android 模块同一口径）：
/// - MNIST 手写数字（`digit_classifier.tflite`）直接喂 0~255 原始值；
/// - EfficientNet-Lite / Selfie Multiclass 取 `(v - 127.5) / 127.5`（即 [-1, 1]）。
enum MlInputNormalize {
  /// 原始 0~255，不做归一化
  raw0_255,

  /// 归一化到 [-1, 1]：(v - 127.5) / 127.5
  floatMinus1To1,

  /// 归一化到 [0, 1]：v / 255
  float0To1,
}

/// 一次引擎构建的产物：解释器 + 实际生效的后端描述。
///
/// 与 Android 页一样把「实际生效的后端」如实带回界面：GPU 构建失败时回退 CPU，
/// 避免出现「开关打开了、实际仍跑在 CPU 上」而界面看不出来的情况。
typedef MlEngineBundle = ({Interpreter interpreter, String engineLabel});

/// LiteRT / TFLite 推理公共 helper —— 与 Android `TFLiteModelHelper` 功能对应。
///
/// 收口五件事：引擎构建（含 GPU 回退）、位图编码、词表解析、输出解码、
/// LSTM variable tensors 重置。全部为不依赖页面生命周期的静态方法；
/// 后处理一律是纯函数，便于单测覆盖。
///
/// **后处理一律不叠加 Softmax**：本模块 4 个模型里 3 个的输出张量本身就是概率
/// （`digit_classifier` / `efficientnet_lite0` / `bert_classifier` 的末算子就是
/// SOFTMAX，实测各类之和为 1）；`selfie_multiclass` 输出 logits 但只做 argmax
/// —— argmax 在 Softmax 前后不变。对已是概率的输出再叠加 Softmax 会把 0.90
/// 压到 ≈0.21，置信度与柱状图严重失真，因此这里**不提供** Softmax 工具函数。
class MlTfliteHelper {
  const MlTfliteHelper._();

  /// 模块统一的 CPU 线程数：按设备核心数自适应并封顶 4（与 Android `CPU_THREADS` 同口径）
  ///
  /// 主流手机大小核混合，超过 4 线程后小核参与计算反而拉长时延；
  /// 各示例页共用同一档配置，保证时延数据可以横向比较。
  static int get cpuThreads => Platform.numberOfProcessors.clamp(2, 4);

  // ─────────────────── 引擎构建 ───────────────────

  /// 从资产构建推理引擎；GPU 不可用时回退 CPU，并把实际后端写进 [MlEngineBundle.engineLabel]
  ///
  /// 与 Android 2.x 的差别：Flutter 侧走 `tflite_flutter`（经典 `Interpreter` API），
  /// GPU 是运行时挂载的 `GpuDelegateV2` 而不是编译期选项，因此「切 GPU」不需要重建页面状态，
  /// 但仍要关闭旧解释器再建新的（delegate 挂在实例上，无法热替换）。
  static Future<MlEngineBundle> createInterpreter(
    String assetPath, {
    bool useGpu = false,
  }) async {
    if (useGpu) {
      // 同 createInterpreterFromFile：回退 CPU 前先释放已建成的 GPU 解释器
      Interpreter? created;
      try {
        final InterpreterOptions gpuOptions = InterpreterOptions()
          ..threads = cpuThreads
          ..addDelegate(
            GpuDelegateV2(
              options: GpuDelegateOptionsV2(isPrecisionLossAllowed: true),
            ),
          );
        final Interpreter gpu =
            await Interpreter.fromAsset(assetPath, options: gpuOptions);
        created = gpu;
        gpu.allocateTensors();
        return (
          interpreter: gpu,
          engineLabel: 'GPU 硬件加速 (GpuDelegateV2)',
        );
      } catch (error) {
        created?.close();
        // GPU 构建失败（设备不支持 / 驱动异常）时回退 CPU，与 Android 页「不静默降级」语义一致
        debugPrint('GPU delegate 创建失败，回退 CPU: $error');
      }
    }

    final InterpreterOptions cpuOptions = InterpreterOptions()..threads = cpuThreads;
    final Interpreter cpu =
        await Interpreter.fromAsset(assetPath, options: cpuOptions);
    cpu.allocateTensors();
    return (
      interpreter: cpu,
      engineLabel: useGpu
          ? 'GPU 不可用，已回退 CPU ($cpuThreads 线程 + XNNPACK)'
          : 'CPU ($cpuThreads 线程 + XNNPACK)',
    );
  }

  /// 从**本地文件**构建推理引擎（运行时下载的模型走这条路径）。
  ///
  /// [isPrecisionLossAllowed] 透传给 `GpuDelegateOptionsV2`：官方对部分模型
  /// （如 Laya 主图）明确要求 GPU 显式 FP32，此时必须传 false —— 这是**模型规格**
  /// 而非性能调优。
  static Future<MlEngineBundle> createInterpreterFromFile(
    File file, {
    bool useGpu = false,
    bool isPrecisionLossAllowed = true,
  }) async {
    if (useGpu) {
      // 已建成但未返回的解释器要在回退前释放：`allocateTensors()` 抛异常时
      // 它仍持有原生句柄与 GPU delegate，交给 GC 只会拖到下一次收集
      Interpreter? created;
      try {
        final InterpreterOptions gpuOptions = InterpreterOptions()
          ..threads = cpuThreads
          ..addDelegate(
            GpuDelegateV2(
              options: GpuDelegateOptionsV2(
                isPrecisionLossAllowed: isPrecisionLossAllowed,
              ),
            ),
          );
        // tflite_flutter 0.12.1 的 fromFile 是同步工厂构造（句柄即刻就绪），
        // 没有 await 的必要；GPU delegate 挂载失败时抛异常，由下方 catch 回退 CPU
        final Interpreter gpu =
            Interpreter.fromFile(file, options: gpuOptions);
        created = gpu;
        gpu.allocateTensors();
        return (
          interpreter: gpu,
          engineLabel: 'GPU 硬件加速 (GpuDelegateV2, '
              '${isPrecisionLossAllowed ? 'FP16' : 'FP32'})',
        );
      } catch (error) {
        created?.close();
        // GPU 构建失败（设备不支持 / 驱动异常）时回退 CPU，与 Android 页「不静默降级」语义一致
        debugPrint('GPU delegate 创建失败，回退 CPU: $error');
      }
    }

    final InterpreterOptions cpuOptions = InterpreterOptions()..threads = cpuThreads;
    final Interpreter cpu = Interpreter.fromFile(file, options: cpuOptions);
    cpu.allocateTensors();
    return (
      interpreter: cpu,
      engineLabel: useGpu
          ? 'GPU 不可用，已回退 CPU ($cpuThreads 线程 + XNNPACK)'
          : 'CPU ($cpuThreads 线程 + XNNPACK)',
    );
  }

  // ─────────────────── 位图 → 输入张量 ───────────────────

  /// 位图**整图拉伸**重采样为目标尺寸，编码为交错 RGB 的 Float32List（行优先，长度 w * h * 3）
  ///
  /// 一律整图拉伸、不做裁剪 —— 与官方 2.x 样例保持一致（官方即 `createScaledBitmap`
  /// 后直接取像素）。两个理由：
  /// - 图像分类的输出是整图级向量，拉伸带来的长宽比变化不影响「图里有什么」；
  /// - 语义分割的掩码相对**整幅输入图**逐像素输出，先裁剪会让掩码与原图整体错位。
  static Float32List encodeImage(
    img.Image source,
    int width,
    int height,
    MlInputNormalize normalize,
  ) {
    final img.Image resized =
        (source.width == width && source.height == height)
            ? source
            : img.copyResize(
                source,
                width: width,
                height: height,
                interpolation: img.Interpolation.linear,
              );

    final Float32List output = Float32List(width * height * 3);
    int index = 0;
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        final img.Pixel pixel = resized.getPixel(x, y);
        final double r = pixel.r.toDouble();
        final double g = pixel.g.toDouble();
        final double b = pixel.b.toDouble();
        switch (normalize) {
          case MlInputNormalize.raw0_255:
            output[index] = r;
            output[index + 1] = g;
            output[index + 2] = b;
          case MlInputNormalize.floatMinus1To1:
            output[index] = (r - 127.5) / 127.5;
            output[index + 1] = (g - 127.5) / 127.5;
            output[index + 2] = (b - 127.5) / 127.5;
          case MlInputNormalize.float0To1:
            output[index] = r / 255.0;
            output[index + 1] = g / 255.0;
            output[index + 2] = b / 255.0;
        }
        index += 3;
      }
    }
    return output;
  }

  // ─────────────────── 词表解析 ───────────────────

  /// 读取资产文本并按行返回（保留空行，行号即 id 时不能过滤）
  ///
  /// 与 Android `BufferedReader.readLine()` 对齐：仅去掉末尾换行产生的幽灵空行，
  /// 行尾 `\r` 一并剥掉；**中间的空行必须保留** —— 单列词表格式下行号参与 id 计算，
  /// 过滤会让后续 id 整体错位。
  static Future<List<String>> loadLines(String assetPath) async {
    final String text = await rootBundle.loadString(assetPath);
    final List<String> lines = text.split('\n');
    if (lines.isNotEmpty && lines.last.isEmpty) {
      lines.removeLast();
    }
    for (int i = 0; i < lines.length; i++) {
      final String line = lines[i];
      if (line.endsWith('\r')) {
        lines[i] = line.substring(0, line.length - 1);
      }
    }
    return lines;
  }

  /// 把词表文本行解析为 `token -> id` 映射 —— 与 Android `buildVocabulary` 对齐
  ///
  /// 两个文本模型内嵌的 `vocab.txt` **格式并不相同**，必须都支持：
  /// - **单列**：整行就是 token，**行号即 id**（`bert_classifier.tflite`，形如 `[PAD]` / `[unused0]`）；
  /// - **双列**：形如 `<PAD> 0` / `the 3`，以**第二列的数字**为 id
  ///   （`average_word_classifier.tflite`）。
  ///
  /// 判别方式是「最后一个空格之后能否解析成整数」：单列词表里 token 不含空格，不会误判。
  static Map<String, int> buildVocabulary(List<String> lines) {
    final Map<String, int> vocabulary = <String, int>{};
    for (int index = 0; index < lines.length; index++) {
      final String line = lines[index];
      final int separator = line.lastIndexOf(' ');
      final int? explicitId =
          separator > 0 ? int.tryParse(line.substring(separator + 1)) : null;
      if (explicitId != null) {
        vocabulary[line.substring(0, separator)] = explicitId;
      } else {
        vocabulary[line] = index;
      }
    }
    return vocabulary;
  }

  // ─────────────────── 输入 / 输出读写 ───────────────────

  /// 写 Float32 输入张量（按字节写入，int32 输入同理走 [writeIntInput]）
  static void writeFloatInput(Interpreter interpreter, int tensorIndex, Float32List data) {
    interpreter.getInputTensor(tensorIndex).data = data.buffer.asUint8List();
  }

  /// 写 Int32 输入张量（MobileBERT 的 word / type / mask 三张量都是 int32）
  static void writeIntInput(Interpreter interpreter, int tensorIndex, Int32List data) {
    interpreter.getInputTensor(tensorIndex).data = data.buffer.asUint8List();
  }

  /// 读 Float32 输出张量
  ///
  /// tflite_flutter 的 `Tensor.data` 是**原生内存上的字节视图**，不依赖
  /// `ByteBuffer` 桥接；这里按 4 字节一组手动解码，避免对外部 typed data
  /// 取 `.buffer` 的兼容性陷阱。
  static Float32List readFloatOutput(Interpreter interpreter, {int tensorIndex = 0}) {
    final Uint8List bytes = interpreter.getOutputTensor(tensorIndex).data;
    final ByteData copy = ByteData(bytes.length);
    copy.buffer.asUint8List().setRange(0, bytes.length, bytes);
    final Float32List output = Float32List(bytes.length ~/ 4);
    for (int i = 0; i < output.length; i++) {
      output[i] = copy.getFloat32(i * 4, Endian.little);
    }
    return output;
  }

  /// 读 Int32 输出张量（KittenTTS 时长预测器的 `durations`）
  ///
  /// 与 [readFloatOutput] 同口径手动解码；长度按缓冲区实际字节数推出 ——
  /// 动态形状的张量（Kitten 的 durations 随序列长度变化）只有在 invoke 之后
  /// 才落定，因此**不能**按静态 shape 预估元素个数。
  static Int32List readIntOutput(Interpreter interpreter, {int tensorIndex = 0}) {
    final Uint8List bytes = interpreter.getOutputTensor(tensorIndex).data;
    final ByteData copy = ByteData(bytes.length);
    copy.buffer.asUint8List().setRange(0, bytes.length, bytes);
    final Int32List output = Int32List(bytes.length ~/ 4);
    for (int i = 0; i < output.length; i++) {
      output[i] = copy.getInt32(i * 4, Endian.little);
    }
    return output;
  }

  /// 按**张量名后缀**定位输出张量下标
  ///
  /// `getOutputTensor(i)` 的序号按 FlatBuffer 张量表排，与签名里的输出顺序
  /// **不一致**（Kitten predictor 的三个输出在表里是 0 / 2 / 1 交错的），
  /// 因此按名字寻址。带 `serving_default` 签名的图，Java 侧默认签名输出不带
  /// 前缀，这里与 Android `Outputs.index(suffix)` 一致只按后缀匹配
  /// （如 `StatefulPartitionedCall:1`）。
  static int outputIndexBySuffix(Interpreter interpreter, String suffix) {
    final List<Tensor> outputs = interpreter.getOutputTensors();
    for (int i = 0; i < outputs.length; i++) {
      if (outputs[i].name.endsWith(suffix)) {
        return i;
      }
    }
    throw StateError('输出张量不存在: $suffix');
  }

  /// 按**规范化张量名**定位输入张量下标（去掉 `serving_default_` 前缀与 `:0` 后缀）
  ///
  /// Kitten 三张图里 predictor / prosody 带签名（`serving_default_x:0`），
  /// vocoder 不带（输入就叫 `asr` / `f0` / `n` / `har` / `style`），
  /// 同一条寻址代码路径要同时服务两种图，与 Android `canonical()` 对齐。
  static int inputIndexByName(Interpreter interpreter, String canonicalName) {
    final List<Tensor> inputs = interpreter.getInputTensors();
    for (int i = 0; i < inputs.length; i++) {
      if (canonicalTensorName(inputs[i].name) == canonicalName) {
        return i;
      }
    }
    throw StateError('输入张量不存在: $canonicalName');
  }

  /// 去掉 `serving_default_` 前缀与 `:0` 后缀，得到可比较的规范名
  static String canonicalTensorName(String name) {
    String canonical = name;
    if (canonical.startsWith('serving_default_')) {
      canonical = canonical.substring('serving_default_'.length);
    }
    final int colon = canonical.indexOf(':');
    if (colon > 0) {
      canonical = canonical.substring(0, colon);
    }
    return canonical;
  }

  /// 重置 LSTM variable tensors —— **必须**在每次 invoke 前调用（Kitten 预测器）
  ///
  /// 这两张图的 LSTM 隐状态存放在 TFLite variable tensors 里并**跨调用保留**：
  /// 形状变化会因重新分配而顺带重置，但**长度相同的两次合成不会** —— 第二次会从
  /// 上一次遗留的末状态开始，把预测时长带偏（复现方式：同一句话连说两遍）。
  ///
  /// ### 为什么绕过 `Interpreter.resetVariableTensors()`
  ///
  /// `tflite_flutter` 0.12.1（截至今日仍是 pub 最新版）的该方法有一处
  /// **极性写反的前置检查**：
  ///
  /// ```dart
  /// // packages/tflite_flutter/lib/src/interpreter.dart（上游 master 同款）
  /// void resetVariableTensors() {
  ///   checkState(_deleted, message: 'Should not acces delegate after it has been closed.');
  ///   tfliteBinding.TfLiteInterpreterResetVariableTensors(_interpreter);
  /// }
  /// ```
  ///
  /// `quiver` 的 `checkState(expression)` 在 expression 为 **false** 时抛
  /// `StateError`，于是对任何**存活**的解释器调用它都必然抛异常
  /// （`_deleted` 只有 close 之后才为 true）。这是上游 bug（应为 `!_deleted`），
  /// 本机 pub 缓存与 GitHub master 均已实码核对。在官方修复发布前，这里走
  /// 同一份 C API（`TfLiteInterpreterResetVariableTensors`，tflite_flutter
  /// 的 bindings 已导出）直接调用，并显式检查返回的状态码。
  static void resetVariableTensors(Interpreter interpreter) {
    final Pointer<TfLiteInterpreter> handle =
        Pointer<TfLiteInterpreter>.fromAddress(interpreter.address);
    final int status = tfliteBinding.TfLiteInterpreterResetVariableTensors(handle);
    if (status != 0) {
      throw StateError('TfLiteInterpreterResetVariableTensors 失败，状态码 $status');
    }
  }

  // ─────────────────── 后处理 ───────────────────

  /// 获取置信度最高的 Top-K 索引与概率值
  static List<({int index, double score})> topK(
    Float32List scores, {
    int k = 5,
  }) {
    final List<({int index, double score})> entries = <({int index, double score})>[];
    for (int i = 0; i < scores.length; i++) {
      entries.add((index: i, score: scores[i]));
    }
    entries.sort((({int index, double score}) a, ({int index, double score}) b) =>
        b.score.compareTo(a.score));
    return entries.take(math.min(k, scores.length)).toList();
  }

  /// 逐像素 argmax 解码语义分割输出 —— 与 Android `decodeSegmentationMask` 对齐
  ///
  /// 入参为平坦输出 `[H * W * C]`（行优先，通道交错），对每个像素在 C 个类别
  /// 通道上取最大响应，返回长度为 H * W 的类别索引掩码。
  static Int32List decodeSegmentationMask(
    Float32List logits,
    int width,
    int height,
    int classCount,
  ) {
    final int pixelCount = width * height;
    final Int32List mask = Int32List(pixelCount);
    if (classCount <= 0 || logits.length < pixelCount * classCount) {
      return mask;
    }
    for (int pixel = 0; pixel < pixelCount; pixel++) {
      final int base = pixel * classCount;
      int bestIndex = 0;
      double bestScore = -double.infinity;
      for (int c = 0; c < classCount; c++) {
        final double score = logits[base + c];
        if (score > bestScore) {
          bestScore = score;
          bestIndex = c;
        }
      }
      mask[pixel] = bestIndex;
    }
    return mask;
  }

  /// 统计掩码中各类别的像素数量，索引即类别 id
  static Int32List countClasses(Int32List mask, int classCount) {
    final Int32List counts = Int32List(classCount);
    for (final int index in mask) {
      if (index >= 0 && index < classCount) {
        counts[index]++;
      }
    }
    return counts;
  }

  /// 将类别掩码映射为 ARGB 像素数组，便于直接构建叠加层图像
  ///
  /// [backgroundTransparent] 为 true 时类别 0（背景）输出全透明，只保留前景色块。
  /// 颜色入参为 `0xAARRGGBB`，与 Android 页调色板同口径。
  static Int32List colorizeMask(
    Int32List mask,
    List<int> colors, {
    bool backgroundTransparent = true,
  }) {
    final Int32List pixels = Int32List(mask.length);
    for (int i = 0; i < mask.length; i++) {
      final int index = mask[i];
      if (backgroundTransparent && index == 0) {
        pixels[i] = 0;
      } else if (index >= 0 && index < colors.length) {
        pixels[i] = colors[index];
      } else {
        pixels[i] = 0;
      }
    }
    return pixels;
  }
}
