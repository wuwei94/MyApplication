/// 固定多语 Laya 分词器的编码器 —— 与 Android `LayaTokenizer` 逐行对应。
///
/// 不依赖原生或 Flutter API，纯 Dart 实现；tokenizer.json（约 33 MB，BPE）
/// 由 `LayaEngine` 从运行时下载目录读入。
library;

import 'dart:collection';
import 'dart:convert';
import 'dart:io';
import 'dart:math' as math;

import 'package:flutter_demo/demos/ml/engines/laya/laya_json.dart';

class LayaTokenizer {
  LayaTokenizer(File file) {
    final Stopwatch watch = Stopwatch()..start();
    final Map<String, Object?> root = layaAsObject(layaParse(file.readAsStringSync()));

    final Map<String, Object?> model = layaAsObject(root['model']);
    if (model['type'] != 'BPE' || model['byte_fallback'] != true) {
      throw ArgumentError('tokenizer.json 的 model 段不是 BPE + byte_fallback');
    }
    if (model['dropout'] != null ||
        model['continuing_subword_prefix'] != null ||
        model['end_of_word_suffix'] != null) {
      throw ArgumentError('tokenizer.json 含不支持的 model 选项');
    }
    if (model['ignore_merges'] != false || model['unk_token'] != '<unk>') {
      throw ArgumentError('tokenizer.json 的 ignore_merges / unk_token 不符');
    }

    final Map<String, Object?> normalizer = layaAsObject(root['normalizer']);
    if (normalizer['type'] != 'Replace' || normalizer['content'] != '▁') {
      throw ArgumentError('tokenizer.json 的 normalizer 段不符');
    }
    final Map<String, Object?> normalizerPattern = layaAsObject(normalizer['pattern']);
    if (normalizerPattern['String'] != ' ') {
      throw ArgumentError('tokenizer.json 的 normalizer pattern 不符');
    }

    final Map<String, Object?> preTokenizer = layaAsObject(root['pre_tokenizer']);
    if (preTokenizer['type'] != 'Metaspace' || preTokenizer['replacement'] != '▁') {
      throw ArgumentError('tokenizer.json 的 pre_tokenizer 段不符');
    }
    if (preTokenizer['prepend_scheme'] != 'always' || preTokenizer['split'] != true) {
      throw ArgumentError('tokenizer.json 的 pre_tokenizer 选项不符');
    }

    final Map<String, Object?> rawVocabulary = layaAsObject(model['vocab']);
    _vocabulary = HashMap<String, int>();
    rawVocabulary.forEach((Object? token, Object? id) {
      _vocabulary[token as String] = (id as num).toInt();
    });
    if (_vocabulary.length != 256000) {
      throw ArgumentError('词表大小应为 256000，实际 ${_vocabulary.length}');
    }
    if (_vocabulary['<pad>'] != 0 ||
        _vocabulary['<eos>'] != 1 ||
        _vocabulary['<bos>'] != 2 ||
        _vocabulary['<unk>'] != 3 ||
        _vocabulary['<mask>'] != 4) {
      throw ArgumentError('特殊 token id 不符');
    }
    // 该 checkpoint 有 255 个字节 token：TAB 有自己的标量 token（226），没有 <0x09>。
    for (int i = 0; i < 256; i++) {
      final String hex = i.toRadixString(16).toUpperCase().padLeft(2, '0');
      _byteIds[i] = _vocabulary['<0x$hex>'] ?? -1;
    }

    final List<Object?> rawMerges = layaAsArray(model['merges']);
    _merges = HashMap<LongKey, _Merge>();
    for (int rank = 0; rank < rawMerges.length; rank++) {
      final List<Object?> pair = layaAsArray(rawMerges[rank]);
      final String left = pair[0] as String;
      final String right = pair[1] as String;
      _merges[LongKey(_vocabulary[left]!, _vocabulary[right]!)] =
          _Merge(rank, _vocabulary[left + right]!);
    }

    for (final Object? value in layaAsArray(root['added_tokens'])) {
      final Map<String, Object?> entry = layaAsObject(value);
      if (entry['normalized'] != false || entry['single_word'] != false) {
        throw ArgumentError('added token 选项不符');
      }
      final _AddedToken token = _AddedToken(
        entry['content'] as String,
        (entry['id'] as num).toInt(),
        entry['lstrip'] == true,
        entry['rstrip'] == true,
      );
      _TrieNode node = _addedRoot;
      for (int i = 0; i < token.content.length; i++) {
        node = node.children.putIfAbsent(token.content.codeUnitAt(i), () => _TrieNode());
      }
      node.token = token;
    }
    watch.stop();
    loadTimeMs = watch.elapsedMicroseconds / 1000.0;
  }

