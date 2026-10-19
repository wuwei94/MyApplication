import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart' show rootBundle;
import 'package:flutter_demo/core/basic/basic.dart';
import 'package:flutter_demo/demos/ml/engines/ml_audio.dart';
import 'package:flutter_demo/demos/ml/engines/ml_model_download.dart';
import 'package:flutter_demo/demos/ml/engines/qwen3_tts_engine.dart';
import 'package:flutter_demo/demos/ml/engines/qwen3_tts_worker.dart';

/// LiteRT — 语音语言模型语音合成（Qwen3-TTS 12Hz）
///
/// 与 Android `:modules:module_ml` 的 `TFLiteTextToSpeechLmActivity` 一一对应，
/// 是本模块第三个语音合成页、也是**最重**的一个：
///
/// | 页面 | 官方任务 | 图的组织 | 模型体积 |
/// |---|---|---|---|
/// | [MlTextToSpeechDemoPage] | `text_to_speech` | 文本编码 + 扩散解码 + 声码器（G2P 前端） | 约 88 MB |
/// | [MlTextToSpeechStreamingDemoPage] | `text_to_speech_streaming` | 三张动态长度图，逐句流式 | 约 32 MB |
/// | 本页 | `text_to_speech_lm` | talker + MTP + codec 三图由宿主循环串联 | 约 1.67 GB |
///
/// 与流式页的关键差别：**这里不是流式**。talker 要先自回归把整段文本吐成 12.5 Hz
/// 的帧序列，再走 codec 分块还原波形，因此「点一次 → 等一段时间 → 一次性播放」，
/// 中途无法按句迭代输出。官方在 Pixel 8a 上实测 RTF ≈ 6.7，所以合成 4 秒音频要等
/// 约 28 秒，界面必须把这一点讲清楚。
///
/// 与 Android 的两处实现差异（都属**功能对等、架构照搬不了**，非需求偏差）：
/// - 推理跑在后台 isolate（Android 是引擎串行队列上的协程），UI 线程全程自由；
/// - KV cache 每步要多一次「有效前缀」搬运 —— 经典 C API 不能像
///   `CompiledModel.run(inputs, outputs, key)` 那样把任意缓冲绑给某个签名，
///   详见 [Qwen3TtsEngine] 的类注释。
///
/// 官方参考：
/// https://github.com/google-ai-edge/litert-samples/tree/main/samples/litert/text_to_speech_lm
class MlTextToSpeechLmDemoPage extends BasicImagePage {
  const MlTextToSpeechLmDemoPage({super.key, required super.title});

  @override
  BasicImagePageState<MlTextToSpeechLmDemoPage> createState() =>
      _MlTextToSpeechLmDemoPageState();
}

