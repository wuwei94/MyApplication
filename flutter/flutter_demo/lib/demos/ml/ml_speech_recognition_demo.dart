import 'dart:async';
import 'dart:typed_data';

import 'package:audioplayers/audioplayers.dart';
import 'package:file_picker/file_picker.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart' show Clipboard, ClipboardData;
import 'package:flutter_demo/core/basic/basic.dart';
import 'package:flutter_demo/demos/ml/engines/asr/asr_audio_source.dart';
import 'package:flutter_demo/demos/ml/engines/asr/asr_engine.dart';
import 'package:flutter_demo/demos/ml/engines/asr/asr_merger.dart';
import 'package:flutter_demo/demos/ml/engines/asr/asr_model_catalog.dart';
import 'package:flutter_demo/demos/ml/engines/asr/asr_worker.dart';
import 'package:flutter_demo/demos/ml/engines/ml_model_download.dart';
import 'package:record/record.dart';

/// LiteRT — 语音识别（Speech Recognition）
///
/// 与 Android `:modules:module_ml` 的 `TFLiteSpeechRecognitionActivity` 一一对应，
/// 是本模块第 9 个、也是最后一个官方任务页。它与前 8 页有两个结构性差异：
///
/// 1. **唯一有两条输入路径的页面**：文件（16 kHz 单声道 16-bit PCM WAV）与麦克风。
///    官方支持的所有模型都**不是流式**的，因此两条路径共用同一套「重叠窗口拼接」——
///    预处理 → 识别 → 后处理三级流水（都在 [AsrWorkerClient] 那一侧）。
/// 2. **唯一一个「模型名 → 解码策略」在宿主侧分派的页面**：`-ctc-` 走 CTC
///    （无 decoder 子图，宿主逐帧 argmax + 压重复），`-tdt` 走 TDT
///    （时间下标驱动 + LSTM 状态 ping-pong），其余走无状态自回归；
///    分派点在 `asrDecoderFor`。
///
/// 模型清单来自 asset 里的 `asr_model_metadata.json`（6 个模型，含 3 个 Parakeet、
/// Moonshine、Whisper、Qwen3-ASR），模型与分词器都走**运行时下载**；
/// 最大的 parakeet 约 614 MB，最小的 whisper-tiny 约 41 MB。
///
/// 与 Android 的两处实现差异（都属**功能对等、架构照搬不了**，非需求偏差）：
/// - 推理跑在后台 isolate（Android 是引擎串行队列上的三条协程）；
/// - 麦克风是**推**模式：录音插件只能在主 isolate 收流，再由主 isolate 把 PCM
///   推给 worker 切窗口；Android 的 `AudioRecord` 是拉模式，窗口切分在音频源内部。
///
/// 官方参考：
/// https://github.com/google-ai-edge/litert-samples/tree/main/samples/litert/speech_recognition
class MlSpeechRecognitionDemoPage extends BasicImagePage {
  const MlSpeechRecognitionDemoPage({super.key, required super.title});

  @override
  BasicImagePageState<MlSpeechRecognitionDemoPage> createState() =>
      _MlSpeechRecognitionDemoPageState();
}

