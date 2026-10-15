/*
 * Copyright 2026 The Google AI Edge Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.william.my.module.ml.zsc

import android.content.Context
import com.example.william.my.module.ml.download.ModelStore
import com.example.william.my.module.ml.helper.TFLiteModelHelper
import com.google.ai.edge.litert.Accelerator
import com.google.ai.edge.litert.CompiledModel
import com.google.ai.edge.litert.Environment
import com.google.ai.edge.litert.TensorBuffer
import java.io.Closeable
import java.io.File

/** 单行多语 Laya 执行；GPU 显式请求 FP32，且不回退 CPU。 */
class LayaEngine(context: Context, val storage: Storage = Storage.WFP16) : Closeable {
    /** 存储只改图权重；两种情况下 GPU 运算都显式保持 FP32。 */
    enum class Storage(val argument: String) {
        WFP16("wfp16"),
        FP32("fp32"),
        ;

        companion object {
            /** 解析受支持的意图选择器，拒绝未知值。 */
            fun fromArgument(value: String): Storage = when (value.lowercase()) {
                "wfp16" -> WFP16
                "fp32" -> FP32
                else -> error("Unknown graph storage: $value; expected wfp16 or fp32")
            }
        }
    }

    /** 显式选择加速器；GPU 创建绝不静默回退。 */
    enum class Backend(val accelerator: Accelerator) {
        GPU(Accelerator.GPU),
        CPU(Accelerator.CPU),
        ;

        companion object {
            /** 解析受支持的意图选择器，拒绝未知值。 */
            fun fromArgument(value: String): Backend = when (value.lowercase()) {
                "gpu" -> GPU
                "cpu" -> CPU
                else -> error("Unknown accelerator: $value; expected gpu or cpu")
            }
        }
    }

    /** 计入输出回读，因为 GPU run() 可能异步入队。 */
    data class GraphTiming(val writeMs: Double, val enqueueMs: Double, val readbackMs: Double) {
        /** 写入、入队与回读的总耗时（毫秒）。 */
        val totalMs: Double
            get() = writeMs + enqueueMs + readbackMs

        /** 输出具名的计时分量，供机器可读的门禁报告使用。 */
        fun toMap(): Map<String, Double> = linkedMapOf(
            "write_ms" to writeMs,
            "run_enqueue_ms" to enqueueMs,
            "readback_ms" to readbackMs,
            "write_run_read_ms" to totalMs,
        )
    }

    /** 一题行的原始模型张量与宿主/图计时。 */
    data class RawResult(
        val tokenLogits: FloatArray,
        val pooledCls: FloatArray,
        val markerLogits: FloatArray,
        val actFeatures: FloatArray,
        val actLogits: FloatArray,
        val mainTiming: GraphTiming,
        val actTiming: GraphTiming?,
        val embeddingLookupMs: Double,
        val featureMs: Double,
        val callMs: Double,
    ) {
        /** 展示解码答案前先拒绝任何 NaN 或无穷。 */
        val finite: Boolean
            get() =
                tokenLogits.all { it.isFinite() } &&
                    pooledCls.all { it.isFinite() } &&
                    markerLogits.all { it.isFinite() } &&
                    actFeatures.all { it.isFinite() } &&
                    actLogits.all { it.isFinite() }

        /** 主图与动作图耗时之和，不含独立的嵌入查找。 */
        val graphMs: Double
            get() = mainTiming.totalMs + (actTiming?.totalMs ?: 0.0)
    }

    /** 标定后的答案，连同其构建的序列与完整的逐题计时。 */
    data class AnswerResult(
        val sequence: LayaSequence,
        val answer: Map<String, Any?>,
        val raw: RawResult,
        val prepareMs: Double,
        val decodeMs: Double,
        /** 写入、入队与回读的总耗时（毫秒）。 */
        val totalMs: Double,
    )

    private data class Key(val window: Int, val backend: Backend, val storage: Storage)

    private class Graph(
        val model: CompiledModel,
        val inputs: Map<String, TensorBuffer>,
        val outputs: Map<String, TensorBuffer>,
    ) : Closeable {
        override fun close() {
            try {
                (inputs.values + outputs.values).forEach { it.close() }
            } finally {
                model.close()
            }
        }
    }

    /**
     * 模型目录：与其余新增任务一致，走运行时下载缓存（`filesDir/models/`）
     */
    private val filesDir = ModelStore.dir(context.applicationContext)

    /**
     * 原生运行环境
     *
     * 官方用「一个常驻线程 + 单一 Environment」承载全部图；本模块的串行性由调用方的
     * 引擎队列保证（见 [LayaProcessRuntime]），因此这里只需持有 Environment，
     * 并在 [close] 时显式释放——`Environment` 同样是 `JniHandle`，漏关会泄漏到进程结束。
     */
    private val environment: Environment = Environment.create()

    private val mainGraphs = linkedMapOf<Key, Graph>()
    private val actGraphs = linkedMapOf<Backend, Graph>()
    private val embeddingInputs = linkedMapOf<Int, FloatArray>()
    private val embeddings: LayaEmbeddings
    private val tokenizer: LayaTokenizer

