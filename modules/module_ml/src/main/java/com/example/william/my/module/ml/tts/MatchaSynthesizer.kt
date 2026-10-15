package com.example.william.my.module.ml.tts

import android.content.Context
import com.example.william.my.module.ml.download.ModelStore
import com.example.william.my.module.ml.download.RemoteModels
import com.example.william.my.module.ml.helper.TFLiteModelHelper
import com.google.ai.edge.litert.CompiledModel
import com.google.ai.edge.litert.TensorBuffer
import java.io.Closeable
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Random
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.sin

/**
 * Matcha-TTS 端侧声学模型（对应官方 `samples/litert/text_to_speech/kotlin_cpu_gpu`）
 *
 * 三张重图跑在 LiteRT CompiledModel 上，**所有随机 / 顺序逻辑都留在 host 侧**：
 * FiLM 声码器是时域 GAN（不含 FFT），这正是整条合成链路能停留在 GPU 上的原因。
 *
 * ```
 * 音素 id --(host: 嵌入 + 补位)--> text_encoder(--> mu, logw)
 *         --(host: 时长展开 + length regulator)--> mu_y[1,80,T]
 *         --(host: Euler ODE 循环, N 步)-->
 *             decoder x N  (x_t, mu_y, sin_emb(t), mask) --> v
 *         --(host: 反归一化)--> mel --> vocoder --> 波形[T*256]
 * ```
 *
 * 三个关键设计（照搬官方，改动会破坏时间对齐）：
 * 1. **固定形状**（[MAX_TEXT] 个音素 / [MAX_MEL] 帧），靠**运行时浮点 mask** 把补位位置
 *    变成空操作（加性注意力偏置），因此一张编译好的图能吃任意长度输入。
 * 2. **decoder 走 CPU、text encoder 与 vocoder 走 GPU**：官方注释指出 diffusers transformer
 *    在 Mali ML Drift delegate 上存在算子融合缺陷（整图里残差相关性塌到 0.006，而同一个
 *    transformer 单独成图时是 0.98）——这是融合 bug 而非算子问题，因此 CPU 上反而精确。
 * 3. 模型**不入 APK**，与官方一致地按需下载到应用私有目录（见 [ModelStore]）。
 */
class MatchaSynthesizer(private val context: Context) : Closeable {

    /** 一次合成结果：PCM 浮点波形 + 供界面展示的计时信息 */
    data class Result(val audio: FloatArray, val frames: Int, val steps: Int, val ms: Long)

    /** 音素嵌入表，随 assets 分发（体积很小，官方同样放在 assets） */
    private val embeddingTable: FloatArray =
        readFloats(context.assets.open(EMBEDDING_ASSET).use { it.readBytes() })

    /** 是否有图从 GPU 回退到了 CPU（构造期置位，供界面如实展示） */
    @Volatile
    private var gpuFallback: Boolean = false

    private val textEncoder: CompiledModel
    private val decoder: CompiledModel
    private val vocoder: CompiledModel

    private val textEncoderInputs: List<TensorBuffer>
    private val textEncoderOutputs: List<TensorBuffer>
    private val decoderInputs: List<TensorBuffer>
    private val decoderOutputs: List<TensorBuffer>
    private val vocoderInputs: List<TensorBuffer>
    private val vocoderOutputs: List<TensorBuffer>

    /**
     * 三张图与各自的缓冲区
     *
     * 构造中途失败（GPU 分配失败、磁盘上是半截文件等）时逐个回收**已建成的**原生实例：
     * `CompiledModel` 与 `TensorBuffer` 都持有原生内存，构造器抛异常时 GC 兜不住它们。
     */
    init {
        val created = ArrayList<AutoCloseable>(9)
        try {
            val encoder = loadModel(TEXT_ENCODER_MODEL, useGpu = true)
            created += encoder
            val dec = loadModel(DECODER_MODEL, useGpu = false)
            created += dec
            val voc = loadModel(VOCODER_MODEL, useGpu = true)
            created += voc

            val encoderIn = encoder.createInputBuffers()
            created.addAll(encoderIn)
            val encoderOut = encoder.createOutputBuffers()
            created.addAll(encoderOut)
            val decIn = dec.createInputBuffers()
            created.addAll(decIn)
            val decOut = dec.createOutputBuffers()
            created.addAll(decOut)
            val vocIn = voc.createInputBuffers()
            created.addAll(vocIn)
            val vocOut = voc.createOutputBuffers()
            created.addAll(vocOut)

            textEncoder = encoder
            decoder = dec
            vocoder = voc
            textEncoderInputs = encoderIn
            textEncoderOutputs = encoderOut
            decoderInputs = decIn
            decoderOutputs = decOut
            vocoderInputs = vocIn
            vocoderOutputs = vocOut
        } catch (e: Throwable) {
            // 逆序回收：TensorBuffer 先于所属 CompiledModel 关闭
            created.asReversed().forEach { runCatching { it.close() } }
            throw e
        }
    }

