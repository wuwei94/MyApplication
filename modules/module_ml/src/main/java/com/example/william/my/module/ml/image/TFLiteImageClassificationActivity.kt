package com.example.william.my.module.ml.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.ui.activity.BaseVBActivity
import com.example.william.my.module.ml.R
import com.example.william.my.module.ml.databinding.MlActivityImageClassificationBinding
import com.example.william.my.module.ml.download.ModelDownloader
import com.example.william.my.module.ml.download.ModelStore
import com.example.william.my.module.ml.download.RemoteModel
import com.example.william.my.module.ml.download.RemoteModels
import com.example.william.my.module.ml.helper.InputNormalize
import com.example.william.my.module.ml.helper.TFLiteModelHelper
import com.example.william.my.module.ml.helper.buildOffMainThreadSafely
import com.example.william.my.module.ml.helper.canTouchUi
import com.example.william.my.module.ml.helper.createEngineDispatcher
import com.example.william.my.module.ml.helper.runOnEngineQueue
import com.google.ai.edge.litert.CompiledModel
import com.google.ai.edge.litert.TensorBuffer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext
import kotlin.system.measureNanoTime

/**
 * LiteRT — EfficientNet-Lite 图像分类与物体识别
 *
 * 演示从模型加载、图像预处理到 Top-5 分类结果展示的完整流程，
 * 支持 EfficientNet-Lite0 / Lite2 模型切换与 CPU / GPU 硬件加速切换。
 * 推理侧走 LiteRT 2.x 的 CompiledModel API。
 *
 * 核心机制与避坑点：
 * 1. **两个模型、两种输入尺寸**：Lite0 输入 `[1, 224, 224, 3]`、Lite2 输入
 *    `[1, 260, 260, 3]`（均为 flatbuffer 实测）。官方样例在 `classify()` 里写死 224，
 *    切到 Lite2 后预处理数组长度与输入张量不符；本页从引擎读真实形状
 *    （[TFLiteModelHelper.readInputEdge]），只在读不到时回退到枚举里的已知值。
 * 2. **Lite0 随包、Lite2 运行时下载**：Lite0 是默认模型，不能依赖网络；Lite2 有 24 MB，
 *    按模块既定策略不入 assets，首次选中时下载到应用私有目录（之后离线可用）。
 * 3. **标签表是独立资产**：实测两个模型的 `TFLITE_METADATA` 都只有 1088 字节，
 *    不含 `labels_without_background.txt`（官方样例的注释与事实不符），因此标签表以
 *    `assets/imagenet_labels.txt` 分发（1000 行、无背景类），两个模型共用 ——
 *    用三个样本离线跑过：两模型 top-1 索引完全一致（207 golden retriever /
 *    817 sports car / 504 coffee mug），类别顺序相同。
 * 4. CPU / GPU 切换在 2.x 里是**引擎编译期**选项：`Accelerator.CPU` 与 `Accelerator.GPU`
 *    分别在构建 CompiledModel 时选定，切换后端即重建引擎。
 * 5. 模型输出为 FP32 概率 `[1, 1000]`，**没有背景类**，与标签表 1000 行严格一一对应
 *    （官方样例同样是 `labels.zip(output)` 直接下标），**不需要反量化也不需要 Softmax**。
 * 6. 整图拉伸重采样，与官方样例的 `createScaledBitmap` 一致。
 *
 * 官方样例：https://github.com/google-ai-edge/litert-samples/tree/main/samples/litert/image_classification
 */
