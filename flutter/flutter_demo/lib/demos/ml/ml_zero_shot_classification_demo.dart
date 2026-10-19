import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart' show rootBundle;
import 'package:flutter_demo/core/basic/basic.dart';
import 'package:flutter_demo/demos/ml/engines/laya/laya_calibration.dart';
import 'package:flutter_demo/demos/ml/engines/laya/laya_decoder.dart';
import 'package:flutter_demo/demos/ml/engines/laya/laya_engine.dart';
import 'package:flutter_demo/demos/ml/engines/laya/laya_json.dart';
import 'package:flutter_demo/demos/ml/engines/laya/laya_prompt_builder.dart';
import 'package:flutter_demo/demos/ml/engines/ml_model_download.dart';

/// LiteRT — Laya 多语言零样本分类（LiteRT / TFLite 侧推理）
///
/// 与 Android `:modules:module_ml` 的 `TFLiteZeroShotClassificationActivity` 一一对应：
/// 1. 「零样本」= 不给示例、不做微调：把「问题描述 + 每个候选的判定标准」拼进提示词，
///    模型直接对输入文本作答；问题按 `choice`（多选一）/ `noul`（真·假）/ `score`（分级）
///    三种类型编码，分别对应提示词里的 `qtype_onehot`；
/// 2. 三张图 + 两套数据文件：主图（`inputs_embeds` / `attention_mask` / `qtype_onehot` →
///    `token_logits` + `pooled_cls`）+ 动作头（`pooled_cls` + marker 推出的 `feats` →
///    `act_logits`）+ BPE 词表 + fp16 嵌入表；
/// 3. 命名张量寻址：`tflite_flutter` 的 `Tensor.name` 可用，引擎构造时按名字建
///    「名字 → 索引」映射（这两个 artifact 的签名顺序与 FlatBuffer 张量顺序并不一致）；
/// 4. GPU 显式请求 FP32（`isPrecisionLossAllowed: false`，模型规格而非性能调优）；
///    构建失败时不静默降级——把原因写进状态栏、拨回开关，并显式重试一次 CPU；
/// 5. 温度校准开关：关闭时解码改用 identity 温度，与官方 `if (state.calibrated)` 同层选择；
/// 6. 模型约 679 MB，与官方一致地**运行时下载**、不入包（见 [MlRemoteModels.laya]）。
///
/// 官方参考：
/// https://github.com/google-ai-edge/litert-samples/tree/main/samples/litert/zero_shot_classification
class MlZeroShotClassificationDemoPage extends BasicImagePage {
  const MlZeroShotClassificationDemoPage({super.key, required super.title});

  @override
  BasicImagePageState<MlZeroShotClassificationDemoPage> createState() =>
      _MlZeroShotClassificationDemoPageState();
}