    /** 实际生效的后端描述，供界面如实展示（某张图回退到 CPU 时会被标注出来） */
    val acceleratorSummary: String
        get() {
            val gpu = if (gpuFallback) "GPU 不可用，已回退 CPU" else "GPU"
            return "text encoder / vocoder: $gpu | decoder: CPU"
        }

    /**
     * 从应用私有目录加载一张图
     *
     * 后端分配与官方一致。GPU 不可用时（模拟器、无兼容驱动的设备）回退到 CPU，
     * 只是慢一些，不影响结果正确性——因此不必让整页失败。
     */
    private fun loadModel(fileName: String, useGpu: Boolean): CompiledModel {
        val path = ModelStore.pathIn(context.filesDir, fileName)
        check(ModelStore.isReady(context, remoteOf(fileName))) {
            "模型尚未下载: $fileName"
        }
        if (!useGpu) {
            return CompiledModel.create(path, TFLiteModelHelper.createCpuOptions(), null)
        }
        return try {
            CompiledModel.create(path, TFLiteModelHelper.createGpuOptions(), null)
        } catch (e: Exception) {
            gpuFallback = true
            e.printStackTrace()
            CompiledModel.create(path, TFLiteModelHelper.createCpuOptions(), null)
        }
    }

    /**
     * 由 matcha 音素 id（尚未穿插空音素）合成语音
     *
     * @param phonemeIds 来自 [EnglishG2P.phonemize] 的 matcha 符号 id
     * @param nSteps Euler ODE 步数（越多质量越高、越慢）
     * @param seed 固定噪声种子，便于复现
     */
    fun synthesize(phonemeIds: IntArray, nSteps: Int = DEFAULT_STEPS, seed: Long? = null): Result {
        val startNanos = System.nanoTime()

        // 穿插空音素（id 0）、补齐到 MAX_TEXT，并构造文本 mask
        val textLength = minOf(phonemeIds.size * 2 + 1, MAX_TEXT)
        val ids = IntArray(MAX_TEXT)
        var writeIndex = 1
        for (id in phonemeIds) {
            if (writeIndex >= MAX_TEXT) break
            ids[writeIndex] = id
            writeIndex += 2
        }
        val textMask = FloatArray(MAX_TEXT) { if (it < textLength) 1f else 0f }

        // host 侧音素嵌入查表 -> [1, MAX_TEXT, N_CHANNELS]
        val embedded = FloatArray(MAX_TEXT * N_CHANNELS)
        for (t in 0 until MAX_TEXT) {
            System.arraycopy(embeddingTable, ids[t] * N_CHANNELS, embedded, t * N_CHANNELS, N_CHANNELS)
        }

        // text encoder（GPU）-> mu[1,80,T], logw[1,1,T]
        textEncoderInputs[0].writeFloat(embedded)
        textEncoderInputs[1].writeFloat(textMask)
        textEncoder.run(textEncoderInputs, textEncoderOutputs)
        val mu = textEncoderOutputs[0].readFloat() // [N_FEATS * MAX_TEXT]，通道优先
        val logDurations = textEncoderOutputs[1].readFloat() // [MAX_TEXT]

        // 时长 -> 累积 -> length regulator（mu_y）
        val durations = FloatArray(MAX_TEXT) {
            ceil(exp(logDurations[it]) * textMask[it]) * LENGTH_SCALE
        }
        val cumulative = FloatArray(MAX_TEXT)
        var total = 0f
        for (i in 0 until MAX_TEXT) {
            total += durations[i]
            cumulative[i] = total
        }
        val melLength = minOf(maxOf(total.toInt(), 1), MAX_MEL)

        val muY = FloatArray(N_FEATS * MAX_MEL)
        var phonemeIndex = 0
        for (frame in 0 until melLength) {
            while (phonemeIndex < MAX_TEXT - 1 && cumulative[phonemeIndex] <= frame) {
                phonemeIndex++
            }
            for (c in 0 until N_FEATS) {
                muY[c * MAX_MEL + frame] = mu[c * MAX_TEXT + phonemeIndex]
            }
        }
        val melMask = FloatArray(MAX_MEL) { if (it < melLength) 1f else 0f }

        // Euler ODE: x_{k+1} = x_k + dt * decoder(x_k, mu_y, sin_emb(t), mask)
        val random = if (seed != null) Random(seed) else Random()
        val x = FloatArray(N_FEATS * MAX_MEL)
        for (c in 0 until N_FEATS) {
            for (frame in 0 until melLength) {
                x[c * MAX_MEL + frame] = random.nextGaussian().toFloat()
            }
        }
        val dt = 1f / nSteps
        var time = 0f
        repeat(nSteps) {
            decoderInputs[0].writeFloat(x)
            decoderInputs[1].writeFloat(muY)
            decoderInputs[2].writeFloat(sinusoidalPositionEmbedding(time))
            decoderInputs[3].writeFloat(melMask)
            decoder.run(decoderInputs, decoderOutputs)
            val velocity = decoderOutputs[0].readFloat()
            for (i in x.indices) {
                x[i] += dt * velocity[i]
            }
            time += dt
        }

        // 反归一化 + 零填充 -> mel -> vocoder（GPU）-> 波形
        val mel = FloatArray(N_FEATS * MAX_MEL)
        for (c in 0 until N_FEATS) {
            for (frame in 0 until melLength) {
                val i = c * MAX_MEL + frame
                mel[i] = x[i] * MEL_STD + MEL_MEAN
            }
        }
        vocoderInputs[0].writeFloat(mel)
        vocoder.run(vocoderInputs, vocoderOutputs)
        val waveform = vocoderOutputs[0].readFloat() // [MAX_MEL * HOP]
        val sampleCount = melLength * HOP
        val audio = FloatArray(sampleCount) { waveform[it].coerceIn(-1f, 1f) }

        return Result(audio, melLength, nSteps, (System.nanoTime() - startNanos) / 1_000_000)
    }

