package com.example.william.my.module.ml.helper

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.graphics.Bitmap
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import kotlin.math.exp

/**
 * TensorFlow Lite 模型加载、张量映射与数据预处理/后处理核心工具类
 */
object TFLiteModelHelper {

    /**
     * 通过 AssetFileDescriptor 以零拷贝方式（Zero-copy mmap）内存映射加载 TFLite 模型文件
     */
    /**
     * 通过 AssetFileDescriptor 以零拷贝方式（Zero-copy mmap）内存映射加载 TFLite 模型文件。
     * 若遇到压缩 Asset 或系统 openFd 限制，自动降级为 Direct ByteBuffer 安全加载。
     */
    @Throws(Exception::class)
    fun loadModelFile(context: Context, modelPath: String): ByteBuffer = try {
        val fileDescriptor: AssetFileDescriptor = context.assets.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel: FileChannel = inputStream.channel
        val startOffset: Long = fileDescriptor.startOffset
        val declaredLength: Long = fileDescriptor.declaredLength
        fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    } catch (e: Exception) {
        context.assets.open(modelPath).use { inputStream ->
            val bytes = inputStream.readBytes()
            val buffer = ByteBuffer.allocateDirect(bytes.size).apply {
                order(ByteOrder.nativeOrder())
                put(bytes)
                rewind()
            }
            buffer
        }
    }

    /**
     * 从 assets 加载分类标签列表
     */
    fun loadLabels(context: Context, labelPath: String): List<String> {
        val labels = mutableListOf<String>()
        try {
            context.assets.open(labelPath).use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    var line: String? = reader.readLine()
                    while (line != null) {
                        if (line.isNotBlank()) {
                            labels.add(line.trim())
                        }
                        line = reader.readLine()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return labels
    }

    /**
     * 检查 Asset 下文件是否存在
     */
    fun isAssetFileExists(context: Context, fileName: String): Boolean {
        return try {
            val list = context.assets.list("") ?: return false
            list.contains(fileName)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 将 Bitmap 转换为 MNIST 单通道灰度 Direct ByteBuffer [1, targetWidth, targetHeight, 1]
     * 格式为 FP32 浮点型，归一化范围 [0.0f, 1.0f]
     */
    fun convertBitmapToDigitByteBuffer(
        bitmap: Bitmap,
        targetWidth: Int = 28,
        targetHeight: Int = 28,
    ): ByteBuffer {
        val scaled = if (bitmap.width == targetWidth && bitmap.height == targetHeight) {
            bitmap
        } else {
            Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
        }

        val byteBuffer = ByteBuffer.allocateDirect(targetWidth * targetHeight * 4).apply {
            order(ByteOrder.nativeOrder())
            rewind()
        }

        val pixels = IntArray(targetWidth * targetHeight)
        scaled.getPixels(pixels, 0, targetWidth, 0, 0, targetWidth, targetHeight)

        for (pixel in pixels) {
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF
            // 灰度加权公式：Y = 0.299R + 0.587G + 0.114B
            val gray = (0.299f * r + 0.587f * g + 0.114f * b) / 255.0f
            byteBuffer.putFloat(gray)
        }

        byteBuffer.rewind()
        return byteBuffer
    }

    /**
     * 将彩色 Bitmap 转换为 RGB 3 通道 Direct ByteBuffer [1, targetWidth, targetHeight, 3]
     *
     * @param isQuantized true 表示 UINT8 量化模型 [0, 255]，false 表示 FP32 浮点模型 [-1.0f, 1.0f]
     */
    fun convertBitmapToRgbByteBuffer(
        bitmap: Bitmap,
        targetWidth: Int = 224,
        targetHeight: Int = 224,
        isQuantized: Boolean = true,
    ): ByteBuffer {
        // 1. 中心正方形等比裁剪，避免非 1:1 图片拉伸变形失真
        val minDim = minOf(bitmap.width, bitmap.height)
        val xOffset = (bitmap.width - minDim) / 2
        val yOffset = (bitmap.height - minDim) / 2
        val cropped = if (bitmap.width != bitmap.height) {
            Bitmap.createBitmap(bitmap, xOffset, yOffset, minDim, minDim)
        } else {
            bitmap
        }

        // 2. 缩放到目标 224x224 尺寸
        val scaled = if (cropped.width == targetWidth && cropped.height == targetHeight) {
            cropped
        } else {
            Bitmap.createScaledBitmap(cropped, targetWidth, targetHeight, true)
        }

        val bytesPerChannel = if (isQuantized) 1 else 4
        val byteBuffer = ByteBuffer.allocateDirect(targetWidth * targetHeight * 3 * bytesPerChannel).apply {
            order(ByteOrder.nativeOrder())
            rewind()
        }

        val pixels = IntArray(targetWidth * targetHeight)
        scaled.getPixels(pixels, 0, targetWidth, 0, 0, targetWidth, targetHeight)

        for (pixel in pixels) {
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF

            if (isQuantized) {
                // UINT8 量化输入 [0, 255]
                byteBuffer.put(r.toByte())
                byteBuffer.put(g.toByte())
                byteBuffer.put(b.toByte())
            } else {
                // FP32 归一化输入 [-1.0f, 1.0f] (ImageNet 标准)
                byteBuffer.putFloat((r - 127.5f) / 127.5f)
                byteBuffer.putFloat((g - 127.5f) / 127.5f)
                byteBuffer.putFloat((b - 127.5f) / 127.5f)
            }
        }

        byteBuffer.rewind()
        return byteBuffer
    }

    /**
     * 计算 Softmax 概率归一化
     */
    fun softmax(logits: FloatArray): FloatArray {
        val max = logits.maxOrNull() ?: 0.0f
        var sum = 0.0f
        val expArray = FloatArray(logits.size)
        for (i in logits.indices) {
            expArray[i] = exp(logits[i] - max)
            sum += expArray[i]
        }
        if (sum > 0f) {
            for (i in expArray.indices) {
                expArray[i] /= sum
            }
        }
        return expArray
    }

    /**
     * 获取置信度最高的 Top-K 索引与概率值
     */
    fun getTopK(probabilities: FloatArray, k: Int = 5): List<Pair<Int, Float>> = probabilities
        .mapIndexed { index, score -> Pair(index, score) }
        .sortedByDescending { it.second }
        .take(k)
}
