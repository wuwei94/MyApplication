package com.example.william.my.module.ml.tts

import android.content.Context
import com.example.william.my.module.ml.download.ModelStore
import com.example.william.my.module.ml.download.RemoteModels
import com.example.william.my.module.ml.helper.TFLiteModelHelper
import org.tensorflow.lite.Interpreter
import java.io.Closeable
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * KittenTTS nano 0.8（StyleTTS2 + ISTFTNet，15M 参数，24 kHz）的 CPU 端侧实现
 *
 * 三张图都是**动态序列长度**——同一套图跑任意长度的句子，不做 padding 分桶，
 * 因此算力随文本长度伸缩：
 *
 * ```
 * 符号 id [1,N] + style [1,256] + speed [1]
 *   -> predictor -> d [1,N,256], t_en [1,N,128], durations [N]
 *   -> (host：按 durations 重复行) -> en [1,T,256], asr [1,T,128]
 *   -> prosody  -> f0 [1,2T], noise [1,2T], harmonics [1,120T+1,22]
 *   -> vocoder  -> 波形 [1,600T] @ 24 kHz
 * ```
 *
 * 图之间唯一的「胶水」是按 duration 重复行（`en = repeat(d, durations)`），与参考 ONNX 图内
 * 的 `Loop` 逐位一致。
 *
 * ### 与官方实现的一处刻意偏离：统一走经典 Interpreter
 *
 * 官方这条示例同时用两套 API：predictor / prosody 用 `Interpreter`（它们的定长 LSTM 隐状态
 * 存在 TFLite *variable tensors* 里，CompiledModel 的加载器不接受，官方注释 b/365299994），
 * 而 vocoder 用 `CompiledModel` + 一份 JNI 桥（`cpp/dynamic_shape_jni.cc`）——因为
 * CompiledModel 的输出缓冲是**调用前**一次性分配的，而 vocoder 的波形张量是 TFLite
 * *dynamic* tensor，形状要等到 invoke 时才落定。
 *
 * 本模块**三张图统一走 `Interpreter`**，理由：
 * 1. 该 JNI 桥是官方自己标注的**临时**权宜之计（"Delete this file (and the cpp/ directory)
 *    once the official Kotlin resize API ships"），而 `Interpreter` 本来就原生支持动态输出——
 *    `resizeInput` → `allocateTensors` → `run` 之后直接读输出张量的实际形状即可；
 * 2. 在**库模块**里引入 `externalNativeBuild` + NDK 会牵连模块的每个构建变体，
 *    为一条示例页付这个代价不成比例。
 *
 * 实测（本机 2.3.0 运行时）：同一段文本连跑两次输出逐位一致，不同长度交错运行也正确，
 * 波形长度恒为 `600 × T`。
 */
class KittenSynthesizer(context: Context) : Closeable {

    /** 一句的合成结果：24 kHz 浮点 PCM，外加各阶段耗时供界面展示 */
    data class Result(val audio: FloatArray, val tokens: Int, val frames: Int, val synthMs: Long)

    private val predictor: Interpreter
    private val prosody: Interpreter
    private val vocoder: Interpreter

    /**
     * `[VOICES.size, STYLE_ROWS, STYLE_DIM]` 音色表
     *
     * 官方要求设备上有一份由 `conversion/` 从 `voices.npz` 重打包出来的 `voices.bin`；
     * 这里直接从远端取 `voices.npz` 在端侧解包（见 [KittenVoiceTable]），省掉一份派生产物。
     */
    private val voiceTable: FloatArray

    /** 三张图在磁盘上的总字节数，供界面显示「模型很小」这一卖点 */
    val modelBytes: Long

    init {
        // 三张图建好后，音色表的解析仍可能失败（半截下载、内存不足）；此时实例不会交给
        // 调用方，已建成的 Interpreter 必须就地回收 —— 它持有原生内存与 mmap，GC 兜不住
        val created = ArrayList<AutoCloseable>(3)
        try {
            predictor = load(context, PREDICTOR_MODEL)
            created += predictor
            prosody = load(context, PROSODY_MODEL)
            created += prosody
            vocoder = load(context, VOCODER_MODEL)
            created += vocoder
            modelBytes = listOf(PREDICTOR_MODEL, PROSODY_MODEL, VOCODER_MODEL).sumOf {
                File(ModelStore.dir(context.applicationContext), it).length()
            }
            voiceTable = ModelStore.fileFor(context.applicationContext, RemoteModels.KITTEN_VOICES)
                .inputStream()
                .buffered()
                .use(KittenVoiceTable::read)
        } catch (e: Throwable) {
            created.asReversed().forEach { runCatching { it.close() } }
            throw e
        }
    }

    private fun load(context: Context, name: String): Interpreter {
        val file = requireFile(context, name)
        return Interpreter(file, TFLiteModelHelper.createInterpreterOptions())
    }