  late final Map<String, int> _vocabulary;
  late final Map<LongKey, _Merge> _merges;
  final _TrieNode _addedRoot = _TrieNode();
  final List<int> _byteIds = List<int>.filled(256, -1);

  /// JSON 解析与索引构建耗时，不含编码。
  late final double loadTimeMs;

  /// 词表条目数，用于与固定多语分词器核对。
  int get vocabularySize => _vocabulary.length;

  /// 从 tokenizer.json 加载的带排序 BPE 合并对数。
  int get mergeCount => _merges.length;

  /// 先匹配 added token（在规范化之前），随后每个剩余片段用 Metaspace/BPE。
  List<int> encode(String text, {bool addSpecialTokens = false}) {
    final List<int> result = <int>[];
    if (addSpecialTokens) result.add(2);
    int unprocessed = 0;
    int position = 0;
    while (position < text.length) {
      _TrieNode? node = _addedRoot;
      int end = position;
      _AddedToken? matched;
      int matchedEnd = position;
      while (end < text.length) {
        node = node?.children[text.codeUnitAt(end)];
        if (node == null) break;
        end++;
        if (node.token != null) {
          matched = node.token;
          matchedEnd = end;
        }
      }
      if (matched == null) {
        position++;
        continue;
      }
      int begin = position;
      if (matched.leftStrip) {
        while (begin > unprocessed && _isRustWhitespace(_codePointBefore(text, begin))) {
          begin -= _charCount(_codePointBefore(text, begin));
        }
      }
      _encodeOrdinary(text.substring(unprocessed, begin), result);
      result.add(matched.id);
      int tail = matchedEnd;
      if (matched.rightStrip) {
        while (tail < text.length && _isRustWhitespace(_codePointAt(text, tail))) {
          tail += _charCount(_codePointAt(text, tail));
        }
      }
      position = tail;
      unprocessed = tail;
    }
    _encodeOrdinary(text.substring(unprocessed), result);
    if (addSpecialTokens) result.add(1);
    return result;
  }

  void _encodeOrdinary(String text, List<int> output) {
    if (text.isEmpty) return;
    String normalized = text.replaceAll(' ', '▁');
    if (!normalized.startsWith('▁')) normalized = '▁$normalized';
    int begin = 0;
    for (int index = 1; index < normalized.length; index++) {
      if (normalized[index] == '▁') {
        _bpe(normalized.substring(begin, index), output);
        begin = index;
      }
    }
    _bpe(normalized.substring(begin), output);
  }

  void _bpe(String piece, List<int> output) {
    final List<int> symbols = <int>[];
    int offset = 0;
    while (offset < piece.length) {
      final int codePoint = _codePointAt(piece, offset);
      final String character = String.fromCharCode(codePoint);
      final int? id = _vocabulary[character];
      if (id != null) {
        symbols.add(id);
      } else {
        for (final int byte in utf8.encode(character)) {
          final int fallback = _byteIds[byte & 255];
          if (fallback < 0) {
            throw ArgumentError(
                'Missing byte fallback for unknown scalar U+${codePoint.toRadixString(16)}');
          }
          symbols.add(fallback);
        }
      }
      offset += _charCount(codePoint);
    }
    if (symbols.length < 2) {
      output.addAll(symbols);
      return;
    }

    final List<int> ids = symbols.toList();
    final List<int> previous = List<int>.generate(ids.length, (int i) => i - 1);
    final List<int> next =
        List<int>.generate(ids.length, (int i) => i + 1 < ids.length ? i + 1 : -1);
    final List<bool> alive = List<bool>.filled(ids.length, true);

    // 与 Kotlin 的优先队列等价：每轮取「秩最小、并列取最左」的有效相邻对合并。
    // 片段长度有界（prompt 构建时截到 48），线性扫描足够快且结果确定。
    while (true) {
      int bestLeft = -1;
      int bestRank = 1 << 30;
      for (int left = 0; left < ids.length; left++) {
        if (!alive[left]) continue;
        final int right = next[left];
        if (right < 0) continue;
        final _Merge? merge = _merges[LongKey(ids[left], ids[right])];
        if (merge == null) continue;
        if (merge.rank < bestRank) {
          bestRank = merge.rank;
          bestLeft = left;
        }
      }
      if (bestLeft < 0) break;
      final int right = next[bestLeft];
      ids[bestLeft] = _merges[LongKey(ids[bestLeft], ids[right])]!.id;
      alive[right] = false;
      next[bestLeft] = next[right];
      if (next[right] >= 0) previous[next[right]] = bestLeft;
    }
    for (int index = 0; index < ids.length; index++) {
      if (alive[index]) output.add(ids[index]);
    }
  }

