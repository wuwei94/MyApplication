import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';
import 'package:flutter_demo/demos/ml/engines/english_g2p.dart';
import 'package:flutter_demo/demos/ml/engines/kitten_synthesizer.dart';
import 'package:flutter_demo/demos/ml/engines/kitten_worker.dart';
import 'package:flutter_demo/demos/ml/engines/ml_audio.dart';
import 'package:flutter_demo/demos/ml/engines/ml_model_download.dart';
import 'package:flutter_demo/demos/ml/engines/sentence_chunker.dart';

/// LiteRT — 流式语音合成（KittenTTS nano）
///
/// 与 Android `:modules:module_ml` 的 `TFLiteTextToSpeechStreamingActivity` 一一对应，
/// 也与同目录的 Matcha 页（[MlTextToSpeechDemoPage]）是**两个不同的官方任务**，
/// 差别不在于换了模型：
///
/// 1. **动态序列长度**：三张图都接受任意长度，不做 padding 分桶，算力随文本长度伸缩；
///    因此每句都要先 `resizeInput` 再跑，而不是固定形状一次算完；
/// 2. **流式播放**：文本先按句切分（[sentenceChunk]），然后生产者/消费者一对——
///    合成在后台 isolate 上逐句进行，播放在主 isolate 上按句排队：第 N 句在响的
///    同时第 N+1 句在算，所以首音延迟只有一句（界面上以 TTFA 直接展示）；
/// 3. **小而全离线**：三张图合计约 32 MB，8 个音色，24 kHz。
///
/// 与 Android 的两处实现差异（都属**功能对等、架构照搬不了**，非需求偏差）：
/// - Android 的 producer 跑在引擎串行队列（协程），本侧合成是同步阻塞调用，
///   只能整体搬进后台 isolate —— UI 线程因此全程不被合成占住，但**音素化**
///   （词典查表 + 偶发神经 G2P）仍在主 isolate，单句耗时在毫秒级；
/// - Android 用一条流式 `AudioTrack` 边收边放，本侧 audioplayers 一次只能挂一个
///   源，按句 `playWav` 排队（上一句播完再播下一句），重叠发生在
///   「播放第 N 句 × 合成第 N+1 句」之间，与 Android 同曲目。
///
/// 官方参考：
/// https://github.com/google-ai-edge/litert-samples/tree/main/samples/litert/text_to_speech_streaming
class MlTextToSpeechStreamingDemoPage extends BasicImagePage {
  const MlTextToSpeechStreamingDemoPage({super.key, required super.title});

  @override
  BasicImagePageState<MlTextToSpeechStreamingDemoPage> createState() =>
      _MlTextToSpeechStreamingDemoPageState();
}