    /** 交互式标定解码器使用的已安装温度。 */
    val calibration: LayaCalibration

    /** 分词器初始化耗时（毫秒）。 */
    val tokenizerLoadMs: Double

    /** BPE 词表条目数；仅用于状态栏展示，不参与推理。 */
    val tokenizerSize: Int
        get() = tokenizer.vocabularySize

    /** 元数据解析与表映射耗时（毫秒）。 */
    val embeddingLoadMs: Double

    /** 记录在每次设备门禁报告中的元数据哈希。 */
    val embeddingTableSha256: String
        get() = embeddings.sha256

    private var closed = false

    init {
        REQUIRED_FILES.forEach { requireFile(it) }
        val started = System.nanoTime()
        tokenizer = LayaTokenizer(requireFile("tokenizer.json"))
        tokenizerLoadMs = milliseconds(System.nanoTime() - started)
        calibration = LayaCalibration.load(requireFile("laya_ml_calibration.json"))
        val embeddingStarted = System.nanoTime()
        embeddings =
            LayaEmbeddings(requireFile("token_embeddings_fp16.bin"), requireFile("token_embeddings.json"))
        embeddingLoadMs = milliseconds(System.nanoTime() - embeddingStarted)
    }

    /**
     * 与计时调用分开编译，这样首次图调用能保持「冷启动」观测。
     */
    fun initialize(backend: Backend, window: Int = 256) = LayaProcessRuntime.call {
        checkOpen()
        requireWindow(window)
        mainGraph(window, backend)
        actGraph(backend)
        Unit
    }

    /** 门禁与交互 App 使用完全相同的分词器与 prompt 构建器。 */
    fun prepare(
        state: Any?,
        question: Map<String, Any?>,
        window: Int = 256,
        questionId: String = "",
    ): LayaSequence {
        checkOpen()
        requireWindow(window)
        return LayaPromptBuilder(tokenizer, maxLen = window, headMaxLen = 256)
            .build(state, question, questionId)
    }

    /**
     * 一次主图调用 + 一次动作头调用，均含回读。具名签名缓冲避免这些工件中
     * 「签名顺序」与「FlatBuffer 张量顺序」不一致的问题。
     * 非有限的主图输出会保留在结果中，且绝不送入动作头。
     */
    fun runRaw(sequence: LayaSequence, backend: Backend, window: Int = 256): RawResult = LayaProcessRuntime.call {
        checkOpen()
        requireWindow(window)
        require(sequence.ids.size <= window) { "Sequence exceeds window $window" }
        require(sequence.markers.size == sequence.question.optionCount) { "Missing option marker" }
        val main = mainGraph(window, backend)
        val act = actGraph(backend)
        val attention = FloatArray(window) { if (it < sequence.ids.size) 1f else 0f }
        val qtype = FloatArray(3).also { it[sequence.question.qtype] = 1f }

        val lookupStarted = System.nanoTime()
        val embeds = embeddingInputs.getOrPut(window) { FloatArray(window * LayaEmbeddings.WIDTH) }
        embeddings.gather(sequence.ids, window, embeds)
        val started = System.nanoTime()
        val embeddingLookupMs = milliseconds(started - lookupStarted)
        main.inputs.getValue("inputs_embeds").writeFloat(embeds)
        main.inputs.getValue("attention_mask").writeFloat(attention)
        main.inputs.getValue("qtype_onehot").writeFloat(qtype)
        val written = System.nanoTime()
        main.model.run(main.inputs, main.outputs, SIGNATURE)
        val enqueued = System.nanoTime()
        val tokens = main.outputs.getValue("token_logits").readFloat()
        val pooled = main.outputs.getValue("pooled_cls").readFloat()
        val read = System.nanoTime()
        require(tokens.size == window && pooled.size == 768) { "Unexpected main graph output shape" }
        val mainTiming = timing(started, written, enqueued, read)
        val markers = LayaDecoder.gather(tokens, sequence.markers)
        if (!tokens.all { it.isFinite() } || !pooled.all { it.isFinite() }) {
            return@call RawResult(
                tokens,
                pooled,
                markers,
                FloatArray(4) { Float.NaN },
                FloatArray(2) { Float.NaN },
                mainTiming,
                null,
                embeddingLookupMs,
                milliseconds(System.nanoTime() - read),
                milliseconds(System.nanoTime() - lookupStarted),
            )
        }
        val features = LayaDecoder.actFeatures(markers)
        val featured = System.nanoTime()
        act.inputs.getValue("pooled_cls").writeFloat(pooled)
        act.inputs.getValue("feats").writeFloat(features)
        val actWritten = System.nanoTime()
        act.model.run(act.inputs, act.outputs, SIGNATURE)
        val actEnqueued = System.nanoTime()
        val action = act.outputs.getValue("act_logits").readFloat()
        val finished = System.nanoTime()
        require(action.size == 2) { "Unexpected action graph output shape" }
        RawResult(
            tokens,
            pooled,
            markers,
            features,
            action,
            mainTiming,
            timing(featured, actWritten, actEnqueued, finished),
            embeddingLookupMs,
            milliseconds(featured - read),
            milliseconds(finished - lookupStarted),
        )
    }

