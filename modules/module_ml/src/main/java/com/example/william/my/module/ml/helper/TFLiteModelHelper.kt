package com.example.william.my.module.ml.helper

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import com.google.ai.edge.litert.Accelerator
import com.google.ai.edge.litert.CompiledModel
import com.google.ai.edge.litert.support.metadata.MetadataExtractor
import org.tensorflow.lite.Interpreter
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.ByteBuffer

/**
 * LiteRT CompiledModel 推理工具类
 *
 * 统一收口三件事：推理配置与引擎构建、位图到输入张量的编码、模型输出到业务结果解码；
 * 同时作为模块内推理配置的唯一入口，避免各示例页出现线程数与加速后端不一致。
 *
 * 全部实现基于 `com.google.ai.edge.litert` 的 CompiledModel API。位图解码与后处理
 * 全部是纯函数（不接收 Context），落在 JVM 单测覆盖范围内。
 *
 * **后处理一律不叠加 Softmax**：本模块 5 个模型里 4 个的输出张量本身就是概率
 * （`digit_classifier` / `efficientnet_lite0` / `average_word_classifier` / `bert_classifier`
 * 的末算子就是 `SOFTMAX`，实测各类之和为 1）；唯一输出 logits 的 `selfie_multiclass`
 * （实测末算子是 `CONV_2D`，其前一个算子是 `TRANSPOSE_CONV`，输出通道未过 Softmax）
 * 只做 argmax，而 argmax 在 Softmax 前后不变。
 * 对已是概率的输出再叠加 Softmax 会把 0.90 压到 ≈0.21，置信度与柱状图严重失真，
 * 因此本模块**不提供** Softmax 工具函数。
 */
object TFLiteModelHelper {

    /**
     * 模块统一的 CPU 推理线程数
     *
     * 各示例页共用同一档配置，避免同模块内出现 2T / 4T 混用导致时延数据无法横向比较。
     * 取值按设备可用核心数自适应并封顶 4 线程：主流手机大小核混合，超过 4 线程后
     * 小核参与计算反而拉长时延。
     */
    val CPU_THREADS: Int = Runtime.getRuntime().availableProcessors().coerceIn(2, 4)

    /**
     * `TFLITE_XNNPACK_DELEGATE_FLAG_FORCE_FP16`
     *
     * 让 XNNPACK 用 fp16 执行一张 fp32 图。这是官方 `text_to_speech_lm` 样例对
     * `codec_partB` 的明确要求（那一半图在 fp16 下才跑得动实时），因此放在单一入口里
     * 作为具名常量，而不是把魔数 4 散落到页面。
     */
    const val XNNPACK_FORCE_FP16: Int = 4

    // ─────────────────── 推理配置与引擎构建 ───────────────────

    /**
     * 构建模块统一的 CPU 推理配置：多线程 + XNNPACK ARM NEON 指令集优化
     *
     * 线程数统一由 [CPU_THREADS] 提供，保证各示例页的时延数据可以直接横向比较。
     *
     * @param xnnPackFlags XNNPACK 标志位；默认（null）沿用运行时的默认值，只有明确要求
     *   低精度执行的模型才显式传 [XNNPACK_FORCE_FP16]（属于**模型规格**，不是性能调优）。
     */
    fun createCpuOptions(
        threads: Int = CPU_THREADS,
        xnnPackFlags: Int? = null,
    ): CompiledModel.Options = CompiledModel.Options(Accelerator.CPU).apply {
        cpuOptions = CompiledModel.CpuOptions(numThreads = threads, xnnPackFlags = xnnPackFlags)
    }

