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
 * MobileNet 图像分类与物体识别实战
 *
 * 官方文档: https://www.tensorflow.org/lite/examples/image_classification/overview
 *
 * 核心技术流程：
 * 1. 从 Assets 加载 mobilenet_v1_1.0_224_quant.tflite 与 ImageNet labels.txt
 * 2. 支持 CPU (多线程+XNNPACK) 与 GPU Delegate 硬件加速无缝切换
 * 3. 支持预置 4:3 实物样本与系统相册自选图片输入
 * 4. Center-Crop 居中等比裁剪、224x224 RGB 像素 Direct ByteBuffer 封装
 * 5. 解析输出张量，计算 Softmax 概率并进行 Top-5 字符等宽排序展示
 */
@Route(path = RouterPath.Ml.ImageClassification)
class TFLiteImageClassificationActivity :
    BaseVBActivity<MlActivityImageClassificationBinding>(), View.OnClickListener {

    private var interpreter: Interpreter? = null
    private var gpuDelegate: GpuDelegate? = null
    private var labels: List<String> = emptyList()

    private val modelFileName = "mobilenet_v1_1.0_224_quant.tflite"
    private val labelsFileName = "labels.txt"

    private var currentBitmap: Bitmap? = null
    private var useGpu = false

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { loadBitmapFromUri(it) }
    }

    override fun getViewBinding(): MlActivityImageClassificationBinding {
        return MlActivityImageClassificationBinding.inflate(layoutInflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        mBinding.btnSample1.setOnClickListener(this)
        mBinding.btnSample2.setOnClickListener(this)
        mBinding.btnSample3.setOnClickListener(this)
        mBinding.btnPickImage.setOnClickListener(this)

        mBinding.switchGpu.setOnCheckedChangeListener { _, isChecked ->
            useGpu = isChecked
            rebuildInterpreter()
        }

        // 异步有序初始化：加载模型与标签 -> 就绪后加载默认样本
        initModelAndLabels()
    }

    private fun initModelAndLabels() {
        lifecycleScope.launch {
            try {
                labels = withContext(Dispatchers.IO) {
                    TFLiteModelHelper.loadLabels(
                        this@TFLiteImageClassificationActivity,
                        labelsFileName
                    )
                }
                rebuildInterpreter {
                    // 模型初始化就绪后，首次加载样本 1 (金毛犬)
                    loadSampleImage(1)
                }
            } catch (e: Exception) {
                mBinding.tvTop5Results.text = "初始化模型失败: ${e.message}"
            }
        }
    }

    private fun rebuildInterpreter(onReady: (() -> Unit)? = null) {
        lifecycleScope.launch {
            try {
                interpreter?.close()
                interpreter = null
                gpuDelegate?.close()
                gpuDelegate = null

                val modelBuffer = withContext(Dispatchers.IO) {
                    TFLiteModelHelper.loadModelFile(
                        this@TFLiteImageClassificationActivity,
                        modelFileName
                    )
                }

                val options = Interpreter.Options()
                if (useGpu) {
                    try {
                        val compat = CompatibilityList()
                        val gpuOptions = compat.bestOptionsForThisDevice.apply {
                            setPrecisionLossAllowed(true)
                        }
                        val delegate = GpuDelegate(gpuOptions)
                        options.addDelegate(delegate)
                        gpuDelegate = delegate
                        mBinding.tvEngineInfo.text = "推理引擎: GPU Delegate 硬件加速 (FP16)"
                    } catch (e: Exception) {
                        options.setNumThreads(4)
                        options.setUseXNNPACK(true)
                        mBinding.tvEngineInfo.text = "GPU 创建失败，回退到 CPU: ${e.message}"
                    }
                } else {
                    options.setNumThreads(4)
                    options.setUseXNNPACK(true)
                    mBinding.tvEngineInfo.text = "推理引擎: CPU (4 线程 + XNNPACK)"
                }

                interpreter = Interpreter(modelBuffer, options)

                if (onReady != null) {
                    onReady()
                } else {
                    // 重新推理当前图片
                    currentBitmap?.let { runClassification(it) }
                }
            } catch (e: Exception) {
                mBinding.tvEngineInfo.text = "切换加速引擎失败: ${e.message}"
            }
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            mBinding.btnSample1 -> loadSampleImage(1)
            mBinding.btnSample2 -> loadSampleImage(2)
            mBinding.btnSample3 -> loadSampleImage(3)
            mBinding.btnPickImage -> pickImageLauncher.launch("image/*")
        }
    }

    private fun loadSampleImage(type: Int) {
        val fileName = when (type) {
            1 -> "sample_dog.jpg"
            2 -> "sample_car.jpg"
            3 -> "sample_mug.jpg"
            else -> "sample_dog.jpg"
        }

        lifecycleScope.launch {
            val bitmap = withContext(Dispatchers.IO) {
                try {
                    assets.open(fileName).use {
                        BitmapFactory.decodeStream(it)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }

            if (bitmap != null) {
                currentBitmap = bitmap
                mBinding.ivPreview.setImageBitmap(bitmap)
                runClassification(bitmap)
            }
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
                currentBitmap = bitmap
                mBinding.ivPreview.setImageBitmap(bitmap)
                runClassification(bitmap)
            }
        }
    }

    private fun runClassification(bitmap: Bitmap) {
        val currentInterpreter = interpreter ?: return

        lifecycleScope.launch {
            try {
                val (topKList, preprocessMs, inferenceMs) = withContext(Dispatchers.Default) {
                    var inputBuffer: ByteBuffer
                    val preprocessTime = measureNanoTime {
                        // 量化 MobileNet 输入 224x224x3 UINT8
                        inputBuffer = TFLiteModelHelper.convertBitmapToRgbByteBuffer(
                            bitmap = bitmap,
                            targetWidth = 224,
                            targetHeight = 224,
                            isQuantized = true
                        )
                    }

                    // MobileNet 1001 类别输出张量 [1, 1001]
                    val outputBuffer = ByteBuffer.allocateDirect(1001).apply {
                        order(ByteOrder.nativeOrder())
                        rewind()
                    }

                    val inferenceTime = measureNanoTime {
                        currentInterpreter.run(inputBuffer, outputBuffer)
                    }

                    outputBuffer.rewind()
                    val rawScores = ByteArray(1001)
                    outputBuffer.get(rawScores)

                    // UINT8 byte 转 Float 概率并 Softmax / 归一化
                    val probabilities = FloatArray(1001) { i ->
                        (rawScores[i].toInt() and 0xFF) / 255.0f
                    }

                    val topK = TFLiteModelHelper.getTopK(probabilities, k = 5)
                    Triple(topK, preprocessTime / 1_000_000.0, inferenceTime / 1_000_000.0)
                }

                mBinding.tvTimeDetails.text =
                    "预处理耗时: ${"%.2f".format(preprocessMs)} ms | 模型推理耗时: ${"%.2f".format(inferenceMs)} ms"

                val sb = StringBuilder()
                topKList.forEachIndexed { rank, (index, prob) ->
                    val label = labels.getOrElse(index) { "类别 #$index" }
                    val percent = "%.2f".format(prob * 100).padStart(5, ' ')
                    sb.append("Top ${rank + 1}:  ${percent}%  ──  $label\n")
                }
                mBinding.tvTop5Results.text = sb.toString().trimEnd()
            } catch (e: Exception) {
                mBinding.tvTop5Results.text = "图像识别异常: ${e.message}"
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        interpreter?.close()
        interpreter = null
        gpuDelegate?.close()
        gpuDelegate = null
        currentBitmap = null
    }
}
