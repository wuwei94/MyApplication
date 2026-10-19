/// Qwen3-TTS（语音语言模型）的宿主侧三图解码循环 —— 与 Android 侧
/// `Qwen3TtsEngine` 一一对应。
///
/// 对应官方 `samples/litert/text_to_speech_lm`。本模块三个语音合成页的分工是：
/// Matcha 是紧凑的流匹配声学模型，KittenTTS 走动态长度的轻量三图，而这里代表
/// **语音语言模型**这一支 —— Qwen3 talker LM 以 12.5 Hz 逐帧吐出 16 层编解码器
/// token，再由神经编解码器还原成 24 kHz 波形。
///
/// 三张图由宿主循环串联，图之间不直接相连：
///
/// 1. **talker**：标准 Qwen3 Transformer（28 层 / hidden 1024），带 `prefill_32` 与
///    `decode` 两个签名与显式 KV cache。导出时在 lm_head 后面接了 identity 块，
///    因此每个 decode 步除了 logits 还吐出 hidden state，用来喂给 MTP。
/// 2. **MTP（码本预测器）**：5 层 Transformer，输入 `past_hidden` + `cb0_embed` +
///    `noise`，一次调用就在图内把 15 步自回归跑完。
/// 3. **codec**：拆成 `codec_partA`（codes → hidden）与 `codec_partB`
///    （hidden → 波形，走 XNNPACK 的 FORCE_FP16）。按 64 帧一块解码，每块带 25 帧
///    左侧上下文，上采样倍率 1920。
///
/// 图与图之间的一切 —— BPE 分词、提示词拼装、16 层码本嵌入求和、带重复惩罚与
/// 控制 token 抑制的采样、分块编解码 —— 都在宿主侧完成。
library;

import 'dart:io';
import 'dart:math' as math;
import 'dart:typed_data';

import 'package:flutter_demo/demos/ml/engines/ml_npy.dart';
import 'package:flutter_demo/demos/ml/engines/ml_signature.dart';
import 'package:flutter_demo/demos/ml/engines/ml_tflite_helper.dart';
import 'package:flutter_demo/demos/ml/engines/qwen_bpe_tokenizer.dart';

/// 一次合成的结果与分阶段耗时 —— 与 Android `Qwen3TtsEngine.Result` 对应
class Qwen3TtsResult {
  const Qwen3TtsResult({
    required this.audio,
    required this.frames,
    required this.prefillMs,
    required this.talkerMs,
    required this.mtpMs,
    required this.codecMs,
  });

  /// 24 kHz 单声道 PCM（float，范围约 ±1）
  final Float32List audio;

  /// 生成的帧数（12.5 Hz）
  final int frames;

  final int prefillMs;
  final int talkerMs;
  final int mtpMs;
  final int codecMs;

  /// 三图净耗时（不含宿主侧采样与嵌入求和）
  int get graphMs => prefillMs + talkerMs + mtpMs + codecMs;

  /// 音频时长（秒）
  double get audioSeconds => audio.length / Qwen3TtsEngine.sampleRate;

  /// 实时率 = 三图耗时 / 音频时长，> 1 表示慢于实时
  double get realTimeFactor =>
      audioSeconds > 0 ? graphMs / 1000.0 / audioSeconds : 0.0;
}

/// Qwen3-TTS 引擎：四张图 + 宿主侧张量 + KV cache 的持有者
///
/// ### 与 Android 的一处**能力性**差异（不是取舍）
///
/// Android 的 `CompiledModel.run(inputs, outputs, key)` 允许把**任意缓冲**按名字
/// 绑进某一步的输入/输出，于是 KV cache 可以做真正的「零拷贝 ping-pong」：两份
/// 完整 KV（setA / setB）交替充当输入与输出，每步只切指针。
///
/// Flutter 侧走的 LiteRT 经典 C API（`TfLiteSignatureRunner*`）**没有**这个能力 ——
/// 输入/输出永远是图内固定张量。因此本引擎的做法是：`decode` 跑完之后，把输出侧的
/// KV 张量拷回输入侧，供下一步使用。为把这一次拷贝压到最小，只拷贝**有效前缀**：
/// KV 布局是 `[1, heads, cache, headDim]`，第 i 步只有 `[0, position]` 这一段参与
/// 下一步的注意力计算，更远的槽位被因果掩码（`mask` 输入）屏蔽，拷不拷都不影响结果。
/// 即便如此，每步仍要多搬 `heads × (position+1) × headDim` 个 fp32 —— 这是 C API
/// 层面的限制，实测耗时里如实展示，不与 Android 的零拷贝口径混为一谈。
///
/// ### 生命周期
///
/// 原生对象（解释器、模型、delegate）不受 GC 管理，必须显式 [close]；构造中途失败
/// 时逆序回收**已建成的**实例 —— 本页峰值在 1.5 GB 量级，页面会捕获失败后允许重试，
/// 只靠 GC 会让每次失败都留下多份文件映射。
class Qwen3TtsEngine {
  Qwen3TtsEngine._({
    required List<String> errorLog,
    required QwenBpeTokenizer tokenizer,
    required Float32List codecEmbedding,
    required MlNpyHalfTable mtpEmbeddings,
    required MlNpyHalfTable textEmbedding,
    required Map<String, Float32List> projection,
    required Float32List speakerVector,
    required _TalkerSession talker,
    required MlSignatureGraph mtpGraph,
    required MlSignatureRunner mtpRunner,
    required Float32List mtpPastHidden,
    required Float32List mtpCb0Embed,
    required Int32List mtpCodes,
    required MlSignatureGraph codecAGraph,
    required MlSignatureRunner codecARunner,
    required Int32List codecCodesIn,
    required Float32List codecHiddenIn,
    required Float32List codecHiddenOut,
    required MlSignatureGraph codecBGraph,
    required MlSignatureRunner codecBRunner,
    required Float32List codecWaveformOut,
    required List<String> graphNames,
  })  : _errorLog = errorLog,
        _tokenizer = tokenizer,
        _codecEmbedding = codecEmbedding,
        _mtpEmbeddings = mtpEmbeddings,
        _textEmbedding = textEmbedding,
        _projection = projection,
        _speakerVector = speakerVector,
        _talker = talker,
        _mtpGraph = mtpGraph,
        _mtpRunner = mtpRunner,
        _mtpPastHidden = mtpPastHidden,
        _mtpCb0Embed = mtpCb0Embed,
        _mtpCodes = mtpCodes,
        _codecAGraph = codecAGraph,
        _codecARunner = codecARunner,
        _codecCodesIn = codecCodesIn,
        _codecHiddenIn = codecHiddenIn,
        _codecHiddenOut = codecHiddenOut,
        _codecBGraph = codecBGraph,
        _codecBRunner = codecBRunner,
        _codecWaveformOut = codecWaveformOut,
        _graphNames = graphNames;

