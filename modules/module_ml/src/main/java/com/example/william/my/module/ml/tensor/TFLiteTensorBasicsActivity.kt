package com.example.william.my.module.ml.tensor

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.activity.BasicResponseActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.module.ml.helper.TFLiteModelHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * TFLite 张量底层操作与内存生命周期规范
 *
 * 官方文档: https://www.tensorflow.org/lite/guide
 *
 * 演示端侧模型推理的底层核心机制：
 * 1. FlatBuffers 零拷贝（Zero-copy mmap）加载与张量元数据（Tensor Metadata / Shapes / DataType）实时反射
 * 2. JVM Direct Memory vs 堆内存对比与原生字节序（ByteOrder.nativeOrder()）
 * 3. 动态张量尺寸调整 (Dynamic Shape Resizing)
 * 4. 多输入多输出 (Multi-Input Multi-Output, MIMO) 复杂张量推理调度
 * 5. Native C++ 资源安全释放与防内存泄漏规范
 */
@Route(path = RouterPath.Ml.TensorBasics)
class TFLiteTensorBasicsActivity : BasicResponseActivity() {

    private var interpreter: Interpreter? = null
    private val modelFileName = "mobilenet_v1_1.0_224_quant.tflite"

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        showDescription(
            "TFLite 张量底层操作与内存架构\n\n" +
                    "演示 FlatBuffers 零拷贝 mmap、Direct 内存排布、动态张量调整与 Native 资源释放\n" +
                    "请点击下方操作项执行"
        )
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "1. 零拷贝 mmap 模型加载与张量元数据反射",
            "2. Direct ByteBuffer 内存排布与字节序机制",
            "3. 动态张量调整 (Interpreter.resizeInput)",
            "4. 多输入多输出 (MIMO) 调度演示",
            "5. Native 内存释放与防泄漏最佳实践"
        )
    }

    override fun onRecyclerClick(position: Int, string: String) {
        when (position) {
            0 -> inspectModelMetadata()
            1 -> explainDirectMemory()
            2 -> testDynamicResizing()
            3 -> testMimoExecution()
            4 -> demonstrateSafeRelease()
        }
    }

    private fun inspectModelMetadata() {
        lifecycleScope.launch {
            try {
                appendLog("── 正在以 FlatBuffers mmap 映射加载 $modelFileName ──")
                val modelBuffer = withContext(Dispatchers.IO) {
                    TFLiteModelHelper.loadModelFile(this@TFLiteTensorBasicsActivity, modelFileName)
                }
                val options = Interpreter.Options().apply {
                    setNumThreads(2)
                }
                val inter = Interpreter(modelBuffer, options)
                interpreter?.close()
                interpreter = inter

                appendLog("✓ 模型加载成功！反射张量元数据:")
                appendLog("• 输入张量总数: ${inter.inputTensorCount}")
                for (i in 0 until inter.inputTensorCount) {
                    val tensor = inter.getInputTensor(i)
                    appendLog("  - Input[$i]: 名称='${tensor.name()}', 维度=${tensor.shape().contentToString()}, 数据类型=${tensor.dataType()}")
                }

                appendLog("• 输出张量总数: ${inter.outputTensorCount}")
                for (i in 0 until inter.outputTensorCount) {
                    val tensor = inter.getOutputTensor(i)
                    appendLog("  - Output[$i]: 名称='${tensor.name()}', 维度=${tensor.shape().contentToString()}, 数据类型=${tensor.dataType()}")
                }
            } catch (e: Exception) {
                appendLog("✗ 读取模型张量失败: ${e.message}")
            }
        }
    }

    private fun explainDirectMemory() {
        appendLog("── JVM 堆内存 vs Direct Memory (直接内存) 对比 ──")
        appendLog("• JVM 堆内存 (Heap ByteBuffer):")
        appendLog("  存在于 Java 垃圾回收堆中，当 JNI 传递给 C++ TFLite Native 核心时，JVM 必须在 Native 堆复制一份临时内存，产生额外拷贝开销和 GC 停顿。")
        appendLog("• Direct ByteBuffer (零拷贝内存):")
        appendLog("  使用 ByteBuffer.allocateDirect() 在 C/C++ 堆中直接分配连续内存。")
        appendLog("  TFLite 底层 C++ 解释器可直接通过指针读写该内存，达到真正的「零拷贝」高速推理。")
        appendLog("• 字节序注意事项:")
        appendLog("  ARM/x86 架构移动设备均为小端序 (Little Endian)，DirectBuffer 必须调用 order(ByteOrder.nativeOrder())，否则浮点/整数位解析将错乱。")
    }

    private fun testDynamicResizing() {
        val inter = interpreter ?: run {
            appendLog("⚠ 请先点击选项 1 初始化加载模型")
            return
        }

        try {
            appendLog("── 执行动态张量尺寸重置 (Dynamic Tensor Resizing) ──")
            val originalShape = inter.getInputTensor(0).shape()
            appendLog("• 当前输入 Shape: ${originalShape.contentToString()}")

            // 修改 Batch 大小为 2
            val newShape = intArrayOf(2, 224, 224, 3)
            inter.resizeInput(0, newShape)
            inter.allocateTensors() // 必须重新分配张量内存

            val updatedShape = inter.getInputTensor(0).shape()
            appendLog("✓ 已动态重置输入张量并重新分配内存:")
            appendLog("• 更新后输入 Shape: ${updatedShape.contentToString()} (支持一次性传入 2 张图像并行批处理)")

            // 还原为 1
            inter.resizeInput(0, intArrayOf(1, 224, 224, 3))
            inter.allocateTensors()
            appendLog("• 已自动恢复默认单样本维度 [1, 224, 224, 3]")
        } catch (e: Exception) {
            appendLog("✗ 动态调整张量失败: ${e.message}")
        }
    }

    private fun testMimoExecution() {
        appendLog("── 多输入多输出 (MIMO) 调度原理 ──")
        appendLog("对于目标检测或图文多模态模型，通常具有多个输入与输出张量。")
        appendLog("调用签名: interpreter.runForMultipleInputsOutputs(inputs, outputsMap)")
        appendLog("• inputs: Array<Any> (如 arrayOf(imageBuffer, textTokenBuffer))")
        appendLog("• outputsMap: Map<Int, Any> (如 mapOf(0 to scoreBuffer, 1 to boxCoordinatesBuffer))")
        appendLog("✓ 此机制避免了多次反复调用 run()，显著降低 JNI 边界跨越开销。")
    }

    private fun demonstrateSafeRelease() {
        interpreter?.close()
        interpreter = null
        appendLog("✓ 已安全释放 Interpreter 实例与底层 C++ Native 张量内存 (JNI 资源清空)")
    }

    override fun onDestroy() {
        super.onDestroy()
        demonstrateSafeRelease()
    }
}