@Route(path = RouterPath.Ml.ImageClassification)
class TFLiteImageClassificationActivity :
    BaseVBActivity<MlActivityImageClassificationBinding>(),
    View.OnClickListener {

    /**
     * 页面可选的两个模型
     *
     * [remote] 为 `null` 表示随 APK 分发（assets），否则从应用私有目录加载、必要时先下载。
     * [defaultInputEdge] 是该模型输入张量的实测边长，仅在
     * [TFLiteModelHelper.readInputEdge] 读不到形状时作为回退。
     */
    private enum class ImageModel(
        val displayName: String,
        val fileName: String,
        val remote: RemoteModel?,
        val defaultInputEdge: Int,
    ) {
        LITE0("Lite0", "efficientnet_lite0.tflite", null, 224),
        LITE2("Lite2", "efficientnet_lite2.tflite", RemoteModels.EFFICIENTNET_LITE2, 260),
    }

    @Volatile
    private var model: CompiledModel? = null

    private var labels: List<String> = emptyList()

    private val labelsFileName = "imagenet_labels.txt"

    /** 当前选中的模型；界面切换与重建协程都以它为准 */
    @Volatile
    private var selectedModel = ImageModel.LITE0

    /** 当前引擎的输入边长，预处理按它重采样（Lite0 224 / Lite2 260） */
    @Volatile
    private var inputEdge = ImageModel.LITE0.defaultInputEdge

    /** 上一次未完成的重建句柄：拨动开关时先取消，避免两次重建并发改写引擎字段 */
    private var rebuildJob: Job? = null

    /** 当前推理协程：切模型 / 切后端前必须取消，否则旧任务会拿着旧引擎的语义继续跑 */
    private var inferenceJob: Job? = null

    /** 当前引擎实际对应的模型（用于加载失败时把选择器拨回去）；null 表示引擎尚未建好 */
    @Volatile
    private var activeModel: ImageModel? = null

    /** 程序化改变选择器时置真，避免监听器把「回滚」当成一次用户切换 */
    private var suppressModelCallback = false

    private val downloader by lazy { ModelDownloader(this) }

    /**
     * 推理与引擎重建共用的串行队列（见 [createEngineDispatcher]）
     *
     * 旧引擎只能在「没有 run() 在跑」时关闭，否则会 close 掉正在推理的原生实例；
     * 把推理、构建与关闭动作都排到这条单并发队列上，三者天然互斥。
     */
    private val engineDispatcher = createEngineDispatcher()

    private var currentBitmap: Bitmap? = null

    @Volatile
    private var useGpu = false

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        uri?.let { loadBitmapFromUri(it) }
    }

    override fun getViewBinding(): MlActivityImageClassificationBinding = MlActivityImageClassificationBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        binding.btnPickImage.setOnClickListener(this)

        binding.switchGpu.setOnCheckedChangeListener { _, isChecked ->
            useGpu = isChecked
            rebuildModel()
        }

        binding.rgModel.setOnCheckedChangeListener { _, checkedId ->
            if (suppressModelCallback) return@setOnCheckedChangeListener
            val target = if (checkedId == R.id.rbLite2) ImageModel.LITE2 else ImageModel.LITE0
            if (target == selectedModel) return@setOnCheckedChangeListener
            selectedModel = target
            rebuildModel()
        }

        // 异步有序初始化：加载标签 -> 构建引擎
        initModelAndLabels()
    }

    private fun initModelAndLabels() {
        lifecycleScope.launch {
            try {
                labels = withContext(Dispatchers.IO) {
                    // 标签表末尾有换行，解析出的尾部空行必须去掉；中间行不能动（行号即类别下标）
                    TFLiteModelHelper.loadAssetLines(
                        this@TFLiteImageClassificationActivity,
                        labelsFileName,
                    ).dropLastWhile { it.isEmpty() }
                }
                check(labels.size == LABEL_COUNT) {
                    "标签表行数异常：期望 $LABEL_COUNT，实得 ${labels.size}"
                }
                rebuildModel()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (canTouchUi) {
                    binding.tvTop5Results.text = "初始化模型失败: ${e.message}"
                }
            }
        }
    }

    private fun rebuildModel() {
        // 上一次重建可能还停在模型构建 / 模型下载上：先取消，避免两次重建并发改写 model 字段。
        // 在途推理也要取消 —— 它的结果对应旧模型的语义，继续跑完只会把过期 Top-5 写回界面。
        rebuildJob?.cancel()
        inferenceJob?.cancel()
        rebuildJob = lifecycleScope.launch {
            val previous = model
            val target = selectedModel
            try {
                ensureModelReady(target)
                // 下载期间用户可能又切了模型：让位给后一次重建，别把旧目标装上
                if (selectedModel != target) return@launch

                // 构建过程不写字段：构建失败时旧引擎仍留在字段里，页面不会变成「引擎已置空」的半死状态；
                // 若构建完成后本协程已被取消，helper 会就地释放刚建好的引擎
                val engine = buildOffMainThreadSafely(engineDispatcher) { createEngine(target) }

                model = engine.model
                activeModel = target
                inputEdge = engine.inputEdge
                if (canTouchUi) {
                    binding.tvEngineInfo.text = engine.description
                }

                // 旧引擎的关闭排进同一条串行队列，确保不会与正在执行的 run() 并发；
                // 同时用 NonCancellable 兜住，避免被取消时漏关
                if (previous != null) {
                    withContext(NonCancellable + engineDispatcher) {
                        previous.close()
                    }
                }

                if (selectedModel != target) return@launch
                // 重推理当前图片（无图时保持界面不变）
                currentBitmap?.let { runClassification(it) }
            } catch (e: CancellationException) {
                // 被后一次重建取代属于正常流程：原样抛出，既不吞掉取消，也不误报「切换失败」
                throw e
            } catch (e: Exception) {
                if (canTouchUi) {
                    binding.tvEngineInfo.text = "加载 ${target.displayName} 失败: ${e.message}"
                    // 引擎仍是上一个模型：把选择器拨回去，别让界面显示的和实际跑的不一致
                    activeModel?.let(::checkModelRadio)
                }
            }
        }
    }

    /**
     * 确保目标模型的本地文件已就绪（Lite2 首次选中时下载）
     *
     * 只有 Lite2 需要下载；Lite0 随 APK 分发。[ModelStore.pendingBytes] 以「文件存在且非空」
     * 判定就绪，而下载器只在完整写完后 rename，因此不会把半截文件当成可用模型。
     * 下载回调在 IO 线程，进度更新切回主线程。
     */
    private suspend fun ensureModelReady(target: ImageModel) {
        val remote = target.remote ?: return
        // 判存在性要走文件系统：本页在 lifecycleScope（Main）里调用，必须切到 IO，
        // 否则 ModelStore 的 isFile / length 会落在主线程上
        val pending = withContext(Dispatchers.IO) {
            ModelStore.pendingBytes(this@TFLiteImageClassificationActivity, listOf(remote))
        }
        if (pending <= 0L) return

        // 本函数就运行在 rebuildJob 里，用它做「本轮是否仍是最新」的判据：
        // 旧的下载协程被取消后，它的 finally / 进度回调不能再去复位新轮刚设上的 UI。
        val selfJob = coroutineContext[Job]

        if (canTouchUi) {
            binding.progressDownload.visibility = View.VISIBLE
            binding.tvEngineInfo.text = "首次使用，需下载 ${target.displayName} 约 ${formatMb(pending)}…"
        }

        try {
            downloader.ensureAll(listOf(remote)) { _, _, _, overall, overallTotal ->
                runOnUiThread {
                    if (!canTouchUi || selfJob !== rebuildJob) return@runOnUiThread
                    val percent = if (overallTotal > 0L) {
                        (overall * 100 / overallTotal).toInt().coerceIn(0, 100)
                    } else {
                        0
                    }
                    binding.progressDownload.progress = percent
                    binding.tvEngineInfo.text =
                        "下载 ${target.displayName} · $percent% " +
                        "(${formatMb(overall)} / ${formatMb(overallTotal)})"
                }
            }
        } finally {
            if (canTouchUi && selfJob === rebuildJob) {
                binding.progressDownload.visibility = View.GONE
            }
        }
    }

    /** 把选择器拨到指定模型（屏蔽回调，避免回滚动作再触发一次重建） */
    private fun checkModelRadio(target: ImageModel) {
        suppressModelCallback = true
        binding.rgModel.check(if (target == ImageModel.LITE2) R.id.rbLite2 else R.id.rbLite0)
        suppressModelCallback = false
        selectedModel = target
    }

    private fun formatMb(bytes: Long): String = "%.1f MB".format(bytes / 1024.0 / 1024.0)

    /**
     * 按「当前模型 + 当前加速开关」构建引擎，须在 [engineDispatcher] 上调用
     *
     * GPU 后端构建失败（设备不支持 / 驱动异常）时用**同一个模型**回退 CPU，并把回退原因
     * 如实写进描述，避免出现「开关打开了、实际仍跑在 CPU 上」而界面看不出来的情况。
     */
    private fun createEngine(target: ImageModel): EngineBundle {
        if (!useGpu) {
            return openEngine(target, TFLiteModelHelper.createCpuOptions())
        }

        return try {
            openEngine(target, TFLiteModelHelper.createGpuOptions())
        } catch (e: Exception) {
            openEngine(target, TFLiteModelHelper.createCpuOptions(), fallbackReason = e.message)
        }
    }

    /**
     * 用指定配置打开模型，并读出这次引擎的输入边长
     *
     * [fallbackReason] 非空表示 GPU 构建失败后回退 CPU，用于在描述里保留失败原因。
     */
    private fun openEngine(
        target: ImageModel,
        options: CompiledModel.Options,
        fallbackReason: String? = null,
    ): EngineBundle {
        val engine = if (target.remote == null) {
            TFLiteModelHelper.createModel(assets, target.fileName, options)
        } else {
            TFLiteModelHelper.createModel(ModelStore.pathIn(filesDir, target.fileName), options)
        }
        val edge = TFLiteModelHelper.readInputEdge(engine, target.defaultInputEdge)
        val backend = when {
            fallbackReason != null -> "GPU 后端不可用，已回退 CPU ($fallbackReason)"
            useGpu -> "GPU 硬件加速 (Accelerator.GPU)"
            else -> "CPU (${TFLiteModelHelper.CPU_THREADS} 线程 + XNNPACK)"
        }
        return EngineBundle(
            model = engine,
            description = "推理引擎: $backend · ${target.displayName} · 输入 $edge×$edge",
            inputEdge = edge,
        )
    }

    /**
     * 一次引擎构建的产物：CompiledModel + 引擎描述 + 该引擎的输入边长
     *
     * 实现 [AutoCloseable] 是为了能交给 `buildOffMainThreadSafely` —— 构建完成后
     * 若调用方协程已被取消（例如拨动开关触发了下一次重建），helper 会就地关闭它，
     * 避免刚建好的 MB 级原生引擎无人持有而只靠 GC 兜底。
     */
    private class EngineBundle(
        val model: CompiledModel,
        val description: String,
        val inputEdge: Int,
    ) : AutoCloseable {

        override fun close() {
            model.close()
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.btnPickImage -> pickImageLauncher.launch("image/*")
        }
    }

    private fun loadBitmapFromUri(uri: Uri) {
        lifecycleScope.launch {
            val bitmap = withContext(Dispatchers.IO) {
                try {
                    contentResolver.openInputStream(uri)?.use {
                        BitmapFactory.decodeStream(it)
                    }
                } catch (e: Exception) {
                    null
                }
            }
            if (bitmap != null) {
                swapPreview(bitmap)
                runClassification(bitmap)
            }
        }
    }

    /**
     * 换图统一按「赋新值 → 交给 View → 回收旧值」处理
     *
     * 先换图再回收上一张：顺序反了会让 ImageView 绘制到已回收位图，快速连续换图时就会偶现崩溃。
     * 旧位图可能仍被后台预处理的 `getPixels()` 持用，回收必须排进引擎队列。
     */
    private fun swapPreview(bitmap: Bitmap) {
        val previous = currentBitmap
        currentBitmap = bitmap
        binding.ivPreview.setImageBitmap(bitmap)
        if (previous != null && previous !== bitmap) {
            runOnEngineQueue(engineDispatcher) { previous.recycle() }
        }
    }

    private fun runClassification(bitmap: Bitmap) {
        // 换图 / 切模型都会发起新推理：旧任务的结果已无意义，先取消再排队
        inferenceJob?.cancel()
        // 输入边长与模型在发起时快照：重建流程可能在本任务排队期间改写字段
        val edge = inputEdge
        val modelAtStart = selectedModel
        inferenceJob = lifecycleScope.launch {
            try {
                // 引擎字段在串行队列内才读取：保证拿到的是此刻仍有效的实例，
                // 而不是「取出后被重建流程关掉」的那个
                val result = withContext(engineDispatcher) {
                    val active = model ?: return@withContext null

                    var inputArray: FloatArray
                    val preprocessTime = measureNanoTime {
                        // EfficientNet-Lite 输入 N×N×3 FP32，归一化到 [-1, 1]（整图拉伸）
                        inputArray = TFLiteModelHelper.bitmapToFloatArray(
                            bitmap = bitmap,
                            targetWidth = edge,
                            targetHeight = edge,
                            normalize = InputNormalize.FLOAT_MINUS1_1,
                        )
                    }

                    // 输入/输出缓冲区都在 try 内创建：任一步失败，已建成的部分也能被 finally 关闭
                    var inputBuffers: List<TensorBuffer>? = null
                    var outputBuffers: List<TensorBuffer>? = null
                    try {
                        inputBuffers = active.createInputBuffers()
                        outputBuffers = active.createOutputBuffers()
                        // 上两步任一失败都不会走到这里，因此此处引用必然非空
                        val inputs = inputBuffers
                        val outputs = outputBuffers
                        inputs[0].writeFloat(inputArray)

                        var probabilities: FloatArray
                        val inferenceTime = measureNanoTime {
                            active.run(inputs, outputs)
                            probabilities = outputs[0].readFloat()
                        }

                        // 输出 1000 类与标签表 1000 行严格一一对应：索引 i 就是 labels[i]，
                        // 既没有背景类需要置零，也不需要做 -1 偏移（实测模型输出即 [1, 1000]）
                        val topK = TFLiteModelHelper.getTopK(probabilities, k = 5)
                        Triple(topK, preprocessTime / 1_000_000.0, inferenceTime / 1_000_000.0)
                    } finally {
                        outputBuffers?.forEach { runCatching { it.close() } }
                        inputBuffers?.forEach { runCatching { it.close() } }
                    }
                } ?: return@launch

                if (!canTouchUi) return@launch
                // 用户可能已在等待推理期间切到另一张图：结果必须对应发起时的位图，否则 Top-5 与预览不对应
                if (currentBitmap !== bitmap) return@launch
                // 也可能期间切了模型：新引擎会重新推理同一张图，旧结果直接丢弃
                if (selectedModel != modelAtStart) return@launch
                val (topKList, preprocessMs, inferenceMs) = result

                binding.tvTimeDetails.text =
                    "预处理耗时: ${"%.2f".format(preprocessMs)} ms | 模型推理耗时: ${"%.2f".format(inferenceMs)} ms"

                val sb = StringBuilder()
                topKList.forEachIndexed { rank, (index, prob) ->
                    // 输出下标与标签表下标一一对应（标签表不含背景类，模型也没有背景类）
                    val label = labels.getOrElse(index) { "类别 #$index" }
                    val percent = "%.2f".format(prob * 100).padStart(5, ' ')
                    sb.append("Top ${rank + 1}:  $percent%  ──  $label\n")
                }
                binding.tvTop5Results.text = sb.toString().trimEnd()
            } catch (e: CancellationException) {
                // 换图 / 切模型会主动取消上一轮推理：取消不是失败，不能把结果区
                // 改写成「图像识别异常」，否则刚显示的 Top-5 会被一行报错顶掉
                throw e
            } catch (e: Exception) {
                if (canTouchUi) {
                    binding.tvTop5Results.text = "图像识别异常: ${e.message}"
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 页面销毁时中断下载，保留 .part 供下次续传
        downloader.cancel()
        // 关闭动作排进与推理同一条串行队列，避免 close 掉正在执行 run() 的原生实例；
        // 此刻 lifecycleScope 已被取消，故由 runOnEngineQueue 另起独立作用域派发
        val closingModel = model
        val closingBitmap = currentBitmap
        model = null
        activeModel = null
        currentBitmap = null
        runOnEngineQueue(engineDispatcher) {
            closingModel?.close()
            closingBitmap?.recycle()
        }
    }

    private companion object {
        /** ImageNet 1000 类；标签表必须正好这么多行，否则下标与输出对不上 */
        const val LABEL_COUNT = 1000
    }
}