  // ─────────────────── 宿主侧张量 ───────────────────

  final QwenBpeTokenizer _tokenizer;

  /// codec 各码本的嵌入表（fp32，3072 × 1024，约 12.6 MB）
  final Float32List _codecEmbedding;

  /// MTP 的 15 张残差码本嵌入表（fp16，15 × 2048 × 1024，约 63 MB）；按需读，不入堆
  final MlNpyHalfTable _mtpEmbeddings;

  /// 文本嵌入表（fp16，约 622 MB）；按需读，不入堆
  final MlNpyHalfTable _textEmbedding;

  /// 文本嵌入到 talker hidden 的 SiLU 投影 MLP（2048 → 2048 → 1024）
  final Map<String, Float32List> _projection;

  /// 内置示例说话人（1024 维 x-vector）
  final Float32List _speakerVector;

  final _TalkerSession _talker;
  final MlSignatureGraph _mtpGraph;
  final MlSignatureRunner _mtpRunner;
  final Float32List _mtpPastHidden;
  final Float32List _mtpCb0Embed;
  final Int32List _mtpCodes;

  final MlSignatureGraph _codecAGraph;
  final MlSignatureRunner _codecARunner;
  final Int32List _codecCodesIn;
  final Float32List _codecHiddenIn;
  final Float32List _codecHiddenOut;

  final MlSignatureGraph _codecBGraph;
  final MlSignatureRunner _codecBRunner;
  final Float32List _codecWaveformOut;

  final List<String> _graphNames;

  /// 复用的单行暂存（文本查表 / MTP 查表），避免每次查表都新建 Float32List
  final Float32List _textRowScratch = Float32List(textDim);
  final Float32List _mtpRowScratch = Float32List(hidden);

  /// 控制 token 抑制表与重复惩罚的历史集合（一次合成内有效）
  final Float32List _suppress = _buildSuppress();

  /// LiteRT 最近一次错误上报的内容，用于把「状态码 1」变成可读原因。
  ///
  /// 与各张图共享**同一个** List 实例（`MlSignatureGraph.open` 的 `onError` 闭包捕获它），
  /// 因此无论哪个签名失败都能读到原因。
  final List<String> _errorLog;

  /// 默认说话人向量（1024 维 x-vector）
  Float32List get speakerVector => _speakerVector;

  /// 当前装载的图集名，用于状态栏展示
  String get graphNames => _graphNames.join(' + ');

  /// 词表条目数；仅用于状态栏展示
  int get tokenizerSize => _tokenizer.vocabularySize;

  bool _closed = false;

  // ─────────────────── 构建 ───────────────────

