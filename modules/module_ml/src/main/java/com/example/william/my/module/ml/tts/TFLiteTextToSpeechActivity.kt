package com.example.william.my.module.ml.tts

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.ui.activity.BaseVBActivity
import com.example.william.my.module.ml.databinding.MlActivityTextToSpeechBinding
import com.example.william.my.module.ml.download.ModelDownloader
import com.example.william.my.module.ml.download.ModelStore
import com.example.william.my.module.ml.download.RemoteModels
import com.example.william.my.module.ml.helper.buildOffMainThreadSafely
import com.example.william.my.module.ml.helper.canTouchUi
import com.example.william.my.module.ml.helper.closeEngineOn
import com.example.william.my.module.ml.helper.createEngineDispatcher
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.system.measureNanoTime

/**
 * LiteRT — 端侧语音合成（Matcha-TTS）
 *
 * 对应官方 `samples/litert/text_to_speech/kotlin_cpu_gpu`，是「文本 → 语音」模态的端侧链路：
 * 文本 → espeak-IPA G2P（词典 + 神经）→ 音素 id → 文本编码器 → 时长展开 → 扩散(Euler ODE)
 * → mel → HiFi-GAN 声码器 → PCM 波形 → AudioTrack 播放。
 *
 * 与官方一致的三点：
 * 1. **模型不入 APK**：四张图（text encoder / decoder / vocoder / G2P）合计约 88 MB，
 *    官方由 `install_to_device.sh` 推到 `filesDir`，本模块改为首次使用时下载到应用私有目录
 *    （见 [ModelDownloader]）。只有 `emb.bin`、`config.json`、`g2p_meta.json`、
 *    `g2p_dict.txt` 这四个小文件随 assets 分发，与官方相同。
 * 2. **后端分配是模型特定的**：text encoder 与 vocoder 走 GPU，CFM decoder 走 CPU ——
 *    官方注释说明 diffusers transformer 在 Mali ML Drift delegate 上存在算子融合缺陷。
 *    因此本页不提供统一的 CPU/GPU 开关，GPU 不可用时由引擎内部回退并如实标注。
 * 3. **warm up 一次**：官方在加载后先跑一次 2 步合成预热 GPU 与 JIT，避免首次点击时卡顿。
 *
 * 引擎构建、推理、关闭全部走同一条引擎串行队列（见 `TFLiteLifecycleSupport`）：
 * `CompiledModel` 的 `run()` / `close()` 没有同步保护，并发会踩到已释放的原生 handle。
 */
