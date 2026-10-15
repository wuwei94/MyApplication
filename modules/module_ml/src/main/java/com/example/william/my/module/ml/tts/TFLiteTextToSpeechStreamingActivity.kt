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
import com.example.william.my.module.ml.databinding.MlActivityTextToSpeechStreamingBinding
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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * LiteRT — 流式语音合成（KittenTTS nano）
 *
 * 对应官方 `samples/litert/text_to_speech_streaming/kotlin_cpu`。与同目录下的 Matcha 示例
 * （[TFLiteTextToSpeechActivity]）是**两个不同的官方任务**，差别不在于换了模型：
 *
 * 1. **动态序列长度**：三张图都接受任意长度，不做 padding 分桶，算力随文本长度伸缩；
 *    因此每句都要先 `resizeInput` 再跑，而不是固定形状一次算完。
 * 2. **流式播放**：文本先按句切分（[SentenceChunker]），然后生产者/消费者一对协程——
 *    合成协程把每句 PCM 推进 channel，播放协程在同一条流式 [AudioTrack] 上边收边放。
 *    第 N 句在响的同时第 N+1 句在算，所以首音延迟只有一句（界面上以 TTFA 直接展示）。
 * 3. **小而全离线**：三张图合计约 32 MB，8 个音色，24 kHz。
 *
 * 三张图统一走经典 `Interpreter`（原因见 [KittenSynthesizer] 的类注释：官方对该示例的
 * 声码器用了临时 JNI 桥，而 `Interpreter` 本来就支持动态输出）。
 *
 * 合成、引擎构建、引擎关闭排在同一条引擎串行队列上；播放单独跑在 IO 线程。
 */