  /// 打开四张图与全部宿主侧张量
  ///
  /// 模型文件必须在 [dir] 下就位（页面负责先下载）。任何一份缺失或形状不符都在
  /// 这里以清晰信息失败，而不是拖到按错误偏移读内存越界。
  static Qwen3TtsEngine open(Directory dir, {int? numThreads}) {
    final int threads = numThreads ?? MlTfliteHelper.cpuThreads;
    final List<void Function()> undo = <void Function()>[];
    final List<String> errorLog = <String>[];
    try {
      MlSignatureGraph openGraph(String fileName, {int? xnnpackFlags}) {
        final File file = File('${dir.path}/$fileName');
        if (!file.existsSync() || file.lengthSync() == 0) {
          throw StateError('模型未就绪：$fileName');
        }
        final MlSignatureGraph graph = MlSignatureGraph.open(
          file.path,
          numThreads: threads,
          xnnpackFlags: xnnpackFlags,
          onError: (String message) {
            errorLog
              ..clear()
              ..add(message);
          },
        );
        undo.add(graph.close);
        return graph;
      }

      // ── 图 ──
      final MlSignatureGraph talkerGraph = openGraph(talkerFile);
      final MlSignatureGraph mtpGraph = openGraph(mtpFile);
      final MlSignatureGraph codecAGraph = openGraph(codecPartAFile);
      // 官方对这一张图显式要求 XNNPACK 以 fp16 执行（FORCE_FP16 = bit 2）
      final MlSignatureGraph codecBGraph =
          openGraph(codecPartBFile, xnnpackFlags: xnnpackForceFp16);

      // ── 宿主张量 ──
      final QwenBpeTokenizer tokenizer = QwenBpeTokenizer.fromFiles(
        File('${dir.path}/$vocabFile'),
        File('${dir.path}/$mergesFile'),
      );
      final Float32List codecEmbedding =
          MlNpy.loadFile(File('${dir.path}/$codecEmbeddingFile'));
      final MlNpyHalfTable mtpEmbeddings =
          MlNpyHalfTable.open(File('${dir.path}/$mtpEmbeddingsFile'));
      undo.add(mtpEmbeddings.close);
      final MlNpyHalfTable textEmbedding =
          MlNpyHalfTable.open(File('${dir.path}/$textEmbeddingFile'));
      undo.add(textEmbedding.close);
      final Map<String, Float32List> projection = MlNpy.loadNpz(
        File('${dir.path}/$textProjectionFile'),
        projectionNames,
      );
      final Float32List speaker =
          MlNpy.loadFile(File('${dir.path}/$speakerFile'));

      // 形状校验：半截下载的文件在这里就以清晰信息失败
      if (codecEmbedding.length != codecVocab * hidden) {
        throw StateError('$codecEmbeddingFile 形状不符：${codecEmbedding.length} 个 float，'
            '期望 ${codecVocab * hidden}');
      }
      if (mtpEmbeddings.count != mtpSlots * mtpVocab * hidden) {
        throw StateError('$mtpEmbeddingsFile 形状不符：${mtpEmbeddings.count} 个元素，'
            '期望 ${mtpSlots * mtpVocab * hidden}');
      }
      if (textEmbedding.count % textDim != 0) {
        throw StateError('$textEmbeddingFile 末维不是 $textDim：'
            '共 ${textEmbedding.count} 个元素');
      }
      if (speaker.length != hidden) {
        throw StateError('$speakerFile 形状不符：${speaker.length} 维，期望 $hidden');
      }
      _requireLength(projection, 'w1', textDim * textDim);
      _requireLength(projection, 'b1', textDim);
      _requireLength(projection, 'w2', hidden * textDim);
      _requireLength(projection, 'b2', hidden);

      // ── talker：双签名 + KV ──
      final _TalkerSession talker = _TalkerSession(
        graph: talkerGraph,
        errorLog: errorLog,
      );
      undo.add(talker.close);

      // ── MTP：位置入参 past_hidden / cb0_embed / noise，出参 codes ──
      final MlSignatureRunner mtpRunner = mtpGraph.runner(mtpGraph.signatureKeys.first);
      mtpRunner.allocateTensors();
      final List<String> mtpInputNames = mtpRunner.inputNames;
      final List<String> mtpOutputNames = mtpRunner.outputNames;
      if (mtpInputNames.length < 3 || mtpOutputNames.isEmpty) {
        throw StateError('MTP 图契约不符：$mtpInputNames -> $mtpOutputNames');
      }
      final Float32List mtpPastHidden =
          mtpRunner.floatView(mtpInputNames[0], input: true);
      final Float32List mtpCb0Embed =
          mtpRunner.floatView(mtpInputNames[1], input: true);
      // noise 输入：写零即图内贪心（与 Android 一样只写一次，之后不再动）
      final Float32List mtpNoise =
          mtpRunner.floatView(mtpInputNames[2], input: true);
      mtpNoise.fillRange(0, mtpNoise.length, 0);
      final Int32List mtpCodes =
          mtpRunner.intView(mtpOutputNames[0], input: false);

      // ── codec A / B ──
      final MlSignatureRunner codecARunner =
          codecAGraph.runner(codecAGraph.signatureKeys.first);
      codecARunner.allocateTensors();
      final MlSignatureRunner codecBRunner =
          codecBGraph.runner(codecBGraph.signatureKeys.first);
      codecBRunner.allocateTensors();
      final Int32List codecCodesIn =
          codecARunner.intView(codecARunner.inputNames.first, input: true);
      final Float32List codecHiddenOut =
          codecARunner.floatView(codecARunner.outputNames.first, input: false);
      final int hiddenInLength =
          codecBRunner.inputByteSize(codecBRunner.inputNames.first) ~/ 4;
      if (hiddenInLength != codecHiddenOut.length) {
        throw StateError('codec 中间张量长度不符：'
            'partA 输出 ${codecHiddenOut.length}，partB 输入 $hiddenInLength');
      }
      // 两张独立的图 —— 与 Android 不同，partA 的输出缓冲**不能**直接当 partB 的
      // 输入（各图的内存各自独立），必须显式拷一次中间张量
      final Float32List codecHiddenIn =
          codecBRunner.floatView(codecBRunner.inputNames.first, input: true);
      final Float32List codecWaveformOut =
          codecBRunner.floatView(codecBRunner.outputNames.first, input: false);

      return Qwen3TtsEngine._(
        errorLog: errorLog,
        tokenizer: tokenizer,
        codecEmbedding: codecEmbedding,
        mtpEmbeddings: mtpEmbeddings,
        textEmbedding: textEmbedding,
        projection: projection,
        speakerVector: speaker,
        talker: talker,
        mtpGraph: mtpGraph,
        mtpRunner: mtpRunner,
        mtpPastHidden: mtpPastHidden,
        mtpCb0Embed: mtpCb0Embed,
        mtpCodes: mtpCodes,
        codecAGraph: codecAGraph,
        codecARunner: codecARunner,
        codecCodesIn: codecCodesIn,
        codecHiddenIn: codecHiddenIn,
        codecHiddenOut: codecHiddenOut,
        codecBGraph: codecBGraph,
        codecBRunner: codecBRunner,
        codecWaveformOut: codecWaveformOut,
        graphNames: <String>[
          talkerFile,
          mtpFile,
          codecPartAFile,
          codecPartBFile,
        ].map((String name) => name.split('.').first).toList(),
      );
    } catch (_) {
      // 逆序回收：张量缓冲区先于所属图关闭
      for (final void Function() revert in undo.reversed) {
        try {
          revert();
        } catch (_) {
          // 回收期异常不能覆盖原始失败原因
        }
      }
      rethrow;
    }
  }