    /**
     * 构建 GPU 推理配置：通过 [Accelerator.GPU] 在引擎编译期选定加速后端
     *
     * 后端属于**编译期**选项而非运行时挂载的插件，因此页面上「切换加速后端」的实现
     * 是重建引擎，而不是替换某个对象。设备不支持时构建会直接抛异常，由调用方回退 CPU。
     *
     * @param precision GPU 计算精度；默认（null）沿用运行时的默认精度。
     *   个别模型要求显式 FP32（Laya 零样本分类的主图即如此，官方样例明确指定），
     *   这类精度要求属于**模型规格**而非性能调优，因此在此处显式声明而不是散落在页面里。
     */
    fun createGpuOptions(
        precision: CompiledModel.GpuOptions.Precision? = null,
    ): CompiledModel.Options = CompiledModel.Options(Accelerator.GPU).apply {
        if (precision != null) {
            gpuOptions = CompiledModel.GpuOptions(precision = precision)
        }
    }

    /**
     * CPU 打底 + 追加一个可选后端的配置
     *
     * 官方 ASR 样例传的是 `setOf(Accelerator.CPU, accelerator)` —— 把 CPU **一起**塞进后端集合，
     * 由运行时在目标后端不可用时自行退回，而不是等编译期抛异常。这与其它页面
     * 「先试 [createGpuOptions]，失败再重建 CPU 引擎」是两条不同的容错路径，
     * 因此单独开一个入口，避免页面自己拼 `Options(...)` 破坏「推理配置单一入口」。
     *
     * 目标后端是 GPU 时**一律**填 [CompiledModel.GpuOptions]（即使两个参数都是 null）：
     * 官方 `LiteRtRunner` 正是这样写的，而 `GpuOptions` 的 null 字段等价于「沿用运行时默认值」，
     * 所以行为不会因为填了一个空壳对象而改变，反而与官方结构一一对应。
     *
     * @param accelerator 期望的目标后端；传 [Accelerator.CPU] 时等价于 [createCpuOptions]
     * @param gpuPrecision GPU 计算精度；`null` 或 [CompiledModel.GpuOptions.Precision.DEFAULT]
     *   表示允许低精度（fp16）。官方 ASR 清单的 `gpuEnforceFloat32` 默认为 true，
     *   即**必须**传 [CompiledModel.GpuOptions.Precision.FP32]。
     * @param gpuConstantTensorSharing 是否允许 GPU 共享常量张量；仅在 [accelerator] 为 GPU 时生效
     */
    fun createFallbackOptions(
        accelerator: Accelerator,
        gpuPrecision: CompiledModel.GpuOptions.Precision? = null,
        gpuConstantTensorSharing: Boolean? = null,
    ): CompiledModel.Options = CompiledModel.Options(setOf(Accelerator.CPU, accelerator)).apply {
        cpuOptions = createCpuOptions().cpuOptions
        if (accelerator == Accelerator.GPU) {
            gpuOptions = CompiledModel.GpuOptions(
                precision = gpuPrecision,
                constantTensorSharing = gpuConstantTensorSharing,
            )
        }
    }

    /**
     * 从 assets 直接构建 CompiledModel
     *
     * `CompiledModel.create(assets, ...)` 内部走原生 AssetManager 读取，模型无需先读进 JVM 堆；
     * `Environment` 传 null 时由运行时创建默认环境。构建会解析并编译计算图，耗时较长，
     * 调用方一律通过 [buildOffMainThreadSafely] 在后台线程执行。
     */
    @Throws(Exception::class)
    fun createModel(
        assets: AssetManager,
        fileName: String,
        options: CompiledModel.Options,
    ): CompiledModel = CompiledModel.create(assets, fileName, options, null)

    /**
     * 从应用私有目录的文件构建 CompiledModel（运行时下载的模型走这条路径）
     *
     * `create(path, ...)` 与 `create(assets, ...)` 是两条并列的原生入口：前者按绝对路径打开
     * 文件，后者交给 AssetManager。两者都只接受完整落地后的文件，因此调用方必须先经
     * `ModelStore` 确认下载完成（`.part` 不会被当成模型）。
     */
    @Throws(Exception::class)
    fun createModel(
        path: String,
        options: CompiledModel.Options,
    ): CompiledModel = CompiledModel.create(path, options, null)