class _MlSpeechRecognitionDemoPageState
    extends BasicImagePageState<MlSpeechRecognitionDemoPage> {
  /// 后端选项（与 Android 的 `BACKENDS` 一致）
  static const String cpuLabel = 'CPU';
  static const String gpuLabel = 'GPU';

  /// 文件转写：5 秒窗口 / 2 秒重叠（官方 `FILE_AUDIO_CHUNK_OVERLAP_DURATION`）
  static const int fileOverlapMs = 2000;

  /// 实时录音：窗口取 `min(5 s, 模型窗口)`、每 1 秒滑一次（官方 `LIVE_AUDIO_CHUNK_*`）
  static const int liveIntervalMs = 5000;
  static const int liveSlidingMs = 1000;

  AsrModelCatalog? _catalog;
  AsrWorkerClient? _worker;
  MlModelDownloader? _downloader;
  final AudioRecorder _recorder = AudioRecorder();
  final ScrollController _transcriptScroll = ScrollController();

  /// 试听用的播放器（懒建）；麦克风数据订阅**不保存句柄** —— 录音插件只在主
  /// isolate 收流，停止录音走 `recorder.stop()`（流随之结束），保存到字段反而会被
  /// `cancel_subscriptions` 判成「无法在创建它的函数里取消」。
  AudioPlayer? _player;

  /// `onPlayerComplete` 的订阅只挂一次；同样不保存句柄（随播放器一起 dispose）
  bool _playbackListenerAttached = false;

  String _model = '';
  String _backend = cpuLabel;

  /// 已完成加载（或正在加载）的「模型 + 后端」组合，用于把重复的选中回调挡掉
  String? _loadedSignature;

  /// 引擎就绪后拿到的信息（窗口长度、词表大小、实际后端）
  AsrEngineInfo? _engineInfo;

  bool _initializing = false;
  bool _busy = false;
  bool _recording = false;
  bool _playing = false;
  bool _downloading = false;
  double _downloadProgress = 0.0;

  /// 选中的音频文件（16 kHz 单声道 16-bit PCM WAV）
  String? _audioPath;
  String? _audioName;

  String _statusText = '初始化中';
  String _specText = '';
  String _confirmed = '';
  String _unconfirmed = '';

  @override
  void initState() {
    super.initState();
    unawaited(_reload());
  }

  @override
  void dispose() {
    final AudioPlayer? player = _player;
    _player = null;
    if (player != null) {
      // 停止 + 释放播放器：它同时会结束 onPlayerComplete 的订阅
      unawaited(player.dispose());
    }
    // 停止录音：流随之结束，麦克风订阅自动完成
    unawaited(_recorder.dispose());
    _downloader?.cancel();
    final AsrWorkerClient? worker = _worker;
    _worker = null;
    // worker 必须显式关闭：解释器持有原生内存与 mmap，页面销毁不杀 isolate 的话
    // 它会连整张图一起泄漏到进程结束
    if (worker != null) {
      unawaited(worker.close());
    }
    _transcriptScroll.dispose();
    super.dispose();
  }

  @override
  List<String> buildList() => <String>[
        '1. 选择音频文件（16 kHz 单声道 WAV）',
        _recording ? '2. 停止录音' : '2. 开始录音',
        '3. 转写所选文件',
        _playing ? '4. 暂停试听' : '4. 试听',
        '5. 复制文本',
        '6. 清空文本',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        unawaited(_pickAudio());
      case 1:
        unawaited(_toggleRecording());
      case 2:
        unawaited(_transcribeFile());
      case 3:
        unawaited(_togglePlayback());
      case 4:
        _copyToClipboard();
      case 5:
        setState(() {
          _confirmed = '';
          _unconfirmed = '';
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
            '全离线语音识别 · 6 个多语模型（最大约 614 MB，运行时下载）',
            style: TextStyle(fontSize: BasicDemoDimens.fontTitle),
          ),
          const SizedBox(height: BasicDemoDimens.itemPadding),
          _labeledDropdown<String>(
            label: '模型（按名字分派解码策略）',
            value: _catalog == null || _model.isEmpty ? null : _model,
            items: _catalog?.modelNames ?? const <String>[],
            onChanged: (String? value) {
              if (value == null || value == _model) {
                return;
              }
              setState(() => _model = value);
              unawaited(_preparePipeline());
            },
          ),
          const SizedBox(height: BasicDemoDimens.itemPadding),
          _labeledDropdown<String>(
            label: '后端（切换即重建引擎）',
            value: _backend,
            items: const <String>[cpuLabel, gpuLabel],
            onChanged: (String? value) {
              if (value == null || value == _backend) {
                return;
              }
              setState(() => _backend = value);
              unawaited(_preparePipeline());
            },
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
          const SizedBox(height: 2),
          Text(
            _specText,
            style: const TextStyle(
              fontSize: BasicDemoDimens.fontBody,
              color: Colors.blueGrey,
            ),
          ),
          const SizedBox(height: BasicDemoDimens.itemPadding),
          const Divider(height: 1),
          Expanded(
            child: SingleChildScrollView(
              controller: _transcriptScroll,
              child: Padding(
                padding: const EdgeInsets.symmetric(vertical: 8),
                // 已确认文本正常显示，未确认部分标灰 ——
                // 直接体现「还可能被下一个重叠窗口改写」
                child: Text.rich(
                  TextSpan(
                    style: const TextStyle(
                      fontSize: BasicDemoDimens.fontTitle,
                      height: 1.4,
                    ),
                    children: <TextSpan>[
                      TextSpan(text: _confirmed),
                      if (_unconfirmed.isNotEmpty)
                        TextSpan(
                          text: '${_confirmed.isEmpty ? '' : ' '}$_unconfirmed',
                          style: TextStyle(color: Colors.grey.shade500),
                        ),
                      if (_confirmed.isEmpty && _unconfirmed.isEmpty)
                        const TextSpan(
                          text: '（转写结果在此显示；灰色部分为尚未稳定的词）',
                          style: TextStyle(color: Colors.grey),
                        ),
                    ],
                  ),
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  /// 受控下拉框
  ///
  /// 用 [DropdownButton] 而不是 `DropdownButtonFormField`：后者把选中值存放在
  /// 自己的 `FormFieldState` 里，只在首次构建时读 `initialValue`；
  /// 而本页会在**代码里**改选中值（引擎就绪后填入首个模型、不支持 GPU 的模型
  /// 把后端拨回 CPU），那时它不会跟着变，界面与实际不一致。
  Widget _labeledDropdown<T>({
    required String label,
    required T? value,
    required List<T> items,
    required ValueChanged<T?> onChanged,
  }) {
    return InputDecorator(
      decoration: InputDecoration(
        labelText: label,
        border: const OutlineInputBorder(),
        isDense: true,
      ),
      child: DropdownButtonHideUnderline(
        child: DropdownButton<T>(
          value: value,
          isExpanded: true,
          items: <DropdownMenuItem<T>>[
            for (final T item in items)
              DropdownMenuItem<T>(value: item, child: Text('$item')),
          ],
          onChanged: items.isEmpty ? null : onChanged,
        ),
      ),
    );
  }

  // ─────────────────── 引擎：按「模型 + 后端」组合重建 ───────────────────

  Future<void> _reload() async {
    if (_initializing) {
      return;
    }
    _initializing = true;
    try {
      final AsrModelCatalog catalog = await AsrModelCatalog.load();
      if (!mounted) {
        return;
      }
      setState(() {
        _catalog = catalog;
        _model = catalog.modelNames.first;
        _statusText = '正在拉起后台推理 isolate…';
      });

      final String modelsDir = (await MlModelStore.dir()).path;
      final AsrWorkerClient worker = AsrWorkerClient(
        catalogJson: catalog.rawJson,
        modelsDir: modelsDir,
      );
      worker.onTranscript = _onTranscript;
      await worker.start();
      if (!mounted) {
        await worker.close();
        return;
      }
      _worker = worker;
      await _preparePipeline();
    } catch (error) {
      debugPrint('ASR 页面初始化失败: $error');
      if (mounted) {
        setState(() => _statusText = '初始化失败: $error');
      }
    } finally {
      _initializing = false;
    }
  }

  /// 选中项变化时才重建
  ///
  /// 与 Android 的 `reloadPipelineIfChanged` 同义：签名比对把「同一组合的重复触发」
  /// 挡掉，避免一进页面就用第一个模型触发两次下载 / 构建。
  Future<void> _preparePipeline() async {
    final AsrModelCatalog? catalog = _catalog;
    final AsrWorkerClient? worker = _worker;
    if (catalog == null || worker == null || _model.isEmpty) {
      return;
    }
    final AsrModelConfig config = catalog.configOf(_model);

    // Qwen3-ASR 官方只标了 CPU：选了 GPU 也要如实回落，并把下拉框拨回去。
    // Android 是先建完再拨（会白建一次），这里提前拨，只建一次。
    if (!config.supportsGpu && _backend == gpuLabel) {
      setState(() => _backend = cpuLabel);
    }
    final bool useGpu = config.supportsGpu && _backend == gpuLabel;

    final String signature = '${config.key}|${useGpu ? gpuLabel : cpuLabel}';
    if (signature == _loadedSignature) {
      return;
    }
    _loadedSignature = signature;

    // 在途转写手里握着旧引擎的引用：新引擎就绪后旧实例会被释放，
    // 而识别仍会继续对已释放的原生句柄 invoke。换模型 / 换后端一律先停掉它。
    await _stopRecording(notifyWorker: true);
    await _stopPlayback();
    if (!mounted) {
      return;
    }

    setState(() {
      _busy = true;
      _confirmed = '';
      _unconfirmed = '';
      _statusText = '正在准备模型…';
    });

    try {
      await _ensureModels(config);
      if (!mounted) {
        return;
      }
      setState(() {
        _statusText =
            '推理引擎: 正在构建（${config.key} / ${useGpu ? gpuLabel : cpuLabel}）…';
      });

      final AsrEngineInfo info =
          await worker.loadEngine(config.key, gpu: useGpu);
      if (!mounted || _loadedSignature != signature) {
        return;
      }
      setState(() {
        _engineInfo = info;
        _statusText = '推理引擎: 就绪 · 全离线 | ${config.key} | '
            '${info.backend} | 词表 ${info.vocabSize}';
        _specText = '窗口 ${(info.windowMs / 1000).toStringAsFixed(1)} s · '
            '重叠 ${(fileOverlapMs / 1000).toStringAsFixed(1)} s';
      });
    } catch (error) {
      debugPrint('ASR 引擎构建失败: $error');
      if (mounted) {
        setState(() => _statusText = '初始化失败: $error');
      }
    } finally {
      if (mounted && _loadedSignature == signature) {
        setState(() => _busy = false);
      }
    }
  }

  /// 确保该配置需要的两个文件（模型 + 分词器）都在本地；缺哪个下哪个
  Future<void> _ensureModels(AsrModelConfig config) async {
    final List<MlRemoteModel> models = config.remoteModels();
    final int pending = await MlModelStore.pendingBytes(models);
    if (pending <= 0) {
      return;
    }

    final MlModelDownloader downloader = _downloader ??= MlModelDownloader();
    _downloading = true;
    _downloadProgress = 0.0;
    if (mounted) {
      setState(() {
        _statusText = '首次使用 ${config.key}，需下载约 ${mlFormatBytes(pending)}'
            '（模型 ${mlFormatBytes(config.approxModelBytes)} + '
            '分词器 ${mlFormatBytes(config.approxTokenizerBytes)}）…';
      });
    }

    await downloader.ensureAll(
      models,
      onProgress: (int index, int downloaded, int total, int overall,
          int overallTotal) {
        if (!mounted) {
          return;
        }
        setState(() {
          final int percent = overallTotal > 0
              ? (overall * 100 / overallTotal).round().clamp(0, 100)
              : 0;
          _downloadProgress = overallTotal > 0 ? overall / overallTotal : 0.0;
          _statusText = '下载 ${index + 1}/${models.length} · '
              '${models[index].fileName} · $percent% '
              '(${mlFormatBytes(overall)} / ${mlFormatBytes(overallTotal)})';
        });
      },
    );

    _downloading = false;
    if (mounted) {
      setState(() {});
    }
  }

  // ─────────────────── 交互 ───────────────────

  void _onTranscript(String confirmed, String unconfirmed) {
    if (!mounted) {
      return;
    }
    setState(() {
      _confirmed = confirmed;
      _unconfirmed = unconfirmed;
    });
    // 滚动到底部（与 Android 的 fullScroll(FOCUS_DOWN) 同义）
    if (_transcriptScroll.hasClients) {
      unawaited(
        _transcriptScroll.animateTo(
          _transcriptScroll.position.maxScrollExtent,
          duration: const Duration(milliseconds: 120),
          curve: Curves.easeOut,
        ),
      );
    }
  }

  /// 选音频文件（只列 WAV；采样率 / 位深在 worker 里校验）
  Future<void> _pickAudio() async {
    try {
      // file_picker 13 起 `FilePicker.platform.pickFiles()` 换成了静态方法
      // `FilePicker.pickFile()`，返回单个 `PlatformFile?` 而不是 `FilePickerResult`
      final PlatformFile? file = await FilePicker.pickFile(
        type: FileType.custom,
        allowedExtensions: <String>['wav'],
      );
      final String? path = file?.path;
      if (file == null || path == null) {
        return;
      }
      await _stopPlayback();
      if (!mounted) {
        return;
      }
      setState(() {
        _audioPath = path;
        _audioName = file.name;
        _statusText = '已选择音频: $_audioName';
      });
    } catch (error) {
      debugPrint('选择音频失败: $error');
      _showMessage('选择音频失败: $error');
    }
  }

  /// 转写所选文件：整段跑完才返回（过程中能看到灰字滚动）
  Future<void> _transcribeFile() async {
    final String? path = _audioPath;
    if (path == null) {
      _showMessage('请先选择一个音频文件');
      return;
    }
    final AsrWorkerClient? worker = _worker;
    final AsrEngineInfo? info = _engineInfo;
    if (worker == null || info == null) {
      _showMessage('引擎尚未就绪');
      return;
    }
    if (_recording) {
      _showMessage('请先停止录音');
      return;
    }
    // 与 Android 的 `setBusy` 同义：长任务期间不接第二段
    if (_busy) {
      _showMessage('上一段仍在处理中，请稍候');
      return;
    }

    setState(() {
      _busy = true;
      _confirmed = '';
      _unconfirmed = '';
      _statusText = '正在转写…';
    });
    try {
      await worker.transcribeFile(
        path,
        intervalMs: info.windowMs,
        overlapMs: fileOverlapMs,
        maxDistance: AsrLevenshteinMerger.fileMaxLevenshteinDistance,
      );
      if (mounted) {
        setState(() => _statusText = '转写完成 · 全离线');
      }
    } catch (error) {
      debugPrint('转写失败: $error');
      if (mounted) {
        setState(() => _statusText = '转写失败: $error');
        _showMessage('转写失败: $error（仅支持 16 kHz 单声道 16-bit PCM WAV）');
      }
    } finally {
      if (mounted) {
        setState(() => _busy = false);
      }
    }
  }

  Future<void> _toggleRecording() async {
    if (_recording) {
      await _stopRecording(notifyWorker: true);
      return;
    }
    // 转写 / 建引擎期间不接录音（Android 的 `setBusy` 会把录音按钮置灰；
    // 「停止录音」是例外，它必须始终可点）
    if (_busy) {
      _showMessage('正在处理上一段，请稍候');
      return;
    }
    // 与官方一致：**拿到权限后直接开始录音**，用户不需要再点一次按钮
    final bool granted = await _recorder.hasPermission();
    if (!granted) {
      _showMessage('未授予麦克风权限，无法录音');
      return;
    }
    await _startRecording();
  }

  Future<void> _startRecording() async {
    final AsrWorkerClient? worker = _worker;
    final AsrEngineInfo? info = _engineInfo;
    if (worker == null || info == null) {
      _showMessage('引擎尚未就绪');
      return;
    }
    // 实时路径用比模型窗口更短的块，滚动更快、延迟更低（官方 LIVE_AUDIO_CHUNK_*）
    final int interval =
        liveIntervalMs < info.windowMs ? liveIntervalMs : info.windowMs;
    final int overlap = interval - liveSlidingMs;

    try {
      setState(() {
        _confirmed = '';
        _unconfirmed = '';
        _busy = true;
        _statusText = '正在识别…';
      });
      await worker.startMicrophone(
        intervalMs: interval,
        overlapMs: overlap,
        maxDistance: AsrLevenshteinMerger.liveMaxLevenshteinDistance,
      );
      final Stream<Uint8List> stream = await _recorder.startStream(
        const RecordConfig(
          encoder: AudioEncoder.pcm16bits,
          sampleRate: AsrEngine.sampleRate,
          numChannels: 1,
        ),
      );
      // 不保存订阅句柄：停止录音走 `_recorder.stop()`，流结束订阅自然完成
      stream.listen(_onMicrophoneData);
      if (mounted) {
        setState(() => _recording = true);
      }
    } catch (error) {
      debugPrint('录音失败: $error');
      await _recorder.stop();
      if (mounted) {
        setState(() {
          _recording = false;
          _busy = false;
          _statusText = '录音失败: $error';
        });
      }
    }
  }

  /// 录音插件的回调分片大小由平台决定；这里只做「字节 → int16」的转换，
  /// 攒够一个 step 再切窗口的活留给 worker（窗口边界因此与 Android 一致）
  void _onMicrophoneData(Uint8List bytes) {
    _worker?.feedMicrophone(shortsFromPcm16Bytes(bytes));
  }

  Future<void> _stopRecording({required bool notifyWorker}) async {
    // 与 Android `stopRecording()` 的 `if (!isRecording) return` 同义
    if (!_recording) {
      return;
    }
    try {
      // 停止录音 → 插件的数据流随之结束，麦克风订阅自动完成
      await _recorder.stop();
    } catch (error) {
      debugPrint('停止录音失败: $error');
    }
    if (notifyWorker) {
      try {
        // worker 收到 mic-end 会把最后一段「未确认」也转正并回传，
        // 与 Android 的 `text.toString()` 收尾同义
        await _worker?.stopMicrophone();
      } catch (error) {
        debugPrint('worker 收尾失败: $error');
      }
    }
    if (mounted) {
      setState(() {
        _recording = false;
        _busy = false;
      });
    }
  }

  /// 试听所选文件（与转写互不影响）
  Future<void> _togglePlayback() async {
    final String? path = _audioPath;
    if (path == null) {
      _showMessage('请先选择一个音频文件');
      return;
    }
    final AudioPlayer player = _player ??= AudioPlayer();
    try {
      if (_playing) {
        await player.pause();
        if (mounted) {
          setState(() => _playing = false);
        }
        return;
      }
      if (!_playbackListenerAttached) {
        _playbackListenerAttached = true;
        // 订阅只挂一次、不保存句柄：它随播放器一起 dispose（见 dispose 注释）
        player.onPlayerComplete.listen((_) {
          if (mounted) {
            setState(() => _playing = false);
          }
        });
      }
      if (player.state == PlayerState.paused) {
        await player.resume();
      } else {
        await player.play(DeviceFileSource(path));
      }
      if (mounted) {
        setState(() => _playing = true);
      }
    } catch (error) {
      // 选到的可能不是音频，或编码不受支持 —— 播放器不可用不影响转写
      debugPrint('试听失败: $error');
      _showMessage('试听失败: $error');
    }
  }

  Future<void> _stopPlayback() async {
    final AudioPlayer? player = _player;
    if (player != null) {
      try {
        await player.stop();
      } catch (error) {
        debugPrint('停止试听失败: $error');
      }
    }
    if (mounted && _playing) {
      setState(() => _playing = false);
    }
  }

  void _copyToClipboard() {
    final String text = _confirmed.isEmpty
        ? _unconfirmed
        : (_unconfirmed.isEmpty ? _confirmed : '$_confirmed $_unconfirmed');
    if (text.trim().isEmpty) {
      return;
    }
    unawaited(Clipboard.setData(ClipboardData(text: text)));
    _showMessage('已复制到剪贴板');
  }

  void _showMessage(String message) {
    if (!mounted) {
      return;
    }
    ScaffoldMessenger.of(context)
        .showSnackBar(SnackBar(content: Text(message)));
  }
}
