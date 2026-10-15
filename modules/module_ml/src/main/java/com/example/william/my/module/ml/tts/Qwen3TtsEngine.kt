package com.example.william.my.module.ml.tts

import com.example.william.my.module.ml.helper.TFLiteModelHelper
import com.google.ai.edge.litert.CompiledModel
import com.google.ai.edge.litert.TensorBuffer
import java.io.Closeable
import java.io.File
import java.util.Random
import kotlin.math.exp
import kotlin.math.min

/**
 * Qwen3-TTS（语音语言模型）—— 宿主侧编排的三图解码循环
 *
 * 对应官方 `samples/litert/text_to_speech_lm`（`Qwen3TtsEngine.kt`，CompiledModel API，CPU）。
 * 与同目录另外两个 TTS 示例的分工是：Matcha 是紧凑的流匹配声学模型，KittenTTS 走动态长度的
 * 轻量三图，而这里代表**语音语言模型**这一支 —— 用 Qwen3 talker LM 以 12.5 Hz 逐帧吐出 16 层
 * 编解码器 token，再由神经编解码器还原成 24 kHz 波形。
 *
 * 三张图（都由宿主循环驱动，图之间不直接相连）：
 *
 * 1. **talker**：标准 Qwen3 Transformer（28 层 / hidden 1024），带 `prefill_32` 与 `decode`
 *    两个签名与显式 KV cache。预测每帧的第一层（语义）码本。导出时在 lm_head 后面接了一个
 *    identity 块，因此**每个 decode 步骤除了 logits 还吐出 hidden state**，用来喂给 MTP。
 * 2. **MTP（码本预测器）**：5 层 Transformer，输入 `past_hidden` + `cb0_embed` + `noise`，
 *    一次调用就在图内把 15 步自回归跑完（17 槽 KV，15 个残差码本由图内 argmax 选出）。
 * 3. **codec**：拆成 `codec_partA`（codes → hidden）与 `codec_partB`（hidden → 波形，走
 *    XNNPACK 的 FORCE_FP16）。按 64 帧一块解码，每块带 25 帧左侧上下文，上采样倍率 1920。
 *
 * 图与图之间的一切 —— BPE 分词、提示词拼装（控制 token + 说话人 x-vector + 逐帧文本条件）、
 * 16 层码本嵌入求和、带重复惩罚与控制 token 抑制的采样、分块编解码 —— 都在宿主侧完成。
 *
 * ## 与官方的三处刻意偏离
 *
 * 1. **推理配置走 [TFLiteModelHelper.createCpuOptions]**（模块单一入口），而不是各处自建
 *    `CompiledModel.Options`。官方对 talker / 折叠 MTP / codec 都用 4 线程，本模块的
 *    [TFLiteModelHelper.CPU_THREADS] 同样是 `min(核数, 4)`，因此线程档位一致，耗时可比。
 * 2. **KV cache 缓冲区改为引擎级复用并在 [close] 中释放**：官方每次 [synthesize] 都新建一份
 *    `TalkerState`，其中 2 × 56 个 KV 缓冲区（每层 k/v 各一份，`[1,8,1024,128]` fp32 ≈ 4 MB，
 *    合计约 470 MB）**从不释放**。本模块把它提到引擎级、每段合成复用（prefill 会把 KV 显式
 *    写零，因此复用不会串音），并在 [close] 里逐个释放。不加这一处，演示页连说话两次就会耗尽内存。
 * 3. **只实现官方默认（FAST）图集**：talker 的 int4 图两套共用，MTP 与 codec 各有一套 ——
 *    折叠 int8 MTP + 两段 codec（官方 `install_to_device.sh` 默认，本模块下载层也只装这套），
 *    或 reference 图集 `mtp_fp32` + `codec_decoder_fp32`（官方 `FAST=0`）。两套图产出的音频
 *    并不相同（折叠 MTP 的残差码本由图内 argmax 决定，与 `greedy` 开关无关），且 reference
 *    那套要再多下约 210 MB。既然本页只装 FAST 一套，官方那条**永远不会走到**的 reference
 *    分支就不再保留 —— 换成 reference 图集需要同时改本文件与下载清单，两者必须一致。
 *
 * 另：官方常量 `PROMPT_SUFFIX`（`<|im_end|>` 之后重开 assistant 段）在官方代码里声明了但
 * 从未被使用，本模块不保留该死常量；提示词模板记录在 [PROMPT_PREFIX] 的注释里。
 *
 * ## 代价
 *
 * 官方在 Pixel 8a 上的端到端实测是 **RTF ≈ 6.7**（4.16 s 音频耗 28.0 s，其中 MTP 占 19.2 s）——
 * 明显**慢于实时**，瓶颈是 78 M 参数的 MTP 每帧要把权重流式过 16 遍。
 * 显存/内存侧：4 张图约 940 MB（文件支撑的内存映射）+ 表约 723 MB + KV cache 约 470 MB，
 * 峰值在 1.5 GB 量级，面向 8 GB 内存设备。
 */
