# 端侧机器学习（LiteRT，原 TensorFlow Lite）开发指南

> 本文档系统梳理 `:modules:module_ml` 模块的架构设计、端侧 AI 推理流程、图像预处理与防失真规范、视觉 / 文本 / 语音三类模态的模型实战、`Accelerator` 硬件加速、Native 内存生命周期管理以及纯函数单元测试体系。
>
> **推理 API 以 LiteRT 2.x 的 `CompiledModel` 为主**（`com.google.ai.edge.litert.*`）：
> 官方 `samples/litert/` 下有 Android + Kotlin 实现的一共 **9 个任务**，本模块**全覆盖**
> （视觉 3 + 文本 2 + 语音 4），实现与预处理逐项对齐。
> 唯一例外是流式语音合成的两张定长 LSTM 图 —— 它们把隐状态放在 TFLite *variable tensors*
> 里，而 `CompiledModel` 的加载器目前不接受这类图（官方注释 `b/365299994`），
> 因此与官方一样改走 **`org.tensorflow.lite.Interpreter`**（该 API 就在 `litert-2.3.0.aar` 内，
> 不需新增依赖；见第四节第 6 条）。

---

## 一、 端侧 AI 核心技术体系

端侧机器学习（On-Device Machine Learning）开发主要包含以下 **5 个核心技术环节**：

```
┌───────────────────────────────────────────────────────────┐
│ 1. 引擎构建 (CompiledModel.create 解析并编译计算图)       │
└──────────────────────────┬────────────────────────────────┘
                           ▼
┌───────────────────────────────────────────────────────────┐
│ 2. 输入构造 (整图拉伸重采样 → FloatArray →                │
│              TensorBuffer.writeFloat / writeInt)          │
└──────────────────────────┬────────────────────────────────┘
                           ▼
┌───────────────────────────────────────────────────────────┐
│ 3. 硬件加速 (Accelerator.CPU 多线程 / XNNPACK / GPU / NPU)│
└──────────────────────────┬────────────────────────────────┘
                           ▼
┌───────────────────────────────────────────────────────────┐
│ 4. 前向推理与后处理 (run → readFloat → argmax / 概率)     │
└──────────────────────────┬────────────────────────────────┘
                           ▼
┌───────────────────────────────────────────────────────────┐
│ 5. 资源释放 (TensorBuffer / CompiledModel / Bitmap 回收)  │
└───────────────────────────────────────────────────────────┘
```

---

## 二、 核心功能模块详解（Android 原生 & Flutter 双端对照）

