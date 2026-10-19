import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';
import 'package:flutter_demo/demos/ml/engines/ml_tflite_helper.dart';
import 'package:tflite_flutter/tflite_flutter.dart';

/// MobileBERT / 平均词向量 — 英文文本情感分类（LiteRT / TFLite 侧推理）
///
/// 与 Android `:modules:module_ml` 的 `TFLiteTextClassificationActivity` 一一对应：
/// 1. 词表来自模型内嵌 metadata（`vocab.txt`），已随资产分发为 `bert_vocab.txt` /
///    `average_word_vocab.txt`；**两个模型的词表格式不同**：BERT 是单列裸词（行号即 id），
///    平均词向量是双列 `token id`（取第二列的 id），统一由 [MlTfliteHelper.buildVocabulary] 判别；
/// 2. 两个模型输入签名不同：平均词向量是单输入 `[1, 256]`，MobileBERT 是三输入 `[1, 128]`；
///    **以模型自身声明的顺序为准**：`input_word_ids` → `input_type_ids` → `input_mask`，
///    其余输入必须显式写入，否则原生侧读到的是未初始化内存；
/// 3. 输入张量元素类型是 INT32；序列长度与模型一起切换，不从 UI 开关现读；
/// 4. 输出 `[1, 2]` 为 FP32 概率（负面 / 正面），无需反量化与 Softmax；
/// 5. 分词：去标点 → 空格切词 → 查词表，未登录词统一回落到 id 0（与 padding 同样被掩码屏蔽）。
///
/// 官方参考：
/// https://github.com/google-ai-edge/litert-samples/tree/main/samples/litert/text_classification
class MlTextClassificationDemoPage extends BasicImagePage {
  const MlTextClassificationDemoPage({super.key, required super.title});

  @override
  BasicImagePageState<MlTextClassificationDemoPage> createState() =>
      _MlTextClassificationDemoPageState();
}

/// 一个文本模型引擎：解释器 + 与该模型规格绑定的词表 / 序列长度
class _TextEngine {
  const _TextEngine({
    required this.interpreter,
    required this.vocabulary,
    required this.sequenceLength,
    required this.inputCount,
    required this.displayName,
  });

  final Interpreter interpreter;
  final Map<String, int> vocabulary;

  /// 序列长度与模型同源：切换模型时两者一起换，避免拿新模型的长度写旧模型的输入
  final int sequenceLength;
  final int inputCount;
  final String displayName;
}