internal class Qwen3TtsEngine(private val dir: File) : Closeable {

    // ─────────────────── 宿主侧张量 ───────────────────

    private val tokenizer = QwenBpeTokenizer.fromFiles(File(dir, VOCAB_FILE), File(dir, MERGES_FILE))

    /** codec 各码本的嵌入表（fp32，3072 × 1024，约 12.6 MB） */
    private val codecEmbedding = Npy.load(File(dir, CODEC_EMBEDDING_FILE))

    /** MTP 的 15 张残差码本嵌入表（fp16，15 × 2048 × 1024，约 63 MB）；映射，不入堆 */
    private val mtpEmbeddings = Npy.mmapHalf(File(dir, MTP_EMBEDDINGS_FILE))

    /** 文本嵌入表（fp16，约 622 MB）；映射，不入堆 */
    private val textEmbedding = Npy.mmapHalf(File(dir, TEXT_EMBEDDING_FILE))

    /** 文本嵌入到 talker hidden 的 SiLU 投影 MLP（2048 → 2048 → 1024） */
    private val projection = Npy.loadNpz(File(dir, TEXT_PROJECTION_FILE), PROJECTION_NAMES)

    /** 内置示例说话人（1024 维 x-vector） */
    val speakerVector: FloatArray = Npy.load(File(dir, SPEAKER_FILE))

    init {
        // 表形状先校验：半截下载的文件在这里就以清晰信息失败，而不是在后面按错误偏移读到内存越界
        require(codecEmbedding.size == CODEC_VOCAB * HIDDEN) {
            "$CODEC_EMBEDDING_FILE 形状不符：${codecEmbedding.size} 个 float，期望 ${CODEC_VOCAB * HIDDEN}"
        }
        require(mtpEmbeddings.count == MTP_SLOTS * MTP_VOCAB * HIDDEN) {
            "$MTP_EMBEDDINGS_FILE 形状不符：${mtpEmbeddings.count} 个元素，期望 ${MTP_SLOTS * MTP_VOCAB * HIDDEN}"
        }
        require(textEmbedding.count % TEXT_DIM == 0) {
            "$TEXT_EMBEDDING_FILE 末维不是 $TEXT_DIM：共 ${textEmbedding.count} 个元素"
        }
        require(speakerVector.size == HIDDEN) {
            "$SPEAKER_FILE 形状不符：${speakerVector.size} 维，期望 $HIDDEN"
        }
        require(projection.getValue("w1").size == TEXT_DIM * TEXT_DIM)
        require(projection.getValue("b1").size == TEXT_DIM)
        require(projection.getValue("w2").size == HIDDEN * TEXT_DIM)
        require(projection.getValue("b2").size == HIDDEN)
    }

    // ─────────────────── 四张图 ───────────────────

    private val talker: CompiledModel

    private val mtp: CompiledModel

    private val codecPartA: CompiledModel

    /** 官方对这一张图显式要求 XNNPACK 以 fp16 执行（`FORCE_FP16`） */
    private val codecPartB: CompiledModel

    /** 当前装载的图集，用于状态栏展示 */
    val graphNames: String = listOf(TALKER_FILE, MTP_FILE, CODEC_PART_A_FILE, CODEC_PART_B_FILE)
        .joinToString(" + ") { it.substringBeforeLast('.') }

