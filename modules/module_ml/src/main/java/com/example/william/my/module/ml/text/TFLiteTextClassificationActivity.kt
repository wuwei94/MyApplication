package com.example.william.my.module.ml.text

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.ui.activity.BaseVBActivity
import com.example.william.my.module.ml.databinding.MlActivityTextClassificationBinding
import com.example.william.my.module.ml.helper.TFLiteModelHelper
import com.example.william.my.module.ml.helper.buildOffMainThreadSafely
import com.example.william.my.module.ml.helper.canTouchUi
import com.example.william.my.module.ml.helper.closeEngineOn
import com.example.william.my.module.ml.helper.createEngineDispatcher
import com.google.ai.edge.litert.CompiledModel
import com.google.ai.edge.litert.TensorBuffer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.system.measureNanoTime

/**
 * LiteRT — 英文文本情感分类（平均词向量 / MobileBERT）
 *
 * 演示自然语言模态的端侧推理链路：文本 → 词表查表分词 → 定长整型输入张量 →
 * 二分类概率。推理侧走 LiteRT 2.x 的 CompiledModel API。
 *
 * 核心机制与避坑点：
 * 1. 词表来自模型内嵌 metadata（`vocab.txt`），且**两个模型的词表格式不同**：
 *    BERT 是单列裸词（行号即 id），平均词向量是双列 `token id`（取第二列的 id）；
 *    统一交给 `TFLiteModelHelper.buildVocabulary` 解析。
 *    注意**不能过滤空行** —— 单列格式下行号参与 id 计算，过滤会让后续 id 整体错位
 * 2. 两个模型输入签名不同：平均词向量是单输入 `[1, 256]`，MobileBERT 是三输入 `[1, 128]`；
 *    **以模型自身声明的顺序为准**：`input_word_ids` → `input_type_ids` → `input_mask`
 *    （官方样例把 mask 写进 `[1]`，是因为它配的是另一个模型文件），
 *    其余输入必须显式写入，否则原生侧读到的是未初始化内存
 * 3. 输入张量元素类型是 INT32，用 `TensorBuffer.writeInt` 写入而不是浮点
 * 4. 输出 `[1, 2]` 为 FP32 概率（负面 / 正面），无需反量化与 Softmax
 * 5. 切换模型走同一条引擎串行队列，先取消上一次未完成的重建，避免并发改写引擎字段；
 *    序列长度与模型一起打包成 [TextEngine]，**不从 UI 开关现读**，
 *    否则切换窗口内会拿新模型的长度去写旧模型的输入缓冲区
 *
 * 官方样例：https://github.com/google-ai-edge/litert-samples/tree/main/samples/litert/text_classification
 */