  // ─────────────────── 对外接口 ───────────────────

  /// 纯文本分词，供启动自检使用
  Int32List encodeText(String text) => _tokenizer.encode(text);

  /// 启动自检：返回不一致的描述，**空列表表示全部通过**
  List<String> findTokenizerMismatches(
    List<({String text, Int32List ids})> vectors,
  ) =>
      _tokenizer.findMismatches(vectors);

  /// 用 [speaker]（1024 维 x-vector）的嗓音合成 [text]
  ///
  /// [greedy] 为真时整条循环按 argmax 走（与 Python 参考实现方向一致）；否则走
  /// top-50 / 温度 0.9 采样（模型默认参数）。两个开关都只作用于第一层码本 ——
  /// 15 层残差码本由折叠 MTP 图内 argmax 决定，与本参数无关。
  ///
  /// [onFrame] **在调用线程上被调用**，调用方负责切线程；每生成一帧回调一次。
  Qwen3TtsResult synthesize(
    String text, {
    String language = 'english',
    Float32List? speaker,
    bool greedy = false,
    int? seed,
    void Function(int frames)? onFrame,
  }) {
    if (_closed) {
      throw StateError('引擎已关闭');
    }
    final math.Random random = seed == null ? math.Random() : math.Random(seed);

    // ── 提示词拼装（宿主侧查表 + 投影 MLP） ──
    final Int32List textIds = _tokenizer.encode(text);
    if (textIds.isEmpty) {
      throw StateError('分词结果为空，没有可合成的文本');
    }
    final Float32List ttsBos = _embedTextRow(ttsBosId);
    final Float32List ttsEos = _embedTextRow(ttsEosId);
    final Float32List ttsPad = _embedTextRow(ttsPadId);

    final List<int> control;
    if (language == 'auto') {
      control = <int>[noThink, thinkBos, thinkEos];
    } else {
      final int? languageId = languageIds[language.toLowerCase()];
      if (languageId == null) {
        throw ArgumentError.value(language, 'language', '不支持的语言');
      }
      control = <int>[think, thinkBos, languageId, thinkEos];
    }

    // codec 侧前缀 = 控制 token | 说话人 | [PAD, BOS] → 每项 1024 维
    final List<Float32List> codecPrefix = <Float32List>[
      for (final int id in control) _codecRow(id),
      speaker ?? _speakerVector,
      _codecRow(padId),
      _codecRow(bosId),
    ];

    final List<Float32List> role = _embedText(promptPrefix);
    // 文本体 = 「PAD×n + BOS」与 codecPrefix 前 n 项逐一相加（最后一项 BOS 对的是首字）
    final List<Float32List> body = <Float32List>[
      for (int i = 0; i < codecPrefix.length - 1; i++)
        _add(
          i < codecPrefix.length - 2 ? ttsPad : ttsBos,
          codecPrefix[i],
        ),
    ];
    final Float32List firstText =
        _add(_embedTextRow(textIds[0]), codecPrefix.last);
    final List<Float32List> prefill = <Float32List>[
      ...role,
      ...body,
      firstText,
    ];

    // 余下文本按帧依次作为条件；末尾补 TTS_EOS
    final List<Float32List> trailing = <Float32List>[
      for (int i = 1; i < textIds.length; i++) _embedTextRow(textIds[i]),
      ttsEos,
    ];

    // ── talker 预填充 + 首次 decode ──
    int mark = _nowMs();
    _talker.prefill(prefill);
    int position = prefill.length - 1;
    _QwenStep step = _talker.decode(prefill.last, position);
    final int prefillMs = _nowMs() - mark;

    // ── 逐帧循环 ──
    // 码本 2048 以上是非语音控制 token，抑制掉；只有 EOS 例外
    final List<Int32List> frames = <Int32List>[];
    final Set<int> history = <int>{};
    int talkerMs = 0;
    int mtpMs = 0;

    while (frames.length < maxFrames) {
      final Float32List scores = Float32List(codecVocab);
      for (int i = 0; i < codecVocab; i++) {
        scores[i] = step.logits[i] + _suppress[i];
      }
      if (frames.length < minFrames) {
        scores[eos] = neg;
      }
      // 重复惩罚：出现过的 token 按其符号向零靠近 5%
      for (final int token in history) {
        scores[token] =
            scores[token] > 0 ? scores[token] / 1.05 : scores[token] * 1.05;
      }

      final int firstCode = _pick(scores, greedy, random);
      history.add(firstCode);
      if (firstCode == eos) {
        break;
      }

      mark = _nowMs();
      final Int32List residual = _mtpFrame(step.hidden, firstCode);
      mtpMs += _nowMs() - mark;

      final Int32List frame = Int32List(codecBooks);
      frame[0] = firstCode;
      for (int i = 0; i < mtpSlots; i++) {
        frame[i + 1] = residual[i];
      }
      frames.add(frame);
      onFrame?.call(frames.length);

      // 下一步输入嵌入 = 16 层码本嵌入之和 + 文本条件
      final Float32List embed = _codecRow(firstCode);
      for (int i = 0; i < mtpSlots; i++) {
        _addMtpRow(embed, i, residual[i]);
      }
      final int textIndex = frames.length - 1;
      final Float32List condition =
          textIndex < trailing.length ? trailing[textIndex] : ttsPad;
      for (int i = 0; i < hidden; i++) {
        embed[i] += condition[i];
      }

      position += 1;
      mark = _nowMs();
      step = _talker.decode(embed, position);
      talkerMs += _nowMs() - mark;
    }

    // ── 编解码器还原波形 ──
    mark = _nowMs();
    final Float32List audio = _decodeCodes(frames);
    final int codecMs = _nowMs() - mark;

    return Qwen3TtsResult(
      audio: audio,
      frames: frames.length,
      prefillMs: prefillMs,
      talkerMs: talkerMs,
      mtpMs: mtpMs,
      codecMs: codecMs,
    );
  }

