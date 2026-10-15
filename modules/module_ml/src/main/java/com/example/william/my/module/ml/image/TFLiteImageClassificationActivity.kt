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
import com.example.william.my.module.ml.databinding.MlActivityImageClassificationBinding
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
import kotlin.system.measureNanoTime

/**
 * LiteRT — EfficientNet-Lite 图像分类与物体识别
 *
 * 演示从模型加载、图像预处理到 Top-5 分类结果展示的完整流程，
 * 支持 CPU 与 GPU 硬件加速切换。推理侧走 LiteRT 2.x 的 CompiledModel API。
 *
 * 核心机制与避坑点：
 * 1. 从 Assets 加载 efficientnet_lite0.tflite，标签表取自模型内嵌 metadata
 *    （`labels_without_background.txt`），不再依赖独立 asset
 * 2. CPU / GPU 切换在 2.x 里是**引擎编译期**选项：`Accelerator.CPU` 与 `Accelerator.GPU`
 *    分别在构建 CompiledModel 时选定，切换后端即重建引擎
 * 3. 模型输出为 FP32 概率 `[1, 1000]`，**没有背景类**，与内嵌标签表的 1000 行严格一一对应
 *    （官方样例同样是 `labels.zip(output)` 直接下标），**不需要反量化也不需要 Softmax**
 * 4. 整图拉伸重采样到 224×224，与官方样例的 `createScaledBitmap` 一致
 *
 * 官方样例：https://github.com/google-ai-edge/litert-samples/tree/main/samples/litert/image_classification
 */
@Route(path = RouterPath.Ml.ImageClassification)
class TFLiteImageClassificationActivity :
    BaseVBActivity<MlActivityImageClassificationBinding>(),
    View.OnClickListener {

    @Volatile
    private var model: CompiledModel? = null

    private var labels: List<String> = emptyList()

    private val modelFileName = "efficientnet_lite0.tflite"
    private val labelsFileName = "labels_without_background.txt"

    /** 上一次未完成的重建句柄：拨动开关时先取消，避免两次重建并发改写引擎字段 */
    private var rebuildJob: Job? = null

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

        // 异步有序初始化：加载标签 -> 构建引擎
        initModelAndLabels()
    }

    private fun initModelAndLabels() {
        lifecycleScope.launch {
            try {
                labels = withContext(Dispatchers.IO) {
                    TFLiteModelHelper.loadMetadataLines(
                        this@TFLiteImageClassificationActivity,
                        modelFileName,
                        labelsFileName,
                    )
                }
                rebuildModel()
            } catch (e: Exception) {
                if (canTouchUi) {
                    binding.tvTop5Results.text = "初始化模型失败: ${e.message}"
                }
            }
        }
    }

    private fun rebuildModel() {
        // 上一次重建可能还停在模型构建上：先取消，避免两次重建并发改写 model 字段
        rebuildJob?.cancel()
        rebuildJob = lifecycleScope.launch {
            val previous = model
            try {
                // 构建过程不写字段：构建失败时旧引擎仍留在字段里，页面不会变成「引擎已置空」的半死状态；
                // 若构建完成后本协程已被取消，helper 会就地释放刚建好的引擎
                val engine = buildOffMainThreadSafely(engineDispatcher) { createEngine() }

                model = engine.model
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

                // 重推理当前图片（无图时保持界面不变）
                currentBitmap?.let { runClassification(it) }
            } catch (e: CancellationException) {
                // 被后一次重建取代属于正常流程：原样抛出，既不吞掉取消，也不误报「切换失败」
                throw e
            } catch (e: Exception) {
                if (canTouchUi) {
                    binding.tvEngineInfo.text = "切换加速引擎失败: ${e.message}"
                }
            }
        }
    }

    /**
     * 按当前加速开关构建引擎，须在 [engineDispatcher] 上调用
     *
     * GPU 后端构建失败（设备不支持 / 驱动异常）时回退 CPU，并把回退原因如实写进描述，
     * 避免出现「开关打开了、实际仍跑在 CPU 上」而界面看不出来的情况。
     */
    private fun createEngine(): EngineBundle {
        if (!useGpu) {
            return EngineBundle(
                model = TFLiteModelHelper.createModel(
                    assets,
                    modelFileName,
                    TFLiteModelHelper.createCpuOptions(),
                ),
                description = "推理引擎: CPU (${TFLiteModelHelper.CPU_THREADS} 线程 + XNNPACK)",
            )
        }

        return try {
            EngineBundle(
                model = TFLiteModelHelper.createModel(
                    assets,
                    modelFileName,
                    TFLiteModelHelper.createGpuOptions(),
                ),
                description = "推理引擎: GPU 硬件加速 (Accelerator.GPU)",
            )
        } catch (e: Exception) {
            EngineBundle(
                model = TFLiteModelHelper.createModel(
                    assets,
                    modelFileName,
                    TFLiteModelHelper.createCpuOptions(),
                ),
                description = "GPU 后端不可用，已回退 CPU: ${e.message}",
            )
        }
    }

    /**
     * 一次引擎构建的产物：CompiledModel + 引擎描述
     *
     * 实现 [AutoCloseable] 是为了能交给 `buildOffMainThreadSafely` —— 构建完成后
     * 若调用方协程已被取消（例如拨动开关触发了下一次重建），helper 会就地关闭它，
     * 避免刚建好的 MB 级原生引擎无人持有而只靠 GC 兜底。
     */
    private class EngineBundle(
        val model: CompiledModel,
        val description: String,
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
        lifecycleScope.launch {
            try {
                // 引擎字段在串行队列内才读取：保证拿到的是此刻仍有效的实例，
                // 而不是「取出后被重建流程关掉」的那个
                val result = withContext(engineDispatcher) {
                    val active = model ?: return@withContext null

                    var inputArray: FloatArray
                    val preprocessTime = measureNanoTime {
                        // EfficientNet-Lite 输入 224x224x3 FP32，归一化到 [-1, 1]（整图拉伸）
                        inputArray = TFLiteModelHelper.bitmapToFloatArray(
                            bitmap = bitmap,
                            targetWidth = INPUT_SIZE,
                            targetHeight = INPUT_SIZE,
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
            } catch (e: Exception) {
                if (canTouchUi) {
                    binding.tvTop5Results.text = "图像识别异常: ${e.message}"
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 关闭动作排进与推理同一条串行队列，避免 close 掉正在执行 run() 的原生实例；
        // 此刻 lifecycleScope 已被取消，故由 runOnEngineQueue 另起独立作用域派发
        val closingModel = model
        val closingBitmap = currentBitmap
        model = null
        currentBitmap = null
        runOnEngineQueue(engineDispatcher) {
            closingModel?.close()
            closingBitmap?.recycle()
        }
    }

    private companion object {
        /** EfficientNet-Lite 输入边长 */
        const val INPUT_SIZE = 224
    }
}
