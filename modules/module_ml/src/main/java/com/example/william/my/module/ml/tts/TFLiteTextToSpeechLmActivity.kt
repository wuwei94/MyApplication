package com.example.william.my.module.ml.tts

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.core.base.ui.activity.BaseVBActivity
import com.example.william.my.module.ml.databinding.MlActivityTextToSpeechLmBinding
import com.example.william.my.module.ml.download.ModelDownloader
import com.example.william.my.module.ml.download.ModelStore
import com.example.william.my.module.ml.download.RemoteModels
import com.example.william.my.module.ml.helper.buildOffMainThreadSafely
import com.example.william.my.module.ml.helper.canTouchUi
import com.example.william.my.module.ml.helper.closeEngineOn
import com.example.william.my.module.ml.helper.createEngineDispatcher
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray

/**
 * LiteRT — 语音语言模型语音合成（Qwen3-TTS）
 *
 * 对应官方 `samples/litert/text_to_speech_lm/kotlin_cpu`。这是本模块第三个语音合成页，
 * 也是**最重**的一个：
 *
 * | 页面 | 任务 | 图的组织 | 模型体积 |
 * |---|---|---|---|
 * | [TFLiteTextToSpeechActivity] | `text_to_speech` | 文本编码 + 扩散解码 + 声码器（G2P 前端） | 约 88 MB |
 * | [TFLiteTextToSpeechStreamingActivity] | `text_to_speech_streaming` | 三张动态长度图，逐句流式 | 约 32 MB |
 * | 本页 | `text_to_speech_lm` | talker + MTP + codec 三图由宿主循环串联 | 约 1.67 GB |
 *
 * 与流式页的关键差别：**这里不是流式**。talker 要先自回归把整段文本吐成 12.5 Hz 的帧序列，
 * 再走 codec 分块还原波形，因此「点一次 → 等一段时间 → 一次性播放」，中途无法按句迭代输出。
 * 官方在 Pixel 8a 上实测 RTF ≈ 6.7，所以合成 4 秒音频要等约 28 秒，界面必须把这一点讲清楚。
 *
 * 模型走运行时下载（约 1.67 GB，11 个文件），下载层带断点续传；已下载的不会重下。
 * 推理、引擎构建、引擎关闭共用同一条引擎串行队列（见 [createEngineDispatcher]）。
 */
