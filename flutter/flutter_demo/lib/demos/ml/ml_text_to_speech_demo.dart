import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';
import 'package:flutter_demo/demos/ml/engines/english_g2p.dart';
import 'package:flutter_demo/demos/ml/engines/matcha_synthesizer.dart';
import 'package:flutter_demo/demos/ml/engines/ml_audio.dart';
import 'package:flutter_demo/demos/ml/engines/ml_model_download.dart';

/// LiteRT — 端侧语音合成（Matcha-TTS）
///
/// 与 Android `:modules:module_ml` 的 `TFLiteTextToSpeechActivity` 一一对应：
/// 1. 链路：文本 → espeak-IPA G2P（词典 + 神经）→ 音素 id → 文本编码器 → 时长展开 →
///    扩散(Euler ODE) → mel → HiFi-GAN 声码器 → PCM 波形 → 播放；
/// 2. **模型不入包**：四张图（text encoder / decoder / vocoder / G2P）合计约 88 MB，
///    首次使用时下载到应用私有目录；只有 `emb.bin`、`config.json`、`g2p_meta.json`、
///    `g2p_dict.txt.gz` 这四个小文件随资产分发，与官方相同；
/// 3. **后端分配是模型特定的**：text encoder 与 vocoder 走 GPU，CFM decoder 走 CPU
///    （官方注释说明 diffusers transformer 在 Mali ML Drift delegate 上存在算子融合缺陷），
///    因此本页不提供统一的 CPU/GPU 开关，GPU 不可用时由引擎内部回退并如实标注；
/// 4. **warm up 一次**：官方在加载后先跑一次 2 步合成预热 GPU 与 JIT。
///
/// 官方参考：
/// https://github.com/google-ai-edge/litert-samples/tree/main/samples/litert/text_to_speech
class MlTextToSpeechDemoPage extends BasicImagePage {
  const MlTextToSpeechDemoPage({super.key, required super.title});

  @override
  BasicImagePageState<MlTextToSpeechDemoPage> createState() =>
      _MlTextToSpeechDemoPageState();
}