    /**
     * 合成一句
     *
     * @param symbolIds 来自 [EnglishG2P.phonemize] 的符号 id（**不含** BOS/EOS，这里补）
     * @param voice 音色下标，见 [VOICES]
     * @param textLength 原文本的字符数，用于挑选音色表的行
     * @param speed 语速，1.0 为正常
     */
    fun synthesize(symbolIds: IntArray, voice: Int, textLength: Int, speed: Float = 1f): Result {
        val started = System.nanoTime()
        val style = FloatArray(STYLE_DIM)
        KittenVoiceTable.styleFor(voiceTable, voice, textLength, style)

        // predictor：[0] + ids + [0] -> 逐 token 特征 + 整数时长
        val tokenCount = symbolIds.size + 2
        val ids = IntArray(tokenCount)
        symbolIds.copyInto(ids, 1)
        val predictorOut = run(
            predictor,
            mapOf(
                "input_ids" to Feed(intArrayOf(1, tokenCount), intBuffer(ids)),
                "style" to Feed(intArrayOf(1, STYLE_DIM), floatBuffer(style)),
                "speed" to Feed(intArrayOf(1), floatBuffer(floatArrayOf(speed))),
            ),
        )
        val tEn = predictorOut.float(OUT_T_EN) // [1,N,128]
        val durations = predictorOut.int(OUT_DURATIONS) // [N]
        val d = predictorOut.float(OUT_D) // [1,N,256]

        // 按时长重复每个 token 的特征行（上游 ONNX 图内 Loop 的等价 host 实现）
        val frames = durations.sum()
        check(frames > 0) { "模型预测的时长全为 0，无法合成" }
        val en = repeatRows(d, durations, D_DIM)
        val asr = repeatRows(tEn, durations, ASR_DIM)

        // prosody：整句的谐波 F0 / 噪声 / 谐波包络
        val prosodyOut = run(
            prosody,
            mapOf(
                "en" to Feed(intArrayOf(1, frames, D_DIM), floatBuffer(en)),
                "style" to Feed(intArrayOf(1, STYLE_DIM), floatBuffer(style)),
            ),
        )
        val f0 = prosodyOut.float(OUT_F0) // [1,2T]
        val noise = prosodyOut.float(OUT_NOISE) // [1,2T]
        val harmonics = prosodyOut.float(OUT_HARMONICS) // [1,120T+1,22]

        // vocoder（ISTFTNet）：对齐后的文本特征 + 韵律 -> 波形 [1,600T]
        val waveform = vocode(asr, frames, f0, noise, harmonics, style)

        // 上游 pip 包会裁掉每段末尾的合成尾巴
        val sampleCount = maxOf(waveform.size - TAIL_TRIM, MIN_SAMPLES.coerceAtMost(waveform.size))
        val audio = FloatArray(sampleCount) { waveform[it].coerceIn(-1f, 1f) }

        return Result(audio, tokenCount, frames, (System.nanoTime() - started) / 1_000_000)
    }

    /**
     * 用本句的动态形状跑 vocoder
     *
     * 官方这里调 JNI 的 `runDynamic` 并**显式传入**输出形状 `[1, 600T]`；`Interpreter` 路径
     * 不需要——`allocateTensors` 之后 invoke，张量形状就在运行时落定，[Outputs] 直接按
     * 缓冲区的实际长度读取。
     */
    private fun vocode(
        asr: FloatArray,
        frames: Int,
        f0: FloatArray,
        noise: FloatArray,
        harmonics: FloatArray,
        style: FloatArray,
    ): FloatArray = run(
        vocoder,
        mapOf(
            "asr" to Feed(intArrayOf(1, frames, ASR_DIM), floatBuffer(asr)),
            "f0" to Feed(intArrayOf(1, f0.size), floatBuffer(f0)),
            "n" to Feed(intArrayOf(1, noise.size), floatBuffer(noise)),
            "har" to Feed(intArrayOf(1, harmonics.size / HAR_DIM, HAR_DIM), floatBuffer(harmonics)),
            "style" to Feed(intArrayOf(1, STYLE_DIM), floatBuffer(style)),
        ),
    ).float(OUT_WAVEFORM)

    /** `np.repeat(x[0], durations, axis=0)`，`x` 是行优先的 `[1,N,dim]` */
    private fun repeatRows(x: FloatArray, durations: IntArray, dim: Int): FloatArray {
        var total = 0
        for (duration in durations) {
            total += duration
        }
        val out = FloatArray(total * dim)
        var write = 0
        for (row in durations.indices) {
            repeat(durations[row]) {
                System.arraycopy(x, row * dim, out, write, dim)
                write += dim
            }
        }
        return out
    }

    // ─────────────────── 动态形状驱动 ───────────────────

    private class Feed(val shape: IntArray, val data: ByteBuffer)

    /** 某张图的输出读取器，按**张量名后缀**定位下标（`getOutputTensor` 的顺序不保证） */
    private class Outputs(private val interpreter: Interpreter) {

        private val indexes = HashMap<String, Int>()

