package com.example.william.my.module.ml.asr

import com.example.william.my.module.ml.download.ModelStore
import com.example.william.my.module.ml.helper.TFLiteModelHelper
import com.google.ai.edge.litert.Accelerator
import com.google.ai.edge.litert.CompiledModel
import java.io.File
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * 一个 ASR 模型完整的推理侧组合体：分词器 + 预处理 + 主图（含解码器）。
 *
 * 官方把这三样散在 `MainActivity` 的字段里各自 new / close，本模块收成一个
 * `AutoCloseable`，理由有二：
 * 1. 引擎构建必须整体在后台单并发队列上完成（`helper/TFLiteLifecycleSupport.kt`），
 *    组合体自身可 `AutoCloseable` 才能交给 `buildOffMainThreadSafely<T : AutoCloseable>`；
 * 2. 「构建到一半就抛异常」时能就地回滚 —— 官方在 `preparePipeline` 的 catch 里
 *    调 `resetPipeline()` 兜底，这里靠构造函数内的补偿关闭达到同样效果。
 *
 * 与官方的其余差异：后端走 [TFLiteModelHelper.createFallbackOptions]（`CPU + accelerator`
 * 的集合，与官方 `setOf(Accelerator.CPU, accelerator)` 一致），NPU 分支不保留
 * （见 [AsrModelConfig] 的说明）。
 *
 * ⚠️ 生成的实例**必须**在与推理、引擎关闭同一条队列上 `close()` —— 主图的原生 handle
 * 由 `CompiledModel` 持有，关闭期间并发 `run()` 会踩已释放内存。页面侧统一走
 * `closeEngineOn(engineDispatcher, engine)`。
 */
internal class AsrEngine(
    filesDir: File,
    val config: AsrModelConfig,
    accelerator: Accelerator = Accelerator.CPU,
) : AutoCloseable {

    /** 分词器只做「id → 文本」一个方向，因此整张词表常驻即可 */
    val tokenizer: Tokenizer = HfTokenizer.fromFile(File(ModelStore.dirIn(filesDir), config.tokenizerFileName))

    /** `logMelSpectro` 为 null 的模型（Moonshine）直接吃原始波形 */
    val audioPreprocessor: AudioPreprocessor =
        config.logMelSpectro?.let { MelSpectroProcessor(SAMPLING_RATE, it) } ?: DummyAudioProcessor()

    val recognizer: SpeechRecognizer

    /** 该模型一次能吃多长的音频（与 [AudioSource.interval] 对齐） */
    val inputInterval: Duration = config.inputMilliseconds.milliseconds

    init {
        val path = ModelStore.pathIn(filesDir, config.modelFileName)
        val model = CompiledModel.create(
            path,
            TFLiteModelHelper.createFallbackOptions(accelerator),
            null,
        )
        recognizer =
            try {
                LiteRtRunner(model, config) { compiled, modelConfig ->
                    decoderFor(config.key, compiled, modelConfig, tokenizer)
                }
            } catch (e: Exception) {
                // 解码器构造失败时主图已经建好了，就地关掉避免泄漏
                runCatching { model.close() }
                runCatching { audioPreprocessor.close() }
                runCatching { tokenizer.close() }
                throw e
            }
    }

    override fun close() {
        // recognizer 的 close 会连带关掉它持有的 CompiledModel（见 LiteRtRunner.close）
        runCatching { recognizer.close() }
        runCatching { audioPreprocessor.close() }
        runCatching { tokenizer.close() }
    }

    companion object {

        /** 官方全链路统一按 16 kHz 单声道处理 */
        const val SAMPLING_RATE = 16000

        /**
         * 按模型名分派解码器（官方 `preparePipeline` 里的 `when`）
         *
         * 判据是**模型名里的子串**而不是某个字段，顺序也与官方一致：先 `-ctc-` 再 `-tdt`。
         * 注意 `parakeet-tdt_ctc-0.6b-ja` 虽然名字里同时出现 `tdt` 与 `ctc`，
         * 但它不含 `-ctc-` 这一子串，因此落到 [TdtDecoder] —— 与官方行为一致。
         */
        internal fun decoderFor(
            modelKey: String,
            compiledModel: CompiledModel,
            modelConfig: AsrModelConfig,
            tokenizer: Tokenizer,
        ): LiteRtRunner.Decoder = when {
            modelKey.contains("-ctc-") -> CtcDecoder(tokenizer.vocabSize)
            modelKey.contains("-tdt") -> TdtDecoder(compiledModel, modelConfig)
            else -> LiteRtRunner.DefaultDecoder(compiledModel, modelConfig)
        }
    }
}
