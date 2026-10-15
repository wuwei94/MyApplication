package com.example.william.my.module.ml.asr

import com.google.ai.edge.litert.CompiledModel
import com.google.ai.edge.litert.TensorBuffer

/**
 * TDT（Token-and-Duration Transducer）解码（官方 `TdtDecoder` 的等价实现）。
 *
 * 用于 `parakeet-tdt-0.6b-v3` 与 `parakeet-tdt_ctc-0.6b-ja`。与 [LiteRtRunner.DefaultDecoder]
 * （无状态、喂整条 token 历史）相比，它有三个特点：
 * 1. **每个位置同时预测一个 token 和一个时长**，时长决定时间轴前进多少帧，
 *    因此循环变量是「时间下标」而不是「token 下标」。
 * 2. **先无状态、后有状态**：序列开头用 `decode` 签名（无状态）跑满一条定长 token 数组以利用上下文，
 *    数组填满后切到 `decode_1` 签名（有状态），从 LSTM 隐状态续跑。
 * 3. **状态用缓冲区 ping-pong 传递**：`decode_1` 的输入状态就是上一次的输出状态，
 *    直接交换输入/输出缓冲的引用，不做拷贝。
 *
 * 与官方的差异：
 * - 官方把 encoder 的特征维 `NUM_FEATURES` 硬编码成 1024，注释理由是
 *   「CompileModel doesn't expose shape info」。实测 2.3.0 **是**暴露的
 *   （`getInputTensorType(...).layout.dimensions`，官方自己的 `LiteRtRunner` 也在用），
 *   所以这里改成从张量形状读**最后一维**。encoder 输出是 `[1, T, F]`，
 *   末维即特征维，因此 `numElements / F = T` 与官方原式完全一致；只有 layout 缺失时
 *   才回落到常数 1024。
 * - 增加 `close()` 释放自己的输入/输出缓冲（官方从不释放）。
 */