    /** Matcha SinusoidalPosEmb（无权重）：t -> [TIME_DIM] = [sin(...) | cos(...)] */
    private fun sinusoidalPositionEmbedding(t: Float, scale: Float = 1000f): FloatArray {
        val half = TIME_DIM / 2
        val embedding = FloatArray(TIME_DIM)
        val logScale = -ln(10000.0) / (half - 1)
        for (i in 0 until half) {
            val angle = scale * t * exp(i * logScale).toFloat()
            embedding[i] = sin(angle)
            embedding[half + i] = cos(angle)
        }
        return embedding
    }

    private fun readFloats(bytes: ByteArray): FloatArray {
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        return FloatArray(bytes.size / 4) { buffer.float }
    }

    override fun close() {
        textEncoderInputs.forEach { it.close() }
        textEncoderOutputs.forEach { it.close() }
        decoderInputs.forEach { it.close() }
        decoderOutputs.forEach { it.close() }
        vocoderInputs.forEach { it.close() }
        vocoderOutputs.forEach { it.close() }
        textEncoder.close()
        decoder.close()
        vocoder.close()
    }

    private fun remoteOf(fileName: String) = RemoteModels.MATCHA.first { it.fileName == fileName }

    companion object {
        const val MAX_TEXT = 256
        const val MAX_MEL = 512
        const val N_FEATS = 80
        const val N_CHANNELS = 192
        const val HOP = 256
        const val SAMPLE_RATE = 22050
        const val LENGTH_SCALE = 0.95f
        const val MEL_MEAN = -5.536622f
        const val MEL_STD = 2.116101f
        const val TIME_DIM = 160
        const val DEFAULT_STEPS = 10
        const val MAX_SAMPLES = MAX_MEL * HOP

        /** 音素嵌入表，随 assets 分发 */
        private const val EMBEDDING_ASSET = "emb.bin"

        private const val TEXT_ENCODER_MODEL = "matcha_textenc_fp16.tflite"
        private const val DECODER_MODEL = "matcha_decoder_fp16.tflite"
        private const val VOCODER_MODEL = "matcha_vocoder_fp16.tflite"
    }
}
