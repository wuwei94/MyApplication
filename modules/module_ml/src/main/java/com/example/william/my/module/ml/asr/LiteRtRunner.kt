package com.example.william.my.module.ml.asr

import com.google.ai.edge.litert.CompiledModel
import com.google.ai.edge.litert.TensorBuffer
import com.google.ai.edge.litert.TensorType
import java.io.Closeable

/** 张量元素个数（官方 `TensorType.numElements` 的等价形式；`internal` 因为 [TdtDecoder] 也要用） */
internal val TensorType.numElements: Int
    get() = requireNotNull(layout) { "张量缺少 layout，无法推导元素个数" }.dimensions.fold(1, Int::times)

/**
 * 跑 ASR 主图并产出 token 序列（官方 `LiteRtRunner` 的等价实现）。
 *
 * 结构上是一条**两段式**流水：先用 `encode` 签名把音频特征编码成隐状态，
 * 再由 [Decoder] 按各自的策略逐 token 解码。除了 Parakeet-CTC 之外，
 * 官方支持的所有模型都是这种「encoder + decoder 多子图」形态，
 * 因此这里有两处刻意的**零拷贝**：解码阶段的输入列表直接把 encoder 的输出缓冲
 * 拼在前面，靠 `TensorBuffer` 不绑定创建它的模型这一性质在子图之间传数据。
 *
 * 与官方的差异（都记在各自的类 KDoc 里）：
 * 1. 模型由外部构建后传入，不在这里 new —— 本模块要求引擎构建统一走后台单并发队列
 *    （`helper/TFLiteLifecycleSupport.kt`），页面侧才持有生命周期。
 * 2. 去掉 NPU 分支（官方默认也把 NPU 运行时库注释掉了，见 [AsrModelConfig] 的说明）。
 * 3. [Decoder] 增加 `close()`：官方从不释放解码器的输入/输出缓冲，
 *    而本模块的纪律是「漏关即泄漏」。
 */