@Route(path = RouterPath.Ml.TextToSpeech)
class TFLiteTextToSpeechActivity :
    BaseVBActivity<MlActivityTextToSpeechBinding>(),
    View.OnClickListener {

    /** 当前引擎（G2P + 合成器）；推理线程与主线程共享，必须 volatile */
    @Volatile
    private var engine: MatchaSynthesizer? = null

    @Volatile
    private var phonemizer: EnglishG2P? = null

    /** 合成、引擎构建、引擎关闭共用的串行队列 */
    private val engineDispatcher = createEngineDispatcher()

    private val downloader by lazy { ModelDownloader(this) }

    private var initJob: Job? = null
    private var synthJob: Job? = null

    /** 当前正在播放的 AudioTrack；再次合成时先停掉它 */
    @Volatile
    private var audioTrack: AudioTrack? = null

    override fun getViewBinding(): MlActivityTextToSpeechBinding = MlActivityTextToSpeechBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        binding.btnSpeak.setOnClickListener(this)
        binding.btnReset.setOnClickListener(this)
        binding.etInput.setText(DEFAULT_TEXT)

        initEngine()
    }

    /**
     * 异步初始化：确保模型就绪（必要时下载）→ 构建引擎 → 预热
     */
    private fun initEngine() {
        initJob?.cancel()
        initJob = lifecycleScope.launch {
            try {
                ensureModels()
                if (!canTouchUi) return@launch

                binding.tvStatus.text = "推理引擎: 正在构建 (Loading graphs…)"

                // 构建排在引擎队列上；若期间页面退出，已建好的实例由 helper 就地释放
                val builtSynthesizer = buildOffMainThreadSafely(engineDispatcher) {
                    MatchaSynthesizer(this@TFLiteTextToSpeechActivity)
                }
                val builtG2P = try {
                    buildOffMainThreadSafely(engineDispatcher) {
                        EnglishG2P(this@TFLiteTextToSpeechActivity)
                    }
                } catch (e: Exception) {
                    // G2P 构建失败时不能把已建好的合成器留在半途，否则原生实例泄漏。
                    // 清理必须走 closeEngineOn：本 catch 最常见的进入原因就是取消（页面退出），
                    // 而被取消的协程进 withContext 会在执行块体前直接抛出，close() 根本不会跑；
                    // closeEngineOn 另起独立作用域派发，取消与否都会真的回收原生实例。
                    closeEngineOn(engineDispatcher, builtSynthesizer)
                    throw e
                }

                engine = builtSynthesizer
                phonemizer = builtG2P

                // 预热 GPU 与 JIT（官方同样在加载后跑一次短合成）
                binding.tvStatus.text = "推理引擎: 预热中 (Warm up…)"
                withContext(engineDispatcher) {
                    builtSynthesizer.synthesize(builtG2P.phonemize(WARM_UP_TEXT), nSteps = 2)
                }

                if (canTouchUi) {
                    binding.tvStatus.text =
                        "推理引擎: ${builtSynthesizer.acceleratorSummary} | " +
                        "采样率 ${MatchaSynthesizer.SAMPLE_RATE} Hz"
                    binding.tvResult.text = "合成结果: 就绪，输入英文后点击「合成并朗读」"
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (canTouchUi) {
                    binding.tvStatus.text = "初始化失败: ${e.message}"
                }
            }
        }
    }

    /**
     * 确保四张图都在本地；缺哪个下哪个（已下载的不会重下）
     *
     * 下载源与官方不同：官方硬编码 `huggingface.co`，本模块走内容一致的镜像
     * （见 `RemoteModel.MIRROR_BASE`）。
     */
    private suspend fun ensureModels() {
        val pending = ModelStore.pendingBytes(this, RemoteModels.MATCHA)
        if (pending <= 0L) return

        if (canTouchUi) {
            binding.progressDownload.visibility = View.VISIBLE
            binding.tvStatus.text = "首次使用，需下载模型约 ${formatMb(pending)}…"
        }

        downloader.ensureAll(RemoteModels.MATCHA) { index, _, _, overall, overallTotal ->
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
                    "下载模型 ${index + 1}/${RemoteModels.MATCHA.size} · $percent% " +
                    "(${formatMb(overall)} / ${formatMb(overallTotal)})"
            }
        }

        if (canTouchUi) {
            binding.progressDownload.visibility = View.GONE
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.btnSpeak -> synthesize()
            binding.btnReset -> binding.etInput.setText(DEFAULT_TEXT)
        }
    }

    private fun synthesize() {
        val text = binding.etInput.text?.toString().orEmpty().trim()
        if (text.isEmpty()) {
            binding.tvResult.text = "合成结果: 请先输入英文"
            return
        }
        if (phonemizer == null || engine == null) {
            binding.tvResult.text = "合成结果: 引擎尚未就绪"
            return
        }

        synthJob?.cancel()
        synthJob = lifecycleScope.launch {
            try {
                stopPlayback()
                if (canTouchUi) {
                    binding.btnSpeak.isEnabled = false
                    binding.tvResult.text = "合成结果: 正在合成…"
                }

                var phonemeCount = 0
                var phonemizeMs = 0.0
                val result = withContext(engineDispatcher) {
                    val activeG2P = phonemizer ?: return@withContext null
                    val activeEngine = engine ?: return@withContext null

                    var ids = IntArray(0)
                    val phonemizeTime = measureNanoTime { ids = activeG2P.phonemize(text) }
                    phonemeCount = ids.size
                    phonemizeMs = phonemizeTime / 1_000_000.0

                    activeEngine.synthesize(ids)
                } ?: return@launch

                if (!canTouchUi) return@launch

                val duration = result.audio.size.toDouble() / MatchaSynthesizer.SAMPLE_RATE
                val rtf = if (duration > 0) result.ms / 1000.0 / duration else 0.0

                binding.tvResult.text = "合成结果: 已朗读 ${"%.2f".format(duration)} 秒音频"
                binding.tvTimeDetails.text =
                    "音素化耗时: ${"%.2f".format(phonemizeMs)} ms | 合成耗时: ${result.ms} ms" +
                    " | RTF ${"%.2f".format(rtf)}"
                binding.tvPipeline.text = buildPipelineReport(
                    phonemeCount = phonemeCount,
                    frames = result.frames,
                    steps = result.steps,
                    samples = result.audio.size,
                )

                // 播放放到 IO 线程：AudioTrack.write(WRITE_BLOCKING) 会阻塞到写完，
                // 不能占着引擎队列，也不能卡主线程
                withContext(Dispatchers.IO) { play(result.audio) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (canTouchUi) {
                    binding.tvResult.text = "合成失败: ${e.message}"
                }
            } finally {
                if (canTouchUi) {
                    binding.btnSpeak.isEnabled = true
                }
            }
        }
    }

    private fun buildPipelineReport(
        phonemeCount: Int,
        frames: Int,
        steps: Int,
        samples: Int,
    ): String {
        val duration = samples.toDouble() / MatchaSynthesizer.SAMPLE_RATE
        return buildString {
            appendLine("音素 id 数  : $phonemeCount")
            appendLine("Mel 帧数    : $frames / ${MatchaSynthesizer.MAX_MEL}")
            appendLine("ODE 步数    : $steps")
            appendLine("波形采样点  : $samples")
            append("时长        : ${"%.2f".format(duration)} s @ ${MatchaSynthesizer.SAMPLE_RATE} Hz")
        }
    }

    /**
     * 用 AudioTrack 播放 PCM 浮点波形
     *
     * 与官方一致使用 MODE_STATIC：一次写满静态缓冲再播放，播放时长由采样数与采样率算出。
     * 这里用 [delay] 而非 `Thread.sleep` —— 官方在受限 worker 上阻塞等待，
     * 协程里改成挂起不会占住线程。
     */
    private suspend fun play(audio: FloatArray) {
        if (audio.isEmpty()) return
        val track = AudioTrack(
            AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).build(),
            AudioFormat.Builder()
                .setSampleRate(MatchaSynthesizer.SAMPLE_RATE)
                .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build(),
            audio.size * BYTES_PER_FLOAT,
            AudioTrack.MODE_STATIC,
            AudioManager.AUDIO_SESSION_ID_GENERATE,
        )
        audioTrack = track
        try {
            track.write(audio, 0, audio.size, AudioTrack.WRITE_BLOCKING)
            track.play()
            delay(audio.size * 1000L / MatchaSynthesizer.SAMPLE_RATE + TAIL_MS)
        } finally {
            if (audioTrack === track) {
                audioTrack = null
            }
            runCatching { track.stop() }
            runCatching { track.release() }
        }
    }

    private fun stopPlayback() {
        val track = audioTrack ?: return
        audioTrack = null
        runCatching { track.stop() }
        runCatching { track.release() }
    }

    override fun onDestroy() {
        super.onDestroy()
        downloader.cancel()
        stopPlayback()
        // 引擎关闭排进与推理同一条队列：此刻 lifecycleScope 已取消，
        // 但后台 run() 不可中断仍会跑完，直接 close 会释放掉它正在用的原生 handle。
        val closingEngine = engine
        val closingG2P = phonemizer
        engine = null
        phonemizer = null
        closeEngineOn(engineDispatcher, closingEngine, closingG2P)
    }

    private companion object {
        const val DEFAULT_TEXT = "Hello, this is Matcha running on your device."
        const val WARM_UP_TEXT = "warm up"
        const val BYTES_PER_FLOAT = 4
        const val TAIL_MS = 250L

        fun formatMb(bytes: Long): String = "%.1f MB".format(bytes / 1024.0 / 1024.0)
    }
}
