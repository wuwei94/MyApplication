package com.example.william.my.module.ml.asr

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ScrollView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.ui.activity.BaseVBActivity
import com.example.william.my.module.ml.databinding.MlActivitySpeechRecognitionBinding
import com.example.william.my.module.ml.download.ModelDownloader
import com.example.william.my.module.ml.download.ModelStore
import com.example.william.my.module.ml.helper.buildOffMainThreadSafely
import com.example.william.my.module.ml.helper.canTouchUi
import com.example.william.my.module.ml.helper.closeEngineOn
import com.example.william.my.module.ml.helper.createEngineDispatcher
import com.google.ai.edge.litert.Accelerator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.time.Duration.Companion.seconds

/**
 * LiteRT — 语音识别（Speech Recognition）
 *
 * 对应官方 `samples/litert/speech_recognition/kotlin_cpu`，是本模块第 9 个、也是最后一个
 * 官方任务页。它与前 8 页有两个结构性差异：
 *
 * 1. **唯一有两条输入路径的页面**：文件（16 kHz 单声道 WAV）与麦克风。官方支持的所有模型
 *    都**不是流式**的，因此两条路径共用同一套「重叠窗口拼接」——
 *    [AudioPreprocessor] → [SpeechRecognizer] → [Postprocessor] 三级流水，
 *    用两条 `Channel` 串成生产者 / 消费者。
 * 2. **唯一一个「模型名 → 解码策略」在宿主侧分派的页面**：`-ctc-` 走 [CtcDecoder]（无 decoder 子图，
 *    宿主逐帧 argmax + 压重复），`-tdt` 走 [TdtDecoder]（时间下标驱动 + LSTM 状态 ping-pong），
 *    其余走 [LiteRtRunner.DefaultDecoder]（无状态自回归）。见 [AsrEngine.decoderFor]。
 *
 * 模型清单来自 asset 里的 `asr_model_metadata.json`（6 个模型，含 3 个 Parakeet、Moonshine、
 * Whisper、Qwen3-ASR），模型与分词器都走**运行时下载**；最大的 parakeet 约 614 MB，
 * 最小的 whisper-tiny 约 41 MB。
 *
 * 推理、引擎构建、引擎关闭共用同一条引擎串行队列（见 [createEngineDispatcher]）：
 * ASR 的识别协程是「一直跑」的消费者，若不与关闭排在同一条队列，
 * 退出页面时的 `close()` 会释放掉它正在读的原生 handle。
 */