@Route(path = RouterPath.Ml.TextToSpeechLm)
class TFLiteTextToSpeechLmActivity :
    BaseVBActivity<MlActivityTextToSpeechLmBinding>(),
    View.OnClickListener {

    /** 三图引擎；引擎队列与主线程共享，必须 volatile */
    @Volatile
    private var engine: Qwen3TtsEngine? = null

    /** 推理、引擎构建、引擎关闭共用的串行队列 */
    private val engineDispatcher = createEngineDispatcher()

    private val downloader by lazy { ModelDownloader(this) }

    private var initJob: Job? = null
    private var speakJob: Job? = null

    /** 当前播放的 AudioTrack；再次朗读或退出页面时先停掉它 */
    @Volatile
    private var audioTrack: AudioTrack? = null

    /**
     * 用户是否已放弃本段结果
     *
     * 三图前向是**一整段不可中断的调用**，取消协程并不能让它提前返回，因此「停止」不是取消
     * 协程，而是打这个标记：波形算完后就地丢弃、不播放，界面立刻回到可交互状态。
     */
    @Volatile
    private var abandoned = false

    override fun getViewBinding(): MlActivityTextToSpeechLmBinding = MlActivityTextToSpeechLmBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        binding.spLanguage.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, Qwen3TtsEngine.LANGUAGES).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
        binding.btnSpeak.setOnClickListener(this)
        binding.btnStop.setOnClickListener(this)
        binding.etInput.setText(DEFAULT_TEXT)

        initEngine()
    }

    /** 确保 11 个文件就绪（必要时下载）→ 构建三图引擎 → 跑分词器启动自检 */
    private fun initEngine() {
        initJob?.cancel()
        initJob = lifecycleScope.launch {
            try {
                ensureModels()
                if (!canTouchUi) return@launch

                val pending = ModelStore.pendingBytes(this@TFLiteTextToSpeechLmActivity, RemoteModels.QWEN)
                if (pending > 0L) {
                    binding.tvStatus.text = "模型不完整，仍有 ${formatMb(pending)} 未就绪"
                    return@launch
                }

                binding.tvStatus.text = "推理引擎: 正在构建三张图（首次约需数十秒）..."

                // 构建排在引擎队列上；若期间页面退出，已建好的实例由 helper 就地释放
                val built = buildOffMainThreadSafely(engineDispatcher) {
                    Qwen3TtsEngine(ModelStore.dir(this@TFLiteTextToSpeechLmActivity))
                }
                engine = built
                if (canTouchUi) {
                    binding.tvStatus.text = "推理引擎: 正在自检分词器..."
                }

                // 自检与参考向量比对在同一张图上跑；只读文件，不改状态
                val mismatches = withContext(engineDispatcher) {
                    built.findTokenizerMismatches(readTestVectors())
                }

                if (canTouchUi) {
                    val selfTest = if (mismatches.isEmpty()) {
                        "分词器自检 PASS"
                    } else {
                        "分词器自检 FAIL (${mismatches.size} 条不一致)"
                    }
                    binding.tvStatus.text =
                        "推理引擎: 就绪 · 全离线 | $selfTest | 词表 ${built.tokenizerSize} | " +
                        "图 ${built.graphNames}"
                    binding.tvMetrics.text = "实时指标: 就绪，点击「合成并朗读」"
                    binding.tvStats.text = mismatches.joinToString("\n").ifEmpty { "等待合成..." }
                    binding.btnSpeak.isEnabled = true
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: OutOfMemoryError) {
                // 三图 + KV cache 峰值在 1.5 GB 量级，内存不足的设备必须给出可读的失败原因
                if (canTouchUi) {
                    binding.tvStatus.text = "初始化失败: 内存不足（峰值约 1.5 GB，建议 8 GB 内存设备）"
                }
            } catch (e: Exception) {
                if (canTouchUi) {
                    binding.tvStatus.text = "初始化失败: ${e.message}"
                }
            }
        }
    }

    /**
     * 确保 11 个模型文件都在本地；缺哪个下哪个（已下载的不会重下）
     *
     * 单文件最大 622 MB（文本嵌入表），下载层用 `Range` 请求续传，中断后重进页面会接着下。
     */
    private suspend fun ensureModels() {
        val pending = ModelStore.pendingBytes(this, RemoteModels.QWEN)
        if (pending <= 0L) return

        if (canTouchUi) {
            binding.progressDownload.visibility = View.VISIBLE
            binding.tvStatus.text = "首次使用，需下载模型约 ${formatMb(pending)}（共 ${RemoteModels.QWEN.size} 个文件）..."
        }

        downloader.ensureAll(RemoteModels.QWEN) { index, _, _, overall, overallTotal ->
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
                    "下载模型 ${index + 1}/${RemoteModels.QWEN.size} · ${RemoteModels.QWEN[index].fileName} · " +
                    "$percent% (${formatMb(overall)} / ${formatMb(overallTotal)})"
            }
        }

        if (canTouchUi) {
            binding.progressDownload.visibility = View.GONE
        }
    }

    /** 读取官方 baked 的参考 token id 向量（`assets/tokenizer_test_vectors.json`） */
    private fun readTestVectors(): List<Pair<String, IntArray>> {
        val json = assets.open(TOKENIZER_VECTORS_ASSET).bufferedReader().use { it.readText() }
        val cases = JSONArray(json)
        return (0 until cases.length()).map { index ->
            val case = cases.getJSONObject(index)
            val ids = case.getJSONArray("ids")
            case.getString("text") to IntArray(ids.length()) { ids.getInt(it) }
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.btnSpeak -> speak()
            binding.btnStop -> stopSpeaking()
        }
    }

    private fun speak() {
        val text = binding.etInput.text?.toString().orEmpty().trim()
        if (text.isEmpty()) {
            binding.tvMetrics.text = "实时指标: 请先输入文本"
            return
        }
        // 按钮的禁用是在协程体里做的，同一次事件循环内的连点仍可能排进两段合成，这里再挡一道
        if (speakJob?.isActive == true) {
            binding.tvMetrics.text = "实时指标: 上一段仍在合成中，请稍候"
            return
        }
        val active = engine
        if (active == null) {
            binding.tvMetrics.text = "实时指标: 引擎尚未就绪"
            return
        }
        val language = binding.spLanguage.selectedItem as? String ?: DEFAULT_LANGUAGE

        speakJob = lifecycleScope.launch {
            try {
                stopPlayback()
                abandoned = false
                if (canTouchUi) {
                    binding.btnSpeak.isEnabled = false
                    binding.btnStop.isEnabled = true
                    binding.tvMetrics.text = "实时指标: 正在合成…（整段一次前向，不可中断）"
                    binding.tvStats.text = "等待合成..."
                }

                val synthesized = withContext(engineDispatcher) {
                    active.synthesize(text, language = language) { frame ->
                        // 回调在推理线程；每 5 帧刷一次，够密也够省
                        if (frame % PROGRESS_STEP == 0) {
                            runOnUiThread {
                                if (!canTouchUi || abandoned) return@runOnUiThread
                                val seconds = "%.1f".format(frame / FRAME_RATE)
                                binding.tvMetrics.text = "实时指标: 已生成 $frame 帧（$seconds s 音频）…"
                            }
                        }
                    }
                }

                if (abandoned) {
                    return@launch
                }
                if (canTouchUi) {
                    binding.tvMetrics.text = "实时指标: ${buildMetrics(synthesized)}"
                    binding.tvStats.text = buildStats(synthesized)
                }
                play(synthesized.audio)
            } catch (e: CancellationException) {
                throw e
            } catch (e: OutOfMemoryError) {
                if (canTouchUi) {
                    binding.tvMetrics.text = "合成失败: 内存不足（KV cache 与三图合计峰值约 1.5 GB）"
                }
            } catch (e: Exception) {
                if (canTouchUi) {
                    binding.tvMetrics.text = "合成失败: ${e.message}"
                }
            } finally {
                abandoned = false
                if (canTouchUi) {
                    binding.btnSpeak.isEnabled = true
                    binding.btnStop.isEnabled = false
                }
            }
        }
    }

    private fun buildMetrics(result: Qwen3TtsEngine.Result): String {
        val slow = if (result.realTimeFactor > 1) "慢于实时" else "快于实时"
        return "首帧 ${result.prefillMs} ms | RTF ${"%.2f".format(result.realTimeFactor)} ($slow) | " +
            "${result.frames} 帧 = ${"%.2f".format(result.audioSeconds)} s 音频"
    }

    private fun buildStats(result: Qwen3TtsEngine.Result): String = buildString {
        appendLine("帧数   : ${result.frames} 帧 (12.5 Hz -> ${"%.2f".format(result.audioSeconds)} s)")
        appendLine("预填充 : ${result.prefillMs} ms")
        appendLine("talker : ${result.talkerMs} ms")
        appendLine("MTP    : ${result.mtpMs} ms")
        appendLine("codec  : ${result.codecMs} ms")
        append("合计   : ${result.graphMs} ms")
    }

    /**
     * 一次性播放
     *
     * 三图前向产出的是**完整波形**（不像 KittenTTS 页那样边算边放），因此用
     * [AudioTrack.MODE_STATIC] 把整段写进单块缓冲即可：最坏情况 41 s × 24 kHz × 4 B ≈ 3.9 MB，
     * 在可控范围内。
     */
    private fun play(audio: FloatArray) {
        if (audio.isEmpty()) return
        stopPlayback()
        val track = AudioTrack(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build(),
            AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                .setSampleRate(Qwen3TtsEngine.SAMPLE_RATE)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build(),
            audio.size * BYTES_PER_FLOAT,
            AudioTrack.MODE_STATIC,
            AudioManager.AUDIO_SESSION_ID_GENERATE,
        )
        track.write(audio, 0, audio.size, AudioTrack.WRITE_BLOCKING)
        audioTrack = track
        // MODE_STATIC 播完不会自动释放；挂一个标记监听器，播到结尾就地回收，避免 track
        // 一直悬挂到下一次操作。监听器回调与 stopPlayback / 下一次 play 都在主线程
        // （AudioTrack 建于主线程，监听按契约回主线程 Looper），用「仍是当前 track」
        // 判等防止竞态；release 也必须放在判等内——否则 stopPlayback 已释放过同一个
        // track 时，这里会二次 release。
        track.setNotificationMarkerPosition(audio.size)
        track.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
            override fun onMarkerReached(track: AudioTrack) {
                if (audioTrack === track) {
                    audioTrack = null
                    runCatching { track.release() }
                }
            }

            override fun onPeriodicNotification(track: AudioTrack) = Unit
        })
        track.play()
    }

    /**
     * 停止：丢弃本段结果并停掉正在播放的音频
     *
     * 三图前向**不可中断**，因此这里不取消协程（取消也拦不住已经进图的那次调用），
     * 只打标记 + 停播放；波形算完后就地丢弃。「合成并朗读」按钮会一直禁用，直到本段前向收尾。
     */
    private fun stopSpeaking() {
        abandoned = true
        stopPlayback()
        if (canTouchUi) {
            binding.tvMetrics.text = "实时指标: 已停止（本段前向在后台静默收尾，收尾前无法开始新的一段）"
            binding.btnStop.isEnabled = false
        }
    }

    private fun stopPlayback() {
        val track = audioTrack ?: return
        audioTrack = null
        runCatching { track.pause() }
        runCatching { track.flush() }
        runCatching { track.release() }
    }

    override fun onDestroy() {
        super.onDestroy()
        downloader.cancel()
        stopPlayback()
        // 引擎关闭排进与推理同一条队列：此刻 lifecycleScope 已取消，
        // 但后台那次前向不可中断仍会跑完，直接 close 会释放掉它正在用的原生 handle
        val closing = engine
        engine = null
        closeEngineOn(engineDispatcher, closing)
    }

    private companion object {
        const val DEFAULT_TEXT = "Hello! This is Qwen3 text to speech running on device."
        const val DEFAULT_LANGUAGE = "english"
        const val TOKENIZER_VECTORS_ASSET = "tokenizer_test_vectors.json"

        /** 官方模型输出 12.5 Hz 帧 */
        const val FRAME_RATE = 12.5

        const val PROGRESS_STEP = 5
        const val BYTES_PER_FLOAT = 4

        fun formatMb(bytes: Long): String = "%.1f MB".format(bytes / 1024.0 / 1024.0)
    }
}