    private fun load(fileName: String, xnnPackFlags: Int? = null): CompiledModel {
        val file = File(dir, fileName)
        check(file.isFile && file.length() > 0L) { "模型未就绪：$fileName" }
        return CompiledModel.create(
            file.absolutePath,
            TFLiteModelHelper.createCpuOptions(xnnPackFlags = xnnPackFlags),
            null,
        )
    }

    private val kvNames: List<String> = (0 until TALKER_LAYERS).flatMap {
        listOf("kv_cache_k_$it", "kv_cache_v_$it")
    }

    private val talkerSession: TalkerSession

    // ─────────────────── MTP 缓冲区 ───────────────────

    /** 折叠图的位置入参顺序：`past_hidden` / `cb0_embed` / `noise` */
    private val mtpInputs: List<TensorBuffer>

    /** 折叠图的位置出参顺序：`codes[15]` / `logits[15,2048]` */
    private val mtpOutputs: List<TensorBuffer>

    /**
     * 建图与缓冲区
     *
     * 该页峰值在 1.5 GB 量级，构造中途失败（OOM、磁盘上是半截文件）时必须逐个回收**已建成的**
     * 原生实例：页面会捕获 OOM 后继续存活并允许重试，只靠 GC 会让每次失败都留下一份文件映射。
     */
    init {
        val created = ArrayList<AutoCloseable>(6)
        try {
            talker = load(TALKER_FILE)
            created += talker
            mtp = load(MTP_FILE)
            created += mtp
            codecPartA = load(CODEC_PART_A_FILE)
            created += codecPartA
            codecPartB = load(CODEC_PART_B_FILE, xnnPackFlags = TFLiteModelHelper.XNNPACK_FORCE_FP16)
            created += codecPartB
            mtpInputs = mtp.createInputBuffers()
            created.addAll(mtpInputs)
            mtpOutputs = mtp.createOutputBuffers()
            created.addAll(mtpOutputs)
            talkerSession = TalkerSession()
            created += talkerSession
        } catch (e: Throwable) {
            // 逆序回收：TensorBuffer 先于所属 CompiledModel 关闭
            created.asReversed().forEach { runCatching { it.close() } }
            throw e
        }
    }

    init {
        // 图在 argmax 之前会给每个码本的 logits 加一行噪声；写零即图内贪心（官方同样只写一次）
        mtpInputs[2].writeFloat(FloatArray(MTP_SLOTS * MTP_VOCAB))
    }

    // ─────────────────── 对外接口 ───────────────────

    /** 一次合成的结果与分阶段耗时 */
    data class Result(
        val audio: FloatArray,
        val frames: Int,
        val prefillMs: Long,
        val talkerMs: Long,
        val mtpMs: Long,
        val codecMs: Long,
    ) {
        /** 三图净耗时（不含宿主侧采样与嵌入求和） */
        val graphMs: Long
            get() = prefillMs + talkerMs + mtpMs + codecMs

        val audioSeconds: Double
            get() = audio.size.toDouble() / Qwen3TtsEngine.SAMPLE_RATE

        /** 实时率 = 三图耗时 / 音频时长，> 1 表示慢于实时 */
        val realTimeFactor: Double
            get() = if (audioSeconds > 0) graphMs / 1000.0 / audioSeconds else 0.0
    }

    /** 逐帧进度回调；**在推理线程上被调用**，调用方要自己切回主线程 */
    fun interface Progress {
        fun onFrame(frame: Int)
    }

    /** 纯文本分词，供启动自检使用 */
    fun encodeText(text: String): IntArray = tokenizer.encode(text)

    /** 启动自检：返回不一致的描述，**空列表表示全部通过** */
    fun findTokenizerMismatches(vectors: List<Pair<String, IntArray>>): List<String> = tokenizer.findMismatches(vectors)

    /** 词表条目数；仅用于状态栏展示 */
    val tokenizerSize: Int
        get() = tokenizer.vocabularySize