@Route(path = RouterPath.Ml.SpeechRecognition)
class TFLiteSpeechRecognitionActivity :
    BaseVBActivity<MlActivitySpeechRecognitionBinding>(),
    View.OnClickListener {

    /** 当前引擎；引擎队列与主线程共享，必须 volatile */
    @Volatile
    private var engine: AsrEngine? = null

    /** 推理、引擎构建、引擎关闭共用的串行队列 */
    private val engineDispatcher = createEngineDispatcher()

    private val downloader by lazy { ModelDownloader(this) }

    private var catalog: AsrModelCatalog? = null

    private var loadJob: Job? = null
    private var transcribeJob: Job? = null
    private var recordingJob: Job? = null

    private val isRecording get() = recordingJob != null

    private var selectedAudioUri: Uri? = null
    private var mediaPlayer: MediaPlayer? = null

    /** 已完成加载（或正在加载）的「模型 + 后端」组合，用于把重复的选中回调挡掉 */
    private var currentSignature: String? = null

    /**
     * 麦克风权限回调
     *
     * 与官方一致：**拿到权限后直接开始录音**，用户不需要再点一次按钮。
     */
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                toggleRecording()
            } else if (canTouchUi) {
                Toast.makeText(this, "未授予麦克风权限，无法录音", Toast.LENGTH_SHORT).show()
            }
        }

    /** 选音频文件（WAV：16 kHz 单声道 16-bit PCM） */
    private val pickAudioLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri == null) return@registerForActivityResult
            selectedAudioUri = uri
            binding.tvSelectedAudio.text = queryDisplayName(uri)
            binding.btnPlayPause.isEnabled = prepareMediaPlayer(uri)
        }

    /** 两个下拉框共用的监听器：选中项一变就重建引擎 */
    private val selectionListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            reloadPipelineIfChanged()
        }

        override fun onNothingSelected(parent: AdapterView<*>?) = Unit
    }

    override fun getViewBinding(): MlActivitySpeechRecognitionBinding = MlActivitySpeechRecognitionBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        val models = AsrModelCatalog(
            assets.open(AsrModelCatalog.ASSET_NAME).bufferedReader().use { it.readText() },
        )
        catalog = models

        binding.spModel.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, models.modelNames).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
        binding.spBackend.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, BACKENDS).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }

        // 适配器先设好、监听器后挂：Spinner 在 setAdapter 时就会派发一次初始选中回调
        binding.spModel.onItemSelectedListener = selectionListener
        binding.spBackend.onItemSelectedListener = selectionListener

        binding.btnSelectAudio.setOnClickListener(this)
        binding.btnRecord.setOnClickListener(this)
        binding.btnTranscribe.setOnClickListener(this)
        binding.btnPlayPause.setOnClickListener(this)
        binding.btnCopy.setOnClickListener(this)
        binding.btnClear.setOnClickListener(this)

        reloadPipelineIfChanged()
    }

    // ─────────────────── 引擎：按「模型 + 后端」组合重建 ───────────────────

    /**
     * 选中项变化时才重建
     *
     * Spinner 的初始回调与用户点击走同一条路径，靠签名比对把前者挡掉，
     * 避免一进页面就用第一个模型触发两次下载 / 构建。
     */
    private fun reloadPipelineIfChanged() {
        val key = binding.spModel.selectedItem as? String ?: return
        val backend = binding.spBackend.selectedItem as? String ?: BACKENDS[0]
        val signature = "$key|$backend"
        if (signature == currentSignature) return
        currentSignature = signature
        preparePipeline(key, backend)
    }

    /**
     * 确保模型就绪 → 在引擎队列上构建 → 替换旧引擎（旧引擎就地关闭）
     *
     * 构建排在引擎队列上：这样「关旧引擎」与「建新引擎」天然按提交顺序串行，
     * 不会出现新引擎已就绪、旧引擎还在跑推理的中间态。
     */
    private fun preparePipeline(modelKey: String, backend: String) {
        loadJob?.cancel()

        // 在途转写手里握着旧引擎的引用：新引擎就绪后 closeEngineOn 会关掉旧实例，
        // 而识别协程仍会继续在引擎队列上对已释放的原生 handle 调 recognize。
        // 换模型 / 换后端一律先停掉它；同时把字段置空，让它 finally 里的复位判等失败，
        // 不会误清新流水线刚设上的 busy 状态。
        transcribeJob?.cancel()
        transcribeJob = null

        val config = catalog?.configOf(modelKey) ?: return

        // Qwen3-ASR 官方只标了 CPU；选了 GPU 也要如实回落，并把下拉框拨回去
        val accelerator = if (config.supportsGpu && backend == BACKEND_GPU) {
            Accelerator.GPU
        } else {
            Accelerator.CPU
        }
        if (!config.supportsGpu && backend == BACKEND_GPU && canTouchUi) {
            binding.spBackend.setSelection(BACKENDS.indexOf(BACKEND_CPU))
        }

        loadJob = lifecycleScope.launch {
            stopRecording()
            stopPlayback()
            setBusy(true, "正在准备模型…")
            try {
                ensureModels(config)
                if (!canTouchUi) return@launch

                if (canTouchUi) {
                    binding.tvStatus.text = "推理引擎: 正在构建（${config.key} / $backend）..."
                }

                val built = buildOffMainThreadSafely(engineDispatcher) {
                    AsrEngine(filesDir, config, accelerator)
                }
                val previous = engine
                engine = built
                closeEngineOn(engineDispatcher, previous)

                if (canTouchUi) {
                    binding.tvStatus.text =
                        "推理引擎: 就绪 · 全离线 | ${config.key} | " +
                        "${if (accelerator == Accelerator.GPU) "GPU" else "CPU"} | " +
                        "词表 ${built.tokenizer.vocabSize}"
                    binding.tvInputSpec.text = String.format(
                        Locale.ENGLISH,
                        "窗口 %.1f s · 重叠 %.1f s",
                        built.inputInterval.inWholeMilliseconds / 1000.0,
                        FILE_OVERLAP.inWholeMilliseconds / 1000.0,
                    )
                    binding.btnTranscribe.isEnabled = true
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: OutOfMemoryError) {
                if (canTouchUi) {
                    binding.tvStatus.text = "初始化失败: 内存不足（该模型约 ${formatMb(config.approxBytes)}）"
                }
            } catch (e: Exception) {
                if (canTouchUi) {
                    binding.tvStatus.text = "初始化失败: ${e.message}"
                }
            } finally {
                // 只复位当前这一次装载：若期间用户又切了一次模型，旧 Job 的 finally 不能
                // 清掉新 Job 刚设上的 busy 状态（判等保证只有「仍是当前 Job」才复位 UI）
                if (loadJob === coroutineContext[Job]) setBusy(false)
            }
        }
    }

    /** 确保该配置需要的两个文件（模型 + 分词器）都在本地；缺哪个下哪个 */
    private suspend fun ensureModels(config: AsrModelConfig) {
        val models = config.remoteModels()
        val pending = ModelStore.pendingBytes(this, models)
        if (pending <= 0L) return

        if (canTouchUi) {
            binding.progressDownload.visibility = View.VISIBLE
            binding.tvStatus.text =
                "首次使用 ${config.key}，需下载约 ${formatMb(pending)}（模型 ${formatMb(config.approxModelBytes)} + " +
                "分词器 ${formatMb(config.approxTokenizerBytes)}）..."
        }

        downloader.ensureAll(models) { index, _, _, overall, overallTotal ->
            // 回调在 IO 线程，切回主线程再触碰视图
            runOnUiThread {
                if (!canTouchUi) return@runOnUiThread
                val percent = if (overallTotal > 0) {
                    (overall * 100 / overallTotal).toInt().coerceIn(0, 100)
                } else {
                    0
                }
                binding.progressDownload.progress = percent
                binding.tvStatus.text =
                    "下载 ${index + 1}/${models.size} · ${models[index].fileName} · " +
                    "$percent% (${formatMb(overall)} / ${formatMb(overallTotal)})"
            }
        }

        if (canTouchUi) {
            binding.progressDownload.visibility = View.GONE
        }
    }

    // ─────────────────── 交互 ───────────────────

    override fun onClick(v: View?) {
        when (v) {
            binding.btnSelectAudio -> pickAudioLauncher.launch("audio/*")
            binding.btnRecord -> requestRecording()
            binding.btnTranscribe -> startFileTranscription()
            binding.btnPlayPause -> togglePlayback()
            binding.btnCopy -> copyToClipboard()
            binding.btnClear -> clearTranscription()
        }
    }

    private fun requestRecording() {
        if (isRecording) {
            stopRecording()
            return
        }
        val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
        if (granted) {
            toggleRecording()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun startFileTranscription() {
        val uri = selectedAudioUri
        if (uri == null) {
            Toast.makeText(this, "请先选择一个音频文件", Toast.LENGTH_SHORT).show()
            return
        }
        val active = engine
        if (active == null) {
            Toast.makeText(this, "引擎尚未就绪", Toast.LENGTH_SHORT).show()
            return
        }

        binding.tvTranscription.text = ""
        setBusy(true, "正在转写…")
        transcribeJob?.cancel()
        transcribeJob = lifecycleScope.launch {
            try {
                contentResolver.openInputStream(uri)?.use { stream ->
                    FileAudioSource(
                        stream,
                        AsrEngine.SAMPLING_RATE,
                        active.inputInterval,
                        FILE_OVERLAP,
                    ).use { source ->
                        transcribeAudio(
                            source,
                            (FILE_OVERLAP / active.inputInterval).toFloat(),
                            LevenshteinTokenMerger.FILE_MAX_LEVENSHTEIN_DISTANCE,
                            active,
                        )
                    }
                } ?: throw IllegalArgumentException("无法打开该音频文件")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (canTouchUi) {
                    Toast.makeText(
                        this@TFLiteSpeechRecognitionActivity,
                        "转写失败: ${e.message}（仅支持 16 kHz 单声道 16-bit PCM WAV）",
                        Toast.LENGTH_LONG,
                    ).show()
                }
            } finally {
                // 只在「仍是当前转写」时复位：换模型 / 换后端、或再次点转写都会先取消并接管
                // 本 Job，此时 busy 状态已归新流水线，不能在这里覆盖掉
                if (transcribeJob === coroutineContext[Job]) setBusy(false)
            }
        }
    }

    private fun toggleRecording() {
        if (isRecording) {
            stopRecording()
        } else {
            startRecording()
        }
    }

    private fun startRecording() {
        val active = engine ?: run {
            Toast.makeText(this, "引擎尚未就绪", Toast.LENGTH_SHORT).show()
            return
        }
        binding.tvTranscription.text = ""
        // 实时路径用比模型窗口更短的块，滚动更快、延迟更低（官方 LIVE_AUDIO_CHUNK_*）
        val interval = LIVE_INTERVAL.coerceAtMost(active.inputInterval)
        val overlap = interval - LIVE_SLIDING
        recordingJob = lifecycleScope.launch {
            try {
                MicrophoneAudioSource(AsrEngine.SAMPLING_RATE, interval, overlap).use { source ->
                    transcribeAudio(
                        source,
                        (overlap / interval).toFloat(),
                        LevenshteinTokenMerger.LIVE_MAX_LEVENSHTEIN_DISTANCE,
                        active,
                    )
                }
            } catch (e: CancellationException) {
                // 用户停止录音，静默收尾
            } catch (e: Exception) {
                if (canTouchUi) {
                    Toast.makeText(
                        this@TFLiteSpeechRecognitionActivity,
                        "录音失败: ${e.message}",
                        Toast.LENGTH_LONG,
                    ).show()
                }
            } finally {
                // 只在「仍是当前录音」时复位：切模型 / 切后端会先取消并置空本 Job，
                // 复位由 stopRecording() 统一处理，这里不能再覆盖新流水线的 busy 状态
                if (canTouchUi && recordingJob === coroutineContext[Job]) {
                    binding.btnRecord.text = "开始录音"
                    setBusy(false)
                }
            }
        }
        if (canTouchUi) {
            binding.btnRecord.text = "停止录音"
        }
        setBusy(true, "正在识别…")
    }

    private fun stopRecording() {
        if (!isRecording) return
        recordingJob?.cancel()
        recordingJob = null
        // 收尾：把残留的「未确认」文字转正（去掉灰字）。
        // 本方法会被 preparePipeline 的 loadJob 协程调用（切模型时顺带停录音），
        // 那时 Activity 可能已进入销毁流程，写 UI 前必须过 canTouchUi。
        if (canTouchUi) {
            binding.tvTranscription.text = binding.tvTranscription.text.toString()
            binding.btnRecord.text = "开始录音"
        }
        setBusy(false)
    }

    // ─────────────────── 三级流水：预处理 → 识别 → 后处理 ───────────────────

    /**
     * 把一条 [AudioSource] 跑完整条流水
     *
     * 三个协程由两条 [Channel] 串联，各自跑在合适的调度器上：
     * - 预处理（可能含 FFT）→ [kotlinx.coroutines.Dispatchers.IO]
     * - 识别 → **引擎队列**（与引擎构建 / 关闭同队列，保证原生 handle 不被并发触碰）
     * - 后处理 + 刷 UI → [kotlinx.coroutines.Dispatchers.Default]
     */
    private suspend fun transcribeAudio(
        audioSource: AudioSource,
        overlapRatio: Float,
        maxLevenshteinDistance: Int,
        active: AsrEngine,
    ) {
        val preprocessedChannel = Channel<FloatArray>(Channel.BUFFERED)
        val tokenChannel = Channel<Pair<Int, Int>>(Channel.BUFFERED)
        LevenshteinTokenMerger(
            active.tokenizer,
            overlapRatio,
            maxLevenshteinDistance = maxLevenshteinDistance,
        ).use { postprocessor ->
            coroutineScope {
                launch(kotlinx.coroutines.Dispatchers.IO) {
                    try {
                        for (chunk in audioSource.getAudioData()) {
                            preprocessedChannel.send(active.audioPreprocessor.process(chunk))
                            kotlinx.coroutines.yield()
                        }
                    } finally {
                        preprocessedChannel.close()
                    }
                }

                launch(engineDispatcher) {
                    try {
                        for (preprocessed in preprocessedChannel) {
                            for ((tokenId, timestamp) in active.recognizer.recognize(preprocessed)) {
                                tokenChannel.send(Pair(tokenId, timestamp))
                                if (tokenId == SpeechRecognizer.END_OF_SEQUENCE) break
                            }
                            kotlinx.coroutines.yield()
                        }
                    } finally {
                        tokenChannel.close()
                    }
                }

                launch(kotlinx.coroutines.Dispatchers.Default) {
                    var totalConfirmedText = ""
                    for ((tokenId, timestamp) in tokenChannel) {
                        val decoded = postprocessor.decode(tokenId, timestamp) ?: continue
                        totalConfirmedText = merge(totalConfirmedText, decoded.confirmedText)
                        withContext(kotlinx.coroutines.Dispatchers.Main) {
                            if (canTouchUi) {
                                updateTranscriptionUi(totalConfirmedText, decoded.unconfirmedText)
                            }
                        }
                    }
                    // 序列收尾：把最后一段未确认文字也转正
                    withContext(kotlinx.coroutines.Dispatchers.Main) {
                        if (canTouchUi) {
                            binding.tvTranscription.text = binding.tvTranscription.text.toString()
                        }
                    }
                }
            }
        }
    }

    /** 已确认文本正常显示，未确认部分标灰 —— 直接体现「还可能被下一窗口改写」 */
    private fun updateTranscriptionUi(confirmedText: String, unconfirmedText: String) {
        val spannable = SpannableStringBuilder(confirmedText)
        if (unconfirmedText.isNotEmpty()) {
            if (confirmedText.isNotEmpty()) {
                spannable.append(" ")
            }
            val start = spannable.length
            spannable.append(unconfirmedText)
            spannable.setSpan(
                ForegroundColorSpan(Color.GRAY),
                start,
                spannable.length,
                SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE,
            )
        }
        binding.tvTranscription.text = spannable
        binding.svTranscription.post { binding.svTranscription.fullScroll(ScrollView.FOCUS_DOWN) }
    }

    private fun merge(previous: String, next: String) = listOf(previous, next).filter { it.isNotEmpty() }.joinToString(separator = " ")

    // ─────────────────── 播放器 / 剪贴板 / 状态 ───────────────────

    /** @return 是否成功准备好播放器 */
    private fun prepareMediaPlayer(uri: Uri): Boolean {
        releaseMediaPlayer()
        return try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(this@TFLiteSpeechRecognitionActivity, uri)
                prepare()
            }
            binding.btnPlayPause.text = "试听"
            true
        } catch (e: Exception) {
            // 选到的可能不是音频，或编码不受支持 —— 播放器不可用不影响转写
            e.printStackTrace()
            releaseMediaPlayer()
            false
        }
    }

    private fun togglePlayback() {
        val player = mediaPlayer ?: return
        if (player.isPlaying) {
            player.pause()
            binding.btnPlayPause.text = "继续"
        } else {
            player.start()
            binding.btnPlayPause.text = "暂停"
        }
    }

    private fun releaseMediaPlayer() {
        val player = mediaPlayer ?: return
        mediaPlayer = null
        runCatching { player.stop() }
        runCatching { player.release() }
    }

    private fun stopPlayback() {
        releaseMediaPlayer()
        if (canTouchUi) {
            binding.btnPlayPause.text = "试听"
        }
    }

    private fun copyToClipboard() {
        val text = binding.tvTranscription.text.toString()
        if (text.isBlank()) return
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Transcription", text))
        Toast.makeText(this, "已复制到剪贴板", Toast.LENGTH_SHORT).show()
    }

    private fun clearTranscription() {
        binding.tvTranscription.text = ""
    }

    /**
     * 长任务期间统一置灰按钮 + 显示进度条
     *
     * 「录音」按钮是例外：录音进行中它必须保持可点，否则用户没法停下来。
     */
    private fun setBusy(busy: Boolean, label: String = "") {
        if (!canTouchUi) return
        binding.progressBusy.visibility = if (busy) View.VISIBLE else View.GONE
        binding.btnTranscribe.isEnabled = !busy && engine != null
        binding.btnSelectAudio.isEnabled = !busy
        binding.btnRecord.isEnabled = !busy || isRecording
        binding.btnPlayPause.isEnabled = !busy && mediaPlayer != null
        if (busy && label.isNotEmpty()) {
            binding.tvStatus.text = label
        }
    }

    private fun queryDisplayName(uri: Uri): String {
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index != -1 && cursor.moveToFirst()) {
                return cursor.getString(index)
            }
        }
        return uri.lastPathSegment ?: "已选择的音频"
    }

    override fun onDestroy() {
        super.onDestroy()
        downloader.cancel()
        transcribeJob?.cancel()
        stopPlayback()
        // 引擎关闭排进与推理同一条队列：此刻 lifecycleScope 已取消，但识别协程可能仍在一块
        // 窗口的 run() 里，直接 close 会释放掉它正在用的原生 handle
        val closing = engine
        engine = null
        closeEngineOn(engineDispatcher, closing)
    }

    private companion object {
        val BACKENDS = listOf("CPU", "GPU")
        const val BACKEND_CPU = "CPU"
        const val BACKEND_GPU = "GPU"

        /** 文件转写：5 秒窗口 / 2 秒重叠（官方 `FILE_AUDIO_CHUNK_OVERLAP_DURATION`） */
        val FILE_OVERLAP = 2.seconds

        /** 实时录音：窗口取 `min(5 s, 模型窗口)`、每 1 秒滑一次（官方 `LIVE_AUDIO_CHUNK_*`） */
        val LIVE_INTERVAL = 5.seconds
        val LIVE_SLIDING = 1.seconds

        fun formatMb(bytes: Long): String = "%.1f MB".format(bytes / 1024.0 / 1024.0)
    }
}
