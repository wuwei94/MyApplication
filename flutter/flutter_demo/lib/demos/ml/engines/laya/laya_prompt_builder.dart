/// Laya 提示词构建器 —— laya 0.3.4 `common.py` 与 `LayaHost._to_internal` 的移植版
/// （Convai Innovations），与 Android `LayaPromptBuilder` 逐行对应。
library;

import 'package:flutter_demo/demos/ml/engines/laya/laya_json.dart';
import 'package:flutter_demo/demos/ml/engines/laya/laya_tokenizer.dart';

/// 归一化后的上游题目结构；criteria 的顺序决定 marker 顺序。
class LayaQuestion {
  const LayaQuestion(this.type, this.instructions, this.criteria);

  final String type;
  final String instructions;
  final Object? criteria;

  /// 图通道：choice=0、score=1、noul=2。
  int get qtype {
    switch (type) {
      case 'choice':
        return 0;
      case 'score':
        return 1;
      case 'noul':
        return 2;
      default:
        throw ArgumentError('Unknown question type: $type');
    }
  }

  /// 解码器需要的 marker logit 数量。
  int get optionCount {
    switch (type) {
      case 'choice':
        return layaAsObject(criteria).length;
      case 'score':
        return layaAsArray(criteria).length;
      default:
        return 2;
    }
  }
}

/// 未填充的 token id 与存活的选项 marker 位置，供一次图调用使用。
class LayaSequence {
  const LayaSequence(this.ids, this.markers, this.question);

  final List<int> ids;
  final List<int> markers;
  final LayaQuestion question;

  /// 与 marker 同序渲染的 criteria。
  List<String> get options => LayaPromptBuilder.renderOptions(question);
}

/// 每次图调用一题。每次编码都关闭分词器后处理。
class LayaPromptBuilder {
  LayaPromptBuilder(this.tokenizer, {this.maxLen = 256, this.headMaxLen = 256}) {
    if (maxLen <= 0 || headMaxLen <= 0) {
      throw ArgumentError('maxLen / headMaxLen 必须为正');
    }
  }

  final LayaTokenizer tokenizer;
  final int maxLen;
  final int headMaxLen;

  /// 在构建 prompt 之前先接收上游 map 结构。
  LayaQuestion normalize(Map<String, Object?> question) =>
      LayaPromptBuilder.normalizeStatic(question);

  /// 用精确的选项上限、压缩规则与右侧截断构建一行。
  LayaSequence build(Object? state, Map<String, Object?> question,
          {String questionId = ''}) =>
      buildRaw(state, normalize(question), questionId: questionId);

  /// 用精确的选项上限、压缩规则与右侧截断构建一行。
  LayaSequence buildRaw(Object? state, LayaQuestion question,
      {String questionId = ''}) {
    final List<String> options = LayaPromptBuilder.renderOptions(question);
    final String instructions = question.instructions.replaceAll(maskLiteral, ' ');
    final List<int> head =
        tokenizer.encode('${question.type} question: $instructions', addSpecialTokens: false);
    List<List<int>> encodedOptions = options.map((String option) {
      final List<int> text = tokenizer
          .encode(' ${option.replaceAll(maskLiteral, ' ')}', addSpecialTokens: false);
      return <int>[mask, ...text.take(layaMin(text.length, 48))];
    }).toList();
    int optionBudget =
        headMaxLen - encodedOptions.fold(0, (int sum, List<int> item) => sum + item.length);
    if (optionBudget < 16) {
      final int per =
          layaMax(4, (headMaxLen - 16) ~/ layaMax(1, encodedOptions.length));
      encodedOptions =
          encodedOptions.map((List<int> item) => item.take(layaMin(item.length, per)).toList()).toList();
      optionBudget =
          headMaxLen - encodedOptions.fold(0, (int sum, List<int> item) => sum + item.length);
    }
    final List<int> ids = <int>[];
    ids.add(cls);
    ids.addAll(head.take(layaMax(8, optionBudget)));
    ids.add(sep);
    final List<int> markers = <int>[];
    for (final List<int> option in encodedOptions) {
      markers.add(ids.length);
      ids.addAll(option);
    }
    ids.add(sep);
    final int room = layaMax(0, maxLen - ids.length - 1);
    final List<int> stateIds = tokenizer
        .encode(serializeState(state).replaceAll(maskLiteral, ' '), addSpecialTokens: false);
    ids.addAll(stateIds.take(room));
    ids.add(sep);
    final List<int> retainedMarkers =
        markers.where((int marker) => marker < maxLen).toList();
    // 这是上游真正的拒绝规则：被压缩的选项只要其 marker 存活即有效。
    if (retainedMarkers.length != options.length) {
      throw ArgumentError(
          "question '$questionId' options exceed head_max_len=$headMaxLen");
    }
    return LayaSequence(ids.take(maxLen).toList(), retainedMarkers, question);
  }