  // ─────────────────── MTP：折叠图一次调用出一帧 ───────────────────

  Int32List _mtpFrame(Float32List hiddenState, int firstCode) {
    _mtpPastHidden.setAll(0, hiddenState);
    _mtpCb0Embed.setAll(0, _codecRow(firstCode));
    _invoke(_mtpRunner, 'MTP');
    return _mtpCodes;
  }

  // ─────────────────── codec：固定 64 帧一块 + 25 帧左上下文 ───────────────────

  Float32List _decodeCodes(List<Int32List> frames) {
    if (frames.isEmpty) {
      return Float32List(0);
    }
    final List<Float32List> pieces = <Float32List>[];
    int start = 0;
    while (start < frames.length) {
      final int context = math.min(codecContext, start);
      // 窗口 = 左上下文 + 新帧，且必须塞进固定 T 的图里，
      // 因此每块最多推进 CODEC_CHUNK - 上下文 帧
      final int end = math.min(start + codecChunk - context, frames.length);
      final int window = end - (start - context);

      // 形状 [16, 64]，第 q 行的第 t 列是该帧第 q 层码本
      _codecCodesIn.fillRange(0, _codecCodesIn.length, 0);
      for (int t = 0; t < window; t++) {
        final Int32List frame = frames[start - context + t];
        for (int q = 0; q < codecBooks; q++) {
          _codecCodesIn[q * codecChunk + t] = frame[q];
        }
      }
      _invoke(_codecARunner, 'codec partA');
      _codecHiddenIn.setAll(0, _codecHiddenOut);
      _invoke(_codecBRunner, 'codec partB');

      // 丢掉左上下文对应的那段波形，只留本块新增帧
      pieces.add(
        Float32List.sublistView(
          _codecWaveformOut,
          context * upsample,
          window * upsample,
        ),
      );
      start = end;
    }

    int total = 0;
    for (final Float32List piece in pieces) {
      total += piece.length;
    }
    final Float32List out = Float32List(total);
    int offset = 0;
    for (final Float32List piece in pieces) {
      out.setRange(offset, offset + piece.length, piece);
      offset += piece.length;
    }
    return out;
  }

  // ─────────────────── 宿主侧数学 ───────────────────

  Float32List _codecRow(int id) {
    final Float32List out = Float32List(hidden);
    out.setRange(0, hidden, _codecEmbedding, id * hidden);
    return out;
  }

  void _addMtpRow(Float32List accumulator, int table, int id) {
    final int base = (table * mtpVocab + id) * hidden;
    _mtpEmbeddings.readInto(_mtpRowScratch, 0, base, hidden);
    for (int i = 0; i < hidden; i++) {
      accumulator[i] += _mtpRowScratch[i];
    }
  }

  static Float32List _add(Float32List left, Float32List right) {
    final Float32List out = Float32List(hidden);
    for (int i = 0; i < hidden; i++) {
      out[i] = left[i] + right[i];
    }
    return out;
  }

  /// 文本嵌入查表 + 2048 → 2048 → 1024 的 SiLU 投影 MLP
  Float32List _embedTextRow(int id) {
    _textEmbedding.readInto(_textRowScratch, 0, id * textDim, textDim);
    final Float32List w1 = _projection['w1'] ?? Float32List(0);
    final Float32List b1 = _projection['b1'] ?? Float32List(0);
    final Float32List w2 = _projection['w2'] ?? Float32List(0);
    final Float32List b2 = _projection['b2'] ?? Float32List(0);

    final Float32List hiddenLayer = Float32List(textDim);
    for (int row = 0; row < textDim; row++) {
      double acc = b1[row];
      final int weightBase = row * textDim;
      for (int column = 0; column < textDim; column++) {
        acc += w1[weightBase + column] * _textRowScratch[column];
      }
      hiddenLayer[row] = acc / (1.0 + math.exp(-acc));
    }
    final Float32List out = Float32List(hidden);
    for (int row = 0; row < hidden; row++) {
      double acc = b2[row];
      final int weightBase = row * textDim;
      for (int column = 0; column < textDim; column++) {
        acc += w2[weightBase + column] * hiddenLayer[column];
      }
      out[row] = acc;
    }
    return out;
  }

  List<Float32List> _embedText(List<int> ids) =>
      <Float32List>[for (final int id in ids) _embedTextRow(id)];

  /// 贪心 argmax 或 top-50 / 温度 0.9 采样
  int _pick(Float32List scores, bool greedy, math.Random random) {
    if (greedy) {
      int best = 0;
      for (int i = 1; i < scores.length; i++) {
        if (scores[i] > scores[best]) {
          best = i;
        }
      }
      return best;
    }
    final int topK = math.min(50, scores.length);
    final List<int> indices = List<int>.generate(scores.length, (int i) => i)
      ..sort((int a, int b) => scores[b].compareTo(scores[a]));
    final Float64List probabilities = Float64List(topK);
    final double maxScaled = scores[indices[0]] / temperature;
    double sum = 0.0;
    for (int i = 0; i < topK; i++) {
      probabilities[i] = math.exp(scores[indices[i]] / temperature - maxScaled);
      sum += probabilities[i];
    }
    double r = random.nextDouble() * sum;
    for (int i = 0; i < topK; i++) {
      r -= probabilities[i];
      if (r <= 0) {
        return indices[i];
      }
    }
    return indices[topK - 1];
  }