class _MlTextToSpeechLmDemoPageState
    extends BasicImagePageState<MlTextToSpeechLmDemoPage> {
  /// 与 Android `DEFAULT_TEXT` 一致
  static const String _defaultText =
      'Hello! This is Qwen3 text to speech running on device.';

  /// 与 Android `DEFAULT_LANGUAGE` 一致
  static const String _defaultLanguage = 'english';

  /// 官方模型输出 12.5 Hz 帧
  static const double _frameRate = 12.5;

  /// 进度刷新的帧间隔（与 Android `PROGRESS_STEP` 一致：够密也够省）
  static const int _progressStep = 5;

  final TextEditingController _inputController = TextEditingController();

  QwenTtsWorkerClient? _worker;
  MlAudioPlayer? _player;
  MlModelDownloader? _downloader;

  String _language = _defaultLanguage;
  bool _initializing = false;
  bool _speaking = false;
  bool _downloading = false;
  double _downloadProgress = 0.0;

  /// 朗读代号：页面销毁或重复触发时自增，过期轮次的结果不得落到 UI
  int _generation = 0;

  /// 用户是否已放弃本段结果
  ///
  /// 四张图的前向是**一整段不可中断的调用**，取消 Future 并不能让它提前返回，
  /// 因此「停止」不是取消，而是打这个标记：波形算完后就地丢弃、不播放，
  /// 界面立刻回到可交互状态（与 Android 的 `abandoned` 同语义）。
  bool _abandoned = false;

  String _statusText = '初始化中';
  String _metricsText = '实时指标: 就绪，点击「合成并朗读」';
  String _statsText = '';

  @override
  void initState() {
    super.initState();
    _inputController.text = _defaultText;
    _initEngine();
  }

  @override
  void dispose() {
    _generation++;
    _downloader?.cancel();
    final MlAudioPlayer? player = _player;
    _player = null;
    player?.dispose();
    final QwenTtsWorkerClient? worker = _worker;
    _worker = null;
    // worker 必须显式关闭：解释器持有原生内存与 mmap，页面销毁不杀 isolate 的话
    // 它会连四张图一起泄漏到进程结束
    worker?.close();
    _inputController.dispose();
    super.dispose();
  }

  @override
  List<String> buildList() => <String>[
        '1. 合成并朗读',
        '2. 停止',
        '3. 恢复默认文本',
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
            '输入文本，用 Qwen3 talker 语言模型逐帧生成后一次性朗读'
            '（12.5 Hz · 24 kHz · 全离线）',
            style: TextStyle(fontSize: BasicDemoDimens.fontTitle),
          ),
          const SizedBox(height: BasicDemoDimens.itemPadding),
          DropdownButtonFormField<String>(
            initialValue: _language,
            decoration: const InputDecoration(
              labelText: '语言（控制 token，10 种语言 + auto）',
              border: OutlineInputBorder(),
              isDense: true,
            ),
            items: <DropdownMenuItem<String>>[
              for (final String language in Qwen3TtsEngine.languages)
                DropdownMenuItem<String>(value: language, child: Text(language)),
            ],
            onChanged: (String? value) {
              if (value == null) return;
              setState(() {
                _language = value;
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
                _statsText.isEmpty ? '（合成时在此展示分阶段耗时）' : _statsText,
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

  /// 异步初始化：确保 11 个文件就绪（必要时下载）→ 拉起后台 isolate →
  /// 建四张图 → 跑分词器自检
  Future<void> _initEngine() async {
    if (_initializing) return;
    _initializing = true;
    try {
      await _ensureModels();
      if (!mounted) return;

      setState(() {
        _statusText = '推理引擎: 正在构建四张图（首次约需数十秒，峰值内存约 1.5 GB）…';
      });

      final List<List<Object>> vectors = await _loadVectors();
      final String modelsDir = (await MlModelStore.dir()).path;
      final QwenTtsWorkerClient worker = QwenTtsWorkerClient(
        modelsDir: modelsDir,
        vectors: vectors,
      );
      await worker.start();
      if (!mounted) {
        await worker.close();
        return;
      }

      _worker = worker;
      final QwenEngineInfo? info = worker.info;
      final List<String> mismatches = info?.mismatches ?? const <String>[];
      setState(() {
        final String selfTest = mismatches.isEmpty
            ? '分词器自检 PASS'
            : '分词器自检 FAIL (${mismatches.length} 条不一致)';
        _statusText = '推理引擎: 就绪 · 全离线 | $selfTest | '
            '词表 ${info?.tokenizerSize ?? 0} | 图 ${info?.graphNames ?? ''} | '
            '构建 ${info?.loadMs ?? 0} ms';
        _metricsText = '实时指标: 就绪，点击「合成并朗读」';
        _statsText = mismatches.isEmpty ? '等待合成...' : mismatches.join('\n');
      });
    } catch (error) {
      debugPrint('Qwen3-TTS 引擎初始化失败: $error');
      if (!mounted) return;
      setState(() {
        _statusText = '初始化失败: $error';
        _metricsText = '实时指标: 引擎未就绪';
      });
    } finally {
      _initializing = false;
    }
  }

  /// 确保 11 个模型文件都在本地；缺哪个下哪个（已下载的不会重下）
  ///
  /// 单文件最大 622 MB（文本嵌入表），下载层用 `Range` 请求续传，中断后重进页面
  /// 会接着下。
  Future<void> _ensureModels() async {
    final int pending = await MlModelStore.pendingBytes(MlRemoteModels.qwen);
    if (pending <= 0) return;

    _downloader ??= MlModelDownloader();
    _downloading = true;
    _downloadProgress = 0.0;
    if (mounted) {
      setState(() {
        _statusText = '首次使用，需下载模型约 ${mlFormatBytes(pending)}'
            '（共 ${MlRemoteModels.qwen.length} 个文件）…';
      });
    }

    await _downloader?.ensureAll(
      MlRemoteModels.qwen,
      onProgress: (int index, int downloaded, int total, int overall,
          int overallTotal) {
        if (!mounted) return;
        setState(() {
          final int percent = overallTotal > 0
              ? (overall * 100 / overallTotal).round().clamp(0, 100)
              : 0;
          _downloadProgress = overallTotal > 0 ? overall / overallTotal : 0.0;
          _statusText = '下载模型 ${index + 1}/${MlRemoteModels.qwen.length} · '
              '${MlRemoteModels.qwen[index].fileName} · $percent% '
              '(${mlFormatBytes(overall)} / ${mlFormatBytes(overallTotal)})';
        });
      },
    );

    _downloading = false;
    if (mounted) setState(() {});
  }

  /// 读取官方 baked 的参考 token id 向量（`assets/ml/tokenizer_test_vectors.json`）
  ///
  /// 后台 isolate 里 `rootBundle` 不可用（没有平台信使），因此**在主 isolate 读好**
  /// 再随 spawn 参数传给 worker。
  Future<List<List<Object>>> _loadVectors() async {
    final String text =
        await rootBundle.loadString('assets/ml/tokenizer_test_vectors.json');
    final List<Object?> cases = jsonDecode(text) as List<Object?>;
    return <List<Object>>[
      for (final Object? item in cases)
        if (item is Map<String, Object?>)
          <Object>[
            item['text'] as String,
            Int32List.fromList(
              (item['ids'] as List<Object?>)
                  .map((Object? id) => (id as num).toInt())
                  .toList(),
            ),
          ],
    ];
  }

  // ─────────────────── 合成与播放 ───────────────────

  Future<void> _speak() async {
    final String text = _inputController.text.trim();
    if (text.isEmpty) {
      setState(() {
        _metricsText = '实时指标: 请先输入文本';
      });
      return;
    }
    final QwenTtsWorkerClient? worker = _worker;
    if (worker == null) {
      setState(() {
        _metricsText = '实时指标: 引擎尚未就绪';
      });
      return;
    }
    // 按钮的禁用发生在 setState 之后，同一次事件循环内的连点仍可能排进两段合成
    if (_speaking) {
      setState(() {
        _metricsText = '实时指标: 上一段仍在合成中，请稍候';
      });
      return;
    }

    final int generation = _generation;
    _speaking = true;
    _abandoned = false;
    final MlAudioPlayer player = _player ?? MlAudioPlayer();
    _player = player;

    setState(() {
      _metricsText = '实时指标: 正在合成…（整段一次前向，不可中断）';
      _statsText = '';
    });

    try {
      await player.stop();
      final QwenSynthResult result = await worker.synthesize(
        text,
        language: _language,
        onProgress: (int frames) {
          // 每 5 帧刷一次，与 Android 的 PROGRESS_STEP 一致
          if (frames % _progressStep != 0) return;
          if (!mounted || generation != _generation || _abandoned) return;
          setState(() {
            final double seconds = frames / _frameRate;
            _metricsText = '实时指标: 已生成 $frames 帧'
                '（${seconds.toStringAsFixed(1)} s 音频）…';
          });
        },
      );

      if (_abandoned || generation != _generation || !mounted) return;

      setState(() {
        _metricsText = '实时指标: ${_buildMetrics(result)}';
        _statsText = _buildStats(result);
      });
      await player.playWav(
        wavFromFloats(result.audio, Qwen3TtsEngine.sampleRate),
      );
    } catch (error) {
      debugPrint('Qwen3-TTS 合成失败: $error');
      if (mounted && generation == _generation) {
        setState(() {
          _metricsText = '合成失败: $error';
        });
      }
    } finally {
      _abandoned = false;
      _speaking = false;
      if (mounted && generation == _generation) setState(() {});
    }
  }

  /// 停止：丢弃本段结果并停掉正在播放的音频
  ///
  /// 四张图前向**不可中断**，因此这里不取消 Future（取消也拦不住已经进图的那次调用），
  /// 只打标记 + 停播放；波形算完后就地丢弃。「合成并朗读」在收尾前无法开始新的一段。
  void _stop() {
    if (!_speaking) return;
    _abandoned = true;
    final MlAudioPlayer? player = _player;
    player?.stop();
    setState(() {
      _metricsText = '实时指标: 已停止（本段前向在后台静默收尾，收尾前无法开始新的一段）';
    });
  }

  String _buildMetrics(QwenSynthResult result) {
    final int graphMs = result.prefillMs +
        result.talkerMs +
        result.mtpMs +
        result.codecMs;
    final double audioSeconds = result.audio.length / Qwen3TtsEngine.sampleRate;
    final double realTimeFactor =
        audioSeconds > 0 ? graphMs / 1000.0 / audioSeconds : 0.0;
    final String pace = realTimeFactor > 1 ? '慢于实时' : '快于实时';
    return '首帧 ${result.prefillMs} ms | '
        'RTF ${realTimeFactor.toStringAsFixed(2)} ($pace) | '
        '${result.frames} 帧 = ${audioSeconds.toStringAsFixed(2)} s 音频';
  }

  String _buildStats(QwenSynthResult result) {
    final int graphMs = result.prefillMs +
        result.talkerMs +
        result.mtpMs +
        result.codecMs;
    final double audioSeconds = result.audio.length / Qwen3TtsEngine.sampleRate;
    return '帧数   : ${result.frames} 帧 '
        '($_frameRate Hz -> ${audioSeconds.toStringAsFixed(2)} s)\n'
        '预填充 : ${result.prefillMs} ms\n'
        'talker : ${result.talkerMs} ms\n'
        'MTP    : ${result.mtpMs} ms\n'
        'codec  : ${result.codecMs} ms\n'
        '合计   : $graphMs ms';
  }
}