class _MlZeroShotClassificationDemoPageState
    extends BasicImagePageState<MlZeroShotClassificationDemoPage> {
  static const String _presetsAsset = 'assets/ml/presets.json';

  /// 与 Android 侧 `WINDOW` 一致：多语 checkpoint 只支持 256 / 512 两个窗口
  static const int _window = 256;

  static const String _presetEmail = 'email';
  static const String _presetTriage = 'triage';
  static const String _presetModeration = 'moderation';

  static const String _typeChoice = 'choice';
  static const String _typeScore = 'score';

  /// 示例文本取自官方 strings.xml（保持原样，便于与上游逐字比对）
  static const String _exampleEmailEnSubject = 'Cannot attach an image after the update';
  static const String _exampleEmailEnBody =
      'Hello support,\nSince updating the app yesterday, the support form closes when I attach an image. '
      'Sending text alone still works. I need it for a task next week. Could you tell me how to check this?';
  static const String _exampleEmailJaSubject = '更新後に画像を添付できません';
  static const String _exampleEmailJaBody =
      'サポート窓口ご担当者様\n昨日アプリを更新してから、問い合わせフォームに画像を添付すると画面が閉じてしまいます。'
      '文章だけなら送信できます。来週の作業で使いたいので、確認方法を教えてください。';
  static const String _exampleSupportEn =
      'I was charged twice for the same subscription. I need to confirm the payment by tomorrow. '
      'Please tell me how to get the extra charge refunded. Having to check this repeatedly has been frustrating.';
  static const String _exampleSupportJa =
      '利用料金が二重に請求されています。明日までに支払いを確認する必要があります。'
      '余分な請求の返金方法を教えてください。何度も確認することになり、少し困っています。';
  static const String _exampleModerationEn =
      'I disagree with that explanation. This morning, pressing Save did not keep my changes. '
      'If anyone has tried the same steps, please share what happened so we can compare the results.';
  static const String _exampleModerationJa =
      'その説明には賛成できません。今朝は保存ボタンを押しても内容が残りませんでした。'
      '同じ状況を試した方がいたら、手順を共有してもらえると助かります。';

  final TextEditingController _subjectController = TextEditingController();
  final TextEditingController _inputController = TextEditingController();

  /// 问题集题库（`assets/ml/presets.json`，与 Android 同源）
  Map<String, Object?> _presets = <String, Object?>{};

  LayaEngine? _engine;
  bool _useGpu = true;

  String _presetKey = _presetEmail;
  bool _japanese = false;
  bool _calibrated = true;

  bool _initializing = false;
  bool _running = false;
  bool _downloading = false;

  /// 引擎构建代号：切换后端时自增，过期构建的结果不得落盘到 UI
  int _generation = 0;

  MlModelDownloader? _downloader;

  String _statusText = '初始化中';
  String _resultText = '分类结果: 等待输入';
  String _answersText = '';
  int _tokenCount = 0;

  /// 下载进度 0..1；非下载期保持上次值即可，进度条只在 _downloading 时显示
  double _downloadProgress = 0.0;

  @override
  void initState() {
    super.initState();
    _loadPresets();
    _fillExample();
    _startEngine(useGpu: true);
  }

  @override
  void dispose() {
    _downloader?.cancel();
    final LayaEngine? engine = _engine;
    _engine = null;
    // 嵌入表持有文件句柄，页面销毁必须显式关闭；与在途推理的互斥由 _running 兜底
    engine?.close();
    _subjectController.dispose();
    _inputController.dispose();
    super.dispose();
  }

  Future<void> _loadPresets() async {
    try {
      final String text = await rootBundle.loadString(_presetsAsset);
      if (!mounted) return;
      setState(() {
        _presets = layaAsObject(layaParse(text));
      });
    } catch (error) {
      debugPrint('题库加载失败: $error');
    }
  }

  @override
  List<String> buildList() => <String>[
        '1. 运行全部问题',
        '2. 填入示例文本',
        '3. 切换加速后端（当前：${_useGpu ? "GPU FP32" : "CPU"}）',
        '4. 切换温度校准（当前：${_calibrated ? "校准" : "未校准"}）',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        _runAllQuestions();
      case 1:
        setState(() {
          _fillExample();
        });
      case 2:
        if (_running || _initializing) {
          setState(() {
            _statusText = '请等待当前操作完成后再切换后端';
          });
          return;
        }
        _useGpu = !_useGpu;
        _startEngine(useGpu: _useGpu);
      case 3:
        setState(() {
          _calibrated = !_calibrated;
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
          Text(
            '问题描述 + 候选标准拼成提示词，模型直接作答（不给示例、不微调）',
            style: TextStyle(
              fontSize: BasicDemoDimens.fontTitle,
              color: Colors.grey.shade700,
            ),
          ),
          const SizedBox(height: BasicDemoDimens.itemPadding),
          Wrap(
            spacing: BasicDemoDimens.itemPadding,
            crossAxisAlignment: WrapCrossAlignment.center,
            children: <Widget>[
              _buildPresetChips(),
              _buildLanguageChips(),
            ],
          ),
          const SizedBox(height: BasicDemoDimens.itemPadding),
          if (_presetKey == _presetEmail) ...<Widget>[
            TextField(
              controller: _subjectController,
              decoration: const InputDecoration(
                labelText: '主题（subject）',
                border: OutlineInputBorder(),
                isDense: true,
              ),
            ),
            const SizedBox(height: BasicDemoDimens.itemPadding),
          ],
          TextField(
            controller: _inputController,
            maxLines: 3,
            decoration: InputDecoration(
              labelText: _bodyLabel,
              border: const OutlineInputBorder(),
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
          const SizedBox(height: BasicDemoDimens.itemPadding),
          Expanded(
            child: SingleChildScrollView(
              child: Text(
                _answersText.isEmpty ? '（运行后在此展示逐题答案）' : _answersText,
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

  Widget _buildPresetChips() {
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: <Widget>[
        for (final MapEntry<String, String> entry
            in const <MapEntry<String, String>>[
          MapEntry<String, String>(_presetEmail, '邮件分流'),
          MapEntry<String, String>(_presetTriage, '工单分诊'),
          MapEntry<String, String>(_presetModeration, '内容审核'),
        ])
          Padding(
            padding: const EdgeInsets.only(right: 4),
            child: ChoiceChip(
              label: Text(entry.value),
              selected: _presetKey == entry.key,
              onSelected: (bool selected) {
                if (!selected || _presetKey == entry.key) return;
                setState(() {
                  _presetKey = entry.key;
                  // 不同问题集的输入字段不同，切换时同步清掉不适用的那一栏
                  if (_presetKey != _presetEmail) _subjectController.text = '';
                  _answersText = '';
                  _tokenCount = 0;
                  _fillExample();
                });
              },
            ),
          ),
      ],
    );
  }

  Widget _buildLanguageChips() {
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: <Widget>[
        for (final MapEntry<bool, String> entry
            in const <MapEntry<bool, String>>[
          MapEntry<bool, String>(false, 'English'),
          MapEntry<bool, String>(true, '日本語'),
        ])
          Padding(
            padding: const EdgeInsets.only(right: 4),
            child: ChoiceChip(
              label: Text(entry.value),
              selected: _japanese == entry.key,
              onSelected: (bool selected) {
                if (!selected) return;
                setState(() {
                  _japanese = entry.key;
                  _fillExample();
                });
              },
            ),
          ),
      ],
    );
  }

  String get _bodyLabel {
    switch (_presetKey) {
      case _presetTriage:
        return '消息正文（message）';
      case _presetModeration:
        return '帖子正文（post）';
      default:
        return '邮件正文（body）';
    }
  }

  /// 确保模型就绪（必要时下载）后构建引擎并编译计算图
  ///
  /// @param useGpu 期望的后端；GPU 失败时页面会明确切到 CPU 并重试一次
  Future<void> _startEngine({required bool useGpu}) async {
    if (_initializing) return;
    final int generation = ++_generation;
    _initializing = true;
    final LayaEngine? previous = _engine;
    _engine = null;
    _downloader ??= MlModelDownloader();

    if (mounted) {
      setState(() {
        _statusText = '推理引擎: 编译中（${useGpu ? "GPU FP32" : "CPU"}）…';
        _resultText = '分类结果: 引擎初始化中';
      });
    }

    try {
      await _ensureModels();
      if (!mounted || generation != _generation) return;

      final LayaEngine built = LayaEngine(window: _window);
      await built.loadTables();
      await built.initialize(useGpu: useGpu);
      if (!mounted || generation != _generation) {
        await built.close();
        return;
      }

      _engine = built;
      if (mounted) {
        setState(() {
          final String ready = useGpu ? 'GPU FP32' : 'CPU';
          _statusText =
              '推理引擎: 就绪 · $ready | 词表 ${built.tokenizerSize} 项 | '
              '嵌入表加载 ${built.embeddingLoadMs.toStringAsFixed(0)} ms';
          _resultText = '分类结果: 就绪，点击「运行全部问题」';
        });
      }

      // 新引擎就位后再关旧的（在途推理可能仍持旧引擎）
      await previous?.close();
    } catch (error) {
      debugPrint('Laya 引擎初始化失败: $error');
      if (!mounted || generation != _generation) return;
      // GPU 失败不静默降级：写明原因、拨回开关，再显式重试一次 CPU
      if (useGpu) {
        setState(() {
          _statusText = 'GPU 不可用（$error），已改用 CPU 重试';
          _useGpu = false;
        });
        _initializing = false;
        await _startEngine(useGpu: false);
        return;
      }
      setState(() {
        _statusText = '初始化失败: $error';
        _resultText = '分类结果: 引擎未就绪';
      });
    } finally {
      _initializing = false;
    }
  }

  Future<void> _ensureModels() async {
    final int pending = await MlModelStore.pendingBytes(MlRemoteModels.laya);
    if (pending <= 0) return;

    _downloading = true;
    _downloadProgress = 0.0;
    if (mounted) {
      setState(() {
        _statusText = '首次使用，需下载模型约 ${mlFormatBytes(pending)}…';
      });
    }

    final MlModelDownloader downloader = _downloader!;
    await downloader.ensureAll(
      MlRemoteModels.laya,
      onProgress: (int index, int downloaded, int total, int overall, int overallTotal) {
        if (!mounted) return;
        setState(() {
          final int percent = overallTotal > 0
              ? (overall * 100 / overallTotal).round().clamp(0, 100)
              : 0;
          _downloadProgress = overallTotal > 0 ? overall / overallTotal : 0.0;
          _statusText =
              '下载模型 ${index + 1}/${MlRemoteModels.laya.length} · $percent% '
              '(${mlFormatBytes(overall)} / ${mlFormatBytes(overallTotal)})';
        });
      },
    );

    _downloading = false;
    if (mounted) setState(() {});
  }

  Future<void> _runAllQuestions() async {
    final LayaEngine? engine = _engine;
    if (engine == null) {
      // 引擎未就绪（首次下载未完成或初始化失败）：由运行动作再触发一次初始化
      if (!_initializing) {
        await _startEngine(useGpu: _useGpu);
      }
      return;
    }
    if (_running) return;

    final String text = _inputController.text.trim();
    if (text.isEmpty) {
      setState(() {
        _resultText = '分类结果: 请先输入待分类文本';
      });
      return;
    }

    final String subject = _subjectController.text;
    final Map<String, Object?> questions = _questionsOf(_presetKey);
    if (questions.isEmpty) {
      setState(() {
        _resultText = '分类结果: 题库为空或尚未加载';
      });
      return;
    }

    _running = true;
    if (mounted) {
      setState(() {
        _resultText = '分类结果: 正在推理…';
        _answersText = '';
        _tokenCount = 0;
      });
    }

    try {
      final Map<String, String> state = _stateFor(_presetKey, subject, text);
      final LayaCalibration effective =
          _calibrated ? engine.calibration : LayaCalibration.identity;
      final StringBuffer buffer = StringBuffer();

      for (final MapEntry<String, Object?> entry in questions.entries) {
        final Map<String, Object?> question = layaAsObject(entry.value);
        final result = await engine.answer(
          state,
          question,
          useGpu: _useGpu,
          calibrationOverride: effective,
          questionId: entry.key,
        );
        buffer.writeln(_formatAnswer(
          entry.key,
          result.sequence.question,
          result.answer,
          result.sequence.ids.length,
          result.markerLogits,
          result.totalMs,
          effective,
        ));
        _tokenCount += result.sequence.ids.length;
        if (!mounted) return;
        setState(() {
          _answersText = buffer.toString().trimRight();
        });
      }

      if (!mounted) return;
      setState(() {
        _resultText = '分类结果: 已完成 ${questions.length} 个问题 · '
            '累计 token 数: $_tokenCount';
        _answersText = '${buffer.toString().trimRight()}\n\n累计 token 数: $_tokenCount';
      });
    } catch (error) {
      debugPrint('零样本分类推理失败: $error');
      if (mounted) {
        setState(() {
          _resultText = '推理失败: $error';
        });
      }
    } finally {
      _running = false;
      if (mounted) setState(() {});
    }
  }

  /// 当前问题集的问题列表（id -> 问题描述）
  Map<String, Object?> _questionsOf(String key) {
    final Object? group = _presets[key];
    return group is Map ? layaAsObject(group) : <String, Object?>{};
  }

  /// 输入字段随问题集变化：邮件是 subject+body，工单是 message，审核是 post
  Map<String, String> _stateFor(String key, String subject, String text) {
    switch (key) {
      case _presetTriage:
        return <String, String>{'message': text};
      case _presetModeration:
        return <String, String>{'post': text};
      default:
        return <String, String>{'subject': subject, 'body': text};
    }
  }

  void _fillExample() {
    final bool ja = _japanese;
    switch (_presetKey) {
      case _presetTriage:
        _subjectController.text = '';
        _inputController.text = ja ? _exampleSupportJa : _exampleSupportEn;
      case _presetModeration:
        _subjectController.text = '';
        _inputController.text =
            ja ? _exampleModerationJa : _exampleModerationEn;
      default:
        _subjectController.text =
            ja ? _exampleEmailJaSubject : _exampleEmailEnSubject;
        _inputController.text = ja ? _exampleEmailJaBody : _exampleEmailEnBody;
    }
  }

  // ─────────────────── 答案渲染（镜像官方 presentAnswer） ───────────────────

  String _formatAnswer(
    String id,
    LayaQuestion question,
    Map<String, Object?> answer,
    int tokenCount,
    Float32List markerLogits,
    double totalMs,
    LayaCalibration effective,
  ) {
    // 三类问题的选项来源不同：choice 以候选标签为键（criteria 键序）、
    // score 以等级整数为键并从 legend 取描述、noul 不出现在 probabilities 里，
    // 需拿 marker logits 重算一次「假 / 真」——重算必须用同一套温度。
    final List<({double probability, String? label, int? level, String? description})> rows;
    switch (question.type) {
      case _typeChoice:
        final Map<String, Object?> criteria = layaAsObject(question.criteria);
        final Map<String, Object?> probabilities =
            layaAsObject(answer['probabilities']);
        rows = probabilities.entries
            .map((MapEntry<String, Object?> entry) => (
                  probability: (entry.value as num).toDouble(),
                  label: entry.key,
                  level: null,
                  description: criteria[entry.key] == null
                      ? null
                      : LayaPromptBuilder.renderCriterion(criteria[entry.key]),
                ))
            .toList();
      case _typeScore:
        final Map<String, Object?> legend = layaAsObject(answer['legend']);
        final Map<String, Object?> probabilities =
            layaAsObject(answer['probabilities']);
        rows = probabilities.entries
            .map((MapEntry<String, Object?> entry) => (
                  probability: (entry.value as num).toDouble(),
                  label: null,
                  level: int.tryParse(entry.key) ?? -1,
                  description: legend[entry.key] == null
                      ? null
                      : LayaPromptBuilder.renderCriterion(legend[entry.key]),
                ))
            .toList();
      default:
        final Float32List values = LayaDecoder.probabilities(
          markerLogits,
          question,
          effective,
        );
        rows = <({double probability, String? label, int? level, String? description})>[
          (probability: values[0].toDouble(), label: '假', level: null, description: null),
          (probability: values[1].toDouble(), label: '真', level: null, description: null),
        ];
    }

    final StringBuffer out = StringBuffer();
    out.writeln('[$id] ${question.instructions}');
    out.write('  类型: ${question.type} · token $tokenCount · ');
    out.writeln('耗时 ${totalMs.toStringAsFixed(0)} ms');
    out.write('  温度: ');
    out.writeln('校准 (${effective.temperature(question.type, rows.length).toStringAsFixed(4)})');

    for (final ({double probability, String? label, int? level, String? description}) row in rows) {
      final String name = row.label ?? '等级 ${row.level}';
      final String percent = '${(row.probability * 100).toStringAsFixed(1)}%';
      out.writeln('  ${name.padRight(12)}${percent.padLeft(6)}  ${row.description ?? ''}');
    }

    final StringBuffer decision = StringBuffer();
    final Object? choice = answer['choice'];
    if (choice is String) decision.write('  判定: $choice');
    final Object? score = answer['score'];
    if (score is num) {
      decision.write('  判分: ${LayaDecoder.round4(score.toDouble())}');
    }
    final Object? noul = answer['noul'];
    if (noul is num) {
      decision.write('  为真的概率: ${(noul.toDouble() * 100).toStringAsFixed(1)}%');
    }
    final Object? confidence = answer['confidence'];
    if (confidence is num) {
      decision.write('  置信度: ${(confidence.toDouble() * 100).toStringAsFixed(1)}%');
    }
    if (decision.isNotEmpty) out.writeln(decision.toString());

    return out.toString().trimRight();
  }
}