    /** 运行构建器、两张图与标定解码，并分别记录各阶段耗时。 */
    fun answer(
        state: Any?,
        question: Map<String, Any?>,
        backend: Backend,
        questionId: String = "",
        window: Int = 256,
    ): AnswerResult {
        val started = System.nanoTime()
        val sequence = prepare(state, question, window, questionId)
        val prepared = System.nanoTime()
        val raw = runRaw(sequence, backend, window)
        check(raw.finite) {
            "Nonfinite model output on ${backend.name}; inspect the device gate report"
        }
        val decodeStarted = System.nanoTime()
        val decoded =
            LayaDecoder.decode(raw.markerLogits, raw.actLogits, sequence.question, calibration)
        val finished = System.nanoTime()
        return AnswerResult(
            sequence,
            decoded,
            raw,
            milliseconds(prepared - started),
            milliseconds(finished - decodeStarted),
            milliseconds(finished - started),
        )
    }

    private fun mainGraph(window: Int, backend: Backend): Graph = mainGraphs.getOrPut(Key(window, backend, storage)) {
        createGraph(
            mainFilename(window),
            backend,
            listOf("attention_mask", "inputs_embeds", "qtype_onehot"),
            listOf("pooled_cls", "token_logits"),
        )
    }

    private fun actGraph(backend: Backend): Graph = actGraphs.getOrPut(backend) {
        createGraph(
            "laya_ml_act_head_fp32.tflite",
            backend,
            listOf("feats", "pooled_cls"),
            listOf("act_logits"),
        )
    }

    private fun createGraph(
        filename: String,
        backend: Backend,
        inputNames: List<String>,
        outputNames: List<String>,
    ): Graph {
        // 后端与精度统一由 helper 收口。Laya 主图要求 GPU 显式 FP32（官方样例明确指定），
        // 这属于**模型规格**而非性能调优，所以在这里显式声明而不是用默认精度。
        val options =
            when (backend) {
                Backend.GPU -> TFLiteModelHelper.createGpuOptions(CompiledModel.GpuOptions.Precision.FP32)
                Backend.CPU -> TFLiteModelHelper.createCpuOptions()
            }
        val model =
            CompiledModel.create(
                requireFile(filename).absolutePath,
                options,
                environment,
            )
        val inputs = linkedMapOf<String, TensorBuffer>()
        val outputs = linkedMapOf<String, TensorBuffer>()
        try {
            inputNames.forEach { inputs[it] = model.createInputBuffer(it, SIGNATURE) }
            outputNames.forEach { outputs[it] = model.createOutputBuffer(it, SIGNATURE) }
            return Graph(model, inputs, outputs)
        } catch (failure: Throwable) {
            (inputs.values + outputs.values).forEach { it.close() }
            model.close()
            throw failure
        }
    }

    private fun requireFile(name: String): File = File(filesDir, name).also {
        check(it.isFile) { "模型尚未下载: $name" }
    }

    private fun checkOpen() = check(!closed) { "LayaEngine is closed" }

    /** Resolves the selected fixed-window storage variant without changing GPU precision. */
    fun mainFilename(window: Int): String = "laya_ml_s${window}_embeds_${storage.argument}.tflite"

    /** 在原生运行时线程上关闭模型缓冲并释放表引用。 */
    override fun close() = LayaProcessRuntime.call {
        if (!closed) {
            closed = true
            try {
                (mainGraphs.values + actGraphs.values).forEach { it.close() }
            } finally {
                mainGraphs.clear()
                actGraphs.clear()
                embeddingInputs.clear()
                embeddings.close()
                environment.close()
            }
        }
    }

    companion object {
        private const val SIGNATURE = "serving_default"

        /** 无论图存哪种方式都需要的共享文件 */
        val REQUIRED_FILES =
            listOf(
                "laya_ml_act_head_fp32.tflite",
                "laya_ml_calibration.json",
                "tokenizer.json",
                "token_embeddings_fp16.bin",
                "token_embeddings.json",
            )

        private fun requireWindow(window: Int) = require(window == 256 || window == 512) {
            "Only multilingual windows 256 and 512 are supported"
        }

        private fun milliseconds(nanos: Long) = nanos / 1_000_000.0

        private fun timing(start: Long, written: Long, enqueued: Long, read: Long) = GraphTiming(
            milliseconds(written - start),
            milliseconds(enqueued - written),
            milliseconds(read - enqueued),
        )
    }
}

/**
 * 原生调用的串行化约定
 *
 * 官方在这里维护「一个常驻线程 + 单一 Environment」，以保证同一时刻只有一次原生调用。
 * 本模块的串行性改由**调用方的引擎队列**保证——`createEngineDispatcher()` 返回
 * `limitedParallelism(1)`，页面在其上调用 [LayaEngine] 的全部方法，因此这里不再自建线程池，
 * 直接在调用线程执行即可：少一条独立队列，也让「推理 / 构建 / 关闭共用同一条队列」
 * 这条模块纪律继续成立。
 */
private object LayaProcessRuntime {
    fun <T> call(block: () -> T): T = block()
}