    /**
     * 用 [speaker]（1024 维 x-vector）的嗓音合成 [text]
     *
     * [greedy] 为真时整条循环与 Python 参考实现（以及 PyTorch 实现）逐 token 一致；否则走
     * top-50 / 温度 0.9 采样（模型默认参数）。两个开关都只作用于第一层码本 ——
     * 15 层残差码本由折叠 MTP 图内 argmax 决定，与本参数无关。
     */
    fun synthesize(
        text: String,
        language: String = "english",
        speaker: FloatArray = speakerVector,
        greedy: Boolean = false,
        seed: Long? = null,
        progress: Progress? = null,
    ): Result {
        val rnd = if (seed != null) Random(seed) else Random()

        // ── 提示词拼装（纯宿主侧查表 + 那个小 MLP） ──
        val textIds = tokenizer.encode(text)
        require(textIds.isNotEmpty()) { "分词结果为空，没有可合成的文本" }

        val ttsBos = embedText(intArrayOf(TTS_BOS))[0]
        val ttsEos = embedText(intArrayOf(TTS_EOS))[0]
        val ttsPad = embedText(intArrayOf(TTS_PAD))[0]

        val control = if (language == "auto") {
            intArrayOf(NOTHINK, THINK_BOS, THINK_EOS)
        } else {
            val languageId = LANGUAGE_IDS[language.lowercase()]
                ?: throw IllegalArgumentException("不支持的语言：$language")
            intArrayOf(THINK, THINK_BOS, languageId, THINK_EOS)
        }
        // codec 侧前缀 = 控制 token | 说话人 | [PAD, BOS] → 每项 1024 维
        val codecPrefix = ArrayList<FloatArray>(control.size + 3)
        for (id in control) {
            codecPrefix.add(codecRow(id))
        }
        codecPrefix.add(speaker.copyOf())
        codecPrefix.add(codecRow(PAD_ID))
        codecPrefix.add(codecRow(BOS_ID))

        val role = embedText(PROMPT_PREFIX)
        // 文本体 = 「PAD×n + BOS」与 codecPrefix 前 n 项逐一相加（最后一项 BOS 对的是首字）
        val body = Array(codecPrefix.size - 1) { i ->
            val condition = if (i < codecPrefix.size - 2) ttsPad else ttsBos
            add(condition, codecPrefix[i])
        }
        val firstText = add(embedText(intArrayOf(textIds[0]))[0], codecPrefix.last())
        val prefill = role + body + arrayOf(firstText)
        // 余下文本按帧依次作为条件；末尾补 TTS_EOS
        val trailing = ArrayList<FloatArray>(textIds.size)
        for (i in 1 until textIds.size) {
            trailing.add(embedText(intArrayOf(textIds[i]))[0])
        }
        trailing.add(ttsEos)

        // ── talker 预填充 + 首次 decode ──
        var mark = System.nanoTime()
        talkerSession.prefill(prefill)
        var position = prefill.size - 1
        var step = talkerSession.decode(prefill.last(), position)
        val prefillMs = (System.nanoTime() - mark) / 1_000_000

        // ── 逐帧循环 ──
        // 码本 2048 以上是非语音控制 token，抑制掉；只有 EOS 例外
        val suppress = FloatArray(CODEC_VOCAB)
        for (i in CONTROL_VOCAB until CODEC_VOCAB) {
            suppress[i] = NEG
        }
        suppress[EOS] = 0f

        val frames = ArrayList<IntArray>()
        val history = HashSet<Int>()
        var talkerNs = 0L
        var mtpNs = 0L
        while (frames.size < MAX_FRAMES) {
            val scores = FloatArray(CODEC_VOCAB) { step.logits[it] + suppress[it] }
            if (frames.size < MIN_FRAMES) {
                scores[EOS] = NEG
            }
            // 重复惩罚：出现过的 token 按其符号向零靠近 5%
            for (token in history) {
                scores[token] = if (scores[token] > 0) scores[token] / 1.05f else scores[token] * 1.05f
            }
            val firstCode = pick(scores, greedy, rnd)
            history.add(firstCode)
            if (firstCode == EOS) break

            mark = System.nanoTime()
            val residual = mtpFrame(step.hidden, firstCode)
            mtpNs += System.nanoTime() - mark

            val frame = IntArray(CODEC_BOOKS)
            frame[0] = firstCode
            for (i in 0 until MTP_SLOTS) {
                frame[i + 1] = residual[i]
            }
            frames.add(frame)
            progress?.onFrame(frames.size)

            // 下一步输入嵌入 = 16 层码本嵌入之和 + 文本条件
            val embed = codecRow(firstCode)
            for (i in 0 until MTP_SLOTS) {
                addMtpRow(embed, i, residual[i])
            }
            val textIndex = frames.size - 1
            val condition = if (textIndex < trailing.size) trailing[textIndex] else ttsPad
            for (i in 0 until HIDDEN) {
                embed[i] += condition[i]
            }

            position += 1
            mark = System.nanoTime()
            step = talkerSession.decode(embed, position)
            talkerNs += System.nanoTime() - mark
        }

        // ── 编解码器还原波形 ──
        mark = System.nanoTime()
        val audio = decodeCodes(frames)
        val codecMs = (System.nanoTime() - mark) / 1_000_000

        return Result(audio, frames.size, prefillMs, talkerNs / 1_000_000, mtpNs / 1_000_000, codecMs)
    }