class _MlTextToSpeechStreamingDemoPageState
    extends BasicImagePageState<MlTextToSpeechStreamingDemoPage> {
  /// 与 Android `DEFAULT_TEXT` 一致
  static const String _defaultText =
      'Hi there! I am a tiny voice with fifteen million parameters. '
      'Everything you hear is generated on this phone, fully offline, '
      'streaming sentence by sentence while the rest is still being synthesized.';

  /// 与 Android `DEFAULT_VOICE` 一致
  static const String _defaultVoice = 'expr-voice-5-m';

  /// 与 Android `WARM_UP_TEXT` 一致
  static const String _warmUpText = 'warm up,';

  final TextEditingController _inputController = TextEditingController();

  EnglishG2P? _phonemizer;
  KittenWorkerClient? _worker;
  MlAudioPlayer? _player;

  String _voice = _defaultVoice;
  bool _initializing = false;
  bool _speaking = false;
  bool _downloading = false;
  double _downloadProgress = 0.0;

  /// 朗读代号：重复触发或「停止」时自增，过期轮次的结果不得落盘到 UI
  int _generation = 0;

  MlModelDownloader? _downloader;

  String _statusText = '初始化中';
  String _metricsText = '实时指标: 就绪，点击「朗读」';
  String _statsText = '';

  @override
  void initState() {
    super.initState();
    _inputController.text = _defaultText;
    _initEngine();
  }

  @override
  void dispose() {
    _downloader?.cancel();
    final MlAudioPlayer? player = _player;
    _player = null;
    player?.dispose();
    final KittenWorkerClient? worker = _worker;
    _worker = null;
    // 后台 isolate 必须显式关闭：解释器持有原生内存与 mmap，页面销毁不杀
    // isolate 的话它会连模型一起泄漏到进程结束
    worker?.close();
    final EnglishG2P? phonemizer = _phonemizer;
    _phonemizer = null;
    phonemizer?.close();
    _inputController.dispose();
    super.dispose();
  }

  @override
  List<String> buildList() => <String>[
        '1. 朗读（流式合成并播放）',
        '2. 停止播放',
        '3. 恢复默认文本',
        '4. 切换音色（当前：$_voice）',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        _speak();
      case 1:
        _stop();
      case 2:
        setState(() {
          _inputController.text = _defaultText;
        });
      case 3:
        setState(() {
          final int index = KittenSynthesizer.voices.indexOf(_voice);
          _voice = KittenSynthesizer.voices[(index + 1) % KittenSynthesizer.voices.length];
        });
    }
  }

  @override
  Widget buildPreview() {
    return Padding(
      padding: const EdgeInsets.all(BasicDemoDimens.pagePadding),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: <Widget>[
          const Text(
            '输入英文，逐句流式合成并朗读（15M 参数 · 24 kHz · 全离线）',
            style: TextStyle(fontSize: BasicDemoDimens.fontTitle),
          ),
          const SizedBox(height: BasicDemoDimens.itemPadding),
          DropdownButtonFormField<String>(
            initialValue: _voice,
            decoration: const InputDecoration(
              labelText: '音色（voices.npz，按文本长度取行）',
              border: OutlineInputBorder(),
              isDense: true,
            ),
            items: <DropdownMenuItem<String>>[
              for (final String voice in KittenSynthesizer.voices)
                DropdownMenuItem<String>(value: voice, child: Text(voice)),
            ],
            onChanged: (String? value) {
              if (value == null) return;
              setState(() {
                _voice = value;
              });
            },
          ),
          const SizedBox(height: BasicDemoDimens.itemPadding),
          TextField(
            controller: _inputController,
            maxLines: 3,
            decoration: const InputDecoration(
              border: OutlineInputBorder(),
              isDense: true,
            ),
          ),
          const SizedBox(height: BasicDemoDimens.itemPadding),
          if (_downloading) ...<Widget>[
            ClipRRect(
              borderRadius: BorderRadius.circular(4),
              child: LinearProgressIndicator(
                value: _downloadProgress,
                minHeight: 8,
                backgroundColor: Colors.grey.shade200,
              ),
            ),
            const SizedBox(height: 4),
          ],
          Text(
            _statusText,
            style: const TextStyle(
              fontSize: BasicDemoDimens.fontBody,
              color: Colors.blueGrey,
            ),
          ),
          const SizedBox(height: 4),
          Text(
            _metricsText,
            style: const TextStyle(
              fontSize: BasicDemoDimens.fontTitle,
              fontWeight: FontWeight.bold,
              color: Colors.blue,
            ),
          ),
          const SizedBox(height: BasicDemoDimens.itemPadding),
          Expanded(
            child: SingleChildScrollView(
              child: Text(
                _statsText.isEmpty ? '（朗读时在此展示逐句指标）' : _statsText,
                style: TextStyle(
                  fontSize: BasicDemoDimens.fontBody,
                  fontFamily: 'monospace',
                  color: Colors.grey.shade800,
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  // ─────────────────── 引擎装配 ───────────────────

  /// 异步初始化：确保模型就绪（必要时下载）→ 构建 G2P → 拉起后台 isolate → 预热
  Future<void> _initEngine() async {
    if (_initializing) return;
    final int generation = ++_generation;
    _initializing = true;
    _downloader ??= MlModelDownloader();

    try {
      await _ensureModels();
      if (!mounted || generation != _generation) return;

      setState(() {
        _statusText = '推理引擎: 正在构建 (Loading graphs…)';
      });

      // G2P 前端留在主 isolate：词典查表是纯内存操作，词典外的词才跑一次神经 G2P，
      // 单句毫秒级；把它搬进 worker 反而要为 rootBundle 引入后台 isolate 的
      // 二进制信使，得不偿失
      final EnglishG2P phonemizer = EnglishG2P(punctuationSpaced: true);
      await phonemizer.load();

      final String modelsDir = (await MlModelStore.dir()).path;
      final KittenWorkerClient worker = KittenWorkerClient(modelsDir: modelsDir);
      await worker.start();

      if (!mounted || generation != _generation) {
        await worker.close();
        phonemizer.close();
        return;
      }

      _phonemizer = phonemizer;
      _worker = worker;

      // 预热：官方同样在加载后跑一次短合成，让首次点击测到的是稳态 TTFA
      setState(() {
        _statusText = '推理引擎: 预热中 (Warm up…)';
      });
      await worker.synthesize(
        phonemizer.phonemize(_warmUpText),
        voice: 0,
        textLength: _warmUpText.length,
      );

      if (!mounted || generation != _generation) return;
      setState(() {
        _statusText =
            '推理引擎: 就绪 · 全离线（合成跑在后台 isolate，UI 不阻塞）| '
            '采样率 ${KittenSynthesizer.sampleRate} Hz | '
            '模型 ${(worker.modelBytes / 1024 / 1024).toStringAsFixed(1)} MB | '
            '词典 ${phonemizer.dictionarySize} 条';
        _metricsText = '实时指标: 就绪，点击「朗读」';
      });
    } catch (error) {
      debugPrint('Kitten 流式引擎初始化失败: $error');
      if (!mounted || generation != _generation) return;
      setState(() {
        _statusText = '初始化失败: $error';
        _metricsText = '实时指标: 引擎未就绪';
      });
    } finally {
      _initializing = false;
    }
  }

  Future<void> _ensureModels() async {
    final int pending = await MlModelStore.pendingBytes(MlRemoteModels.kitten);
    if (pending <= 0) return;

    _downloading = true;
    _downloadProgress = 0.0;
    if (mounted) {
      setState(() {
        _statusText = '首次使用，需下载模型约 ${mlFormatBytes(pending)}…';
      });
    }

    await _downloader!.ensureAll(
      MlRemoteModels.kitten,
      onProgress: (int index, int downloaded, int total, int overall, int overallTotal) {
        if (!mounted) return;
        setState(() {
          final int percent = overallTotal > 0
              ? (overall * 100 / overallTotal).round().clamp(0, 100)
              : 0;
          _downloadProgress = overallTotal > 0 ? overall / overallTotal : 0.0;
          _statusText =
              '下载模型 ${index + 1}/${MlRemoteModels.kitten.length} · $percent% '
              '(${mlFormatBytes(overall)} / ${mlFormatBytes(overallTotal)})';
        });
      },
    );

    _downloading = false;
    if (mounted) setState(() {});
  }

  // ─────────────────── 流式朗读 ───────────────────

  /// 按句流式合成并播放
  ///
  /// 生产者/消费者：本方法逐句把符号 id 送给后台 isolate 合成（`await` 串行，
  /// 同一套图同一时刻只跑一句），拿到的 PCM 不等待播放——直接链到上一步的
  /// 播放 Future 后面。于是「播放第 N 句」与「合成第 N+1 句」天然重叠，
  /// 与 Android 的 channel + AudioTrack 生产者/消费者同曲目。
  Future<void> _speak() async {
    final String text = _inputController.text.trim();
    if (text.isEmpty) {
      setState(() {
        _metricsText = '实时指标: 请先输入英文';
      });
      return;
    }
    final EnglishG2P? phonemizer = _phonemizer;
    final KittenWorkerClient? worker = _worker;
    if (phonemizer == null || worker == null) {
      setState(() {
        _metricsText = '实时指标: 引擎尚未就绪';
      });
      return;
    }
    if (_speaking) return;

    final List<String> sentences = sentenceChunk(text);
    if (sentences.isEmpty) {
      setState(() {
        _metricsText = '实时指标: 切句后没有可合成的内容';
      });
      return;
    }

    // 重复触发先作废上一轮（与 Android speakJob?.cancel() 对应）
    final int generation = ++_generation;
    _speaking = true;
    _player ??= MlAudioPlayer();
    final MlAudioPlayer player = _player!;
    final int voiceIndex = KittenSynthesizer.voices.indexOf(_voice);

    if (mounted) {
      setState(() {
        _metricsText = '实时指标: 正在合成…';
        _statsText = '';
      });
    }

    try {
      await player.stop();

      // 播放链：每句的播放排在上一句后面，链本身不 await —— 合成循环继续往下跑
      Future<void> playbackChain = Future<void>.value();
      int synthMs = 0;
      int audioSamples = 0;
      int ttfaMs = 0;
      final Stopwatch started = Stopwatch()..start();

      for (int index = 0; index < sentences.length; index++) {
        if (generation != _generation) break;
        final String sentence = sentences[index];

        final List<int> ids = phonemizer.phonemize(sentence);
        if (ids.isEmpty) continue;

        final KittenSynthResult result = await worker.synthesize(
          ids,
          voice: voiceIndex,
          textLength: sentence.length,
        );
        if (generation != _generation) break;

        if (index == 0 || ttfaMs == 0) {
          ttfaMs = started.elapsedMilliseconds;
        }
        synthMs += result.synthMs;
        audioSamples += result.audio.length;
        playbackChain = playbackChain.then(
          (_) => player.playWav(wavFromFloats(result.audio, KittenSynthesizer.sampleRate)),
        );

        if (!mounted || generation != _generation) break;
        setState(() {
          final double audioSeconds =
              audioSamples / KittenSynthesizer.sampleRate;
          // RTF = 合成算力 / 音频时长，小于 1 说明合成一直跑在播放前面
          final double rtf =
              audioSeconds > 0 ? synthMs / 1000.0 / audioSeconds : 0.0;
          _metricsText =
              '实时指标: 首音 $ttfaMs ms | RTF ${rtf.toStringAsFixed(2)} '
              '(${rtf < 1 ? '快于播放' : '慢于播放'})';
          _statsText = _buildStats(
            sentencesDone: index + 1,
            sentencesTotal: sentences.length,
            synthMs: synthMs,
            audioSeconds: audioSeconds,
            tokens: result.tokens,
            frames: result.frames,
          );
        });
      }

      if (generation == _generation) {
        await playbackChain;
        if (mounted && generation == _generation) {
          setState(() {
            _metricsText = '实时指标: 播放完成，可再次点击「朗读」';
          });
        }
      } else {
        await player.stop();
      }
    } catch (error) {
      debugPrint('Kitten 流式合成失败: $error');
      await player.stop();
      if (mounted && generation == _generation) {
        setState(() {
          _metricsText = '合成失败: $error';
        });
      }
    } finally {
      if (generation == _generation) {
        _speaking = false;
        if (mounted) setState(() {});
      }
    }
  }

  void _stop() {
    if (!_speaking) return;
    // 作废当前轮次：循环下一处检查点就会退出，在途那句的结果直接丢弃
    _generation++;
    _player?.stop();
    setState(() {
      _metricsText = '实时指标: 已停止';
    });
  }

  static String _buildStats({
    required int sentencesDone,
    required int sentencesTotal,
    required int synthMs,
    required double audioSeconds,
    required int tokens,
    required int frames,
  }) =>
      '句子     : $sentencesDone / $sentencesTotal\n'
      '最近一句 : $tokens token -> $frames 帧\n'
      '音频累计 : ${audioSeconds.toStringAsFixed(2)} s\n'
      '合成累计 : $synthMs ms';
}