@Route(path = RouterPath.Ml.TextClassification)
class TFLiteTextClassificationActivity :
    BaseVBActivity<MlActivityTextClassificationBinding>(),
    View.OnClickListener {

    /** 当前引擎：模型 + 与模型规格绑定的序列长度；推理线程与主线程共享，必须 volatile */
    @Volatile
    private var engine: TextEngine? = null

    @Volatile
    private var vocab: Map<String, Int> = emptyMap()

    /** 上一次未完成的重建句柄：切换模型时先取消 */
    private var rebuildJob: Job? = null

    /**
     * 分词、推理与引擎重建共用的串行队列
     *
     * CompiledModel 不是线程安全的：同一实例并发 `run()` 会踩坏内部状态，
     * 引擎重建与 `run()` 并发更会释放掉它正在用的原生 handle。
     */
    private val engineDispatcher = createEngineDispatcher()

    override fun getViewBinding(): MlActivityTextClassificationBinding = MlActivityTextClassificationBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        binding.btnClassify.setOnClickListener(this)
        binding.btnPickSample.setOnClickListener(this)

        binding.switchModel.setOnCheckedChangeListener { _, isChecked ->
            rebuildModel(mobileBert = isChecked)
        }

        rebuildModel(mobileBert = binding.switchModel.isChecked)
    }

    /**
     * 按当前模型选择重建引擎，并把词表随模型一起换掉
     *
     * 两个模型的词表大小与序列长度都不同，必须与引擎同一步切换，
     * 否则会用 A 模型的词表去编码 B 模型的输入。
     */
    private fun rebuildModel(mobileBert: Boolean) {
        rebuildJob?.cancel()
        rebuildJob = lifecycleScope.launch {
            val fileName = if (mobileBert) MOBILE_BERT else WORD_VEC
            val sequenceLength = if (mobileBert) BERT_SEQUENCE_LENGTH else WORD_VEC_SEQUENCE_LENGTH
            try {
                val lines = withContext(Dispatchers.IO) {
                    TFLiteModelHelper.loadMetadataLines(
                        this@TFLiteTextClassificationActivity,
                        fileName,
                        VOCAB_FILE,
                    )
                }
                // 两个模型的词表格式不同（单列 / 双列），统一由 helper 的纯函数判别与解析
                val tokenizer = TFLiteModelHelper.buildVocabulary(lines)

                // 构建排在引擎队列上；若期间页面退出，已建好的实例由 helper 就地释放
                val built = buildOffMainThreadSafely(engineDispatcher) {
                    TextEngine(
                        model = TFLiteModelHelper.createModel(
                            assets,
                            fileName,
                            TFLiteModelHelper.createCpuOptions(),
                        ),
                        // 序列长度与模型同一步产出：不能让页面在切换窗口内自己推断
                        sequenceLength = sequenceLength,
                    )
                }

                // 读取与赋值之间没有挂起点，previous 必然就是此刻被替换掉的那个实例
                val previous = engine
                engine = built
                vocab = tokenizer

                // 旧引擎的关闭排进同一条串行队列，并用 NonCancellable 兜住，避免被取消时漏关
                if (previous != null) {
                    withContext(NonCancellable + engineDispatcher) {
                        previous.close()
                    }
                }

                if (canTouchUi) {
                    val type = if (mobileBert) "MobileBERT" else "平均词向量"
                    binding.tvStatus.text =
                        "推理引擎: CompiledModel / CPU (${TFLiteModelHelper.CPU_THREADS} 线程 + XNNPACK) | " +
                        "模型: $type | 词表 ${tokenizer.size} 项"
                }
            } catch (e: CancellationException) {
                // 被后一次切换取代属于正常流程：原样抛出，既不吞掉取消，也不误报「切换失败」
                throw e
            } catch (e: Exception) {
                if (canTouchUi) {
                    binding.tvStatus.text = "切换模型失败: ${e.message}"
                }
            }
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.btnClassify -> runClassification()
            binding.btnPickSample -> binding.etInput.setText(POSITIVE_SAMPLE)
        }
    }

    private fun runClassification() {
        val text = binding.etInput.text?.toString().orEmpty().trim()
        if (text.isEmpty()) {
            binding.tvResult.text = "分类结果: 请先输入一句英文"
            return
        }

        lifecycleScope.launch {
            try {
                val result = withContext(engineDispatcher) {
                    val active = engine ?: return@withContext null
                    // 序列长度取自引擎自身而不是 UI 开关：切换模型的过程中两者会短暂不一致
                    val sequenceLength = active.sequenceLength
                    val localModel = active.model

                    var tokenIds: IntArray
                    val tokenizeTime = measureNanoTime {
                        tokenIds = tokenize(text, sequenceLength)
                    }

                    var inputBuffers: List<TensorBuffer>? = null
                    var outputBuffers: List<TensorBuffer>? = null
                    try {
                        inputBuffers = localModel.createInputBuffers()
                        outputBuffers = localModel.createOutputBuffers()
                        // 上两步任一失败都不会走到这里，因此此处引用必然非空
                        val inputs = inputBuffers
                        val outputs = outputBuffers
                        // 写入顺序必须与**模型自身声明的输入顺序**一致。实测 bert_classifier.tflite 为
                        //   [0] input_word_ids → [1] input_type_ids → [2] input_mask
                        // （官方样例把 mask 写进 [1]、segment_ids 写进 [2]，是因为它配的是
                        //   mobile_bert.tflite —— 换成别的模型文件后顺序会变，必须按实测来）
                        inputs[0].writeInt(tokenIds)

                        // [1] input_type_ids：单句分类恒为全 0
                        if (inputs.size > 1) {
                            inputs[1].writeInt(IntArray(sequenceLength))
                        }
                        // [2] input_mask：非 padding 位置为 1；
                        // 不显式写入的话原生侧读到的是未初始化内存，推理结果不可预期
                        if (inputs.size > 2) {
                            inputs[2].writeInt(
                                IntArray(sequenceLength) { if (it < tokenIds.size && tokenIds[it] != PAD_TOKEN_ID) 1 else 0 },
                            )
                        }

                        var probabilities: FloatArray
                        val inferenceTime = measureNanoTime {
                            localModel.run(inputs, outputs)
                            probabilities = outputs[0].readFloat()
                        }

                        Result(active, probabilities, tokenizeTime / 1_000_000.0, inferenceTime / 1_000_000.0)
                    } finally {
                        outputBuffers?.forEach { runCatching { it.close() } }
                        inputBuffers?.forEach { runCatching { it.close() } }
                    }
                } ?: return@launch

                if (!canTouchUi) return@launch
                // 用户可能已在推理期间切换模型：结果必须对应发起时的引擎，否则会展示上一模型的结论
                if (result.engine !== engine) return@launch
                val probabilities = result.probabilities
                val tokenizeMs = result.tokenizeMs
                val inferenceMs = result.inferenceMs

                val top = probabilities.indices.maxByOrNull { probabilities[it] } ?: 0
                val label = LABELS.getOrElse(top) { "类别 #$top" }
                val confidence = probabilities.getOrElse(top) { 0f } * 100

                binding.tvResult.text = "分类结果: $label (置信度: ${"%.1f".format(confidence)}%)"
                binding.tvTimeDetails.text =
                    "分词耗时: ${"%.2f".format(tokenizeMs)} ms | 模型推理耗时: ${"%.2f".format(inferenceMs)} ms"

                val sb = StringBuilder()
                probabilities.forEachIndexed { index, prob ->
                    val name = LABELS.getOrElse(index) { "类别 #$index" }
                    val percent = "%.1f".format(prob * 100).padStart(5, ' ')
                    val barLength = (prob * 20).toInt().coerceIn(0, 20)
                    val bar = "█".repeat(barLength).padEnd(20, '░')
                    val isTop = if (index == top) " 👈 (Top)" else ""
                    sb.append("$name: $bar $percent%$isTop\n")
                }
                binding.tvDistribution.text = sb.toString().trimEnd()
            } catch (e: Exception) {
                if (canTouchUi) {
                    binding.tvResult.text = "文本分类异常: ${e.message}"
                }
            }
        }
    }

    /**
     * 分词：去标点 → 空格切词 → 查词表，未登录词统一回落到 id 0
     *
     * 序列按模型长度定长补齐，空位填 [PAD]（id 为 0）。
     * BERT 词表里 `[PAD]` 是 0 而 `[UNK]` 是 100 —— 这里刻意统一回落 0，而不是 [UNK]：
     * 下方 input_mask 的判定就是 `id != 0`，回落到 0 才能让「OOV 词」与「padding」
     * 在掩码上表现一致，被屏蔽的位置不会再参与注意力。
     * 官方样例是不写入 OOV 词、让后续 token 左移，效果等价但会打乱原词序。
     */
    private fun tokenize(text: String, sequenceLength: Int): IntArray {
        val cleaned = text.replace(PUNCTUATION_REGEX, " ")
        val tokens = cleaned.split(" ").filter { it.isNotBlank() }
        return IntArray(sequenceLength) { index ->
            if (index >= tokens.size) {
                PAD_TOKEN_ID
            } else {
                vocab[tokens[index].lowercase()] ?: PAD_TOKEN_ID
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 引擎关闭排进与推理同一条队列：此刻 lifecycleScope 已取消，
        // 但后台 run() 不可中断仍会跑完，直接 close 会释放掉它正在用的原生 handle。
        // 统一走 closeEngineOn：它另起独立作用域派发，并逐个资源吞掉清理异常。
        val closingEngine = engine
        engine = null
        closeEngineOn(engineDispatcher, closingEngine)
    }

    /**
     * 一次引擎构建的产物：CompiledModel + 该模型的定长序列长度
     *
     * 序列长度必须与模型同源 —— 若从 UI 开关现读，切换模型的窗口内会拿新模型的长度
     * 去写旧模型的输入缓冲区（`[1,128]` 写进 `[1,256]`）。实现 [AutoCloseable]
     * 是为了能交给 `buildOffMainThreadSafely`，构建完成但调用方已被取消时就地释放。
     */
    private class TextEngine(
        val model: CompiledModel,
        val sequenceLength: Int,
    ) : AutoCloseable {

        override fun close() {
            model.close()
        }
    }

    /**
     * 一次推理的产物：发起时的引擎引用 + 结果数据
     *
     * 携带引擎引用是为了在结果回主线程时判「引擎是否已被切换」——切换模型会替换
     * [engine] 字段，旧引擎的推理结果若此时落地会展示成新模型的结论。
     */
    private class Result(
        val engine: TextEngine,
        val probabilities: FloatArray,
        val tokenizeMs: Double,
        val inferenceMs: Double,
    )

    private companion object {
        const val MOBILE_BERT = "bert_classifier.tflite"
        const val WORD_VEC = "average_word_classifier.tflite"
        const val VOCAB_FILE = "vocab.txt"

        /** MobileBERT 的定长序列长度 */
        const val BERT_SEQUENCE_LENGTH = 128

        /** 平均词向量模型的定长序列长度 */
        const val WORD_VEC_SEQUENCE_LENGTH = 256

        /** [PAD] 与 [UNK] 在词表中的 id 均为 0 */
        const val PAD_TOKEN_ID = 0

        /** 去标点，仅保留字母、数字与空格 */
        val PUNCTUATION_REGEX = "[^a-zA-Z0-9 ]".toRegex()

        val LABELS = listOf("负面", "正面")

        const val POSITIVE_SAMPLE = "this movie is fantastic and i love it"
    }
}