    // ─────────────────── talker：双签名 + KV ping-pong ───────────────────

    /**
     * talker 的 KV cache 与输入输出缓冲
     *
     * 两份完整的 KV（`setA` / `setB`）交替作为 `run` 的输入与输出，避免每步拷贝约 235 MB 的
     * cache：某一步从 `current` 读、写进 `next`，写完 `current = next`。这是官方做法，本模块
     * 只是把它的生命周期从「一次合成」提到「整个引擎」，并在 [close] 里释放（见类注释第 2 点）。
     */
    private inner class TalkerSession : Closeable {

        private val kvSetA: Map<String, TensorBuffer> = kvNames.associateWith { talker.createOutputBuffer(it, DECODE_SIGNATURE) }

        private val kvSetB: Map<String, TensorBuffer> = kvNames.associateWith { talker.createOutputBuffer(it, DECODE_SIGNATURE) }

        /** 持有最近一步之后的 cache */
        private var current: Map<String, TensorBuffer> = kvSetA

        private val embeddingIn = talker.createInputBuffer("embeddings", DECODE_SIGNATURE)
        private val positionIn = talker.createInputBuffer("input_pos", DECODE_SIGNATURE)
        private val maskIn = talker.createInputBuffer("mask", DECODE_SIGNATURE)
        private val logitsOut = talker.createOutputBuffer("logits", DECODE_SIGNATURE)

        /** 复用的因果掩码缓冲（`[CACHE]`，不允许的位置填 -1e9） */
        private val mask = FloatArray(CACHE) { NEG }

        /** 预填充：把提示词嵌入一次喂进去，产出初始 KV cache */
        fun prefill(embeddings: Array<FloatArray>) {
            val promptLength = embeddings.size
            check(promptLength <= PREFILL_LENGTH) { "提示词超出 ${PREFILL_LENGTH} 个位置：$promptLength" }

            val flat = FloatArray(PREFILL_LENGTH * HIDDEN)
            for (t in embeddings.indices) {
                System.arraycopy(embeddings[t], 0, flat, t * HIDDEN, HIDDEN)
            }
            // 因果掩码：第 row 行只允许看到前 min(row, promptLength-1) 个位置
            val maskFlat = FloatArray(PREFILL_LENGTH * CACHE) { NEG }
            for (row in 0 until PREFILL_LENGTH) {
                val allowed = min(row, promptLength - 1) + 1
                for (column in 0 until allowed) {
                    maskFlat[row * CACHE + column] = 0f
                }
            }

            val zero = FloatArray(8 * CACHE * 128)
            val inputs = HashMap<String, TensorBuffer>(kvNames.size + 3)
            try {
                inputs["embeddings"] = talker.createInputBuffer("embeddings", PREFILL_SIGNATURE)
                    .also { it.writeFloat(flat) }
                inputs["input_pos"] = talker.createInputBuffer("input_pos", PREFILL_SIGNATURE)
                    .also { it.writeInt(IntArray(PREFILL_LENGTH) { i -> i }) }
                inputs["mask"] = talker.createInputBuffer("mask", PREFILL_SIGNATURE)
                    .also { it.writeFloat(maskFlat) }
                for (name in kvNames) {
                    inputs[name] = talker.createInputBuffer(name, PREFILL_SIGNATURE)
                        .also { it.writeFloat(zero) }
                }
                talker.run(inputs, kvSetA, PREFILL_SIGNATURE)
                current = kvSetA
            } finally {
                // 这 59 个输入缓冲只服务这一次预填充，用完即释放（临时峰值另计约 235 MB）
                for (buffer in inputs.values) {
                    runCatching { buffer.close() }
                }
            }
        }

        /** 单步解码：返回该位置的 logits（码本部分）与 hidden state */
        fun decode(embedding: FloatArray, position: Int): Step {
            embeddingIn.writeFloat(embedding)
            positionIn.writeInt(intArrayOf(position))
            mask.fill(NEG)
            for (column in 0..position) {
                mask[column] = 0f
            }
            maskIn.writeFloat(mask)

            val next = if (current === kvSetA) kvSetB else kvSetA
            val inputs = HashMap<String, TensorBuffer>(kvNames.size + 3)
            inputs["embeddings"] = embeddingIn
            inputs["input_pos"] = positionIn
            inputs["mask"] = maskIn
            for (name in kvNames) {
                inputs[name] = current.getValue(name)
            }
            val outputs = HashMap<String, TensorBuffer>(kvNames.size + 1)
            outputs["logits"] = logitsOut
            for (name in kvNames) {
                outputs[name] = next.getValue(name)
            }
            talker.run(inputs, outputs, DECODE_SIGNATURE)
            current = next

            // logits 张量是 [4096]：前 3072 是码本 logits，后 1024 是 hidden state
            val raw = logitsOut.readFloat()
            return Step(
                raw.copyOfRange(0, CODEC_VOCAB),
                raw.copyOfRange(CODEC_VOCAB, CODEC_VOCAB + HIDDEN),
            )
        }

        override fun close() {
            val buffers = ArrayList<TensorBuffer>(kvNames.size * 2 + 4)
            buffers.addAll(kvSetA.values)
            buffers.addAll(kvSetB.values)
            buffers.add(embeddingIn)
            buffers.add(positionIn)
            buffers.add(maskIn)
            buffers.add(logitsOut)
            for (buffer in buffers) {
                runCatching { buffer.close() }
            }
        }
    }