    /**
     * 读取模型输入张量的正方形边长（H 与 W）
     *
     * **不能按模型名硬编码 224**：官方 `image_classification` 的两个模型输入并不相同 ——
     * `efficientnet_lite0` 是 `[1, 224, 224, 3]`，`efficientnet_lite2` 是 `[1, 260, 260, 3]`
     * （两者均由 flatbuffer 实测）。官方样例在 `classify()` 里写死了 224 并在注释里标注
     * 「typically use 224x224, but let's check if we can get it dynamically」，切到 Lite2
     * 后预处理的数组长度与输入张量不符 —— 本模块改为从引擎读取真实形状。
     *
     * 只处理 NHWC / NCHW 这类「batch + 两个空间维 + 通道」的 4 维输入；形状读不到
     * （或非 4 维、两空间维不等）时回退 [fallback]，由调用方给出该模型的已知值。
     *
     * 传空字符串表示「默认签名 / 第一个输入」：`getInputTensorType` 的 `signatureName`
     * 有默认值而 `inputName` 没有，且空串与本模块 `createInputBuffers()`（同样走默认签名）
     * 的语义一致。
     */
    fun readInputEdge(model: CompiledModel, fallback: Int): Int {
        val dimensions = runCatching {
            model.getInputTensorType("", "").layout?.dimensions
        }.getOrNull().orEmpty()
        val spatial = dimensions.drop(1).dropLast(1).filter { it > 0 }
        return if (spatial.size == 2 && spatial[0] == spatial[1]) spatial[0] else fallback
    }

    /**
     * 构建经典 `Interpreter` 的推理配置
     *
     * **为什么模块里还留着这套 API**：LiteRT 2.x 的 `litert` 同一个 artifact 里同时提供
     * `CompiledModel` 与旧的 `org.tensorflow.lite.Interpreter`，二者不是「1.x / 2.x」的分野。
     * 官方 2.x 的**流式语音合成**样例就同时用两套：它的 predictor / prosody 两张图把
     * 定长 LSTM 的隐状态放在 TFLite *variable tensors* 里，而 CompiledModel 的模型加载器
     * 目前不接受这类图（官方注释 b/365299994），所以那两张图只能走 `Interpreter`，
     * 靠 `resizeInput` + `resetVariableTensors` 逐句驱动。
     *
     * 其余示例页一律仍用 `CompiledModel`（见 [createCpuOptions] / [createGpuOptions]）。
     * 线程数同样取 [CPU_THREADS] 并开启 XNNPACK，保证与 CompiledModel 页面的时延可比。
     */
    fun createInterpreterOptions(threads: Int = CPU_THREADS): Interpreter.Options = Interpreter.Options()
        .setNumThreads(threads)
        .setUseXNNPACK(true)

    // ─────────────────── 模型元数据 ───────────────────

