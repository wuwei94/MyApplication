package com.example.william.my.module.ml.segmentation

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.ui.activity.BaseVBActivity
import com.example.william.my.module.ml.databinding.MlActivityImageSegmentationBinding
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
 * LiteRT — 人像多类语义分割（MediaPipe Selfie Multiclass）
 *
 * 端侧视觉谱系里「逐像素理解」的一环：模型对输入图逐像素输出 6 类得分
 * （背景 / 头发 / 身体皮肤 / 面部皮肤 / 衣物 / 其他），取 argmax 上色后半透明叠加回原图。
 * 推理侧走 LiteRT 2.x 的 CompiledModel API。
 *
 * 核心机制与避坑点：
 * 1. 输入 [1, 256, 256, 3] FP32，归一化区间为 [-1, 1]（(v - 127.5) / 127.5）；
 *    区间必须实测确认 —— 不做归一化直接喂 0~255 会让整图塌成背景
 * 2. 输出 [1, 256, 256, 6] 为**平坦** FloatArray，通道交错排列，
 *    解码时按 `像素序号 * 类别数 + 类别` 取通道做 argmax
 * 3. 该输出实测是**未归一化的 logits**（末算子 `CONCATENATION`，每像素 6 通道之和 ≈ -2.3），
 *    页面只做 argmax —— argmax 在 Softmax 前后不变，因此不需要也不能再叠加 Softmax
 * 4. 分割结果的坐标系相对整幅输入图，因此预处理走整图拉伸，掩码才能与底图严格对齐
 * 5. 256×256 的低分辨率掩码由预览 View 拉伸回原图显示矩形完成叠加
 * 6. 该模型的类别集合是**人像专属**的：非人像场景几乎全部判为背景/其他
 * 7. CPU / GPU 是**编译期**选项（`Accelerator`），拨动开关即重建引擎 ——
 *    与官方 sample 的加速器切换一致；GPU 构建失败时回退 CPU 并把原因写进界面
 *
 * 官方样例：https://github.com/google-ai-edge/litert-samples/tree/main/samples/litert/image_segmentation
 */