  void _invoke(MlSignatureRunner runner, String what) {
    _errorLog.clear();
    try {
      runner.invoke();
    } catch (error) {
      final String detail =
          _errorLog.isEmpty ? '' : '（LiteRT 错误上报: ${_errorLog.join(' / ')}）';
      throw StateError('$what 推理失败: $error$detail');
    }
  }

  /// 释放全部原生实例（幂等）
  void close() {
    if (_closed) {
      return;
    }
    _closed = true;
    _talker.close();
    _mtpGraph.close();
    _codecAGraph.close();
    _codecBGraph.close();
    _mtpEmbeddings.close();
    _textEmbedding.close();
  }

  // ─────────────────── 常量（与 Android `Qwen3TtsEngine` 同值） ───────────────────

  /// 与官方一致的采样文件落盘名（下载层把仓库里的目录结构拍平到这里）
  static const String talkerFile = 'talker_int4.tflite';
  static const String mtpFile = 'mtp_folded_int8.tflite';
  static const String codecPartAFile = 'codec_partA.tflite';
  static const String codecPartBFile = 'codec_partB.tflite';
  static const String codecEmbeddingFile = 'codec_embedding_fp32.npy';
  static const String mtpEmbeddingsFile = 'mtp_embeddings_fp16.npy';
  static const String textEmbeddingFile = 'text_embedding_fp16.npy';
  static const String textProjectionFile = 'text_projection_fp32.npz';
  static const String speakerFile = 'demo_speaker.npy';
  static const String vocabFile = 'vocab.json';
  static const String mergesFile = 'merges.txt';

  /// 24 kHz 单声道
  static const int sampleRate = 24000;

  /// 官方硬上限：12.5 Hz × 512 帧 ≈ 41 s 音频
  static const int maxFrames = 512;

  /// 十种语言 + `auto`；id 取自模型词表
  static const Map<String, int> languageIds = <String, int>{
    'chinese': 2055,
    'english': 2050,
    'german': 2053,
    'italian': 2070,
    'portuguese': 2071,
    'spanish': 2054,
    'japanese': 2058,
    'korean': 2064,
    'french': 2061,
    'russian': 2069,
  };

  /// 界面下拉项，顺序与官方一致：english 打头，其余按字母序，auto 收尾
  static List<String> get languages => <String>[
        'english',
        ...(languageIds.keys.where((String key) => key != 'english').toList()
          ..sort()),
        'auto',
      ];

  static const int hidden = 1024;
  static const int textDim = 2048;

  /// talker 28 层，每层 k/v 一份 cache
  static const int talkerLayers = 28;

  /// 码本词表：前 2048 是语音码本，之上是控制 token
  static const int codecVocab = 3072;
  static const int controlVocab = 2048;

  /// 每帧 16 层码本 = 1 层语义 + 15 层残差
  static const int codecBooks = 16;
  static const int mtpSlots = 15;

  /// MTP 的残差码本词表大小（15 层共用同一张 2048 词表）
  static const int mtpVocab = 2048;

  static const int prefillLength = 32;

  static const int codecChunk = 64;
  static const int codecContext = 25;
  static const int upsample = 1920;

  static const double neg = -1e9;

  static const int eos = 2150;
  static const int padId = 2148;
  static const int bosId = 2149;
  static const int think = 2154;
  static const int thinkBos = 2156;
  static const int thinkEos = 2157;
  static const int noThink = 2155;
  static const int ttsBosId = 151672;
  static const int ttsEosId = 151673;
  static const int ttsPadId = 151671;

  /// 官方设的「至少 2 帧」下界，避免短句立刻命中 EOS
  static const int minFrames = 2;

  static const double temperature = 0.9;

  /// XNNPACK `FORCE_FP16` 标志位（`TFLITE_XNNPACK_DELEGATE_FLAG_FORCE_FP16`）
  static const int xnnpackForceFp16 = 4;

  static const String decodeSignature = 'decode';
  static const String prefillSignature = 'prefill_32';

  /// 提示词前缀：`<|im_start|>assistant\n`
  ///
  /// 官方另有一个 `PROMPT_SUFFIX`（`<|im_end|>\n<|im_start|>assistant\n`）但从未使用 ——
  /// 生成段的条件由 codec 侧前缀与逐帧文本条件承担，因此这里不保留该常量。
  static const List<int> promptPrefix = <int>[151644, 77091, 198];

  static const List<String> projectionNames = <String>['w1', 'b1', 'w2', 'b2'];

  static Float32List _buildSuppress() {
    final Float32List suppress = Float32List(codecVocab);
    for (int i = controlVocab; i < codecVocab; i++) {
      suppress[i] = neg;
    }
    // 只有 EOS 例外：它落在被抑制的区间里，但必须可被选中才能正常收尾
    suppress[eos] = 0;
    return suppress;
  }

  static void _requireLength(
    Map<String, Float32List> table,
    String key,
    int expected,
  ) {
    final Float32List? value = table[key];
    if (value == null) {
      throw StateError('投影表缺少 $key');
    }
    if (value.length != expected) {
      throw StateError('投影表 $key 长度为 ${value.length}，期望 $expected');
    }
  }