        fun index(suffix: String): Int = indexes.getOrPut(suffix) {
            (0 until interpreter.outputTensorCount).firstOrNull {
                interpreter.getOutputTensor(it).name().endsWith(suffix)
            } ?: error("输出张量不存在: $suffix")
        }

        private fun buffer(suffix: String): ByteBuffer {
            // 张量缓冲区只在下一次分配之前有效，因此必须拷贝出来
            val source = interpreter.getOutputTensor(index(suffix)).asReadOnlyBuffer().order(ByteOrder.nativeOrder())
            val copy = ByteBuffer.allocate(source.remaining()).order(ByteOrder.nativeOrder())
            copy.put(source).rewind()
            return copy
        }

        fun float(suffix: String): FloatArray {
            val buffer = buffer(suffix).asFloatBuffer()
            return FloatArray(buffer.remaining()).also { buffer.get(it) }
        }

        fun int(suffix: String): IntArray {
            val buffer = buffer(suffix).asIntBuffer()
            return IntArray(buffer.remaining()).also { buffer.get(it) }
        }
    }

    /**
     * 用一句的实际形状驱动一张图：逐个输入 `resizeInput` → 分配 → invoke
     *
     * 输入按**规范化张量名**匹配（`serving_default_x:0` 与 `x` 都归一到 `x`），
     * 因此同一条代码路径既能驱动带签名的图（predictor / prosody），也能驱动不带签名的图
     * （vocoder 的输入就叫 `asr` / `f0` / `n` / `har` / `style`）。
     *
     * `resetVariableTensors` 是**必须**的：这两张图的 LSTM 隐状态存放在 variable tensors 中
     * 并跨调用保留。形状变化会因重新分配而顺带重置，但**长度相同的两次合成不会**——
     * 第二次会从上一次遗留的末状态开始，把预测时长带偏（复现方式：同一句话连说两遍）。
     */
    private fun run(interpreter: Interpreter, feeds: Map<String, Feed>): Outputs {
        var resized = false
        val inputs = arrayOfNulls<Any>(interpreter.inputTensorCount)
        for (i in 0 until interpreter.inputTensorCount) {
            val tensor = interpreter.getInputTensor(i)
            val name = canonical(tensor.name())
            val feed = checkNotNull(feeds[name]) { "缺少输入张量: $name" }
            if (!tensor.shape().contentEquals(feed.shape)) {
                interpreter.resizeInput(i, feed.shape)
                resized = true
            }
            inputs[i] = feed.data.rewind()
        }
        if (resized) {
            interpreter.allocateTensors()
        }
        interpreter.resetVariableTensors()
        interpreter.runForMultipleInputsOutputs(inputs, emptyMap())
        return Outputs(interpreter)
    }

    override fun close() {
        predictor.close()
        prosody.close()
        vocoder.close()
    }

    companion object {

        /** 24 kHz 单声道浮点 PCM */
        const val SAMPLE_RATE = 24_000

        /** 8 个内置音色，顺序即音色表行序 */
        val VOICES: List<String> = KittenVoiceTable.VOICES

        /** 去掉 `serving_default_` 前缀与 `:0` 后缀，得到可比较的规范名 */
        private fun canonical(name: String): String = name.removePrefix("serving_default_").substringBefore(':')

        private fun requireFile(context: Context, name: String): File = File(ModelStore.dir(context.applicationContext), name).also {
            check(it.isFile && it.length() > 0L) { "模型尚未下载: $name" }
        }

        private const val PREDICTOR_MODEL = "kitten_predictor_fp16.tflite"
        private const val PROSODY_MODEL = "kitten_prosody_fp16.tflite"
        private const val VOCODER_MODEL = "kitten_vocoder_fp16.tflite"

        /** TFLite 默认签名（`serving_default`）的输出在 Java API 里不带前缀，直接按后缀匹配 */
        private const val OUT_T_EN = "StatefulPartitionedCall:0"
        private const val OUT_DURATIONS = "StatefulPartitionedCall:1"
        private const val OUT_D = "StatefulPartitionedCall:2"
        private const val OUT_F0 = "StatefulPartitionedCall:0"
        private const val OUT_NOISE = "StatefulPartitionedCall:1"
        private const val OUT_HARMONICS = "StatefulPartitionedCall:2"
        private const val OUT_WAVEFORM = "Identity"

        private const val STYLE_DIM = 256
        private const val D_DIM = 256
        private const val ASR_DIM = 128
        private const val HAR_DIM = 22

        /** 上游 pip 包会从每段末尾裁掉这么多采样点 */
        private const val TAIL_TRIM = 5000
        private const val MIN_SAMPLES = 1200

        private fun floatBuffer(data: FloatArray): ByteBuffer = ByteBuffer.allocateDirect(data.size * 4)
            .order(ByteOrder.nativeOrder())
            .also { it.asFloatBuffer().put(data) }

        private fun intBuffer(data: IntArray): ByteBuffer = ByteBuffer.allocateDirect(data.size * 4)
            .order(ByteOrder.nativeOrder())
            .also { it.asIntBuffer().put(data) }
    }
}