  // ─────────────────── UTF-16 码点助手 ───────────────────

  static int _codePointAt(String text, int index) {
    final int unit = text.codeUnitAt(index);
    if (unit >= 0xD800 && unit <= 0xDBFF && index + 1 < text.length) {
      final int low = text.codeUnitAt(index + 1);
      if (low >= 0xDC00 && low <= 0xDFFF) {
        return 0x10000 + ((unit - 0xD800) << 10) + (low - 0xDC00);
      }
    }
    return unit;
  }

  static int _charCount(int codePoint) => codePoint > 0xFFFF ? 2 : 1;

  /// 取「在 [index] 之前结束」的码点（Kotlin `codePointBefore`）
  static int _codePointBefore(String text, int index) {
    final int unit = text.codeUnitAt(index - 1);
    if (unit >= 0xDC00 && unit <= 0xDFFF && index >= 2) {
      final int high = text.codeUnitAt(index - 2);
      if (high >= 0xD800 && high <= 0xDBFF) {
        return 0x10000 + ((high - 0xD800) << 10) + (unit - 0xDC00);
      }
    }
    return unit;
  }

  // Rust 的 char::is_whitespace 遵循 Unicode White_Space。
  static bool _isRustWhitespace(int codePoint) =>
      (codePoint >= 0x09 && codePoint <= 0x0D) ||
      codePoint == 0x20 ||
      codePoint == 0x85 ||
      codePoint == 0xA0 ||
      codePoint == 0x1680 ||
      (codePoint >= 0x2000 && codePoint <= 0x200A) ||
      codePoint == 0x2028 ||
      codePoint == 0x2029 ||
      codePoint == 0x202F ||
      codePoint == 0x205F ||
      codePoint == 0x3000;

  /// HashMap 的 long key：Dart 没有原生 64 位整数对哈希，用记录类型 + 自定义相等
  static int _hashLong(int left, int right) {
    // 32 位混合；冲突由相等性判断兜底
    int h = left * 0x9E3779B1;
    h = (h ^ right) & 0xFFFFFFFF;
    return h;
  }
}

/// 32 位对键的相等容器（替代 Kotlin 的 `Long` 位打包 key）
class LongKey {
  const LongKey(this.left, this.right);

  final int left;
  final int right;

  @override
  bool operator ==(Object other) =>
      other is LongKey && other.left == left && other.right == right;

  @override
  int get hashCode => LayaTokenizer._hashLong(left, right);
}

class _Merge {
  const _Merge(this.rank, this.id);

  final int rank;
  final int id;
}

class _AddedToken {
  const _AddedToken(this.content, this.id, this.leftStrip, this.rightStrip);

  final String content;
  final int id;
  final bool leftStrip;
  final bool rightStrip;
}

class _TrieNode {
  final Map<int, _TrieNode> children = <int, _TrieNode>{};
  _AddedToken? token;
}

/// 复现 NumPy 的 float32 成对归约顺序（含 8 累加器）——与 Kotlin `numpySum` 对齐。
///
/// Laya 解码的 softmax / 熵都在 float32 语义下归约；Dart 的 double 是 float64，
/// 这里保留**算法结构**（8 累加器 + 分治拆分），低几位差异对四位小数展示不可见。
double layaNumpySum(List<double> values, {int from = 0, int? sizeArg}) {
  final int size = sizeArg ?? values.length;
  if (size < 8) {
    double sum = -0.0;
    for (int i = from; i < from + size; i++) {
      sum += values[i];
    }
    return sum;
  }
  if (size <= 128) {
    final List<double> sums = List<double>.generate(8, (int i) => values[from + i]);
    int i = 8;
    while (i < size - size % 8) {
      for (int j = 0; j < 8; j++) {
        sums[j] += values[from + i + j];
      }
      i += 8;
    }
    double sum = ((sums[0] + sums[1]) + (sums[2] + sums[3])) +
        ((sums[4] + sums[5]) + (sums[6] + sums[7]));
    while (i < size) {
      sum += values[from + i];
      i++;
    }
    return sum;
  }
  final int left = (size ~/ 2) - (size ~/ 2) % 8;
  return layaNumpySum(values, from: from, sizeArg: left) +
      layaNumpySum(values, from: from + left, sizeArg: size - left);
}

/// `math` 仅用于 min/max 语义对齐，避免与 dart:math 的 Random 混淆
int layaMin(int a, int b) => math.min(a, b);
int layaMax(int a, int b) => math.max(a, b);