@Route(path = RouterPath.Ml.TextToSpeechStreaming)
class TFLiteTextToSpeechStreamingActivity :
    BaseVBActivity<MlActivityTextToSpeechStreamingBinding>(),
    View.OnClickListener {

    /** 合成器；引擎队列与主线程共享，必须 volatile */
    @Volatile
    private var synthesizer: KittenSynthesizer? = null

    /** G2P 前端；同上 */
    @Volatile
    private var phonemizer: EnglishG2P? = null

    /** 合成、引擎构建、引擎关闭共用的串行队列 */
    private val engineDispatcher = createEngineDispatcher()

    private val downloader by lazy { ModelDownloader(this) }

    private var initJob: Job? = null
    private var speakJob: Job? = null

    /**
     * 当前流式播放的 AudioTrack；再次朗读或退出页面时先停掉它
     *
     * 主线程赋值 / 判空，播放协程（IO）的 finally 里置 null —— 跨线程访问，靠 @Volatile 保证可见性
     */
    @Volatile
    private var audioTrack: AudioTrack? = null

    override fun getViewBinding(): MlActivityTextToSpeechStreamingBinding = MlActivityTextToSpeechStreamingBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        binding.spVoice.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, KittenSynthesizer.VOICES).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
        binding.spVoice.setSelection(KittenSynthesizer.VOICES.indexOf(DEFAULT_VOICE).coerceAtLeast(0))

        binding.btnSpeak.setOnClickListener(this)
        binding.btnStop.setOnClickListener(this)
        binding.etInput.setText(DEFAULT_TEXT)

        initEngine()
    }

    /** 确保模型就绪（必要时下载）→ 构建 G2P 与合成器 → 预热一次 */
    private fun initEngine() {
        initJob?.cancel()
        initJob = lifecycleScope.launch {
            try {
                ensureModels()
                if (!canTouchUi) return@launch

                binding.tvStatus.text = "推理引擎: 正在构建 (Loading graphs…)"

                // 构建排在引擎队列上；若期间页面退出，已建好的实例由 helper 就地释放
                val builtSynth = buildOffMainThreadSafely(engineDispatcher) {
                    KittenSynthesizer(this@TFLiteTextToSpeechStreamingActivity)
                }
                val builtG2P = try {
                    // KittenTTS 的时长预测器是对着「标点单独成词」的前端训练的，与 Matcha 不同
                    buildOffMainThreadSafely(engineDispatcher) {
                        EnglishG2P(this@TFLiteTextToSpeechStreamingActivity, punctuationSpaced = true)
                    }
                } catch (e: Exception) {
                    // G2P 构建失败时不能把已建好的合成器留在半途，否则原生实例泄漏。
                    // 清理必须走 closeEngineOn：本 catch 最常见的进入原因就是取消（页面退出），
                    // 而被取消的协程进 withContext 会在执行块体前直接抛出，close() 根本不会跑；
                    // closeEngineOn 另起独立作用域派发，取消与否都会真的回收原生实例。
                    closeEngineOn(engineDispatcher, builtSynth)
                    throw e
                }

                synthesizer = builtSynth
                phonemizer = builtG2P

                // 预热 XNNPACK 打包与 JIT，让首次点击测到的是稳态 TTFA（官方同样预热一次）
                binding.tvStatus.text = "推理引擎: 预热中 (Warm up…)"
                withContext(engineDispatcher) {
                    builtSynth.synthesize(builtG2P.phonemize(WARM_UP_TEXT), voice = 0, textLength = 8)
                }

                if (canTouchUi) {
                    binding.tvStatus.text =
                        "推理引擎: 就绪 · 全离线 | 采样率 ${KittenSynthesizer.SAMPLE_RATE} Hz | " +
                        "模型 ${formatMb(builtSynth.modelBytes)}"
                    binding.tvMetrics.text = "实时指标: 就绪，点击「朗读」"
                    binding.btnSpeak.isEnabled = true
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
     * 确保三张图 + 音色表 + G2P 都在本地；缺哪个下哪个（已下载的不会重下）
     *
     * G2P 与 Matcha 页共用同一个落盘文件名，因此先做过任一页面后另一个不会再下这一份。
     */
    private suspend fun ensureModels() {
        val pending = ModelStore.pendingBytes(this, RemoteModels.KITTEN)
        if (pending <= 0L) return

        if (canTouchUi) {
            binding.progressDownload.visibility = View.VISIBLE
            binding.tvStatus.text = "首次使用，需下载模型约 ${formatMb(pending)}…"
        }

        downloader.ensureAll(RemoteModels.KITTEN) { index, _, _, overall, overallTotal ->
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
                    "下载模型 ${index + 1}/${RemoteModels.KITTEN.size} · $percent% " +
                    "(${formatMb(overall)} / ${formatMb(overallTotal)})"
            }
        }

        if (canTouchUi) {
            binding.progressDownload.visibility = View.GONE
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.btnSpeak -> speak()
            binding.btnStop -> stopSpeaking()
        }
    }

    /**
     * 按句流式合成并播放
     *
     * 生产者/消费者：本协程逐句合成并推进 [Channel]，播放协程在同一条流式 AudioTrack 上
     * 边收边放。**channel 是无界的**，所以 `send` 不会挂起——也因此每轮循环必须显式
     * [ensureActive]，否则「停止」后它仍会把整段文本合完才会察觉。
     */
    private fun speak() {
        val text = binding.etInput.text?.toString().orEmpty().trim()
        if (text.isEmpty()) {
            binding.tvMetrics.text = "实时指标: 请先输入英文"
            return
        }
        if (phonemizer == null || synthesizer == null) {
            binding.tvMetrics.text = "实时指标: 引擎尚未就绪"
            return
        }
        val sentences = SentenceChunker.chunk(text)
        if (sentences.isEmpty()) {
            binding.tvMetrics.text = "实时指标: 切句后没有可合成的内容"
            return
        }

        speakJob?.cancel()
        speakJob = lifecycleScope.launch {
            val voice = binding.spVoice.selectedItemPosition
            stopPlayback()

            val pcmChannel = Channel<FloatArray>(Channel.UNLIMITED)
            val track = newAudioTrack()
            audioTrack = track
            val player = launch(Dispatchers.IO) {
                var framesWritten = 0L
                try {
                    track.play()
                    for (pcm in pcmChannel) {
                        // 分片阻塞写：一次最多写 200 ms，让「停止」能及时生效
                        var offset = 0
                        while (offset < pcm.size && isActive) {
                            val count = minOf(WRITE_SLICE_FRAMES, pcm.size - offset)
                            track.write(pcm, offset, count, AudioTrack.WRITE_BLOCKING)
                            offset += count
                        }
                        framesWritten += offset
                        if (!isActive) break
                    }
                    // 排空：等最后一个已入缓冲的采样真正播完
                    while (isActive && track.playbackHeadPosition < framesWritten) {
                        delay(DRAIN_POLL_MS)
                    }
                } finally {
                    if (audioTrack === track) {
                        audioTrack = null
                    }
                    runCatching { track.pause() }
                    runCatching { track.flush() }
                    runCatching { track.release() }
                }
            }

            try {
                if (canTouchUi) {
                    binding.btnSpeak.isEnabled = false
                    binding.btnStop.isEnabled = true
                    binding.tvMetrics.text = "实时指标: 正在合成…"
                }

                var synthMs = 0L
                var audioSamples = 0L
                var ttfaMs = 0L
                val started = System.nanoTime()

                for ((index, sentence) in sentences.withIndex()) {
                    ensureActive()
                    val result = withContext(engineDispatcher) {
                        val activeG2P = phonemizer ?: return@withContext null
                        val activeSynth = synthesizer ?: return@withContext null
                        val ids = activeG2P.phonemize(sentence)
                        if (ids.isEmpty()) return@withContext null
                        activeSynth.synthesize(ids, voice, sentence.length)
                    } ?: continue

                    if (index == 0) {
                        ttfaMs = (System.nanoTime() - started) / 1_000_000
                    }
                    synthMs += result.synthMs
                    audioSamples += result.audio.size
                    pcmChannel.send(result.audio)

                    if (canTouchUi) {
                        val audioSeconds = audioSamples.toDouble() / KittenSynthesizer.SAMPLE_RATE
                        // RTF = 合成算力 / 音频时长，小于 1 说明合成一直跑在播放前面
                        val rtf = if (audioSeconds > 0) synthMs / 1000.0 / audioSeconds else 0.0
                        binding.tvMetrics.text =
                            "实时指标: 首音 $ttfaMs ms | RTF ${"%.2f".format(rtf)} " +
                            "(${if (rtf < 1) "快于播放" else "慢于播放"})"
                        binding.tvStats.text = buildStats(
                            sentencesDone = index + 1,
                            sentencesTotal = sentences.size,
                            synthMs = synthMs,
                            audioSeconds = audioSeconds,
                            tokens = result.tokens,
                            frames = result.frames,
                        )
                    }
                }
                pcmChannel.close()
                player.join()
                if (canTouchUi) {
                    binding.tvMetrics.text = "实时指标: 播放完成，可再次点击「朗读」"
                }
            } catch (e: CancellationException) {
                pcmChannel.close()
                player.cancel()
                throw e
            } catch (e: Exception) {
                pcmChannel.close()
                player.cancel()
                if (canTouchUi) {
                    binding.tvMetrics.text = "合成失败: ${e.message}"
                }
            } finally {
                if (canTouchUi) {
                    binding.btnSpeak.isEnabled = true
                    binding.btnStop.isEnabled = false
                }
            }
        }
    }

    private fun buildStats(
        sentencesDone: Int,
        sentencesTotal: Int,
        synthMs: Long,
        audioSeconds: Double,
        tokens: Int,
        frames: Int,
    ): String = buildString {
        appendLine("句子     : $sentencesDone / $sentencesTotal")
        appendLine("最近一句 : $tokens token -> $frames 帧")
        appendLine("音频累计 : ${"%.2f".format(audioSeconds)} s")
        append("合成累计 : $synthMs ms")
    }

    private fun stopSpeaking() {
        speakJob?.cancel()
        speakJob = null
        stopPlayback()
        if (canTouchUi) {
            binding.tvMetrics.text = "实时指标: 已停止"
            binding.btnSpeak.isEnabled = true
            binding.btnStop.isEnabled = false
        }
    }

    /** 流式播放用的 AudioTrack：缓冲区给足，避免逐句写入频繁阻塞合成 */
    private fun newAudioTrack(): AudioTrack {
        val minBytes = AudioTrack.getMinBufferSize(
            KittenSynthesizer.SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_FLOAT,
        )
        return AudioTrack(
            AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).build(),
            AudioFormat.Builder()
                .setSampleRate(KittenSynthesizer.SAMPLE_RATE)
                .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build(),
            maxOf(minBytes, KittenSynthesizer.SAMPLE_RATE * BYTES_PER_FLOAT),
            AudioTrack.MODE_STREAM,
            AudioManager.AUDIO_SESSION_ID_GENERATE,
        )
    }

    /**
     * 停掉当前流式播放，但**不 release** 底层 AudioTrack。
     *
     * release 唯一发生在播放协程的 finally 里：那才是与 [AudioTrack.write] 同一线程、
     * 不会与阻塞写发生 use-after-release 竞态的地方。这里只做 pause + flush，让播放协程
     * 下一次 `isActive` 检查（或写满后退出）自然收尾。`audioTrack` 字段由播放协程 finally
     * 用「仍是当前 track」判等后置 null，这里也不动它，避免与下一次 speak 抢着置 null。
     */
    private fun stopPlayback() {
        val track = audioTrack ?: return
        runCatching { track.pause() }
        runCatching { track.flush() }
    }

    override fun onDestroy() {
        super.onDestroy()
        downloader.cancel()
        stopPlayback()
        // 引擎关闭排进与推理同一条队列：此刻 lifecycleScope 已取消，
        // 但后台 run() 不可中断仍会跑完，直接 close 会释放掉它正在用的原生 handle。
        val closingSynth = synthesizer
        val closingG2P = phonemizer
        synthesizer = null
        phonemizer = null
        closeEngineOn(engineDispatcher, closingSynth, closingG2P)
    }

    private companion object {
        const val DEFAULT_TEXT =
            "Hi there! I am a tiny voice with fifteen million parameters. " +
                "Everything you hear is generated on this phone, fully offline, " +
                "streaming sentence by sentence while the rest is still being synthesized."

        const val DEFAULT_VOICE = "expr-voice-5-m"
        const val WARM_UP_TEXT = "warm up,"

        /** 每次写入 200 ms，阻塞写不会让取消等待过久 */
        const val WRITE_SLICE_FRAMES = KittenSynthesizer.SAMPLE_RATE / 5
        const val DRAIN_POLL_MS = 50L
        const val BYTES_PER_FLOAT = 4

        fun formatMb(bytes: Long): String = "%.1f MB".format(bytes / 1024.0 / 1024.0)
    }
}