  static int _nowMs() => DateTime.now().microsecondsSinceEpoch ~/ 1000;
}

/// 单个 talker 解码步的输出
class _QwenStep {
  const _QwenStep(this.logits, this.hidden);

  /// 前 [Qwen3TtsEngine.codecVocab] 个是码本 logits
  final Float32List logits;

  /// `logits` 张量尾部的 1024 维即 hidden state，喂给 MTP
  final Float32List hidden;
}

/// talker 的双签名会话：`prefill_32` 建 KV，`decode` 单步推进
///
/// ### 为什么要搬两次 KV（Android 一次都不用）
///
/// 两个签名各占**一张独立 subgraph**，同名 KV cache 在两边是**各自的内存**：
///
/// ```
/// prefill_32 子图：[embeddings, input_pos, mask, kv_in ×56] -> kv_out ×56
/// decode     子图：[embeddings, input_pos, mask, kv_in ×56] -> kv_out ×56, logits
/// ```
///
/// Android 的 `CompiledModel.run(inputs, outputs, key)` 能把**同一组缓冲**按名字分别
/// 绑给两个签名，于是 prefill 的输出缓冲直接就是 decode 的输入缓冲，天然衔接、零拷贝。
/// 经典 C API 绑不了，因此这里补两次搬运：
///
/// 1. prefill 结束后，prefill 的 KV **输出** → decode 的 KV **输入**（整个合成一次）；
/// 2. 每个 decode 步结束后，decode 的 KV **输出** → decode 的 KV **输入**（每步一次）。
///
/// 两次都只搬**有效前缀**（见 [_handoff]），把多出来的开销压到最小。
class _TalkerSession {
  _TalkerSession({
    required MlSignatureGraph graph,
    required List<String> errorLog,
  })  : _graph = graph,
        _errorLog = errorLog {
    _prefill = graph.runner(Qwen3TtsEngine.prefillSignature);
    _decode = graph.runner(Qwen3TtsEngine.decodeSignature);
    _prefill.allocateTensors();
    _decode.allocateTensors();

    // KV 名字不成对出现就没有意义，两套签名都要齐
    _kvNames = _decode.inputNames
        .where((String name) => name.startsWith('kv_cache_'))
        .toList();
    final int expected = Qwen3TtsEngine.talkerLayers * 2;
    if (_kvNames.length != expected) {
      throw StateError('talker KV 张量应有 $expected 个'
          '（${Qwen3TtsEngine.talkerLayers} 层 × k/v），实得 ${_kvNames.length}：'
          '${_kvNames.join(', ')}');
    }
    for (final String name in _kvNames) {
      if (!_prefill.inputNames.contains(name) ||
          !_prefill.outputNames.contains(name)) {
        throw StateError('prefill 签名缺少 KV 张量 $name');
      }
      if (!_decode.outputNames.contains(name)) {
        throw StateError('decode 签名缺少 KV 输出 $name');
      }
    }

    // KV 布局 [1, heads, cache, headDim]：分块搬运要用到后三维
    final List<int> kvShape = _decode.inputShape(_kvNames.first);
    if (kvShape.length != 4) {
      throw StateError('KV 张量形状不是四维：$kvShape');
    }
    _headCount = kvShape[1];
    _cacheLength = kvShape[2];
    _headDim = kvShape[3];

    _embeddingIn = _decode.floatView(
      _requireName(_decode.inputNames, 'embeddings', 'talker.decode 输入'),
      input: true,
    );
    _positionIn = _decode.intView(
      _requireName(_decode.inputNames, 'input_pos', 'talker.decode 输入'),
      input: true,
    );
    _maskIn = _decode.floatView(
      _requireName(_decode.inputNames, 'mask', 'talker.decode 输入'),
      input: true,
    );
    _logitsOut = _decode.floatView(
      _requireName(_decode.outputNames, 'logits', 'talker.decode 输出'),
      input: false,
    );

    // KV 张量视图在 allocate 之后一次性取好并缓存：每步要碰 112 个张量，
    // 若每次都按名寻址（含一次 C 字符串分配），host 侧开销会盖过推理本身
    for (final String name in _kvNames) {
      _kvIn[name] = _decode.floatView(name, input: true);
      _kvOut[name] = _decode.floatView(name, input: false);
      _prefillKvIn[name] = _prefill.floatView(name, input: true);
      _prefillKvOut[name] = _prefill.floatView(name, input: false);
    }

    _prefillEmbeddingIn = _prefill.floatView(
      _requireName(_prefill.inputNames, 'embeddings', 'talker.prefill 输入'),
      input: true,
    );
    _prefillPositionIn = _prefill.intView(
      _requireName(_prefill.inputNames, 'input_pos', 'talker.prefill 输入'),
      input: true,
    );
    _prefillMaskIn = _prefill.floatView(
      _requireName(_prefill.inputNames, 'mask', 'talker.prefill 输入'),
      input: true,
    );
    _prefillMaskScratch = Float32List(_prefillMaskIn.length);

    if (_prefillEmbeddingIn.length !=
        Qwen3TtsEngine.prefillLength * Qwen3TtsEngine.hidden) {
      throw StateError('prefill 的 embeddings 长度应为 '
          '${Qwen3TtsEngine.prefillLength * Qwen3TtsEngine.hidden}，'
          '实得 ${_prefillEmbeddingIn.length}');
    }
  }

  final MlSignatureGraph _graph;
  final List<String> _errorLog;

  late final MlSignatureRunner _prefill;
  late final MlSignatureRunner _decode;