    /** 单个 talker 解码步的输出 */
    class Step(val logits: FloatArray, val hidden: FloatArray)

    // ─────────────────── MTP：折叠图一次调用出一帧 ───────────────────

    private fun mtpFrame(hidden: FloatArray, firstCode: Int): IntArray {
        mtpInputs[0].writeFloat(hidden)
        mtpInputs[1].writeFloat(codecRow(firstCode))
        mtp.run(mtpInputs, mtpOutputs)
        return mtpOutputs[0].readInt()
    }

    // ─────────────────── codec：固定 64 帧一块 + 25 帧左上下文 ───────────────────

    private fun decodeCodes(frames: List<IntArray>): FloatArray {
        if (frames.isEmpty()) return FloatArray(0)
        // Part A 的输入/输出缓冲与 Part B 的输出缓冲都在这里创建、这里释放
        // （TensorBuffer 不绑定创建它的模型，Part A 的输出可以直接当 Part B 的输入）
        val codesIn = codecPartA.createInputBuffers()
        val hidden = codecPartA.createOutputBuffers()
        val waveform = codecPartB.createOutputBuffers()
        try {
            return decodeChunks(frames, codesIn, hidden, waveform)
        } finally {
            for (buffer in codesIn + hidden + waveform) {
                runCatching { buffer.close() }
            }
        }
    }

