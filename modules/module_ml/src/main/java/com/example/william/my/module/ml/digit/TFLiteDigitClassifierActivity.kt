package com.example.william.my.module.ml.digit

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.ui.activity.BaseVBActivity
import com.example.william.my.module.ml.databinding.MlActivityDigitClassificationBinding
import com.example.william.my.module.ml.helper.InputNormalize
import com.example.william.my.module.ml.helper.TFLiteModelHelper
import com.example.william.my.module.ml.helper.buildOffMainThreadSafely
import com.example.william.my.module.ml.helper.canTouchUi
import com.example.william.my.module.ml.helper.closeEngineOn
import com.example.william.my.module.ml.helper.createEngineDispatcher
import com.example.william.my.module.ml.helper.runOnEngineQueue
import com.google.ai.edge.litert.CompiledModel
import com.google.ai.edge.litert.TensorBuffer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.system.measureNanoTime

/**
 * LiteRT — MNIST 手写数字实时识别
 *
 * 基于画板涂鸦输入，演示从自定义 View 笔迹捕获、图像预处理到端侧前向推理的完整链路，
 * 实时展示 0~9 置信度分布。推理侧走 LiteRT 2.x 的 CompiledModel API。
 *
 * 核心机制与避坑点：
 * 1. 初始化 CompiledModel 加载 digit_classifier.tflite（输入 [1, 28, 28, 3] FP32，取值 0~255 不归一化）
 * 2. 捕获 FingerDrawView 画布整图拉伸到 28×28，预处理与官方 sample 完全一致
 * 3. 输入以 FloatArray 形式直接写入 TensorBuffer
 * 4. 在引擎串行队列上执行前向推理；模型末尾算子即 SOFTMAX，**输出已是概率，不再二次归一化**
 * 5. 解析输出张量 [1, 10]，实时展示置信度最高的数字与 0~9 概率分布
 * 6. CPU / GPU 是**编译期**选项（`Accelerator`），拨动开关即重建引擎 ——
 *    与官方 sample 的加速器切换一致；GPU 构建失败时回退 CPU 并把原因写进界面
 *
 * 官方样例：https://github.com/google-ai-edge/litert-samples/tree/main/samples/litert/digit_classifier
 */