  static const int pad = 0;
  static const int sep = 1;
  static const int cls = 2;
  static const int mask = 4;
  static const String maskLiteral = '<mask>';

  /// 按 Python 宿主的方式转换 choice 列表与非字符串 instructions。
  static LayaQuestion normalizeStatic(Map<String, Object?> question) {
    final Object? rawType = question['type'];
    if (rawType is! String) {
      throw ArgumentError('Question requires string type');
    }
    if (rawType != 'choice' && rawType != 'score' && rawType != 'noul') {
      throw ArgumentError('Unknown question type: $rawType');
    }
    Object? criteria = question['criteria'];
    if (rawType == 'choice' && criteria is List) {
      final Map<String, Object?> labels = <String, Object?>{};
      for (final Object? label in criteria) {
        if (label is! String) {
          throw ArgumentError('Choice label must be a string');
        }
        labels[label] = null;
      }
      criteria = labels;
    }
    if (!question.containsKey('instructions')) {
      throw ArgumentError('Question requires instructions');
    }
    final Object? rawInstructions = question['instructions'];
    final String instructions = rawInstructions is String
        ? rawInstructions
        : layaStringify(rawInstructions, ensureAscii: true);
    return LayaQuestion(rawType, instructions, criteria);
  }

  /// 字符串保持字面量；其它状态使用 Python 兼容的 JSON 序列化。
  static String serializeState(Object? state) =>
      state is String ? state : layaStringify(state);

  /// 用宿主的 JSON 与回退字符串行为渲染单个 criterion。
  static String renderCriterion(Object? value) {
    if (value is String) return value;
    if (value == null || value is num || value is bool || value is Map || value is List) {
      return layaStringify(value);
    }
    return layaStringify(value.toString()); // json.dumps(default=str)
  }

  /// 保留 choice 标签、score 层级与 false/true 的 noul 顺序。
  static List<String> renderOptions(LayaQuestion question) {
    switch (question.type) {
      case 'choice':
        return layaAsObject(question.criteria).entries.map((MapEntry<String, Object?> entry) {
          final Object? value = entry.value;
          if (value == null || value == '') return entry.key;
          return '$entry.key: ${renderCriterion(value)}';
        }).toList();
      case 'score':
        return layaAsArray(question.criteria)
            .asMap()
            .entries
            .map((MapEntry<int, Object?> entry) =>
                'level ${entry.key}: ${renderCriterion(entry.value)}')
            .toList();
      case 'noul':
        // 源码在查找两个具名 criteria 之前先做 `crit = crit or {}`。
        final Map<String, Object?> criteria = _pythonFalsey(question.criteria)
            ? <String, Object?>{}
            : layaAsObject(question.criteria);
        final Object? falseValue = criteria['false'];
        final Object? trueValue = criteria['true'];
        final String falseText = falseValue == null || falseValue == ''
            ? 'no, the statement does not hold'
            : renderCriterion(falseValue);
        final String trueText = trueValue == null || trueValue == ''
            ? 'yes, the statement holds'
            : renderCriterion(trueValue);
        return <String>[
          'false: $falseText',
          'true: $trueText',
        ];
      default:
        throw ArgumentError('Unknown question type: ${question.type}');
    }
  }

  static bool _pythonFalsey(Object? value) {
    if (value == null) return true;
    if (value is bool) return !value;
    if (value is num) return value.toDouble() == 0.0;
    if (value is String) return value.isEmpty;
    if (value is List) return value.isEmpty;
    if (value is Map) return value.isEmpty;
    return false;
  }
}