class _MlTextClassificationDemoPageState
    extends BasicImagePageState<MlTextClassificationDemoPage> {
  static const String _bertAsset = 'assets/ml/bert_classifier.tflite';
  static const String _bertVocabAsset = 'assets/ml/bert_vocab.txt';
  static const String _wordVecAsset = 'assets/ml/average_word_classifier.tflite';
  static const String _wordVecVocabAsset = 'assets/ml/average_word_vocab.txt';

  /// [PAD] 在词表中的 id 为 0
  static const int _padTokenId = 0;

  /// 去标点，仅保留字母、数字与空格
  static final RegExp _punctuation = RegExp('[^a-zA-Z0-9 ]');

  static const List<String> _labels = <String>['负面', '正面'];

  static const String _positiveSample = 'this movie is fantastic and i love it';

  final TextEditingController _inputController = TextEditingController();

  _TextEngine? _engine;
  bool _useMobileBert = true;
  String _engineLabel = '初始化中';

  List<double> _probabilities = <double>[0.0, 0.0];
  double _tokenizeMs = 0.0;
  double _inferenceMs = 0.0;
  String _resultText = '分类结果: 等待输入';

  @override
  void initState() {
    super.initState();
    _inputController.text = _positiveSample;
    _rebuildModel(mobileBert: true);
  }

  @override
  void dispose() {
    _engine?.interpreter.close();
    _inputController.dispose();
    super.dispose();
  }

  @override
  List<String> buildList() => <String>[
        '1. 开始分类',
        '2. 填入示例文本',
        '3. 切换模型（当前：${_useMobileBert ? "MobileBERT" : "平均词向量"}）',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        _runClassification();
      case 1:
        setState(() {
          _inputController.text = _positiveSample;
        });
      case 2:
        _useMobileBert = !_useMobileBert;
        _rebuildModel(mobileBert: _useMobileBert);
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
            '输入一句英文，模型输出负面 / 正面二分类概率',
            style: TextStyle(fontSize: BasicDemoDimens.fontTitle),
          ),
          const SizedBox(height: BasicDemoDimens.itemPadding),
          TextField(
            controller: _inputController,
            maxLines: 3,
            decoration: const InputDecoration(
              border: OutlineInputBorder(),
              isDense: true,
            ),          ),
          const SizedBox(height: BasicDemoDimens.itemPadding),
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
            '分词 ${_tokenizeMs.toStringAsFixed(2)} ms | '
            '推理 ${_inferenceMs.toStringAsFixed(2)} ms',
            style: TextStyle(
              fontSize: BasicDemoDimens.fontBody,
              color: Colors.grey.shade600,
            ),
          ),
          const SizedBox(height: BasicDemoDimens.itemPadding),
          Text(
            '推理引擎: $_engineLabel',
            style: const TextStyle(
              fontSize: BasicDemoDimens.fontBody,
              color: Colors.blueGrey,
            ),
          ),
          const SizedBox(height: BasicDemoDimens.itemPadding),
          Expanded(child: _buildDistribution()),
        ],
      ),
    );
  }

  Widget _buildDistribution() {
    final double negative = _probabilities.isNotEmpty ? _probabilities[0] : 0.0;
    final double positive = _probabilities.length > 1 ? _probabilities[1] : 0.0;
    final List<double> values = <double>[negative, positive];
    final int top = _argMax(values);

    return Column(
      children: <Widget>[
        for (int i = 0; i < _labels.length; i++)
          Padding(
            padding: const EdgeInsets.symmetric(vertical: 4),
            child: Row(
              children: <Widget>[
                SizedBox(
                  width: 48,
                  child: Text(
                    _labels[i],
                    style: TextStyle(
                      fontSize: BasicDemoDimens.fontBody,
                      fontWeight: i == top ? FontWeight.bold : FontWeight.normal,
                    ),
                  ),
                ),
                Expanded(
                  child: ClipRRect(
                    borderRadius: BorderRadius.circular(4),
                    child: LinearProgressIndicator(
                      value: values[i],
                      minHeight: 10,
                      backgroundColor: Colors.grey.shade200,
                      valueColor: AlwaysStoppedAnimation<Color>(
                        i == top ? Colors.blue : Colors.blue.shade200,
                      ),
                    ),
                  ),
                ),
                const SizedBox(width: 8),
                SizedBox(
                  width: 56,
                  child: Text(
                    '${(values[i] * 100).toStringAsFixed(1)}%',
                    textAlign: TextAlign.end,
                    style: TextStyle(
                      fontSize: BasicDemoDimens.fontBody,
                      fontFamily: 'monospace',
                      fontWeight: i == top ? FontWeight.bold : FontWeight.normal,
                    ),
                  ),
                ),
              ],
            ),
          ),
      ],
    );
  }

  /// 按当前模型选择重建引擎，并把词表 / 序列长度随模型一起换掉
  ///
  /// 两个模型的词表大小与序列长度都不同，必须与引擎同一步切换，
  /// 否则会用 A 模型的词表去编码 B 模型的输入。
  Future<void> _rebuildModel({required bool mobileBert}) async {
    final _TextEngine? previous = _engine;
    _engine = null;
    setState(() {
      _engineLabel = '初始化中';
      _resultText = '分类结果: 等待输入';
      _probabilities = <double>[0.0, 0.0];
    });

    try {
      final String modelAsset = mobileBert ? _bertAsset : _wordVecAsset;
      final String vocabAsset = mobileBert ? _bertVocabAsset : _wordVecVocabAsset;
      final List<String> lines = await MlTfliteHelper.loadLines(vocabAsset);
      final Map<String, int> vocabulary = MlTfliteHelper.buildVocabulary(lines);

      final MlEngineBundle built =
          await MlTfliteHelper.createInterpreter(modelAsset, useGpu: false);

      // 序列长度取自模型自身声明的输入形状，而不是硬编码或从 UI 开关现读
      final List<int> shape = built.interpreter.getInputTensor(0).shape;
      final int sequenceLength = shape.length >= 2 ? shape[1] : 128;
      final int inputCount = built.interpreter.getInputTensors().length;

      final _TextEngine engine = _TextEngine(
        interpreter: built.interpreter,
        vocabulary: vocabulary,
        sequenceLength: sequenceLength,
        inputCount: inputCount,
        displayName: mobileBert ? 'MobileBERT' : '平均词向量',
      );
      _engine = engine;
      _engineLabel =
          '${built.engineLabel} | ${engine.displayName} | 词表 ${vocabulary.length} 项';
      if (mounted) setState(() {});

      previous?.interpreter.close();
    } catch (error) {
      debugPrint('切换模型失败: $error');
      _engineLabel = '切换模型失败: $error';
      if (mounted) setState(() {});
    }
  }

  Future<void> _runClassification() async {
    final String text = _inputController.text.trim();
    if (text.isEmpty) {
      setState(() {
        _resultText = '分类结果: 请先输入一句英文';
      });
      return;
    }
    final _TextEngine? engine = _engine;
    if (engine == null) {
      setState(() {
        _resultText = '分类结果: 引擎尚未就绪';
      });
      return;
    }

    try {
      final Stopwatch tokenizeWatch = Stopwatch()..start();
      final Int32List tokenIds = _tokenize(text, engine.sequenceLength, engine.vocabulary);
      tokenizeWatch.stop();

      final Stopwatch inferenceWatch = Stopwatch()..start();
      final Interpreter interpreter = engine.interpreter;
      // 写入顺序与**模型自身声明的输入顺序**一致。实测 bert_classifier.tflite 为
      //   [0] input_word_ids → [1] input_type_ids → [2] input_mask
      // （官方样例把 mask 写进 [1]、segment_ids 写进 [2]，是因为它配的是另一个模型文件，
      //   换成别的模型文件后顺序会变，必须按实测来）
      MlTfliteHelper.writeIntInput(interpreter, 0, tokenIds);
      if (engine.inputCount > 1) {
        // [1] input_type_ids：单句分类恒为全 0
        MlTfliteHelper.writeIntInput(
          interpreter,
          1,
          Int32List(engine.sequenceLength),
        );
      }
      if (engine.inputCount > 2) {
        // [2] input_mask：非 padding 位置为 1；不显式写入的话原生侧读到的是
        // 未初始化内存，推理结果不可预期
        final Int32List mask = Int32List(engine.sequenceLength);
        for (int i = 0; i < engine.sequenceLength; i++) {
          mask[i] = (i < tokenIds.length && tokenIds[i] != _padTokenId) ? 1 : 0;
        }
        MlTfliteHelper.writeIntInput(interpreter, 2, mask);
      }
      interpreter.invoke();
      final Float32List scores = MlTfliteHelper.readFloatOutput(interpreter);
      inferenceWatch.stop();

      final List<double> probabilities = scores.take(2).toList();
      final int top = _argMax(probabilities);
      final String label = top < _labels.length ? _labels[top] : '类别 #$top';
      final double confidence =
          top < probabilities.length ? probabilities[top] : 0.0;

      if (!mounted) return;
      setState(() {
        _probabilities = probabilities;
        _tokenizeMs = tokenizeWatch.elapsedMicroseconds / 1000.0;
        _inferenceMs = inferenceWatch.elapsedMicroseconds / 1000.0;
        _resultText = '分类结果: $label (置信度: ${(confidence * 100).toStringAsFixed(1)}%)';
      });
    } catch (error) {
      debugPrint('文本分类异常: $error');
      if (mounted) {
        setState(() {
          _resultText = '文本分类异常: $error';
        });
      }
    }
  }

  /// 分词：去标点 → 空格切词 → 查词表，未登录词统一回落到 id 0
  ///
  /// 序列按模型长度定长补齐，空位填 [PAD]（id 为 0）。刻意统一回落 0 而不是 [UNK]：
  /// input_mask 的判定就是 `id != 0`，回落到 0 才能让「OOV 词」与「padding」
  /// 在掩码上表现一致，被屏蔽的位置不会再参与注意力。
  Int32List _tokenize(String text, int sequenceLength, Map<String, int> vocabulary) {
    final String cleaned = text.replaceAll(_punctuation, ' ');
    final List<String> tokens =
        cleaned.split(' ').where((String token) => token.isNotEmpty).toList();
    final Int32List ids = Int32List(sequenceLength);
    for (int index = 0; index < sequenceLength; index++) {
      if (index >= tokens.length) {
        ids[index] = _padTokenId;
        continue;
      }
      ids[index] = vocabulary[tokens[index].toLowerCase()] ?? _padTokenId;
    }
    return ids;
  }

  int _argMax(List<double> values) {
    if (values.isEmpty) return 0;
    int best = 0;
    for (int i = 1; i < values.length; i++) {
      if (values[i] > values[best]) best = i;
    }
    return best;
  }
}
