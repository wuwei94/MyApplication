package com.example.william.my.module.ml.digit

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.activity.BaseVBActivity
import com.example.william.my.module.ml.databinding.MlActivityDigitClassificationBinding
import com.example.william.my.module.ml.helper.TFLiteModelHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.system.measureNanoTime

/**
 * MNIST 手写数字实时识别演示
 *
 * 官方文档: https://www.tensorflow.org/lite/examples/digit_classification/overview
 *
 * 核心技术流程：
 * 1. 初始化 TensorFlow Lite Interpreter 加载 mnist.tflite 模型 (28x28 FP32)
 * 2. 捕获 FingerDrawView 笔迹并等比缩放为 28x28 灰度 Bitmap
 * 3. 构造 Direct Memory 的 Native ByteBuffer 输入张量 [1, 28, 28, 1]
 * 4. 在后台协程执行前向推理与 Softmax 概率归一
 * 5. 解析输出张量 [1, 10]，实时展示置信度最高的数字与 0~9 概率分布
 */
@Route(path = RouterPath.Ml.DigitClassifier)
class TFLiteDigitClassifierActivity : BaseVBActivity<MlActivityDigitClassificationBinding>(),
    View.OnClickListener {

    private var interpreter: Interpreter? = null
    private val modelFileName = "mnist.tflite"

    override fun getViewBinding(): MlActivityDigitClassificationBinding {
        return MlActivityDigitClassificationBinding.inflate(layoutInflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        mBinding.btnClear.setOnClickListener(this)
        mBinding.btnRecognize.setOnClickListener(this)

        // 抬笔时自动触发识别
        mBinding.drawView.onStrokeFinishedListener = {
            runClassification()
        }

        initInterpreter()
    }

    private fun initInterpreter() {
        lifecycleScope.launch {
            try {
                val modelBuffer = withContext(Dispatchers.IO) {
                    TFLiteModelHelper.loadModelFile(this@TFLiteDigitClassifierActivity, modelFileName)
                }
                val options = Interpreter.Options().apply {
                    setNumThreads(2)
                    setUseXNNPACK(true)
                }
                interpreter = Interpreter(modelBuffer, options)
                mBinding.tvPrediction.text = "识别结果: 准备就绪，请在画板上写字"
            } catch (e: Exception) {
                mBinding.tvPrediction.text = "初始化模型失败: ${e.message}"
            }
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            mBinding.btnClear -> {
                mBinding.drawView.clear()
                mBinding.tvPrediction.text = "识别结果: 画板已清空"
                mBinding.tvLatency.text = "推理耗时: - ms"
                mBinding.tvDistribution.text = "0: -\n1: -\n2: -\n3: -\n4: -\n5: -\n6: -\n7: -\n8: -\n9: -"
            }

            mBinding.btnRecognize -> {
                runClassification()
            }
        }
    }

    private fun runClassification() {
        if (mBinding.drawView.isCanvasEmpty()) {
            mBinding.tvPrediction.text = "识别结果: 画布为空，请先绘制数字"
            return
        }

        val bitmap = mBinding.drawView.exportBitmap(28, 28) ?: return
        val currentInterpreter = interpreter ?: run {
            mBinding.tvPrediction.text = "识别失败: Interpreter 尚未就绪"
            return
        }

        lifecycleScope.launch {
            val (resultProbabilities, elapsedMs) = withContext(Dispatchers.Default) {
                val inputBuffer = TFLiteModelHelper.convertBitmapToDigitByteBuffer(bitmap, 28, 28)
                val outputBuffer = ByteBuffer.allocateDirect(10 * 4).apply {
                    order(ByteOrder.nativeOrder())
                    rewind()
                }

                val time = measureNanoTime {
                    currentInterpreter.run(inputBuffer, outputBuffer)
                }

                outputBuffer.rewind()
                val rawOutputs = FloatArray(10) { outputBuffer.float }
                val probabilities = TFLiteModelHelper.softmax(rawOutputs)
                Pair(probabilities, time / 1_000_000.0)
            }

            // 获取最高置信度
            val predictedDigit = resultProbabilities.indices.maxByOrNull { resultProbabilities[it] } ?: 0
            val confidence = resultProbabilities[predictedDigit] * 100

            mBinding.tvPrediction.text = "识别结果: 【 $predictedDigit 】 (置信度: ${"%.1f".format(confidence)}%)"
            mBinding.tvLatency.text = "推理耗时: ${"%.2f".format(elapsedMs)} ms (CPU + XNNPACK)"

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
            mBinding.tvDistribution.text = sb.toString().trimEnd()

            bitmap.recycle()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        interpreter?.close()
        interpreter = null
    }
}