    /**
     * 读取模型内嵌关联文件（标签表、词表等）的文本行
     *
     * 只对**确实内嵌了关联文件**的模型有效：本模块两个文本模型的 `vocab.txt` 走这里。
     * 返回**未过滤**的原始行 —— 行号即类别下标，过滤空行会让 index 整体错位。
     *
     * 注意**不要指望 EfficientNet 系列**：实测 `efficientnet_lite0` 与 `efficientnet_lite2`
     * 的 `TFLITE_METADATA` 都只有 1088 字节，内容仅 name / description / version 与
     * 输入输出张量的 name，`SubGraphMetadata.associated_files` 为空 —— 官方样例注释里
     * 说的 `labels_without_background.txt` 并没有随模型分发。标签表因此以独立 asset
     * 分发（见 [loadAssetLines]），两个模型共用同一份 ImageNet 1000 类。
     */
    fun loadMetadataLines(
        context: Context,
        modelFileName: String,
        associatedFile: String,
    ): List<String> = try {
        val bytes = context.assets.open(modelFileName).use { it.readBytes() }
        val extractor = MetadataExtractor(ByteBuffer.wrap(bytes))
        if (!extractor.hasMetadata()) {
            emptyList()
        } else {
            extractor.getAssociatedFile(associatedFile)?.let(::readLines).orEmpty()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }

    /**
     * 读取 assets 里文本文件的原始行（标签表等小体积资产）
     *
     * 与 [loadMetadataLines] 一样**不过滤空行**：调用方按行号对齐类别下标，
     * 过滤会让后续标签整体错位。标签表末尾的换行会解析出一个空行，由调用方
     * 在加载后自行去掉尾部空元素（参见 `TFLiteImageClassificationActivity`）。
     */
    fun loadAssetLines(context: Context, assetFileName: String): List<String> = try {
        context.assets.open(assetFileName).use { readLines(it) }
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }

    private fun readLines(inputStream: InputStream): List<String> = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { it.readLines() }

    /**
     * 把词表文本行解析为 `token -> id` 映射
     *
     * 本模块两个文本模型内嵌的 `vocab.txt` **文本格式并不相同**，解析器要同时吃得下两种形式：
     *
     * - **单列**：整行就是 token，**行号即 id**（`bert_classifier.tflite`，形如 `[PAD]` /
     *   `[unused0]`；实测 `[UNK]` 的行下标为 100，即 id = 100）；
     * - **双列**：形如 `<PAD> 0` / `the 3`，以**第二列的数字**为 id
     *   （`average_word_classifier.tflite`；实测该词表每一行的显式 id 都等于行号，
     *   两种解法结果一致，这里仍按显式 id 解析，避免将来换词表时整体错位）。
     *
     * 判别方式是「最后一个空格之后能否解析成整数」：单列词表里 token 不含空格，
     * 所以不会误判。注意**不能过滤空行** —— 行号参与 id 计算，过滤会让后续 id 整体错位。
     *
     * 官方 Android 的 `TextClassificationHelper.getVocabulary` 只实现了前一种（纯按行号建表），
     * 本模块多支持一列显式 id，属于**增强**而不是偏离。
     */
    fun buildVocabulary(lines: List<String>): Map<String, Int> {
        val vocabulary = HashMap<String, Int>(lines.size * 2)
        lines.forEachIndexed { index, line ->
            val separator = line.lastIndexOf(' ')
            val explicitId = if (separator > 0) line.substring(separator + 1).toIntOrNull() else null
            if (explicitId != null) {
                vocabulary[line.substring(0, separator)] = explicitId
            } else {
                vocabulary[line] = index
            }
        }
        return vocabulary
    }

    // ─────────────────── 位图 → 输入张量 ───────────────────

    /**
     * 位图整图重采样为目标尺寸，编码为交错 RGB 的 FloatArray（行优先，长度 w * h * 3）
     *
     * 一律**整图拉伸**，不做裁剪 —— 与官方 2.x 样例保持一致（官方即 `createScaledBitmap`
     * 后直接取像素）。两个理由：
     * - 图像分类的输出是整图级向量，拉伸带来的长宽比变化不影响「图里有什么」；
     * - 语义分割的掩码相对**整幅输入图**逐像素输出，先裁剪会让掩码与原图整体错位，
     *   无法再叠加回原图。
     */
    fun bitmapToFloatArray(
        bitmap: Bitmap,
        targetWidth: Int,
        targetHeight: Int,
        normalize: InputNormalize,
    ): FloatArray = encodeBitmap(toSizedBitmap(bitmap, targetWidth, targetHeight), normalize, bitmap)

    private fun toSizedBitmap(bitmap: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap = if (bitmap.width == targetWidth && bitmap.height == targetHeight) {
        bitmap
    } else {
        Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
    }

    /**
     * 逐像素写成交错 RGB 浮点数组
     *
     * @param source 实际取像素的位图（可能是缩放/裁剪产物）
     * @param original 调用方传入的位图；两者不同才回收 [source]，避免误回收调用方对象
     */
    private fun encodeBitmap(source: Bitmap, normalize: InputNormalize, original: Bitmap): FloatArray {
        val width = source.width
        val height = source.height
        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)

        val output = FloatArray(width * height * 3)
        for (i in pixels.indices) {
            val pixel = pixels[i]
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF
            val base = i * 3
            when (normalize) {
                InputNormalize.RAW_0_255 -> {
                    output[base] = r.toFloat()
                    output[base + 1] = g.toFloat()
                    output[base + 2] = b.toFloat()
                }

                InputNormalize.FLOAT_MINUS1_1 -> {
                    output[base] = (r - 127.5f) / 127.5f
                    output[base + 1] = (g - 127.5f) / 127.5f
                    output[base + 2] = (b - 127.5f) / 127.5f
                }

                InputNormalize.FLOAT_0_1 -> {
                    output[base] = r / 255.0f
                    output[base + 1] = g / 255.0f
                    output[base + 2] = b / 255.0f
                }
            }
        }

        // 缩放产生新对象时及时回收，避免原生像素内存堆积
        if (source !== original) {
            source.recycle()
        }
        return output
    }

    // ─────────────────── 后处理 ───────────────────

    /**
     * 获取置信度最高的 Top-K 索引与概率值
     */
    fun getTopK(probabilities: FloatArray, k: Int = 5): List<Pair<Int, Float>> = probabilities
        .mapIndexed { index, score -> Pair(index, score) }
        .sortedByDescending { it.second }
        .take(k)

    /**
     * 逐像素 argmax 解码语义分割输出
     *
     * 入参为 CompiledModel 的平坦输出 `[H * W * C]`（行优先，通道交错），
     * 对每个像素在 C 个类别通道上取最大响应，返回长度为 H * W 的类别索引掩码。
     */
    fun decodeSegmentationMask(
        logits: FloatArray,
        width: Int,
        height: Int,
        classCount: Int,
    ): IntArray {
        val pixelCount = width * height
        if (classCount <= 0 || logits.size < pixelCount * classCount) return IntArray(pixelCount)
        val mask = IntArray(pixelCount)
        for (pixel in 0 until pixelCount) {
            val base = pixel * classCount
            var bestIndex = 0
            var bestScore = -Float.MAX_VALUE
            for (c in 0 until classCount) {
                val score = logits[base + c]
                if (score > bestScore) {
                    bestScore = score
                    bestIndex = c
                }
            }
            mask[pixel] = bestIndex
        }
        return mask
    }

    /**
     * 统计掩码中各类别的像素数量，索引即类别 id
     */
    fun countSegmentationClasses(mask: IntArray, classCount: Int): IntArray {
        val counts = IntArray(classCount)
        for (index in mask) {
            if (index in 0 until classCount) {
                counts[index]++
            }
        }
        return counts
    }

    /**
     * 将类别掩码映射为 ARGB 像素数组，便于直接构建叠加层 Bitmap
     *
     * @param backgroundTransparent true 时类别 0（背景）输出全透明，只保留前景色块
     */
    fun colorizeSegmentationMask(
        mask: IntArray,
        colors: IntArray,
        backgroundTransparent: Boolean = true,
    ): IntArray = IntArray(mask.size) { i ->
        val index = mask[i]
        if (backgroundTransparent && index == 0) 0 else colors.getOrElse(index) { 0 }
    }
}

/**
 * 位图到模型输入的归一化区间
 *
 * 区间由模型规格决定，必须以实测为准 —— 官方 2.x 样例中 EfficientNet / SelfieMulticlass
 * 取 `(v - 127.5) / 127.5`（即 [-1, 1]），MNIST 手写数字模型则直接喂 0~255 原始值。
 */
enum class InputNormalize {
    /** 原始 0~255，不做归一化 */
    RAW_0_255,

    /** 归一化到 [-1, 1]：(v - 127.5) / 127.5 */
    FLOAT_MINUS1_1,

    /** 归一化到 [0, 1]：v / 255 */
    FLOAT_0_1,
}