    private fun decodeChunks(
        frames: List<IntArray>,
        codesIn: List<TensorBuffer>,
        hidden: List<TensorBuffer>,
        waveform: List<TensorBuffer>,
    ): FloatArray {
        val pieces = ArrayList<FloatArray>()
        var start = 0
        while (start < frames.size) {
            val context = min(CODEC_CONTEXT, start)
            // 窗口 = 左上下文 + 新帧，且必须塞进固定 T 的图里，因此每块最多推进 CODEC_CHUNK - 上下文 帧
            val end = min(start + CODEC_CHUNK - context, frames.size)
            val window = end - (start - context)

            // 形状 [16, 64]，第 q 行的第 t 列是该帧第 q 层码本
            val buffer = IntArray(CODEC_BOOKS * CODEC_CHUNK)
            for (t in 0 until window) {
                val frame = frames[start - context + t]
                for (q in 0 until CODEC_BOOKS) {
                    buffer[q * CODEC_CHUNK + t] = frame[q]
                }
            }
            codesIn[0].writeInt(buffer)
            codecPartA.run(codesIn, hidden)
            codecPartB.run(hidden, waveform)

            val wav = waveform[0].readFloat()
            // 丢掉左上下文对应的那段波形，只留本块新增帧
            pieces.add(wav.copyOfRange(context * UPSAMPLE, window * UPSAMPLE))
            start = end
        }

        val total = pieces.sumOf { it.size }
        val out = FloatArray(total)
        var offset = 0
        for (piece in pieces) {
            System.arraycopy(piece, 0, out, offset, piece.size)
            offset += piece.size
        }
        return out
    }

    // ─────────────────── 宿主侧数学 ───────────────────

    private fun codecRow(id: Int): FloatArray {
        val out = FloatArray(HIDDEN)
        System.arraycopy(codecEmbedding, id * HIDDEN, out, 0, HIDDEN)
        return out
    }

    private fun addMtpRow(accumulator: FloatArray, table: Int, id: Int) {
        val base = (table * MTP_VOCAB + id) * HIDDEN
        for (i in 0 until HIDDEN) {
            accumulator[i] += mtpEmbeddings.get(base + i)
        }
    }

    private fun add(left: FloatArray, right: FloatArray): FloatArray = FloatArray(HIDDEN) { left[it] + right[it] }

    /** 文本嵌入查表 + 2048 → 2048 → 1024 的 SiLU 投影 MLP */
    private fun embedText(ids: IntArray): Array<FloatArray> {
        val w1 = projection.getValue("w1")
        val b1 = projection.getValue("b1")
        val w2 = projection.getValue("w2")
        val b2 = projection.getValue("b2")
        return Array(ids.size) { n ->
            val x = FloatArray(TEXT_DIM)
            val base = ids[n] * TEXT_DIM
            for (i in 0 until TEXT_DIM) {
                x[i] = textEmbedding.get(base + i)
            }
            val hiddenLayer = FloatArray(TEXT_DIM)
            for (row in 0 until TEXT_DIM) {
                var acc = b1[row]
                val weightBase = row * TEXT_DIM
                for (column in 0 until TEXT_DIM) {
                    acc += w1[weightBase + column] * x[column]
                }
                hiddenLayer[row] = acc / (1f + exp(-acc))
            }
            FloatArray(HIDDEN) { row ->
                var acc = b2[row]
                val weightBase = row * TEXT_DIM
                for (column in 0 until TEXT_DIM) {
                    acc += w2[weightBase + column] * hiddenLayer[column]
                }
                acc
            }
        }
    }

    /** 贪心 argmax 或 top-50 / 温度 0.9 采样 */
    private fun pick(logits: FloatArray, greedy: Boolean, rnd: Random): Int {
        if (greedy) {
            var best = 0
            for (i in logits.indices) {
                if (logits[i] > logits[best]) {
                    best = i
                }
            }
            return best
        }
        val topK = min(TOP_K, logits.size)
        val indices = logits.indices.sortedByDescending { logits[it] }.take(topK)
        val probabilities = DoubleArray(topK)
        val maxScaled = logits[indices[0]] / TEMPERATURE
        var sum = 0.0
        for (i in 0 until topK) {
            probabilities[i] = exp(logits[indices[i]] / TEMPERATURE - maxScaled)
            sum += probabilities[i]
        }
        var r = rnd.nextDouble() * sum
        for (i in 0 until topK) {
            r -= probabilities[i]
            if (r <= 0) return indices[i]
        }
        return indices[topK - 1]
    }

    override fun close() {
        // 先放缓冲区再放模型：TensorBuffer 的原生内存独立于模型，但顺序上「先子后父」更直观
        for (buffer in mtpInputs + mtpOutputs) {
            runCatching { buffer.close() }
        }
        runCatching { talkerSession.close() }
        for (model in listOf(talker, mtp, codecPartA, codecPartB)) {
            runCatching { model.close() }
        }
        runCatching { mtpEmbeddings.close() }
        runCatching { textEmbedding.close() }
    }