class _MlTextToSpeechDemoPageState
    extends BasicImagePageState<MlTextToSpeechDemoPage> {
  static const String _defaultText = 'Hello, this is Matcha running on your device.';
  static const String _warmUpText = 'warm up';

  static const List<int> _stepChoices = <int>[2, 4, 10];

  final TextEditingController _inputController = TextEditingController();

  MatchaSynthesizer? _engine;
  EnglishG2P? _phonemizer;
  MlAudioPlayer? _player;

  int _steps = MatchaSynthesizer.defaultSteps;
  bool _initializing = false;
  bool _synthesizing = false;
  bool _downloading = false;
  double _downloadProgress = 0.0;

  /// 引擎构建代号：页面退出或重复触发时丢弃过期结果
  int _generation = 0;

  MlModelDownloader? _downloader;

  String _statusText = '初始化中';
  String _resultText = '合成结果: 等待输入';
  String _timeDetails = '';
  String _pipeline = '';

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
    final MatchaSynthesizer? engine = _engine;
    final EnglishG2P? phonemizer = _phonemizer;
    _engine = null;
    _phonemizer = null;
    // 解释器与文件句柄必须显式释放；在途合成由 _synthesizing 兜底互斥
    engine?.close();
    phonemizer?.close();
    _inputController.dispose();
    super.dispose();
  }

  @override
  List<String> buildList() => <String>[
        '1. 合成并朗读',
        '2. 停止播放',
        '3. 恢复默认文本',
        '4. 切换 ODE 步数（当前：$_steps）',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        _synthesize();
      case 1:
        _player?.stop();
      case 2:
        setState(() {
          _inputController.text = _defaultText;
        });
      case 3:
        setState(() {
          final int index = _stepChoices.indexOf(_steps);
          _steps = _stepChoices[(index + 1) % _stepChoices.length];
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
            '输入英文句子，端侧合成语音并朗读（G2P + 扩散 + HiFi-GAN 全链路）',
            style: TextStyle(fontSize: BasicDemoDimens.fontTitle),
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
            _resultText,
            style: const TextStyle(
              fontSize: BasicDemoDimens.fontTitle,
              fontWeight: FontWeight.bold,
              color: Colors.blue,
            ),
          ),
          const SizedBox(height: 4),
          Text(
            _timeDetails,
            style: TextStyle(
              fontSize: BasicDemoDimens.fontBody,
              color: Colors.grey.shade600,
            ),
          ),
          const SizedBox(height: BasicDemoDimens.itemPadding),
          Expanded(
            child: SingleChildScrollView(
              child: Text(
                _pipeline.isEmpty ? '（合成后在此展示链路明细）' : _pipeline,
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

  /// 异步初始化：确保模型就绪（必要时下载）→ 构建引擎 → 预热
  Future<void> _initEngine() async {
    if (_initializing) return;
    final int generation = ++_generation;
    _initializing = true;
    _downloader ??= MlModelDownloader();

    if (mounted) {
      setState(() {
        _statusText = '推理引擎: 正在构建 (Loading graphs…)';
      });
    }

    try {
      await _ensureModels();
      if (!mounted || generation != _generation) return;

      final MatchaSynthesizer synthesizer = MatchaSynthesizer();
      await synthesizer.load();
      final EnglishG2P g2p = EnglishG2P();
      await g2p.load();
      if (!mounted || generation != _generation) {
        synthesizer.close();
        g2p.close();
        return;
      }

      _engine = synthesizer;
      _phonemizer = g2p;

      // 预热 GPU 与 JIT（官方同样在加载后跑一次短合成）
      if (mounted) {
        setState(() {
          _statusText = '推理引擎: 预热中 (Warm up…)';
        });
      }
      synthesizer.synthesize(g2p.phonemize(_warmUpText), nSteps: 2);

      if (!mounted || generation != _generation) return;
      setState(() {
        _statusText =
            '推理引擎: ${synthesizer.acceleratorSummary} | '
            '采样率 ${MatchaSynthesizer.sampleRate} Hz | 词典 ${g2p.dictionarySize} 条';
        _resultText = '合成结果: 就绪，输入英文后点击「合成并朗读」';
      });
    } catch (error) {
      debugPrint('Matcha 引擎初始化失败: $error');
      if (!mounted || generation != _generation) return;
      setState(() {
        _statusText = '初始化失败: $error';
        _resultText = '合成结果: 引擎未就绪';
      });
    } finally {
      _initializing = false;
    }
  }

  Future<void> _ensureModels() async {
    final int pending = await MlModelStore.pendingBytes(MlRemoteModels.matcha);
    if (pending <= 0) return;

    _downloading = true;
    _downloadProgress = 0.0;
    if (mounted) {
      setState(() {
        _statusText = '首次使用，需下载模型约 ${mlFormatBytes(pending)}…';
      });
    }

    await _downloader!.ensureAll(
      MlRemoteModels.matcha,
      onProgress: (int index, int downloaded, int total, int overall, int overallTotal) {
        if (!mounted) return;
        setState(() {
          final int percent = overallTotal > 0
              ? (overall * 100 / overallTotal).round().clamp(0, 100)
              : 0;
          _downloadProgress = overallTotal > 0 ? overall / overallTotal : 0.0;
          _statusText =
              '下载模型 ${index + 1}/${MlRemoteModels.matcha.length} · $percent% '
              '(${mlFormatBytes(overall)} / ${mlFormatBytes(overallTotal)})';
        });
      },
    );

    _downloading = false;
    if (mounted) setState(() {});
  }

  // ─────────────────── 合成 ───────────────────

  Future<void> _synthesize() async {
    final String text = _inputController.text.trim();
    if (text.isEmpty) {
      setState(() {
        _resultText = '合成结果: 请先输入英文';
      });
      return;
    }
    final MatchaSynthesizer? engine = _engine;
    final EnglishG2P? phonemizer = _phonemizer;
    if (engine == null || phonemizer == null) {
      setState(() {
        _resultText = '合成结果: 引擎尚未就绪';
      });
      return;
    }
    if (_synthesizing) return;

    _synthesizing = true;
    _player ??= MlAudioPlayer();
    final MlAudioPlayer player = _player!;
    if (mounted) {
      setState(() {
        _resultText = '合成结果: 正在合成…';
      });
    }

    try {
      await player.stop();

      final Stopwatch phonemizeWatch = Stopwatch()..start();
      final List<int> ids = phonemizer.phonemize(text);
      phonemizeWatch.stop();

      final result = engine.synthesize(ids, nSteps: _steps);

      final double duration = durationOf(result.audio, MatchaSynthesizer.sampleRate);
      final double rtf = duration > 0 ? result.ms / 1000.0 / duration : 0.0;

      if (!mounted) return;
      setState(() {
        _resultText =
            '合成结果: 已朗读 ${duration.toStringAsFixed(2)} 秒音频';
        _timeDetails =
            '音素化耗时: ${(phonemizeWatch.elapsedMicroseconds / 1000.0).toStringAsFixed(2)} ms | '
            '合成耗时: ${result.ms} ms | RTF ${rtf.toStringAsFixed(2)}';
        _pipeline = _buildPipelineReport(
          phonemeCount: ids.length,
          frames: result.frames,
          steps: result.steps,
          samples: result.audio.length,
        );
      });

      // 播放：包成 WAV 后交给 audioplayers（与 Android AudioTrack 同曲目）
      await player.playWav(
        wavFromFloats(result.audio, MatchaSynthesizer.sampleRate),
      );
    } catch (error) {
      debugPrint('Matcha 合成失败: $error');
      if (mounted) {
        setState(() {
          _resultText = '合成失败: $error';
        });
      }
    } finally {
      _synthesizing = false;
      if (mounted) setState(() {});
    }
  }

  static String _buildPipelineReport({
    required int phonemeCount,
    required int frames,
    required int steps,
    required int samples,
  }) {
    final double duration = durationOf(
      Float32List(samples),
      MatchaSynthesizer.sampleRate,
    );
    return '音素 id 数  : $phonemeCount\n'
        'Mel 帧数    : $frames / ${MatchaSynthesizer.maxMel}\n'
        'ODE 步数    : $steps\n'
        '波形采样点  : $samples\n'
        '时长        : ${duration.toStringAsFixed(2)} s @ '
        '${MatchaSynthesizer.sampleRate} Hz';
  }
}