internal class LiteRtRunner(
    private val compiledModel: CompiledModel,
    private val modelConfig: AsrModelConfig,
    decoderFactory: (CompiledModel, AsrModelConfig) -> Decoder,
) : SpeechRecognizer {

    /** 把 encoder 的输出缓冲解成 `(tokenId, 相对时间戳)` 序列 */
    interface Decoder : Closeable {
        fun decode(encodeOutputBuffers: List<TensorBuffer>): Sequence<Pair<Int, Int>>
    }

    private val decoder: Decoder = decoderFactory(compiledModel, modelConfig)
    private val inputBuffers: List<TensorBuffer> = compiledModel.createInputBuffers()
    private val outputBuffers: List<TensorBuffer> = compiledModel.createOutputBuffers()

    private val expectedNumOfFloats: Int =
        compiledModel.getInputTensorType(inputBufferName(0), ENCODE_SIGNATURE).numElements

    override fun close() {
        for (buffer in inputBuffers + outputBuffers) runCatching { buffer.close() }
        runCatching { decoder.close() }
        runCatching { compiledModel.close() }
    }

    override fun recognize(features: FloatArray): Sequence<Pair<Int, Int>> {
        inputBuffers[0].writeFloat(padOrTruncateFeatures(features, expectedNumOfFloats))
        compiledModel.run(inputBuffers, outputBuffers)
        return decoder.decode(outputBuffers)
    }

    /**
     * 默认解码器：自回归地喂 token、逐位置取 argmax。
     *
     * 覆盖 Moonshine / Whisper / Qwen3-ASR —— 它们的解码都是**无状态**的
     * （每一步把「迄今所有 token」整条喂回去），靠因果掩码保证不看到未来。
     */
    class DefaultDecoder(
        private val compiledModel: CompiledModel,
        private val modelConfig: AsrModelConfig,
    ) : Decoder {

        private val inputBuffers: List<TensorBuffer> = compiledModel.createInputBuffers(DECODE_SIGNATURE)
        private val tokenIdsBuffer: TensorBuffer = inputBuffers[inputBuffers.size - 2]
        private val maskBuffer: TensorBuffer = inputBuffers[inputBuffers.size - 1]
        private val outputBuffers: List<TensorBuffer> = compiledModel.createOutputBuffers(DECODE_SIGNATURE)

        private val numOfTokenIds: Int =
            compiledModel.getInputTensorType(inputBufferName(inputBuffers.size - 2), DECODE_SIGNATURE).numElements

        private val numOfLogitsPerToken: Int =
            compiledModel.getOutputTensorType(outputBufferName(0), DECODE_SIGNATURE).numElements / numOfTokenIds

        /**
         * 下三角因果掩码
         *
         * 张量是方阵、边长由元素个数开方推得（官方同样如此，因为模型侧不暴露 shape）。
         */
        private val causalMask: FloatArray = run {
            val maskName = inputBufferName(inputBuffers.size - 1)
            val numOfMaskEntries = compiledModel.getInputTensorType(maskName, DECODE_SIGNATURE).numElements
            val n = kotlin.math.sqrt(numOfMaskEntries.toDouble()).toInt()
            FloatArray(numOfMaskEntries) { MASKED_OUT_FLOAT_VALUE }.also { mask ->
                for (r in 0 until n) {
                    for (c in 0..r) {
                        mask[r * n + c] = MASKED_IN_FLOAT_VALUE
                    }
                }
            }
        }

        override fun close() {
            for (buffer in inputBuffers + outputBuffers) runCatching { buffer.close() }
        }

        override fun decode(encodeOutputBuffers: List<TensorBuffer>): Sequence<Pair<Int, Int>> = sequence {
            val decodeInputBuffers = buildList {
                addAll(encodeOutputBuffers)
                add(tokenIdsBuffer)
                add(maskBuffer)
            }
            maskBuffer.writeFloat(causalMask)

            val tokenIds = IntArray(numOfTokenIds)
            tokenIds[0] = modelConfig.decodeStartTokenId

            // Qwen3-ASR 会在正式文本之前吐一段引导标记，官方用「先跳过直到某个 token」处理
            var seenSkipUntilTokenId = modelConfig.decodeSkipUntilTokenId < 0
            for (i in 0 until numOfTokenIds - 1) {
                tokenIdsBuffer.writeInt(tokenIds)
                compiledModel.run(decodeInputBuffers, outputBuffers, DECODE_SIGNATURE)

                val logits = outputBuffers[0].readFloat()
                val startIndex = i * numOfLogitsPerToken
                val endIndex = (i + 1) * numOfLogitsPerToken
                val tokenId =
                    ((startIndex until endIndex).maxByOrNull { logits[it] } ?: startIndex) - startIndex
                if (tokenId == modelConfig.decodeStopTokenId) break

                if (seenSkipUntilTokenId) {
                    yield(Pair(tokenId, SpeechRecognizer.NO_TIMESTAMP))
                } else if (tokenId == modelConfig.decodeSkipUntilTokenId) {
                    seenSkipUntilTokenId = true
                }

                tokenIds[i + 1] = tokenId
            }

            yield(Pair(SpeechRecognizer.END_OF_SEQUENCE, SpeechRecognizer.NO_TIMESTAMP))
        }
    }

    companion object {

        internal const val ENCODE_SIGNATURE = "encode"
        internal const val DECODE_SIGNATURE = "decode"

        internal const val MASKED_IN_FLOAT_VALUE = 0.0f
        internal const val MASKED_OUT_FLOAT_VALUE = -0.7f * Float.MAX_VALUE

        /** 张量名约定：`args_0` / `args_1` / …（官方同样按序号拼名字，而不是用真实张量名） */
        internal fun inputBufferName(index: Int) = "args_$index"

        internal fun outputBufferName(index: Int) = "output_$index"

        /**
         * 把特征对齐到模型要求的定长
         *
         * 短了补零（当静音），长了**留尾部** —— 官方注释说明尾部对应最近的音频，
         * 丢掉开头的老数据比丢掉新的更合理。
         */
        internal fun padOrTruncateFeatures(features: FloatArray, expectedNumOfFloats: Int): FloatArray = when {
            features.size == expectedNumOfFloats -> features
            features.size < expectedNumOfFloats -> features.copyOf(expectedNumOfFloats)
            else -> features.sliceArray((features.size - expectedNumOfFloats) until features.size)
        }
    }
}
