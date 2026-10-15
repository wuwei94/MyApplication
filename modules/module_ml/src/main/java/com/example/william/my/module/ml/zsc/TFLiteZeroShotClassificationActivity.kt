package com.example.william.my.module.ml.zsc

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.ui.activity.BaseVBActivity
import com.example.william.my.module.ml.databinding.MlActivityZeroShotClassificationBinding
import com.example.william.my.module.ml.download.ModelDownloader
import com.example.william.my.module.ml.download.ModelStore
import com.example.william.my.module.ml.download.RemoteModels
import com.example.william.my.module.ml.helper.buildOffMainThreadSafely
import com.example.william.my.module.ml.helper.canTouchUi
import com.example.william.my.module.ml.helper.closeEngineOn
import com.example.william.my.module.ml.helper.createEngineDispatcher
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * LiteRT — Laya 多语言零样本分类
 *
 * 对应官方 `samples/litert/zero_shot_classification/kotlin_cpu_gpu`。所谓「零样本」指
 * **不给示例、不做微调**：把「问题描述 + 每个候选的判定标准」拼进提示词，模型直接对输入文本
 * 作答。问题按 `choice`（多选一）/ `noul`（真·假）/ `score`（分级）三种类型编码，
 * 分别对应提示词里的 `qtype_onehot`。
 *
 * 结构上这是本模块最重的一块自然语言示例，三张图 + 两套数据文件：
 * - 主图 `laya_ml_s{window}_embeds_{storage}.tflite`：吃 `inputs_embeds` / `attention_mask` /
 *   `qtype_onehot`，产出 `token_logits`（逐位置选项打分）与 `pooled_cls`；
 * - 动作头 `laya_ml_act_head_fp32.tflite`：吃 `pooled_cls` + 由 marker 位置推出的 `feats`，
 *   产出 `act_logits`。官方把选项置信度拆成「主图打分 + 小头校准」两步。
 * - 词表 `tokenizer.json`（约 33 MB，BPE）+ 嵌入表 `token_embeddings_fp16.bin`（约 375 MB）。
 *
 * 移植时与原实现的三处对齐/差异：
 * 1. **命名张量 + 显式签名**：官方用 `createInputBuffer(name, "serving_default")` 而非无序的
 *    `createInputBuffers()`，因为这两个 artifact 的签名顺序与 FlatBuffer 张量顺序并不一致。
 *    此处照搬，未改。
 * 2. **加速后端是显式的**：官方枚举里写明「GPU 创建失败绝不悄悄退回 CPU」，失败时只把
 *    原因交给界面、并另给一个「改用 CPU」的显式动作。本页保留「不静默降级」的实质语义，
 *    但沿用模块内 digit / segmentation 两页的既有约定——自动重试一次 CPU，同时把原因
 *    如实写进状态栏并同步拨回开关（见 [startEngine] 的失败分支）。
 * 3. **串行队列**：官方自建了 `Laya-LiteRT` 单线程执行器；本模块改为遵循模块纪律，
 *    所有引擎调用都排进 [createEngineDispatcher] 返回的同一条队列（见 `LayaEngine` KDoc）。
 *
 * 模型约 648 MB，与官方一致地运行时下载、不入 APK（见 [ModelDownloader]）。
 */