@Route(path = RouterPath.Ml.ImageSegmentation)
class TFLiteImageSegmentationActivity :
    BaseVBActivity<MlActivityImageSegmentationBinding>(),
    View.OnClickListener {

    /** 推理线程与主线程共享，必须 volatile（与 image / text 页保持一致） */
    @Volatile
    private var model: CompiledModel? = null

    /** 当前引擎描述，把实际生效的加速后端如实展示在状态行上 */
    @Volatile
    private var engineDescription = "初始化中"

    private var currentBitmap: Bitmap? = null
    private var maskBitmap: Bitmap? = null

    /**
     * 当前是否使用 GPU 后端
     *
     * 2.x 里加速后端是**编译期**选项（`CompiledModel.Options(Accelerator.GPU)`），
     * 因此拨动开关等价于**重建引擎**，而不是给已有引擎替换委托。
     */
    @Volatile
    private var useGpu = false

    /** 引擎重建任务：切换加速器时先取消上一次，避免两次构建并发改写 model 字段 */
    private var rebuildJob: Job? = null

    /**
     * 推理与引擎关闭共用的单并发队列
     *
     * CompiledModel 不是线程安全的：同一实例并发 `run()` 会踩坏内部状态，
     * 而关闭动作若与正在执行的 `run()` 并发，会释放掉它正在用的原生 handle。
     */
    private val engineDispatcher = createEngineDispatcher()

    private val modelFileName = "selfie_multiclass.tflite"

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        uri?.let { loadBitmapFromUri(it) }
    }

    override fun getViewBinding(): MlActivityImageSegmentationBinding = MlActivityImageSegmentationBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        binding.btnPickImage.setOnClickListener(this)

        binding.switchGpu.setOnCheckedChangeListener { _, isChecked ->
            useGpu = isChecked
            rebuildModel()
        }

        rebuildModel()
    }

    private fun rebuildModel() {
        // 上一次重建可能还停在模型构建上：先取消，避免两次构建并发改写 model 字段
        rebuildJob?.cancel()
        rebuildJob = lifecycleScope.launch {
            val previous = model
            try {
                // 构建过程不写字段：构建失败时旧引擎仍留在字段里，页面不会变成「引擎已置空」的半死状态；
                // 若构建完成后本协程已被取消，helper 会就地释放刚建好的引擎
                val engine = buildOffMainThreadSafely(engineDispatcher) { createEngine() }

                model = engine.model
                engineDescription = engine.description
                if (canTouchUi) {
                    binding.tvStatus.text =
                        "推理引擎: CompiledModel / $engineDescription | " +
                        "输入 $INPUT_SIZE×$INPUT_SIZE | 模型: $modelFileName"
                }

                // 旧引擎的关闭排进同一条串行队列，确保不会与正在执行的 run() 并发
                if (previous != null) {
                    withContext(NonCancellable + engineDispatcher) {
                        previous.close()
                    }
                }

                // 已有底图时用新后端重跑一次，避免掩码停留在旧后端的输出上
                if (canTouchUi) {
                    currentBitmap?.let { runSegmentation(it) }
                }
            } catch (e: CancellationException) {
                // 被后一次重建取代属于正常流程：原样抛出，既不吞掉取消，也不误报「切换失败」
                throw e
            } catch (e: Exception) {
                if (canTouchUi) {
                    binding.tvStatus.text = "初始化模型失败: ${e.message}"
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
                description = "CPU (${TFLiteModelHelper.CPU_THREADS} 线程 + XNNPACK)",
            )
        }

        return try {
            EngineBundle(
                model = TFLiteModelHelper.createModel(
                    assets,
                    modelFileName,
                    TFLiteModelHelper.createGpuOptions(),
                ),
                description = "GPU 硬件加速 (Accelerator.GPU)",
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
     * 若调用方协程已被取消（例如拨动开关触发了下一次重建），helper 会就地关闭它。
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
                    contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
                } catch (e: Exception) {
                    null
                }
            }
            if (bitmap != null) {
                swapPreview(bitmap)
                runSegmentation(bitmap)
            }
        }
    }

    /**
     * 换图统一按「赋新值 → 交给 View → 回收旧值」处理
     *
     * 先换图再回收上一张：顺序反了会让预览 View 绘制到已回收位图，快速连续换图时就会偶现崩溃。
     * 旧位图可能仍被后台预处理的 `getPixels()` 持用，回收必须排进引擎队列。
     */
    private fun swapPreview(bitmap: Bitmap) {
        val previous = currentBitmap
        val previousMask = maskBitmap
        currentBitmap = bitmap
        maskBitmap = null
        binding.mlPreview.setImage(bitmap)
        if (previous != null && previous !== bitmap) {
            runOnEngineQueue(engineDispatcher) { previous.recycle() }
        }
        // 换图后旧掩码既不显示、也不再与任何底图对应，在这里一并回收；
        // 否则它要一直留到下一次分割成功才被回收，连续换图会逐张堆积
        if (previousMask != null) {
            runOnEngineQueue(engineDispatcher) { previousMask.recycle() }
        }
    }

    private fun runSegmentation(bitmap: Bitmap) {
        lifecycleScope.launch {
            try {
                val result = withContext(engineDispatcher) {
                    val active = model ?: return@withContext null

                    var inputArray: FloatArray
                    val preprocessTime = measureNanoTime {
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

                        var logits: FloatArray
                        val inferenceTime = measureNanoTime {
                            active.run(inputs, outputs)
                            logits = outputs[0].readFloat()
                        }

                        var mask: IntArray
                        var counts: IntArray
                        var pixels: IntArray
                        val postprocessTime = measureNanoTime {
                            mask = TFLiteModelHelper.decodeSegmentationMask(
                                logits = logits,
                                width = INPUT_SIZE,
                                height = INPUT_SIZE,
                                classCount = CLASS_COUNT,
                            )
                            counts = TFLiteModelHelper.countSegmentationClasses(mask, CLASS_COUNT)
                            pixels = TFLiteModelHelper.colorizeSegmentationMask(mask, CLASS_PALETTE)
                        }

                        SegmentedResult(
                            mask = mask,
                            counts = counts,
                            pixels = pixels,
                            preprocessMs = preprocessTime / 1_000_000.0,
                            inferenceMs = inferenceTime / 1_000_000.0,
                            postprocessMs = postprocessTime / 1_000_000.0,
                        )
                    } finally {
                        outputBuffers?.forEach { runCatching { it.close() } }
                        inputBuffers?.forEach { runCatching { it.close() } }
                    }
                } ?: return@launch

                if (!canTouchUi) return@launch
                // 推理期间用户已切换到别的图：本次掩码与当前底图对不上，整体丢弃。
                // 若继续绘制，会把过期掩码叠到新图上；而且那张旧底图此刻可能已被回收。
                if (currentBitmap !== bitmap) return@launch

                // 掩码位图与底图同坐标系，由预览 View 拉伸到相同显示矩形完成叠加
                val newMask = Bitmap.createBitmap(
                    result.pixels,
                    INPUT_SIZE,
                    INPUT_SIZE,
                    Bitmap.Config.ARGB_8888,
                )
                // 与换图同理：先把新掩码交给预览 View，再回收上一张，避免绘制到已回收位图
                val previousMask = maskBitmap
                maskBitmap = newMask
                binding.mlPreview.setSegmentation(bitmap, newMask)
                if (previousMask != null && previousMask !== newMask) previousMask.recycle()

                binding.tvTimeDetails.text =
                    "预处理耗时: ${"%.2f".format(result.preprocessMs)} ms | " +
                    "模型推理耗时: ${"%.2f".format(result.inferenceMs)} ms | " +
                    "后处理耗时: ${"%.2f".format(result.postprocessMs)} ms"

                binding.tvSegments.text = buildLegend(result.counts, result.mask.size)
            } catch (e: Exception) {
                if (canTouchUi) {
                    binding.tvSegments.text = "语义分割异常: ${e.message}"
                }
            }
        }
    }

    /**
     * 拼装带色块的类别占比图例，颜色与叠加层调色板严格一致
     */
    private fun buildLegend(counts: IntArray, total: Int): CharSequence {
        val ranked = counts
            .mapIndexed { index, count -> index to count }
            .filter { it.first != BACKGROUND_INDEX && it.second > 0 }
            .sortedByDescending { it.second }

        if (ranked.isEmpty()) {
            return "未分割出背景以外的人像区域（该模型只覆盖人像类别）"
        }

        val builder = SpannableStringBuilder()
        ranked.take(TOP_CLASSES).forEach { (index, count) ->
            val square = "■ "
            val start = builder.length
            builder.append(square)
            builder.setSpan(
                ForegroundColorSpan(CLASS_PALETTE[index]),
                start,
                start + square.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
            )
            val name = CLASS_LABELS.getOrElse(index) { "类别 #$index" }
            builder.append("$name  ${"%.1f".format(count * 100f / total)}%\n")
        }
        return builder
    }

    override fun onDestroy() {
        super.onDestroy()
        // 引擎关闭与位图回收都排进推理同一条队列：此刻 lifecycleScope 已取消，
        // 但后台 run() 与预处理不可中断仍会跑完，直接 close/recycle 会踩到它们正在用的原生资源
        val closingModel = model
        val closingMask = maskBitmap
        val closingBitmap = currentBitmap
        model = null
        maskBitmap = null
        currentBitmap = null
        runOnEngineQueue(engineDispatcher) {
            closingModel?.close()
            closingMask?.recycle()
            closingBitmap?.recycle()
        }
    }

    /**
     * 分割推理中间结果，避免在协程内直接触碰 View
     */
    private data class SegmentedResult(
        val mask: IntArray,
        val counts: IntArray,
        val pixels: IntArray,
        val preprocessMs: Double,
        val inferenceMs: Double,
        val postprocessMs: Double,
    )

    private companion object {
        /** MediaPipe Selfie Multiclass 输入边长 */
        const val INPUT_SIZE = 256

        /** 模型输出类别数：背景 / 头发 / 身体皮肤 / 面部皮肤 / 衣物 / 其他 */
        const val CLASS_COUNT = 6

        const val TOP_CLASSES = 6
        const val BACKGROUND_INDEX = 0

        /** 模型固定类别名，索引与输出通道一一对应（模型未提供标签表 metadata） */
        val CLASS_LABELS = listOf("背景", "头发", "身体皮肤", "面部皮肤", "衣物", "其他配饰")

        /** 与类别一一对应的叠加调色板，背景全透明由解码函数处理 */
        val CLASS_PALETTE = intArrayOf(
            0xFF000000.toInt(), // 背景（解码时置透明）
            0xFF7B4B2A.toInt(), // 头发
            0xFFE8B58C.toInt(), // 身体皮肤
            0xFFF2C9A8.toInt(), // 面部皮肤
            0xFF3F6FB5.toInt(), // 衣物
            0xFF9B59B6.toInt(), // 其他配饰
        )
    }
}
