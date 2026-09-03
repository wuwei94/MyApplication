package com.example.william.my.module.ml.gpu

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.ui.activity.BaseVBActivity
import com.example.william.my.module.ml.databinding.MlActivityGpuBenchmarkBinding
import com.example.william.my.module.ml.helper.TFLiteModelHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.gpu.GpuDelegate
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.system.measureNanoTime

/**
 * GPU 硬件加速与多核 / XNNPACK Benchmark 性能实测
 *
 * 官方文档: https://www.tensorflow.org/lite/performance/gpu
 *
 * 核心技术对照矩阵：
 * 1. 单核 1T (Baseline): 1 线程，禁用 XNNPACK
 * 2. 单核 + XNN: 1 线程，启用 XNNPACK ARM NEON 汇编指令集优化
 * 3. 多核 4T: 4 线程纯并发，禁用 XNNPACK
 * 4. 多核 + XNN: 4 线程并发 + 启用 XNNPACK 指令集优化
 * 5. GPU Delegate: 手机显卡 OpenCL / Vulkan 硬件着色器并行
 */
@Route(path = RouterPath.Ml.GpuDelegate)
class TFLiteGpuDelegateActivity :
    BaseVBActivity<MlActivityGpuBenchmarkBinding>(), View.OnClickListener {

    private val modelFileName = "mobilenet_v1_1.0_224_quant.tflite"

    private var cpuSingleAvg = 0.0
    private var cpuSingleXnnAvg = 0.0
    private var cpuMultiAvg = 0.0
    private var cpuMultiXnnAvg = 0.0
    private var gpuAvg = 0.0

    override fun getViewBinding(): MlActivityGpuBenchmarkBinding {
        return MlActivityGpuBenchmarkBinding.inflate(layoutInflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        mBinding.btnCpuSingle.setOnClickListener(this)
        mBinding.btnCpuSingleXnn.setOnClickListener(this)
        mBinding.btnCpuMulti.setOnClickListener(this)
        mBinding.btnCpuMultiXnn.setOnClickListener(this)
        mBinding.btnGpu.setOnClickListener(this)
        mBinding.btnRunAll.setOnClickListener(this)

        checkGpuCompatibility()
    }

    private fun checkGpuCompatibility() {
        val compatList = CompatibilityList()
        val isSupported = compatList.isDelegateSupportedOnThisDevice
        val bestOptions = compatList.bestOptionsForThisDevice

        val info = StringBuilder().apply {
            append("• 设备型号: ${Build.MANUFACTURER} ${Build.MODEL} (API ${Build.VERSION.SDK_INT})\n")
            append("• GPU Delegate 兼容状态: ${if (isSupported) "✓ 支持硬件加速 (Recommended)" else "✗ 当前设备不推荐或不支持"}\n")
            if (isSupported) {
                append("• 推荐配置: 精度模式 FP16=${bestOptions.isPrecisionLossAllowed}")
            }
        }
        mBinding.tvGpuCompatInfo.text = info.toString()
    }

    override fun onClick(v: View?) {
        when (v) {
            mBinding.btnCpuSingle -> runBenchmark(Mode.CPU_SINGLE)
            mBinding.btnCpuSingleXnn -> runBenchmark(Mode.CPU_SINGLE_XNN)
            mBinding.btnCpuMulti -> runBenchmark(Mode.CPU_MULTI)
            mBinding.btnCpuMultiXnn -> runBenchmark(Mode.CPU_MULTI_XNN)
            mBinding.btnGpu -> runBenchmark(Mode.GPU_DELEGATE)
            mBinding.btnRunAll -> runAllBenchmarks()
        }
    }

    private enum class Mode {
        CPU_SINGLE,
        CPU_SINGLE_XNN,
        CPU_MULTI,
        CPU_MULTI_XNN,
        GPU_DELEGATE
    }

    private fun runBenchmark(mode: Mode) {
        setButtonsEnabled(false)
        mBinding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            val title = when (mode) {
                Mode.CPU_SINGLE -> "CPU 单核 (1T, 无 XNN)"
                Mode.CPU_SINGLE_XNN -> "CPU 单核 + XNN (1T + NEON 指令加速)"
                Mode.CPU_MULTI -> "CPU 多核 (4T 并发, 无 XNN)"
                Mode.CPU_MULTI_XNN -> "CPU 多核 + XNN (4T + NEON 指令加速)"
                Mode.GPU_DELEGATE -> "GPU Delegate 显卡硬件加速"
            }

            appendLog("\n── 正在执行 $title 真实 Benchmark (30 轮) ──")

            val result = withContext(Dispatchers.Default) {
                executeRealInference(mode, iterations = 30)
            }

            if (result != null) {
                val (warmUp, steadyAvg, p95, fps) = result
                when (mode) {
                    Mode.CPU_SINGLE -> cpuSingleAvg = steadyAvg
                    Mode.CPU_SINGLE_XNN -> cpuSingleXnnAvg = steadyAvg
                    Mode.CPU_MULTI -> cpuMultiAvg = steadyAvg
                    Mode.CPU_MULTI_XNN -> cpuMultiXnnAvg = steadyAvg
                    Mode.GPU_DELEGATE -> gpuAvg = steadyAvg
                }

                appendLog("✓ $title 实测结果:")
                appendLog("  • 首帧 Warm-up: ${"%.2f".format(warmUp)} ms")
                appendLog("  • 稳态平均耗时: ${"%.2f".format(steadyAvg)} ms / 样本")
                appendLog("  • P95 延迟: ${"%.2f".format(p95)} ms")
                appendLog("  • 吞吐量 (FPS): ${"%.1f".format(fps)} 次/秒")

                if (cpuSingleAvg > 0 && mode != Mode.CPU_SINGLE) {
                    val speedup = cpuSingleAvg / steadyAvg
                    appendLog("  ⚡ 相对单核基线提速: ${"%.2f".format(speedup)}x")
                }
            } else {
                appendLog("✗ $title 执行失败，请检查设备驱动或日志。")
            }

            mBinding.progressBar.visibility = View.GONE
            setButtonsEnabled(true)
        }
    }

    private fun runAllBenchmarks() {
        setButtonsEnabled(false)
        mBinding.progressBar.visibility = View.VISIBLE
        mBinding.tvBenchmarkLogs.text = "════════ 启动全量真机性能 Benchmark ════════"

        lifecycleScope.launch {
            val modes = listOf(
                Mode.CPU_SINGLE,
                Mode.CPU_SINGLE_XNN,
                Mode.CPU_MULTI,
                Mode.CPU_MULTI_XNN,
                Mode.GPU_DELEGATE
            )

            for (mode in modes) {
                val title = when (mode) {
                    Mode.CPU_SINGLE -> "1. 单核 1T (无XNN)"
                    Mode.CPU_SINGLE_XNN -> "2. 单核+XNN (1T+NEON)"
                    Mode.CPU_MULTI -> "3. 多核 4T (无XNN)"
                    Mode.CPU_MULTI_XNN -> "4. 多核+XNN (4T+NEON)"
                    Mode.GPU_DELEGATE -> "5. GPU Delegate (FP16)"
                }

                appendLog("\n── 正在测试: $title ──")
                val result = withContext(Dispatchers.Default) {
                    executeRealInference(mode, iterations = 30)
                }

                if (result != null) {
                    val (warmUp, steadyAvg, _, fps) = result
                    when (mode) {
                        Mode.CPU_SINGLE -> cpuSingleAvg = steadyAvg
                        Mode.CPU_SINGLE_XNN -> cpuSingleXnnAvg = steadyAvg
                        Mode.CPU_MULTI -> cpuMultiAvg = steadyAvg
                        Mode.CPU_MULTI_XNN -> cpuMultiXnnAvg = steadyAvg
                        Mode.GPU_DELEGATE -> gpuAvg = steadyAvg
                    }
                    appendLog("• 首帧: ${"%.1f".format(warmUp)} ms | 稳态均值: ${"%.2f".format(steadyAvg)} ms | 吞吐: ${"%.1f".format(fps)} FPS")
                }
            }

            appendLog("\n════════ 科学对照矩阵与性能总结 ════════")
            if (cpuSingleAvg > 0 && cpuSingleXnnAvg > 0) {
                appendLog("① XNNPACK 纯指令集提速 (单核对比): ${(cpuSingleAvg / cpuSingleXnnAvg).let { "%.2fx".format(it) }}")
            }
            if (cpuSingleAvg > 0 && cpuMultiAvg > 0) {
                appendLog("② CPU 纯多核并发提速 (4T 对比): ${(cpuSingleAvg / cpuMultiAvg).let { "%.2fx".format(it) }}")
            }
            if (cpuSingleAvg > 0 && cpuMultiXnnAvg > 0) {
                appendLog("③ CPU 极限性能 (多核+XNN 组合拳): ${(cpuSingleAvg / cpuMultiXnnAvg).let { "%.2fx".format(it) }}")
            }
            if (cpuSingleAvg > 0 && gpuAvg > 0) {
                appendLog("④ GPU Delegate 硬件加速 (相较单核): ${(cpuSingleAvg / gpuAvg).let { "%.2fx".format(it) }} (相较满血CPU: ${(cpuMultiXnnAvg / gpuAvg).let { "%.2fx".format(it) }})")
            }
            appendLog("👉 选型建议: 连续相机流首选 GPU；低频任务首选 多核+XNN。")

            mBinding.progressBar.visibility = View.GONE
            setButtonsEnabled(true)
        }
    }

    private fun executeRealInference(mode: Mode, iterations: Int): BenchmarkStats? {
        var interpreter: Interpreter? = null
        var delegate: GpuDelegate? = null
        try {
            val modelBuffer = TFLiteModelHelper.loadModelFile(this, modelFileName)
            val options = Interpreter.Options()

            when (mode) {
                Mode.CPU_SINGLE -> {
                    options.setNumThreads(1)
                    options.setUseXNNPACK(false)
                }

                Mode.CPU_SINGLE_XNN -> {
                    options.setNumThreads(1)
                    options.setUseXNNPACK(true)
                }

                Mode.CPU_MULTI -> {
                    options.setNumThreads(4)
                    options.setUseXNNPACK(false)
                }

                Mode.CPU_MULTI_XNN -> {
                    options.setNumThreads(4)
                    options.setUseXNNPACK(true)
                }

                Mode.GPU_DELEGATE -> {
                    val compat = CompatibilityList()
                    val gpuOptions = compat.bestOptionsForThisDevice.apply {
                        setPrecisionLossAllowed(true)
                    }
                    delegate = GpuDelegate(gpuOptions)
                    options.addDelegate(delegate)
                }
            }

            interpreter = Interpreter(modelBuffer, options)

            // 构建真实 224x224x3 UINT8 输入张量
            val inputBuffer = ByteBuffer.allocateDirect(224 * 224 * 3).apply {
                order(ByteOrder.nativeOrder())
                for (i in 0 until (224 * 224 * 3)) {
                    put((i % 255).toByte())
                }
                rewind()
            }

            // 输出张量 [1, 1001]
            val outputBuffer = ByteBuffer.allocateDirect(1001).apply {
                order(ByteOrder.nativeOrder())
                rewind()
            }

            val latencies = mutableListOf<Double>()
            for (i in 1..iterations) {
                inputBuffer.rewind()
                outputBuffer.rewind()
                val elapsed = measureNanoTime {
                    interpreter.run(inputBuffer, outputBuffer)
                }
                latencies.add(elapsed / 1_000_000.0)
            }

            val warmUp = latencies.first()
            val steady = latencies.drop(1)
            val steadyAvg = steady.sum() / steady.size
            val sorted = steady.sorted()
            val p95Index = (sorted.size * 0.95).toInt().coerceAtMost(sorted.size - 1)
            val p95 = sorted[p95Index]
            val fps = 1000.0 / steadyAvg

            return BenchmarkStats(warmUp, steadyAvg, p95, fps)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            interpreter?.close()
            delegate?.close()
        }
    }

    private data class BenchmarkStats(
        val warmUpMs: Double,
        val steadyAvgMs: Double,
        val p95Ms: Double,
        val fps: Double
    )

    private fun appendLog(log: String) {
        val current = mBinding.tvBenchmarkLogs.text.toString()
        mBinding.tvBenchmarkLogs.text = "$current\n$log"
        mBinding.scrollView.post {
            mBinding.scrollView.fullScroll(View.FOCUS_DOWN)
        }
    }

    private fun setButtonsEnabled(enabled: Boolean) {
        mBinding.btnCpuSingle.isEnabled = enabled
        mBinding.btnCpuSingleXnn.isEnabled = enabled
        mBinding.btnCpuMulti.isEnabled = enabled
        mBinding.btnCpuMultiXnn.isEnabled = enabled
        mBinding.btnGpu.isEnabled = enabled
        mBinding.btnRunAll.isEnabled = enabled
    }
}