  late final List<String> _kvNames;
  late final int _headCount;
  late final int _cacheLength;
  late final int _headDim;

  late final Float32List _embeddingIn;
  late final Int32List _positionIn;
  late final Float32List _maskIn;
  late final Float32List _logitsOut;

  final Map<String, Float32List> _kvIn = <String, Float32List>{};
  final Map<String, Float32List> _kvOut = <String, Float32List>{};
  final Map<String, Float32List> _prefillKvIn = <String, Float32List>{};
  final Map<String, Float32List> _prefillKvOut = <String, Float32List>{};

  late final Float32List _prefillEmbeddingIn;
  late final Int32List _prefillPositionIn;
  late final Float32List _prefillMaskIn;
  late final Float32List _prefillMaskScratch;

  /// 预填充：把提示词嵌入一次喂进去，产出初始 KV cache
  ///
  /// KV 输入显式写零（与 Android 一致，每次预填充都重写）：不写零的话，被掩码屏蔽
  /// 的位置上留着上一次合成的残留值，一旦出现非有限值就会以 `0 × NaN` 的形式污染
  /// 整条注意力。
  void prefill(List<Float32List> embeddings) {
    final int promptLength = embeddings.length;
    if (promptLength > Qwen3TtsEngine.prefillLength) {
      throw StateError('提示词超出 ${Qwen3TtsEngine.prefillLength} 个位置：$promptLength');
    }

    final int hidden = Qwen3TtsEngine.hidden;
    final Float32List flat = Float32List(_prefillEmbeddingIn.length);
    for (int t = 0; t < promptLength; t++) {
      flat.setRange(t * hidden, (t + 1) * hidden, embeddings[t]);
    }
    _prefillEmbeddingIn.setAll(0, flat);

    for (int i = 0; i < _prefillPositionIn.length; i++) {
      _prefillPositionIn[i] = i;
    }

    // 因果掩码：第 row 行只允许看到前 min(row, promptLength-1) 个位置。
    // mask 总长度由张量本身决定（形状 [1, prefillLength, cache] 或已拍平），
    // 与 Android 一样按「每行 cache 个」铺开。
    final Float32List mask = _prefillMaskScratch;
    mask.fillRange(0, mask.length, Qwen3TtsEngine.neg);
    final int rows = mask.length ~/ _cacheLength;
    for (int row = 0; row < rows; row++) {
      final int allowed = math.min(row, promptLength - 1) + 1;
      mask.fillRange(row * _cacheLength, row * _cacheLength + allowed, 0);
    }
    _prefillMaskIn.setAll(0, mask);

    for (final String name in _kvNames) {
      final Float32List target = _prefillKvIn[name] ?? Float32List(0);
      target.fillRange(0, target.length, 0);
    }

    _invoke(_prefill, 'talker prefill');

    // 跨子图衔接：prefill 的 KV 输出 → decode 的 KV 输入
    _handoff(_prefillKvOut, promptLength);
  }

  /// 单步解码：返回该位置的码本 logits 与 hidden state
  _QwenStep decode(Float32List embedding, int position) {
    _embeddingIn.setAll(0, embedding);
    _positionIn[0] = position;
    _maskIn.fillRange(0, _maskIn.length, Qwen3TtsEngine.neg);
    _maskIn.fillRange(0, position + 1, 0);

    _invoke(_decode, 'talker decode');

    // logits 张量是 [4096]：前 3072 是码本 logits，后 1024 是 hidden state
    final int codecVocab = Qwen3TtsEngine.codecVocab;
    final int hidden = Qwen3TtsEngine.hidden;
    final Float32List logits = Float32List(codecVocab);
    logits.setRange(0, codecVocab, _logitsOut, 0);
    final Float32List hiddenState = Float32List(hidden);
    hiddenState.setRange(0, hidden, _logitsOut, codecVocab);

    // 子图内衔接：本步产出的 KV 有效前缀供下一步使用
    _handoff(_kvOut, position + 1);
    return _QwenStep(logits, hiddenState);
  }

  /// 把 [source]（某张子图的 KV 输出）的**有效前缀**搬进 decode 子图的 KV 输入
  ///
  /// KV 布局 `[1, heads, cache, headDim]`：只有 `[0, validTokens)` 这一段会参与下一步
  /// 的注意力计算，更远的槽位被因果掩码屏蔽（掩码由 [decode] 逐位置铺开），因此不必
  /// 整块搬运 —— 这是把「C API 不能绑缓冲」的代价压到最小的关键。
  void _handoff(Map<String, Float32List> source, int validTokens) {
    final int perHead = _cacheLength * _headDim;
    final int span = validTokens * _headDim;
    for (final String name in _kvNames) {
      final Float32List from = source[name] ?? Float32List(0);
      final Float32List to = _kvIn[name] ?? Float32List(0);
      if (from.isEmpty || to.isEmpty) {
        continue;
      }
      for (int head = 0; head < _headCount; head++) {
        final int base = head * perHead;
        to.setRange(base, base + span, from, base);
      }
    }
  }

  void _invoke(MlSignatureRunner runner, String what) {
    _errorLog.clear();
    try {
      runner.invoke();
    } catch (error) {
      final String detail =
          _errorLog.isEmpty ? '' : '（LiteRT 错误上报: ${_errorLog.join(' / ')}）';
      throw StateError('$what 推理失败: $error$detail');
    }
  }

  static String _requireName(List<String> names, String wanted, String what) {
    for (final String name in names) {
      if (name == wanted) {
        return name;
      }
    }
    throw StateError('$what 里没有「$wanted」，实际有：${names.join(', ')}');
  }

  void close() {
    _graph.close();
  }
}