    companion object {

        /** 与官方一致的采样文件落盘名（下载层把仓库里的目录结构拍平到这里） */
        const val TALKER_FILE = "talker_int4.tflite"
        const val MTP_FILE = "mtp_folded_int8.tflite"
        const val CODEC_PART_A_FILE = "codec_partA.tflite"
        const val CODEC_PART_B_FILE = "codec_partB.tflite"
        const val CODEC_EMBEDDING_FILE = "codec_embedding_fp32.npy"
        const val MTP_EMBEDDINGS_FILE = "mtp_embeddings_fp16.npy"
        const val TEXT_EMBEDDING_FILE = "text_embedding_fp16.npy"
        const val TEXT_PROJECTION_FILE = "text_projection_fp32.npz"
        const val SPEAKER_FILE = "demo_speaker.npy"
        const val VOCAB_FILE = "vocab.json"
        const val MERGES_FILE = "merges.txt"

        /** 24 kHz 单声道 */
        const val SAMPLE_RATE = 24_000

        /** 官方硬上限：12.5 Hz × 512 帧 ≈ 41 s 音频 */
        const val MAX_FRAMES = 512

        /** 十种语言 + `auto`；id 取自模型词表 */
        val LANGUAGE_IDS: Map<String, Int> = mapOf(
            "chinese" to 2055,
            "english" to 2050,
            "german" to 2053,
            "italian" to 2070,
            "portuguese" to 2071,
            "spanish" to 2054,
            "japanese" to 2058,
            "korean" to 2064,
            "french" to 2061,
            "russian" to 2069,
        )

        /** 界面下拉项，顺序与官方一致：english 打头，其余按字母序，auto 收尾 */
        val LANGUAGES: List<String> = listOf("english") + (LANGUAGE_IDS.keys - "english").sorted() + listOf("auto")

        private const val HIDDEN = 1024
        private const val TEXT_DIM = 2048

        /** talker 28 层，每层 k/v 一份 cache */
        private const val TALKER_LAYERS = 28

        /** 码本词表：前 2048 是语音码本，之上是控制 token */
        private const val CODEC_VOCAB = 3072
        private const val CONTROL_VOCAB = 2048

        /** 每帧 16 层码本 = 1 层语义 + 15 层残差 */
        private const val CODEC_BOOKS = 16
        private const val MTP_SLOTS = 15

        /** MTP 的残差码本词表大小（15 层共用同一张 2048 词表） */
        private const val MTP_VOCAB = 2048

        private const val CACHE = 1024
        private const val PREFILL_LENGTH = 32

        private const val CODEC_CHUNK = 64
        private const val CODEC_CONTEXT = 25
        private const val UPSAMPLE = 1920

        private const val NEG = -1e9f
        private const val EOS = 2150
        private const val PAD_ID = 2148
        private const val BOS_ID = 2149
        private const val THINK = 2154
        private const val THINK_BOS = 2156
        private const val THINK_EOS = 2157
        private const val NOTHINK = 2155
        private const val TTS_BOS = 151672
        private const val TTS_EOS = 151673
        private const val TTS_PAD = 151671

        /** 官方设的「至少 2 帧」下界，避免短句立刻命中 EOS */
        private const val MIN_FRAMES = 2

        private const val TOP_K = 50
        private const val TEMPERATURE = 0.9

        private const val DECODE_SIGNATURE = "decode"
        private const val PREFILL_SIGNATURE = "prefill_32"

        /**
         * 提示词前缀：`<|im_start|>assistant\n`
         *
         * 官方另有一个 `PROMPT_SUFFIX`（`<|im_end|>\n<|im_start|>assistant\n`）但从未使用 ——
         * 生成段的条件由 codec 侧前缀与逐帧文本条件承担，因此本模块不保留该常量。
         */
        private val PROMPT_PREFIX = intArrayOf(151644, 77091, 198)

        private val PROJECTION_NAMES = listOf("w1", "b1", "w2", "b2")
    }
}