@Route(path = RouterPath.Ml.ZeroShotClassification)
class TFLiteZeroShotClassificationActivity :
    BaseVBActivity<MlActivityZeroShotClassificationBinding>(),
    View.OnClickListener {

    /** 当前引擎；推理线程与主线程共享，必须 volatile */
    @Volatile
    private var engine: LayaEngine? = null

    /** 当前生效的加速后端：与引擎实例严格对应，不从 UI 开关现读 */
    @Volatile
    private var backend: LayaEngine.Backend = LayaEngine.Backend.GPU

    private val engineDispatcher = createEngineDispatcher()

    private val downloader by lazy { ModelDownloader(this) }

    private var initJob: Job? = null
    private var runJob: Job? = null

    private var presets: Map<String, Any?>? = null

    override fun getViewBinding(): MlActivityZeroShotClassificationBinding = MlActivityZeroShotClassificationBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        binding.btnRun.setOnClickListener(this)
        binding.btnFillExample.setOnClickListener(this)

        binding.rgPreset.setOnCheckedChangeListener { _, _ ->
            val changed = presetKey() != loadedPresetKey
            if (changed) {
                loadedPresetKey = presetKey()
                // 不同问题集的输入字段不同，切换时同步清掉不适用的那一栏
                if (presetKey() != PRESET_EMAIL) binding.etSubject.setText("")
                fillExample()
            }
        }

        binding.rgLanguage.setOnCheckedChangeListener { _, _ -> fillExample() }

        binding.switchGpu.setOnCheckedChangeListener { _, checked ->
            if (suppressBackendCallback) return@setOnCheckedChangeListener
            onboardBackend(if (checked) LayaEngine.Backend.GPU else LayaEngine.Backend.CPU)
        }

        fillExample()
        startEngine()
    }

    /** 防止预设切换回调与初始化互相触发 */
    private var loadedPresetKey = PRESET_EMAIL

    /**
     * 程序化回写加速开关时抑制回调
     *
     * GPU 构建失败后页面会把开关拨回 CPU；若让回调照常触发，就会**再排队一次**引擎构建 ——
     * LayaEngine 构造要加载 393 MB 嵌入表，重复构建代价很高（而且 `initJob.cancel()` 会取消
     * 当前正在执行的这个协程，形成自取消）。因此这里显式抑制，由失败分支自己补一次重试。
     */
    private var suppressBackendCallback = false

    /**
     * 确保模型就绪（必要时下载）后构建引擎并编译计算图
     *
     * @param target 期望的后端；GPU 失败时页面会明确切到 CPU 并重试一次
     */
    private fun startEngine(target: LayaEngine.Backend = LayaEngine.Backend.GPU) {
        initJob?.cancel()
        initJob = lifecycleScope.launch {
            try {
                ensureModels()
                if (!canTouchUi) return@launch

                val built = buildOffMainThreadSafely(engineDispatcher) {
                    LayaEngine(this@TFLiteZeroShotClassificationActivity)
                }
                if (canTouchUi) {
                    binding.tvStatus.text =
                        "推理引擎: 编译中 (${if (target == LayaEngine.Backend.GPU) "GPU FP32" else "CPU"})…"
                }
                withContext(engineDispatcher) { built.initialize(target, WINDOW) }

                val previous = engine
                engine = built
                backend = target
                if (previous != null) {
                    withContext(NonCancellable + engineDispatcher) { previous.close() }
                }

                if (canTouchUi) {
                    val ready = if (target == LayaEngine.Backend.GPU) "GPU FP32" else "CPU"
                    binding.tvStatus.text =
                        "推理引擎: 就绪 · $ready | 词表 ${built.tokenizerSize} 项 | " +
                        "嵌入表加载 ${"%.0f".format(built.embeddingLoadMs)} ms"
                    binding.tvRunSummary.text = "分类结果: 就绪，点击「运行全部问题」"
                    binding.btnRun.isEnabled = true
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                // 官方对 GPU 失败的处理是**拒绝退到 CPU**、把原因交给用户，并提供一次显式的
                // 「改用 CPU」动作（见 UiState.canFallbackToCpu）。本页沿用模块内 digit / segmentation
                // 两页的既有约定：自动重试一次 CPU，但把原因如实写进状态栏并同步拨回开关，
                // 不构成「静默降级」。
                if (target == LayaEngine.Backend.GPU) {
                    if (canTouchUi) {
                        binding.tvStatus.text = "GPU 不可用（${e.message}），已改用 CPU 重试"
                        suppressBackendCallback = true
                        binding.switchGpu.isChecked = false
                        suppressBackendCallback = false
                    }
                    startEngine(LayaEngine.Backend.CPU)
                    return@launch
                }
                if (canTouchUi) {
                    binding.tvStatus.text = "初始化失败: ${e.message}"
                    binding.btnRun.isEnabled = false
                }
            }
        }
    }

    /** 加速器切换：先切开关，由监听回调重建；这里只做「无变化则忽略」 */
    private fun onboardBackend(target: LayaEngine.Backend) {
        if (target == backend && engine != null) return
        startEngine(target)
    }

    private suspend fun ensureModels() {
        val pending = ModelStore.pendingBytes(this, RemoteModels.LAYA)
        if (pending <= 0L) return

        if (canTouchUi) {
            binding.progressDownload.visibility = View.VISIBLE
            binding.tvStatus.text = "首次使用，需下载模型约 ${formatMb(pending)}…"
        }

        downloader.ensureAll(RemoteModels.LAYA) { index, _, _, overall, overallTotal ->
            runOnUiThread {
                if (!canTouchUi) return@runOnUiThread
                val percent =
                    if (overallTotal > 0) (overall * 100 / overallTotal).toInt().coerceIn(0, 100) else 0
                binding.progressDownload.progress = percent
                binding.tvStatus.text =
                    "下载模型 ${index + 1}/${RemoteModels.LAYA.size} · $percent% " +
                    "(${formatMb(overall)} / ${formatMb(overallTotal)})"
            }
        }

        if (canTouchUi) binding.progressDownload.visibility = View.GONE
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.btnRun -> runAllQuestions()
            binding.btnFillExample -> fillExample()
        }
    }

    private fun runAllQuestions() {
        val text = binding.etInput.text?.toString().orEmpty().trim()
        if (text.isEmpty()) {
            binding.tvRunSummary.text = "分类结果: 请先输入待分类文本"
            return
        }
        if (engine == null) {
            binding.tvRunSummary.text = "分类结果: 引擎尚未就绪"
            return
        }
        val subject = binding.etSubject.text?.toString().orEmpty()
        val calibrated = binding.switchCalibrated.isChecked
        val key = presetKey()
        val activeBackend = backend

        runJob?.cancel()
        runJob = lifecycleScope.launch {
            try {
                if (canTouchUi) {
                    binding.btnRun.isEnabled = false
                    binding.tvRunSummary.text = "分类结果: 正在推理…"
                    binding.tvAnswers.text = ""
                }

                val report = withContext(engineDispatcher) {
                    val active = engine ?: return@withContext null
                    val calibration = if (calibrated) active.calibration else LayaCalibration.identity()
                    val all = questions(key)
                    val state = stateFor(key, subject, text)
                    val started = System.nanoTime()
                    val lines = StringBuilder()
                    var tokenCount = 0

                    all.forEach { (id, raw) ->
                        val question = LayaJson.asObject(raw)
                        val result = answerOf(active, state, id, question, activeBackend, calibration)
                        tokenCount += result.sequence.ids.size
                        lines.append(formatAnswer(id, result, calibrated, calibration))
                        lines.append('\n')
                    }
                    val totalMs = (System.nanoTime() - started) / 1_000_000.0
                    Triple(lines.toString().trimEnd(), totalMs, tokenCount)
                } ?: return@launch

                if (!canTouchUi) return@launch
                val (body, totalMs, tokenCount) = report
                binding.tvRunSummary.text =
                    "分类结果: 已完成 ${questions(key).size} 个问题 · " +
                    "总耗时 ${"%.0f".format(totalMs)} ms"
                binding.tvAnswers.text = "$body\n\n累计 token 数: $tokenCount"
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                if (canTouchUi) {
                    binding.tvRunSummary.text = "推理失败: ${e.message}"
                }
            } finally {
                if (canTouchUi) binding.btnRun.isEnabled = true
            }
        }
    }

    /**
     * 单条问题的完整链路：提示词构建 → 主图 + 动作头 → 校准解码
     *
     * 与官方 `MainViewModel.answer` 等价。**不能**直接调 [LayaEngine.answer]：它固定使用引擎装载的
     * `laya_ml_calibration.json` 温度，而界面上的「校准」开关要求关闭时改用 identity 温度 ——
     * 官方正是在这一层做选择（`if (state.calibrated) helper.calibration else identity()`），
     * 因此这里照搬，把温度作为参数传进解码。
     */
    private fun answerOf(
        active: LayaEngine,
        state: Map<String, String>,
        id: String,
        question: Map<String, Any?>,
        backend: LayaEngine.Backend,
        calibration: LayaCalibration,
    ): LayaEngine.AnswerResult {
        val started = System.nanoTime()
        val sequence = active.prepare(state, question, window = WINDOW, questionId = id)
        val prepared = System.nanoTime()
        val raw = active.runRaw(sequence, backend, WINDOW)
        check(raw.finite) { "模型输出为非有限值（${backend.name}），请检查设备门禁报告" }
        val decodeStarted = System.nanoTime()
        val answer = LayaDecoder.decode(raw.markerLogits, raw.actLogits, sequence.question, calibration)
        val finished = System.nanoTime()
        return LayaEngine.AnswerResult(
            sequence,
            answer,
            raw,
            (prepared - started) / 1_000_000.0,
            (finished - decodeStarted) / 1_000_000.0,
            (finished - started) / 1_000_000.0,
        )
    }

    /**
     * 把一条解码结果渲染成可读文本，镜像官方 `MainViewModel.presentAnswer`。
     *
     * 三类问题的选项来源不同：`choice` 的 `probabilities` 以候选标签为键（即 `criteria` 的键序）、
     * `score` 以等级整数为键并从 `legend` 取描述、`noul` 不产出 `probabilities`，
     * 需要拿 marker logits 重算一次「假 / 真」——**重算必须用同一套温度**，否则与上方
     * `choice`/`score` 的口径不一致。
     *
     * @param calibrated 界面开关状态：关闭时 [effective] 是 identity 温度
     * @param effective 本次实际生效的温度
     */
    private fun formatAnswer(
        id: String,
        result: LayaEngine.AnswerResult,
        calibrated: Boolean,
        effective: LayaCalibration,
    ): String {
        val answer = result.answer
        val question = result.sequence.question
        // 按插入序呈现，与官方一致（不重排）
        val rows: List<Row> =
            when (question.type) {
                TYPE_CHOICE -> {
                    val criteria = LayaJson.asObject(question.criteria)
                    LayaJson.asObject(answer["probabilities"] ?: emptyMap<String, Any?>()).map { (label, value) ->
                        Row(
                            probability = (value as Number).toDouble(),
                            label = label,
                            description = criteria[label]?.let { LayaPromptBuilder.renderCriterion(it) },
                        )
                    }
                }

                TYPE_SCORE -> {
                    val legend = LayaJson.asObject(answer["legend"] ?: emptyMap<String, Any?>())
                    LayaJson.asObject(answer["probabilities"] ?: emptyMap<String, Any?>()).map { (level, value) ->
                        Row(
                            probability = (value as Number).toDouble(),
                            level = level.toInt(),
                            description = legend[level]?.let { LayaPromptBuilder.renderCriterion(it) },
                        )
                    }
                }

                else -> {
                    // noul 不出现在 answer["probabilities"] 里，需按生效温度重算
                    val values = LayaDecoder.probabilities(result.raw.markerLogits, question, effective)
                    listOf(
                        Row(probability = LayaDecoder.round4(values[0].toDouble()), label = "假"),
                        Row(probability = LayaDecoder.round4(values[1].toDouble()), label = "真"),
                    )
                }
            }

        val head = buildString {
            appendLine("[$id] ${question.instructions}")
            append("  类型: ${question.type} · token ${result.sequence.ids.size} · ")
            appendLine("耗时 ${"%.0f".format(result.totalMs)} ms")
            append("  温度: ")
            appendLine(
                if (calibrated) {
                    "校准 (${"%.4f".format(effective.temperature(question.type, rows.size))})"
                } else {
                    "未校准 (identity)"
                },
            )
        }
        val body = rows.joinToString("\n") { row ->
            val name = row.label ?: "等级 ${row.level}"
            "  %-12s %6.1f%%  %s".format(name, row.probability * 100, row.description ?: "")
        }
        val decision = buildString {
            (answer["choice"] as? String)?.let { append("  判定: $it") }
            (answer["score"] as? Number)?.let { append("  判分: ${LayaDecoder.round4(it.toDouble())}") }
            (answer["noul"] as? Number)?.let { append("  为真的概率: ${"%.1f".format(it.toDouble() * 100)}%") }
            (answer["confidence"] as? Number)?.let { append("  置信度: ${"%.1f".format(it.toDouble() * 100)}%") }
        }
        return "$head\n$body\n$decision"
    }

    /** 一行选项：`choice` 用 [label]，`score` 用 [level]，两者都带可选的 [description] */
    private data class Row(
        val probability: Double,
        val label: String? = null,
        val level: Int? = null,
        val description: String? = null,
    )

    /** 当前问题集的问题列表（id -> 问题描述） */
    private fun questions(key: String): Map<String, Any?> {
        val all =
            presets ?: LayaJson.asObject(
                LayaJson.parse(assets.open(PRESETS_ASSET).bufferedReader().use { it.readText() }),
            ).also { presets = it }
        return LayaJson.asObject(all[key] ?: emptyMap<String, Any?>())
    }

    /** 输入字段随问题集变化：邮件是 subject+body，工单是 message，审核是 post */
    private fun stateFor(key: String, subject: String, text: String): Map<String, String> = when (key) {
        PRESET_EMAIL -> linkedMapOf("subject" to subject, "body" to text)
        PRESET_TRIAGE -> linkedMapOf("message" to text)
        else -> linkedMapOf("post" to text)
    }

    private fun fillExample() {
        val japanese = binding.rbJapanese.isChecked
        val example = exampleFor(presetKey(), japanese)
        binding.etSubject.setText(example.subject)
        binding.etInput.setText(example.text)
    }

    private fun presetKey(): String = when (binding.rgPreset.checkedRadioButtonId) {
        R_ID_TRIAGE -> PRESET_TRIAGE
        R_ID_MODERATION -> PRESET_MODERATION
        else -> PRESET_EMAIL
    }

    private fun exampleFor(key: String, japanese: Boolean): Example = when (key) {
        PRESET_EMAIL ->
            if (japanese) {
                Example(EXAMPLE_EMAIL_JA_SUBJECT, EXAMPLE_EMAIL_JA_BODY)
            } else {
                Example(EXAMPLE_EMAIL_EN_SUBJECT, EXAMPLE_EMAIL_EN_BODY)
            }

        PRESET_TRIAGE ->
            Example("", if (japanese) EXAMPLE_SUPPORT_JA else EXAMPLE_SUPPORT_EN)

        else ->
            Example("", if (japanese) EXAMPLE_MODERATION_JA else EXAMPLE_MODERATION_EN)
    }

    override fun onDestroy() {
        super.onDestroy()
        downloader.cancel()
        // 引擎关闭排进与推理同一条队列：此刻 lifecycleScope 已取消，
        // 但后台 run() 不可中断仍会跑完，直接 close 会释放掉它正在用的原生 handle。
        val closing = engine
        engine = null
        closeEngineOn(engineDispatcher, closing)
    }

    /** 一组的示例输入；邮件之外的预设没有 subject */
    private data class Example(val subject: String, val text: String)

    private companion object {
        const val WINDOW = 256
        const val PRESET_EMAIL = "email"
        const val PRESET_TRIAGE = "triage"
        const val PRESET_MODERATION = "moderation"

        const val TYPE_CHOICE = "choice"
        const val TYPE_SCORE = "score"

        /** 与官方同名资源在 `<layout>` 中同序，R 值在 Android 资源系统里是稳定的 */
        val R_ID_TRIAGE = com.example.william.my.module.ml.R.id.rbTriage
        val R_ID_MODERATION = com.example.william.my.module.ml.R.id.rbModeration

        const val PRESETS_ASSET = "presets.json"

        // 示例文本取自官方 strings.xml（保持原样，便于与上游逐字比对）
        const val EXAMPLE_EMAIL_EN_SUBJECT = "Cannot attach an image after the update"
        const val EXAMPLE_EMAIL_EN_BODY =
            "Hello support,\nSince updating the app yesterday, the support form closes when I attach an image. " +
                "Sending text alone still works. I need it for a task next week. Could you tell me how to check this?"
        const val EXAMPLE_EMAIL_JA_SUBJECT = "更新後に画像を添付できません"
        const val EXAMPLE_EMAIL_JA_BODY =
            "サポート窓口ご担当者様\n昨日アプリを更新してから、問い合わせフォームに画像を添付すると画面が閉じてしまいます。" +
                "文章だけなら送信できます。来週の作業で使いたいので、確認方法を教えてください。"
        const val EXAMPLE_SUPPORT_EN =
            "I was charged twice for the same subscription. I need to confirm the payment by tomorrow. " +
                "Please tell me how to get the extra charge refunded. Having to check this repeatedly has been frustrating."
        const val EXAMPLE_SUPPORT_JA =
            "利用料金が二重に請求されています。明日までに支払いを確認する必要があります。" +
                "余分な請求の返金方法を教えてください。何度も確認することになり、少し困っています。"
        const val EXAMPLE_MODERATION_EN =
            "I disagree with that explanation. This morning, pressing Save did not keep my changes. " +
                "If anyone has tried the same steps, please share what happened so we can compare the results."
        const val EXAMPLE_MODERATION_JA =
            "その説明には賛成できません。今朝は保存ボタンを押しても内容が残りませんでした。" +
                "同じ状況を試した方がいたら、手順を共有してもらえると助かります。"

        fun formatMb(bytes: Long): String = "%.1f MB".format(bytes / 1024.0 / 1024.0)
    }
}