模块现共 **9 个示例页**，对应官方 2.x 样例集（[`litert-samples/samples/litert/`](https://github.com/google-ai-edge/litert-samples/tree/main/samples/litert)）中 **9 个「Android + Kotlin」任务的全部**，功能与预处理逐项对齐：

| 功能模块 | Android 原生实现 (`:modules:module_ml`) | Flutter 跨平台实现 (`flutter_demo/demos/ml`) | 核心技术点 |
| :--- | :--- | :--- | :--- |
| **MNIST 手写识别** | `digit/TFLiteDigitClassifierActivity.kt` (`/Ml/DigitClassifier`) | `ml_digit_classifier_demo.dart` (`ml/digit_classifier`) | • 自定义 `FingerDrawView` / `CustomPainter` 画板<br>• 画板整图拉伸到 28×28，预处理与官方 sample 完全一致<br>• 输入 `[1,28,28,3]` FP32，取值 0~255 **不归一化**<br>• 模型末尾算子即 `SOFTMAX`，输出**已是概率**，直接展示 0~9 概率分布与柱状图<br>• CPU / GPU 切换即重建引擎（`Accelerator` 是**编译期**选项） |
| **EfficientNet-Lite 图像分类** | `image/TFLiteImageClassificationActivity.kt` (`/Ml/ImageClassification`) | `ml_image_classification_demo.dart` (`ml/image_classification`) | • 整图拉伸到 224×224，与官方 sample 一致<br>• 224×224 FLOAT `[-1,1]` 输入<br>• 标签表取自模型内嵌 metadata（`labels_without_background.txt`，**1000 行**）<br>• 输出 `[1,1000]` FP32 概率，**模型没有背景类**，与标签表**直接下标一一对应**（取 Top-5，不做置零、不做偏移）<br>• CPU / GPU 切换即重建引擎 |
| **Selfie Multiclass 语义分割** | `segmentation/TFLiteImageSegmentationActivity.kt` (`/Ml/ImageSegmentation`) | — | • 256×256 FP32 `[-1,1]` **整图拉伸**输入<br>• 输出 `[1,256,256,6]` 平坦 FloatArray，按 `像素序号 * 类别数 + 类别` 取通道 argmax<br>• 该输出是**未归一化的 logits**（末算子 `CONCATENATION`），页面只做 argmax，无需 Softmax<br>• 人像专属 6 类调色板（背景 / 头发 / 身体皮肤 / 面部皮肤 / 衣物 / 其他配饰）<br>• 半透明色块叠加与类别占比图例<br>• CPU / GPU 切换即重建引擎 |
| **文本情感分类** | `text/TFLiteTextClassificationActivity.kt` (`/Ml/TextClassification`) | — | • 词表取自模型内嵌 metadata（`vocab.txt`）：**BERT 为单列裸词（行号即 id），平均词向量为双列 `token id`（取第二列）**<br>• 两个模型：平均词向量（单输入 `[1,256]`）与 MobileBERT（三输入 `[1,128]`，声明顺序 `input_word_ids` → `input_type_ids` → `input_mask`）<br>• 输入为 **INT32**，用 `writeInt` 写入<br>• 输出 `[1,2]` FP32 概率（负面 / 正面），无需反量化与 Softmax |
| **Laya 多语言零样本分类** | `zsc/TFLiteZeroShotClassificationActivity.kt` (`/Ml/ZeroShotClassification`) | — | • 把「陈述 + 问句」拼成提示词模板，**从零构建**文本序列（`LayaPromptBuilder`）<br>• 主图（250 MB，含嵌入表）与动作头（0.8 MB）**两张图串联**；后者是**编译期** `Precision.FP32`<br>• 校准解码：温度/阈值取自仓库里的 `laya_ml_calibration.json`，页面上按 3 类问题（choice / score / noul）分别呈现<br>• 分词器为多语言 BPE（`tokenizer.json` 34 MB），嵌入表走 `token_embeddings_fp16.bin`<br>• CPU / GPU 切换即重建引擎；模型 **运行时下载**（约 679 MB） |
| **Matcha-TTS 英文语音合成** | `tts/TFLiteTextToSpeechActivity.kt` (`/Ml/TextToSpeech`) | — | • 前端是神经 G2P（DeepPhonemizer，`dp_g2p_matcha_fp16.tflite`）+ 词表/元数据，与流式页**共用同一张 G2P 图**<br>• 四张图串联：文本编码 → 扩散解码器 → 声码器（24 kHz）<br>• **扩散解码器必须跑 CPU**：该图在 Mali ML Drift delegate 上有算子融合缺陷（官方注释：整图残差相关性塌到 0.006，单独成图 0.98）<br>• 模型 **运行时下载**（约 92 MB） |
| **KittenTTS 流式语音合成** | `tts/TFLiteTextToSpeechStreamingActivity.kt` (`/Ml/TextToSpeechStreaming`) | — | • 三张图都是**动态序列长度**，不做 padding 分桶：每句先 `resizeInput` 再跑<br>• 文本先按句切分（`SentenceChunker`，每块补逗号收尾），再由**生产者/消费者一对协程**边合成边播放（流式 `AudioTrack`），首音延迟只有一句<br>• 两张定长 LSTM 图（predictor / prosody）走 `Interpreter` + `resetVariableTensors()`；声码器输出是动态形状波形<br>• 8 个音色、24 kHz，音色表是**长度条件化**的（行号 = `min(字符数, 399)`），直接从 `voices.npz` 端侧解包<br>• 模型 **运行时下载**（约 61 MB） |
| **Qwen3-TTS 语音语言模型合成** | `tts/TFLiteTextToSpeechLmActivity.kt` (`/Ml/TextToSpeechLm`) | — | • 0.6B 参数语音 LM，**三张图由宿主循环串联**：talker（28 层，`prefill_32` + `decode` 双签名 + 显式 KV cache）→ MTP（5 层，一次调用在图内跑完 15 步自回归）→ codec（64 帧一块 + 25 帧左上下文，上采样 1920）<br>• 图之间的一切都在宿主侧：byte-level BPE、提示词拼装（控制 token + 说话人 x-vector + 逐帧文本条件）、16 层码本嵌入求和、带重复惩罚的 top-50 采样<br>• KV cache 用两份缓冲区 **ping-pong** 交替读写，避免每步拷贝约 235 MB<br>• 分词器带**启动自检**（官方 baked 的 `tokenizer_test_vectors.json`，含日文与全角空格）<br>• 模型 **运行时下载**（约 1.67 GB）；CPU 实测 **慢于实时**（RTF ≈ 6.7） |
| **语音识别（ASR）** | `asr/TFLiteSpeechRecognitionActivity.kt` (`/Ml/SpeechRecognition`) | — | • **6 个模型可选**（3 个 Parakeet + Moonshine + Whisper + Qwen3-ASR），清单纯 JSON 驱动（`assets/asr_model_metadata.json`），模型与分词器都**运行时下载**<br>• **文件（16 kHz 单声道 WAV）与麦克风双输入**：模型都不是流式的，靠 5 秒窗口 + 2 秒重叠滚动拼接<br>• 三级流水由两条 `Channel` 串成生产者/消费者：`AudioPreprocessor`（log-Mel 或原样透传）→ `SpeechRecognizer` → `Postprocessor`（编辑距离对齐，**未确认文本标灰**）<br>• 解码策略按模型名在宿主侧分派：`-ctc-` 逐帧 argmax + 压重复、`-tdt` 时间下标驱动 + LSTM 状态 ping-pong、其余无状态自回归 + 因果掩码<br>• 分词器是**纯 Kotlin** 实现的 HF 解码链（BPE map / Unigram list + ByteLevel / Metaspace / ByteFallback / Fuse / Strip / Replace），替代官方不可获取的 JNI `tokenizers-cpp`<br>• log-Mel 用纯 Kotlin 复刻 librosa 语义，替代官方构建期联网下载的 `jlibrosa` JAR<br>• 后端走 `Options(setOf(CPU, accelerator))`：目标后端不可用时由运行时**自行退回** CPU（而非编译期抛异常） |

> **双端差异说明**：Flutter 侧是独立演进的一套实现，不与 Android 严格同步。
> 目前 Flutter 已覆盖 `digit_classifier` 与 `image_classification` 两页，图像分类页
> 基于 MobileNet 与 `tflite_flutter` 插件（TensorFlow Lite 运行时）；
> 语义分割 / 文本分类 / 零样本 / 三个语音合成页 / 语音识别页则集中在 Android 侧提供。

> **覆盖范围说明**：官方 `samples/litert/` 下共 **17 个目录 = 11 个任务 + 6 个非任务**
> （`.vscode` / `colab` / `flutter` / `google` / `qualcomm` / `phototalk_sample_app`）。
> 11 个任务里有 Android Kotlin 实现的共 **9 个**：
> `digit_classifier` / `image_classification` / `image_segmentation` / `text_classification` /
> `zero_shot_classification` / `speech_recognition` / `text_to_speech` / `text_to_speech_lm` /
> `text_to_speech_streaming`。
> 本模块据此落地全部 9 个任务（视觉 3 + 文本 2 + 语音 4，末位补齐的是
> `speech_recognition`）。其余两个任务 `semantic_similarity`（C++）与
> `image_generation`（ios / macos）在 Android 上以原生方式承载；`kotlin_npu` 是同一
> 示例的加速器配置（高通 NPU），不单独计数。
> 官方 9 个任务之外，`object_detection` / `pose_estimation` / `retrieval` /
> `audio_classification` 属于其它官方示例集，不在本模块对齐范围内。

---

## 三、 图像预处理规范

### 1. 统一走整图拉伸重采样

三个视觉页全部经 [`TFLiteModelHelper.kt`](../../modules/module_ml/src/main/java/com/example/william/my/module/ml/helper/TFLiteModelHelper.kt)
的 `bitmapToFloatArray(bitmap, targetWidth, targetHeight, normalize)` 做预处理：先用
`Bitmap.createScaledBitmap` 整图拉伸到模型输入尺寸，再逐像素写成交错 RGB 的 `FloatArray`
（行优先，长度 `w * h * 3`）。**不做裁剪**，与官方样例完全一致（官方即 `createScaledBitmap`
后直接取像素）：

```kotlin
val scaled = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
// 逐像素 → FloatArray，同时回收缩放产生的中间位图（!== 判断，避免误回收调用方位图）
```

| 任务类型 | 输入尺寸 | 不裁剪的原因 |
| :--- | :--- | :--- |
| **图像分类** | 224 × 224 | 输出是整图级向量，长宽比变化不影响「图里有什么」 |
| **语义分割** | 256 × 256 | 掩码相对**整幅输入图**逐像素生成，先裁剪会让色块整体偏移、无法叠加回原图 |
| **手写数字** | 28 × 28 | 画板本身是正方形，拉伸无几何损失 |

### 2. 样本来源与 4:3 呈现
* 手机相机传感器原生硬件比例全为 **`4:3`**，布局沿用 `ConstraintLayout` 的
  `layout_constraintDimensionRatio="4:3"` 约束预览卡片，实现无黑边、无内容裁剪的渲染。
* **不内置样本图**：图像分类 / 分割页与官方一致，走「相机实时预览 + 相册选图」这条链路，
  资产里不打包样本照片（相机实时预览仍在待办，当前由相册选图承担）。

### 3. 归一化区间

归一到什么区间由模型规格决定，必须以实测为准：EfficientNet-Lite 与 Selfie Multiclass 取
`FLOAT_MINUS1_1`（`(v - 127.5) / 127.5`），MNIST 手写数字模型直接喂 `RAW_0_255` 原始值。
**区间必须实测确认**，且要用能真正区分对错的判据 —— 同一模型喂错区间未必立刻表现为肉眼可见的
异常，必须用「换了正确区间后置信度是否显著回升」来判定。

---

## 四、 硬件加速：Accelerator（CPU / GPU / NPU）

### 1. 后端选择是编译期选项

加速后端由 **`Accelerator` 在引擎构建时选定**：

```kotlin
TFLiteModelHelper.createCpuOptions()   // Accelerator.CPU + CpuOptions(numThreads = CPU_THREADS)
TFLiteModelHelper.createGpuOptions()   // Accelerator.GPU
```

因此「切换加速后端」在页面上的实现是**重建引擎**（`TFLiteImageClassificationActivity.rebuildModel()`），
而不是替换某个运行时对象。切换必须走取消式串行化：先 `cancel()` 上一次未完成的重建，
再把「构建新引擎」与「关旧引擎」都排进引擎串行队列，中途失败时旧引擎仍留在字段里可用，
不会出现「引擎已置空」的半死状态。

### 2. GPU 路径的失败如实上报

GPU 后端构建失败（设备不支持 / 驱动异常）时回退 CPU，并把回退原因写进页面描述，
避免出现「开关打开了、实际仍跑在 CPU 上」而界面看不出来的情况：

```kotlin
description = "GPU 后端不可用，已回退 CPU: ${e.message}"
```

兼容性判断被收进了引擎编译过程，构建抛异常即代表该后端不可用，页面据此回退即可。

### 3. 量化参数的读取限制（关键约束）

`TensorType.ElementType` **只有 `INT` / `FLOAT` / `INT8` / `BOOLEAN` / `INT64`，没有 `UINT8`**，
并且没有暴露量化参数（`scale` / `zeroPoint`）的读取入口（量化参数只存在于旧
`org.tensorflow.lite.Tensor.quantizationParams()`，2.x 的 `TensorType` 不再提供）。这意味着：

* UINT8 全量化模型**拿不到反量化参数**，即便加载成功也没法还原输出。

**但这条限制只约束 I/O 张量的元素类型，与「权重是否量化」是两件事**：量化只发生在权重内部，
而 I/O 张量边界仍是 float32 —— 实测 fp16 / int8 / int4 变体的输入输出张量元素类型都是 `FLOAT`，
所以本模块**可以**使用低精度图（Qwen3-TTS 用的就是 `talker_int4` + `mtp_folded_int8`，
KittenTTS / Matcha 用的是 `_fp16` 图），Java 层照旧 `writeFloat` / `readFloat`。

内嵌 assets 的 5 个模型则**全部是 FP32**（详见第六节），后处理不需要反量化。
用官方 `ai-edge-litert` 运行时逐模型实测，**5 个模型里 4 个的末算子就是 `SOFTMAX`，1 个输出 logits**：

| 模型 | 输出张量 | 末算子 | 输出性质（实测） |
| :--- | :--- | :--- | :--- |
| `digit_classifier` | `StatefulPartitionedCall:0` | `SOFTMAX` | 概率，10 类之和 = 1.000000 |
| `efficientnet_lite0` | `Softmax` | `SOFTMAX` | 概率，1000 类之和 = 1.000000 |
| `average_word_classifier` | `StatefulPartitionedCall:0` | `SOFTMAX` | 概率，2 类之和 = 1.000000 |
| `bert_classifier` | `StatefulPartitionedCall:0` | `SOFTMAX` | 概率，2 类之和 = 1.000000 |
| `selfie_multiclass` | `Identity` | `CONCATENATION` | **logits**，每像素 6 通道之和 ≈ **-2.3**（≠ 1） |

因此页面一律**直接读用、不叠加 Softmax**：

* 对已是概率的输出再 Softmax 会把 0.90 压到 ≈0.21，置信度与柱状图严重失真 ——
  `digit_classifier` 走的正是这条路；
* `selfie_multiclass` 输出的虽是 logits，但页面只做 **argmax**，而 argmax 在 Softmax
  前后不变，结果与「先 Softmax 再取最大」完全一致，所以同样不需要归一化。
* Laya 零样本页的选项概率由自带校准表（`laya_ml_calibration.json`，温度 + 阈值）决定，
  同样不经过 Softmax。

helper 里因此**不提供** softmax 工具函数 —— 本模块没有任何一处需要它。

### 4. 模块统一的推理配置

散落各页的线程数配置已收敛到 helper 单一入口：

```kotlin
TFLiteModelHelper.createCpuOptions()                                   // 自适应 2~4 线程 + XNNPACK
TFLiteModelHelper.createCpuOptions(xnnPackFlags = XNNPACK_FORCE_FP16)  // 要求 XNNPACK 以 fp16 执行
```

线程数由 `TFLiteModelHelper.CPU_THREADS` 统一提供（`availableProcessors()` 按设备核心数取值并封顶 4，
超过 4 线程后小核参与反而拉长时延），避免同模块内出现 2T / 4T 混用导致各页耗时数据无法横向比较。
`xnnPackFlags` 只在模型**明确要求**低精度执行时才传（Qwen3-TTS 的 `codec_partB` 即是，
这是模型规格而不是性能调优），具名常量 `TFLiteModelHelper.XNNPACK_FORCE_FP16 = 4`。
新增页面**不要**自己写 `CompiledModel.Options(...)`。

### 5. 依赖坐标

```toml
litert = "2.3.0"
# com.google.ai.edge.litert:litert        — CompiledModel / TensorBuffer / Accelerator / Interpreter 运行时
# com.google.ai.edge.litert:litert-gpu    — GPU 后端 native 运行库
# com.google.ai.edge.litert:litert-metadata — 模型元数据解析（读内嵌 vocab / 标签表）
```

CompiledModel API 位于 `com.google.ai.edge.litert.*` 包（分布在 `litert` 与 `litert-api`
两个构件上，后者经前者传递暴露）。

### 6. `Interpreter` 不是「1.x 残留」

`litert-2.3.0.aar` 里**同时**提供 `com.google.ai.edge.litert.CompiledModel` 与
`org.tensorflow.lite.Interpreter`，两者是同一个构件里并存的**两套 API**，不是「1.x / 2.x」的分野
（官方 2.x 的流式语音合成样例自己就两套都用）。

本模块只在 `helper/TFLiteModelHelper.createInterpreterOptions()` 一处开启这套 API，
`KittenSynthesizer` 是唯一的调用方，理由是那两张图依赖 TFLite *variable tensors*：

```kotlin
interpreter.resizeInput(i, shape)          // 动态序列长度：按句 reshape
interpreter.allocateTensors()              // resize 之后必须重新分配
interpreter.resetVariableTensors()         // LSTM 隐状态在 variable tensors 里跨调用保留，必须清零
interpreter.runForMultipleInputsOutputs(inputs, emptyMap())
interpreter.getOutputTensor(i).asReadOnlyBuffer()   // 真实波形长度只有跑完才知道
```

三点细节值得记下来：

* `resetVariableTensors()` **不可或缺**：形状变化会顺带重置隐状态，但**长度相同的两次合成不会** ——
  同一句话连说两遍会走样；
* `getOutputTensor` 的**顺序不保证**（predictor 实测为 `:0` / `:2` / `:1`），因此一律按**张量名后缀**定位；
* 张量名要**归一化**后再匹配（`serving_default_x:0` 与 `x` 是同一个输入）。

其余页面一律走 `CompiledModel`。

---

## 五、 Native 内存防泄漏规范

1. **模型从 assets 或应用私有目录构建**：
   * 内嵌资产的 5 个模型走 `CompiledModel.create(assets, fileName, options, null)`，
     内部由原生 `AssetManager` 读取，模型字节不需要先进 JVM 堆；
   * 运行时下载的模型走 `CompiledModel.create(absolutePath, options, null)`，直接按**绝对路径**加载
     （`nativeCreateFromFile`），同样不做 JVM 堆中转。`Environment` 传 `null` 时由运行时创建默认环境。
2. **输入输出走 `TensorBuffer`**：
   `createInputBuffers()` / `createOutputBuffers()` 返回的原生张量缓冲区必须**显式 `close()`**，
   不能依赖 GC。写入用 `writeFloat(FloatArray)` / `writeInt(IntArray)`，读取用 `readFloat()`。
   实现上每个页面都用 `try / finally` 成对关闭（`outputBuffers` 先于 `inputBuffers`，逐个 `runCatching` 隔离容错）。
3. **严格生命周期回收**：
   * 在 `Activity.onDestroy()` 中回收 `CompiledModel` 与预览位图，彻底销毁底层 C++ Runtime 实例与原生像素内存；
     **回收动作必须排进下面的引擎串行队列**，不能直接在主线程做。
   * 中间临时 `Bitmap` 在像素编码完成后立即 `.recycle()`：缩放产生的中间位图由
     `encodeBitmap` 回收，以 `!==` 判断，避免误回收调用方传入的位图。
4. **`CompiledModel` / `TensorBuffer` 属于「必须显式关闭」的一侧**：
   两者继承 `JniHandle implements AutoCloseable`，**只有 `close()`**，没有 `finalize` 兜底，
   漏关即泄漏到进程结束 —— 所有持有者都在 `onDestroy` 里排进队列关闭。
* 各示例页持有的预览位图（`currentBitmap` / 掩码位图）同样在 `onDestroy()` 统一回收；
  `MlPreviewView` 只持有引用，不负责回收，生命周期归宿主 Activity 管理。
* 换图（切换样本 / 相册选图）时统一按 **「赋新值 → 交给 View → 回收旧值」** 的顺序处理：
  直接覆盖 `currentBitmap` 会漏回收上一张，而先 `recycle()` 再交给 ImageView 又可能绘制到已回收位图。
  **回收旧值同样要排进引擎队列**：切图时上一张位图可能仍被上一次推理的后台预处理持用
  （`getPixels()` / `createScaledBitmap`），在主线程直接 `recycle()` 会踩到已释放的像素内存。
  这条竞态不需要退出页面即可触发——连点两次样本，第二张的解码与回收会与第一张的预处理并发。
* **UI 写入前必须过 `canTouchUi` 闸门**：`BaseVBActivity` 会先把 `binding` 置空、再走 `super.onDestroy()`，
  而 `lifecycleScope` 的取消就发生在同一次 `super.onDestroy()` 里。于是「加载/推理期间退出页面」时，
  后台任务恢复后抛出的取消异常会被各页的 `catch (e: Exception)` 捕获，此时再写 `binding` 必然 NPE
  （工程内没有全局 `UncaughtExceptionHandler`，等同于直接崩溃）。因此模块内统一纪律：
  凡是在 `catch` / `finally` 里写 UI 的地方，先判 `!isFinishing && !isDestroyed`。
  注意只有 `BaseVBActivity` 系页面需要它 —— `binding` 是 `lateinit var`、从不置空的页面不要硬加闸门。
* **推理、引擎构建与引擎关闭共用一条串行队列**（全模块统一，`TFLiteLifecycleSupport.createEngineDispatcher()`）：
  `CompiledModel` 底层的解释器不是线程安全对象——LiteRT 的 Java 层 `run()` 与 `close()` 都没有同步保护，
  `close()` 会释放 `run()` 正在读的原生 handle。因此三者必须排进同一条 `limitedParallelism(1)` 队列，
  它同时给出互斥与顺序：
  - 连点按钮（本模块多个页面都有）不会退化成同一实例上的并发 `run()`；
  - 页面退出时 `onDestroy` 的 `close()` 不会撞上仍在执行的 `run()`——`lifecycleScope` 的取消只是
    协作式的，`run()` 与位图预处理都不可中断，仍会跑完；
  - 位图 `recycle()` 也走这条队列，**换图路径与 `onDestroy` 路径一视同仁**：
    它与后台预处理的 `getPixels()` 并发同样会踩到已释放的像素内存。
  **用 `limitedParallelism(1)` 而不是 `Mutex`**——后者只给互斥，关闭动作可能插到两次推理中间。
  关闭动作由 `closeEngineOn` / `runOnEngineQueue` 另起独立作用域派发——此刻 `lifecycleScope`
  已被取消，若用它派发会被静默丢弃，那样原生实例就永久泄漏了。
  这两条入口内部都**吞掉清理过程中的异常**：它们跑在无人监督的独立作用域上，
  异常一旦逃逸到线程默认处理器就会直接杀进程，而清理失败本身不该让 App 崩。
* **构建即取消即释放**：各页一律走 `buildOffMainThreadSafely(...)`（helper 的约束是
  `T : AutoCloseable`，所以「模型 + 描述」这类组合体要自己实现 `AutoCloseable`），
  内部在引擎队列上构建，并在「已构建完成但调用方已被取消」时就地 `close()`，
  避免 MB 级原生实例只靠 GC 兜底。模型元数据读取放在 `Dispatchers.IO`，主线程只做 UI 更新。
* **模型下载也要能取消**：`ModelDownloader` 面向数十 MB 到数 GB 的文件，因此有三条纪律 ——
  写 `<name>.part` 用 `Range` 请求**断点续传**；只有完整写完才 `rename` 成正式名（因此
  `ModelStore.isReady` 只判存在性就不会把半截文件当成可用模型）；页面 `onDestroy` 里 `cancel()`
  —— 已下部分保留供下次续传。
* **切换模型时词表与引擎必须同一步切换**（文本分类页）：两个模型的词表大小与序列长度都不同，
  若只换引擎而词表滞后，会用 A 模型的词表去编码 B 模型的输入。

---

## 六、 模型资产：内嵌 assets + 运行时下载 双轨

### 1. 内嵌 assets（约 62 MB，全部 FP32）

对应官方最早那批「模型小、可随包分发」的任务：

| 资产 | 体积 | 用途 |
| :--- | ---: | :--- |
| `efficientnet_lite0.tflite` | ~18 MB | 图像分类（附内嵌 `labels_without_background.txt`，**1000 行**，与模型 1000 类输出一一对应） |
| `selfie_multiclass.tflite` | ~16 MB | 人像语义分割 |
| `bert_classifier.tflite` | ~25 MB | 文本情感分类（MobileBERT，附内嵌 `vocab.txt`） |
| `digit_classifier.tflite` | ~1.2 MB | MNIST 手写数字 |
| `average_word_classifier.tflite` | ~760 KB | 文本情感分类（轻量备选） |
| `config.json` / `emb.bin` / `g2p_meta.json` / `g2p_dict.txt.gz` | ~1.9 MB | Matcha-TTS 的文字前端（音素表与元数据） |
| `presets.json` | ~1 KB | Laya 零样本分类的提示词预设 |
| `tokenizer_test_vectors.json` | ~1 KB | Qwen3-TTS 分词器的启动自检向量（官方 baked） |
| `asr_model_metadata.json` | ~2 KB | ASR 模型清单：6 个模型的下载地址、log-Mel 参数、解码起止 token id |

`module_ml` 的 `build.gradle.kts` 中 `noCompress += listOf("tflite")` 让模型以未压缩形式进入 APK，
原生侧直接读 assets，省去运行时解压。

> 注意 `aapt` 会**自动解压 `.gz` 资产并去掉扩展名** —— 源文件是 `g2p_dict.txt.gz`（1.76 MB），
> 而打包后资产名变成 `g2p_dict.txt`（7.57 MB）。读取处的写法是「先试 `.txt`、失败再回退
> `GZIPInputStream(.txt.gz)`」，两种情况都能跑。

### 2. 运行时下载（按任务分档，合计约 3 GB）

其余任务（零样本 + 三个语音合成 + 语音识别）的模型从数十 MB 到 1.67 GB 不等。它们**一律不入库、首次使用时
按需下载**，理由是双重的：

* APK 若膨胀到 GB 量级，安装需要等量的空闲存储，包体也远超应用商店的基础包上限；
* **官方这几个 sample 本来就是运行时下载**（各自带 `ModelDownloader` / `ModelMetadataManager`
  或 `install_to_device.sh`），把模型塞进 APK 反而偏离官方。

| 任务 | 仓库（`litert-community/`） | 关键文件 | 体积 |
| :--- | :--- | :--- | ---: |
| 零样本分类 | `Laya-Multilingual-LiteRT` | 主图 + 动作头 + 校准表 + 分词器 + 嵌入表 | ~679 MB |
| 语音合成（Matcha） | `Matcha-TTS` | 文本编码 / 扩散解码 / 声码器三图 + 共用 G2P 图 + 前端表 | ~92 MB |
| 流式语音合成（KittenTTS） | `kitten-tts-nano-0.8` | predictor / prosody / vocoder 三图 + `voices.npz` + 共用 G2P 图 | ~61 MB |
| 语音语言模型合成（Qwen3-TTS） | `Qwen3-TTS-12Hz-0.6B-Base` | talker + 折叠 MTP + 两段 codec + 4 张宿主表 + 分词器 | ~1.67 GB |
| 语音识别（ASR） | `litert-community/*` 与各家上游 | 6 个模型各自「主图 + 分词器」两项；**同一时刻只会下载所选的那一套** | 44 MB ~ 801 MB |

落盘位置是应用私有目录 `filesDir/models/`（`ModelStore`），下载完成后完全离线可用。
下载源固定为 `hf-mirror.com`（官方下载器硬编码 `huggingface.co`，本机不可达，镜像内容一致），
切换镜像只需改 `RemoteModel.MIRROR_BASE` 一处。

两个 TTS 页共用同一张神经 G2P 图（`dp_g2p_matcha_fp16.tflite`），落盘同名，
因此先做过任一页之后另一页不会重复下载。

**Qwen3-TTS 的体积与内存必须提前说清楚**：下载约 1.67 GB；运行期三/四张图约 940 MB（文件支撑的
内存映射）+ 宿主表约 723 MB + KV cache 约 470 MB，峰值在 1.5 GB 量级，面向 8 GB 内存设备；
官方在 Pixel 8a 上实测 **RTF ≈ 6.7**（4.16 s 音频耗 28.0 s），界面必须如实标注「慢于实时」。
页面因此对 `OutOfMemoryError` 单独兜底并给出可读的失败原因，而不是让它冒到顶层。

**引入新模型的准入条件**：再引入大模型前必须先评估仓库与 APK 体积影响，并向用户确认。
assets 未被 `.gitignore` 排除，二进制会永久写入 git 历史；将来若在意仓库体积，
可改用 Git LFS 跟踪 `*.tflite`。

---

## 七、 纯函数单元测试与并发约束测试

模块共 **132 个 JVM 用例**，全部不依赖 Android 运行时，覆盖五类「静默出错代价极高」的逻辑。

### 1. 推理工具与分词（`helper/TFLiteModelHelperTest.kt`，12 例）

| 被测函数 | 覆盖要点 |
| :--- | :--- |
| `CPU_THREADS` | 按设备核心数自适应并封顶在 2~4 |
| `getTopK` | 按概率降序返回并保留原始索引、k 超长时返回全部 |
| `buildVocabulary` | 单列词表行号即 id、双列词表取第二列、显式 id 与行号不一致时以文件里的 id 为准 |
| `decodeSegmentationMask` | 平坦 `[H*W*C]` 数组逐像素 argmax、张量长度不足时返回全零掩码而不越界 |
| `countSegmentationClasses` | 类别计数忽略越界索引 |
| `colorizeSegmentationMask` | 背景透明化开关、类别索引越界回退 |

### 2. 生命周期闸门与引擎串行队列（`helper/TFLiteLifecycleSupportTest.kt`，8 例）

这类逻辑一旦写错，表现是 MB 级原生实例静默泄漏、或运行时原生崩溃，现场没有别的信号可循：

| 被测目标 | 覆盖要点 |
| :--- | :--- |
| `buildOffMainThreadSafely` | 构建完成后被取消 → 就地释放；正常完成 → 原样交还且不越权关闭；factory 抛异常 → 异常原样传播 |
| `createEngineDispatcher` | `limitedParallelism(1)` 确实把并发放成串行（同一时刻并发峰值恒为 1） |
| `closeEngineOn` | 按传入顺序关闭、单个资源释放失败不阻断后续、调用立即返回而不阻塞主线程 |
| `runOnEngineQueue` | 清理任务的异常被就地捕获，不逃逸到线程默认处理器（在 Android 上即直接闪退） |

### 3. 语音合成纯函数（`tts/TtsSynthesisTest.kt`，7 例）

| 被测目标 | 覆盖要点 |
| :--- | :--- |
| `SentenceChunker` | 按句末标点切句并给每块补逗号、已有标点不重复补、空串与纯空白不产出块、超长句按词边界再切且不丢字母 |
| `KittenVoiceTable` | `.npz` 解析后按音色顺序排列、`styleFor` 按文本长度取行并在超长时钳到末行、缺条目时直接报错而不是静默返回空表 |

### 4. 语音语言模型（`tts/QwenTtsTest.kt`，13 例）

| 被测目标 | 覆盖要点 |
| :--- | :--- |
| `Npy` | 按 `shape` 决定元素个数（截断数据要报错而不是静默读歪）、fp16 逐元素上转（含非规格化数与 ±Inf）、不支持的 dtype 与 fortran order 直接报错、`.npz` 按名取条目 |
| `QwenBpeTokenizer` | 字节映射 + 合并链、前导空格按字节参与合并、数字按单字符切、NFC 归一化后组合字符与预组合字符等价、merges 引用词表外 piece 时构造期报错、自检能指出不一致的那条 |
| `Qwen3TtsEngine.Result` | 按官方 Pixel 8a 实测数据折算 RTF 与音频时长 |
| `RemoteModels.QWEN` | 下载清单的文件名与引擎声明的文件名一一对应、落盘名里不含目录（官方脚本会把 `tables/` / `voices/` 拍平） |

### 5. 语音识别（`asr/`，92 例）

ASR 是唯一「一大半逻辑在宿主侧」的示例 —— 分词、log-Mel、重叠窗口对齐、CTC/TDT 解码
全都不在模型图里，因此测试密度也最高。其中最要紧的两条是**与上游逐元素对齐**：
log-Mel 拿真 librosa 的 golden 比、分词器拿真 HF `tokenizers` 的 golden 比。

| 测试类 | 用例数 | 覆盖要点 |
| :--- | ---: | :--- |
| `AsrMelTest.kt` | 9 | 与**真 librosa** 产出的 golden 逐元素比对（80 / 128 两个 mel 数）、帧数公式 `1 + n/hop`、2 的幂 FFT、mel 换算往返（线性段与对数段分别验）、Hann 窗周期性、滤波器组非负且低频峰值更大 |
| `AsrTokenizerTest.kt` | 12 | **六个真分词器**（夹具原样保留真实 decoder 链）逐用例与 HF `tokenizers` 比对；Unigram 以数组下标为 id、special token 跳过、ByteLevel 的 `Ġ`、ByteLevel 攒字节再解码、Metaspace 的 `prepend_scheme`、Strip 的方向、ByteFallback 还原字节、未实现类型显式报错、词表外 id 忽略 |
| `AsrLevenshteinTest.kt` | 29 | 官方重叠窗口合并套件全量移植：无重叠 / 完美对齐 / 自定义 pivot 与搜索窗 / 真实重复词不被误删 / 两段真实长句 / 增量输出 / 编辑距离阈值两侧 / 带时间戳路径（含时间戳倒退时整段丢弃） |
| `AsrModelCatalogTest.kt` | 12 | 直接读**线上 asset 清单本体**（不另抄夹具）：模型顺序、缺省值（未写 `nMels` 即 80）、分词器落盘名唯一、完整 URL 能拆回 `repo` + `remotePath` 并原样拼回 |
| `AsrFileAudioSourceTest.kt` | 8 | WAV 头解析、定长块 + 重叠左移拼接、短音频尾部补 0（**首块短**与**非首块短**各一例）、坏头 / 采样率不符的报错文案、`data` 声明长度大于实际时钳位而不是崩 |
| `AsrMicrophoneAudioSourceTest.kt` | 11 | 注入假 `AudioReader`（不需要真麦克风）：滑动窗口产出、RMS 静音门限整块置 0、`ERROR_DEAD_OBJECT` / `ERROR_INVALID_OPERATION` 终止序列、其余负值跳过重试、部分读累积到满格才产出 |
| `AsrDecoderTest.kt` | 11 | CTC 逐帧 argmax（并列时取小下标）与压重复（时间戳取首次出现、EoS 时间戳为总帧数）、输入长度对齐（短补零 / 长留尾）、`TensorType.numElements` 的标量 / 一维 / 多维 / 零维推导 |

**测试要有鉴别力（变异法）**：改动并发 / 生命周期 / 解析逻辑后，必须做一次变异验证 ——
人为破坏被测不变量后重跑，确认目标用例**确实变红**。只跑绿的测试区分不了「测试有效」与「测试存在」，
本模块已由此查出过一个恒真用例。变异必须**逐个分离注入**：一次注入多个会让归因不干净。

已做的变异验证（每轮改完都在同一个测试类上单独注入并复跑）：

| 注入的破坏 | 变红的用例 |
| :--- | :--- |
| 去掉 `SentenceChunker.ensurePunctuation` 的补逗号 | 2 个 |
| `KittenVoiceTable.styleFor` 的行钳位 `STYLES_ROWS - 1` → `- 2` | 1 个 |
| `Npy` 的 header 起始偏移 `10` → `11` | 2 个 |
| 预分词正则 `\p{N}` → `\p{N}+` | 1 个（数字按单字符切） |
| `Npy.halfToFloat` 非规格化指数偏移 `127-15` → `127-14` | 1 个（fp16 上转） |
| ASR `LiteRtRunner.padOrTruncateFeatures` 截断改成「留头丢尾」 | 1 个（长特征丢的是头部） |
| ASR `LogMel` 三角滤波器斜率反向 | 3 个（两个 mel 数的 golden 与滤波器组形状） |
| ASR `HfTokenizer` 的 Metaspace `prepend_scheme` 判定反向 | 1 个（词首空格去留） |
| ASR `AsrModelCatalog` 的 `nMels` 默认值 `80` → `128` | 2 个（未写 `nMels` 的模型） |
| ASR `FileAudioSource` 去掉尾部补 0 | 起初 **0 个（缺口！）** → 补测后 1 个 |

最后一行的价值超出它本身：变异**先暴露了测试缺口**——原有的三个「补 0」用例全都只走**首块**，
而首块的 `bufferForSamples` 是刚分配出来的（本来就是全 0），补 0 是空操作，删掉它也测不出来；
只有**非首块**读不满时，缓冲区尾部残留的才是上一块左移前的旧采样，补 0 才真正起作用。
于是补了 `getAudioData_shortNonFirstChunk_zeroesStaleTail`（1900 采样 / interval 1600 / overlap 800
→ 第 2 块只读到 300 个新采样，尾部 500 个槽位必须为 0），再注入同一处破坏才变红。
**这就是「变异法」的用途：它不只能证明测试有效，还能证明测试不完整。**


**异步性质的测试不要依赖「派发慢于下一条语句」**：断言两个标记的先后顺序是非保证性时序，
高负载下会偶发误报。正解是用 `CountDownLatch` 卡住被派发的动作 —— 若实现是异步派发，
调用方必在它完成前返回（断言恒成立）；若被改成同步等待，调用方会阻塞在闭锁上，断言立即失败。

运行方式：

```bash
./gradlew :modules:module_ml:testProdDebugUnitTest -PenableFlutter=false
```

---

## 八、 官方文档与资源链接

* **LiteRT 官方文档**：[https://ai.google.dev/edge/litert](https://ai.google.dev/edge/litert)
* **LiteRT 官方样例仓库（本模块页面的对齐来源）**：[https://github.com/google-ai-edge/litert-samples](https://github.com/google-ai-edge/litert-samples)
  * `samples/litert/` — CompiledModel API 样例（**本模块对齐的目标**）
* **TensorFlow Lite 门户（LiteRT 前身）**：[https://www.tensorflow.org/lite](https://www.tensorflow.org/lite)
* **XNNPACK 汇编加速引擎**：[https://github.com/google/XNNPACK](https://github.com/google/XNNPACK)
* **Google 预训练模型中心（Kaggle Models）**：[https://www.kaggle.com/models?framework=tfLite](https://www.kaggle.com/models?framework=tfLite)