internal class TdtDecoder(
    private val compiledModel: CompiledModel,
    private val modelConfig: AsrModelConfig,
) : LiteRtRunner.Decoder {

    private val inputBuffers: List<TensorBuffer> = compiledModel.createInputBuffers(DECODE_SIGNATURE)
    private val outputBuffers: List<TensorBuffer> = compiledModel.createOutputBuffers(DECODE_SIGNATURE)

    private val encoderOutputType = compiledModel.getInputTensorType(inputBufferName(0), DECODE_SIGNATURE)

    private val numFeatures: Int = encoderOutputType.layout?.dimensions?.lastOrNull() ?: FALLBACK_NUM_FEATURES

    private val maxTimeIndex: Int = encoderOutputType.numElements / numFeatures

    private val numOfTokenIds: Int =
        compiledModel.getInputTensorType(inputBufferName(1), DECODE_SIGNATURE).numElements

    private val numOfLogitsPerToken: Int =
        compiledModel.getOutputTensorType(outputBufferName(0), DECODE_SIGNATURE).numElements /
            numOfTokenIds /
            maxTimeIndex

    /** blank（也是官方约定的起始 token） */
    private val blankTokenId = modelConfig.decodeStartTokenId

    /**
     * `decode_1`（有状态）签名的输入缓冲
     *
     * 并非所有 TDT 模型都带 `decode_1` 签名——官方对缺失的情况降级为纯 `decode` 路径。
     * 这里是一次探测性调用：拿不到 `decode_1` 就视为「模型无该签名」回退，属于正常分支
     * 而非错误，故吞掉异常、置 null；`hasDecode1` 据此决定解码路径。
     * 捕获范围与官方一致（`Exception`）：`createInputBuffers` 对不存在签名的异常类型
     * 未公开约定，收窄反而可能漏掉并误报失败。
     */
    private val inputBuffersOfDecode1: List<TensorBuffer>? =
        try {
            compiledModel.createInputBuffers(DECODE_1_SIGNATURE)
        } catch (e: Exception) {
            null
        }

    private val hasDecode1 = inputBuffersOfDecode1 != null

    private val outputBuffersOfDecode1: List<TensorBuffer>? =
        if (hasDecode1) compiledModel.createOutputBuffers(DECODE_1_SIGNATURE) else null

    /**
     * LSTM 状态的输入/输出缓冲
     *
     * 借用 `decode`（而非 `decode_1`）的第 2/3 号输入与第 1/2 号输出当容器 ——
     * 官方注释说明这是为了在 `decode` 与 `decode_1` 之间切换时免去拷贝和重新赋值。
     */
    private val statesBuffers: List<List<TensorBuffer>> =
        listOf(listOf(inputBuffers[2], inputBuffers[3]), listOf(outputBuffers[1], outputBuffers[2]))

    private var inputStatesBuffersIndex = 0

    private val numOfStates: Int =
        compiledModel.getInputTensorType(inputBufferName(2), DECODE_SIGNATURE).numElements

    override fun close() {
        for (buffer in inputBuffers + outputBuffers) runCatching { buffer.close() }
        inputBuffersOfDecode1?.let { buffers -> for (buffer in buffers) runCatching { buffer.close() } }
        outputBuffersOfDecode1?.let { buffers -> for (buffer in buffers) runCatching { buffer.close() } }
    }

    override fun decode(encodeOutputBuffers: List<TensorBuffer>): Sequence<Pair<Int, Int>> = sequence {
        // 初始状态全零
        val zeroStates = FloatArray(numOfStates)
        statesBuffers[inputStatesBuffersIndex].forEach { it.writeFloat(zeroStates) }

        // 先用无状态签名起步：官方认为序列开头处无状态解码的上下文更完整，质量更好
        var inferenceSignature = DECODE_SIGNATURE
        var numOfInferenceTokenIds = numOfTokenIds
        var inferenceTokenIdsBuffer = inputBuffers[1]
        var inferenceTokenIds = IntArray(numOfTokenIds)
        inferenceTokenIds[0] = modelConfig.decodeStartTokenId
        var inferenceLogitsBuffer = outputBuffers[0]

        var tokenIndex = 0
        var timeIndex = 0
        while (timeIndex < maxTimeIndex) {
            inferenceTokenIdsBuffer.writeInt(inferenceTokenIds)

            compiledModel.run(
                getInputBuffers(encodeOutputBuffers, inferenceTokenIdsBuffer),
                getOutputBuffers(inferenceLogitsBuffer),
                inferenceSignature,
            )

            val logits = inferenceLogitsBuffer.readFloat()
            val startIndexOfTimeIndex = timeIndex * numOfInferenceTokenIds * numOfLogitsPerToken
            val startIndexOfTokenId = startIndexOfTimeIndex + tokenIndex * numOfLogitsPerToken
            val endIndexOfDuration = startIndexOfTimeIndex + (tokenIndex + 1) * numOfLogitsPerToken
            // 每个位置的 logits = 词表部分 + 时长部分，时长占最后 NUM_DURATIONS 个
            val endIndexOfTokenId = endIndexOfDuration - NUM_DURATIONS

            val tokenId =
                (
                    (startIndexOfTokenId until endIndexOfTokenId).maxByOrNull { logits[it] }
                        ?: startIndexOfTokenId
                    ) - startIndexOfTokenId
            if (tokenId != blankTokenId) {
                yield(Pair(tokenId, timeIndex))
                if (numOfInferenceTokenIds > 1) {
                    tokenIndex++
                    if (tokenIndex >= numOfInferenceTokenIds) {
                        if (hasDecode1) {
                            // 无状态 token 数组已满 → 切到有状态解码，从 LSTM 隐状态续跑
                            inferenceSignature = DECODE_1_SIGNATURE
                            numOfInferenceTokenIds = 1
                            inferenceTokenIdsBuffer = requireNotNull(inputBuffersOfDecode1)[1]
                            inferenceTokenIds = IntArray(1)
                            inferenceLogitsBuffer = requireNotNull(outputBuffersOfDecode1)[0]
                            tokenIndex = 0
                        } else {
                            break
                        }
                    }
                }
                inferenceTokenIds[tokenIndex] = tokenId
            }

            val duration =
                (
                    (endIndexOfTokenId until endIndexOfDuration).maxByOrNull { logits[it] }
                        ?: endIndexOfTokenId
                    ) - endIndexOfTokenId
            // blank 且时长为 0 时必须至少前进一帧，否则死循环
            timeIndex += if (duration == 0 && tokenId == blankTokenId) 1 else duration

            if (numOfInferenceTokenIds == 1 && tokenId != blankTokenId) {
                // 有状态 RNN 解码器：只在**非 blank** 时接续新状态。
                // 官方注释：RNN-T 贪心解码里预测网络只在吐字时前进，blank 步接续状态
                // 等于把上一个 token 重吃一遍，会逐帧劣化转写结果。
                inputStatesBuffersIndex = 1 - inputStatesBuffersIndex
            }
        }

        yield(Pair(SpeechRecognizer.END_OF_SEQUENCE, maxTimeIndex))
    }

    private fun getInputBuffers(
        encodeOutputBuffers: List<TensorBuffer>,
        tokenIdsBuffer: TensorBuffer,
    ) = buildList {
        addAll(encodeOutputBuffers)
        add(tokenIdsBuffer)
        addAll(statesBuffers[inputStatesBuffersIndex])
    }

    private fun getOutputBuffers(logitsBuffer: TensorBuffer) = listOf(logitsBuffer) + statesBuffers[1 - inputStatesBuffersIndex]

    private companion object {
        const val DECODE_SIGNATURE = LiteRtRunner.DECODE_SIGNATURE
        const val DECODE_1_SIGNATURE = "decode_1"

        /** TDT 的时长分箱数（模型规格） */
        const val NUM_DURATIONS = 5

        /** 仅在张量 layout 缺失时使用的兜底特征维（Parakeet 系列即 1024） */
        const val FALLBACK_NUM_FEATURES = 1024

        private fun inputBufferName(index: Int) = LiteRtRunner.inputBufferName(index)

        private fun outputBufferName(index: Int) = LiteRtRunner.outputBufferName(index)
    }
}