@Route(path = RouterPath.Ml.DigitClassifier)
class TFLiteDigitClassifierActivity :
    BaseVBActivity<MlActivityDigitClassificationBinding>(),
    View.OnClickListener {

    /** 推理线程与主线程共享，必须 volatile（与 image / text 页保持一致） */
    @Volatile
    private var model: CompiledModel? = null

    /** 当前引擎描述，把实际生效的加速后端如实展示在耗时行上 */
    @Volatile
    private var engineDescription = "初始化中"

    private val modelFileName = "digit_classifier.tflite"

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
     * 本页「抬笔即识别」且按钮可连点，最容易被连打触发并发推理；而 CompiledModel
     * 不是线程安全的，同一实例并发 `run()` 会踩坏内部状态，关闭动作与 `run()` 并发
     * 更会释放掉它正在用的原生 handle。
     */
    private val engineDispatcher = createEngineDispatcher()

    override fun getViewBinding(): MlActivityDigitClassificationBinding = MlActivityDigitClassificationBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        binding.btnClear.setOnClickListener(this)
        binding.btnRecognize.setOnClickListener(this)

        // 抬笔时自动触发识别
        binding.drawView.onStrokeFinishedListener = {
            runClassification()
        }

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
                    binding.tvPrediction.text = "识别结果: 准备就绪，请在画板上写字"
                }

                // 旧引擎的关闭排进同一条串行队列，确保不会与正在执行的 run() 并发
                if (previous != null) {
                    withContext(NonCancellable + engineDispatcher) {
                        previous.close()
                    }
                }

                // 画板上还有笔迹时用新后端重跑一次，避免结果停留在旧后端的输出上
                if (canTouchUi && !binding.drawView.isCanvasEmpty()) {
                    runClassification()
                }
            } catch (e: CancellationException) {
                // 被后一次重建取代属于正常流程：原样抛出，既不吞掉取消，也不误报「切换失败」
                throw e
            } catch (e: Exception) {
                if (canTouchUi) {
                    binding.tvPrediction.text = "初始化模型失败: ${e.message}"
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
            binding.btnClear -> {
                binding.drawView.clear()
                binding.tvPrediction.text = "识别结果: 画板已清空"
                binding.tvLatency.text = "推理耗时: - ms"
                binding.tvDistribution.text = "0: -\n1: -\n2: -\n3: -\n4: -\n5: -\n6: -\n7: -\n8: -\n9: -"
            }

            binding.btnRecognize -> {
                runClassification()
            }
        }
    }

    private fun runClassification() {
        if (binding.drawView.isCanvasEmpty()) {
            binding.tvPrediction.text = "识别结果: 画布为空，请先绘制数字"
            return
        }

        val bitmap = binding.drawView.exportScaledBitmap(DIGIT_SIZE) ?: run {
            binding.tvPrediction.text = "识别结果: 画板尚未就绪"
            return
        }
        val currentModel = model ?: run {
            bitmap.recycle()
            binding.tvPrediction.text = "识别失败: 推理引擎尚未就绪"
            return
        }

        lifecycleScope.launch {
            try {
                val (resultProbabilities, elapsedMs) = withContext(engineDispatcher) {
                    // 输入/输出缓冲区都在 try 内创建：任一步失败，已建成的部分也能被 finally 关闭
                    var inputBuffers: List<TensorBuffer>? = null
                    var outputBuffers: List<TensorBuffer>? = null
                    try {
                        inputBuffers = currentModel.createInputBuffers()
                        outputBuffers = currentModel.createOutputBuffers()
                        // 上两步任一失败都不会走到这里，因此此处引用必然非空
                        val inputs = inputBuffers
                        val outputs = outputBuffers
                        // 整图拉伸后的 RGB 值（0~255，不归一化）写入输入 TensorBuffer
                        inputs[0].writeFloat(
                            TFLiteModelHelper.bitmapToFloatArray(
                                bitmap,
                                DIGIT_SIZE,
                                DIGIT_SIZE,
                                InputNormalize.RAW_0_255,
                            ),
                        )

                        var probabilities: FloatArray
                        val time = measureNanoTime {
                            currentModel.run(inputs, outputs)
                            probabilities = outputs[0].readFloat()
                        }
                        // 实测该模型末尾算子就是 SOFTMAX，输出**已经是概率**，直接使用。
                        // 若再叠加一次 Softmax，会把 0.90 压到 ≈0.21，置信度与柱状图严重失真。
                        Pair(probabilities, time / 1_000_000.0)
                    } finally {
                        // TensorBuffer 持有原生内存，必须显式关闭；先输出后输入不敏感，逐个隔离容错
                        outputBuffers?.forEach { runCatching { it.close() } }
                        inputBuffers?.forEach { runCatching { it.close() } }
                    }
                }

                if (!canTouchUi) return@launch

                // 获取最高置信度
                val predictedDigit = resultProbabilities.indices.maxByOrNull { resultProbabilities[it] } ?: 0
                val confidence = resultProbabilities[predictedDigit] * 100

                binding.tvPrediction.text = "识别结果: $predictedDigit (置信度: ${"%.1f".format(confidence)}%)"
                binding.tvLatency.text = "推理耗时: ${"%.2f".format(elapsedMs)} ms (CompiledModel / $engineDescription)"

                // 构建 0~9 概率分布条形图效果
                val sb = StringBuilder()
                for (i in 0..9) {
                    val prob = resultProbabilities[i]
                    val percent = "%.1f".format(prob * 100).padStart(5, ' ')
                    val barLength = (prob * 15).toInt().coerceIn(0, 15)
                    val bar = "█".repeat(barLength).padEnd(15, '░')
                    val isTop = if (i == predictedDigit) " 👈 (Top)" else ""
                    sb.append("数字 $i: $bar $percent%$isTop\n")
                }
                binding.tvDistribution.text = sb.toString().trimEnd()
            } catch (e: Exception) {
                if (canTouchUi) {
                    binding.tvPrediction.text = "识别异常: ${e.message}"
                }
            } finally {
                // 一次性输入位图不管走哪条路径都要回收：协程可能在进入 withContext 之前就被
                // 取消（页面退出），那条路径根本走不到引擎队列；回收同样排进引擎队列，
                // 与后台预处理一视同仁
                runOnEngineQueue(engineDispatcher) { bitmap.recycle() }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 关闭排进与推理同一条队列：此刻 lifecycleScope 已取消，但后台 run() 不可中断仍会跑完，
        // 直接 close 会释放掉它正在使用的原生 handle
        val closingModel = model
        model = null
        closeEngineOn(engineDispatcher, closingModel)
    }

    private companion object {
        /** MNIST 模型输入边长 */
        const val DIGIT_SIZE = 28
    }
}
