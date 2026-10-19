import 'dart:convert';
import 'dart:io';
import 'dart:typed_data';

import 'package:unorm_dart/unorm_dart.dart' as unorm;

/// Qwen2 词表的 byte-level BPE 分词器 —— 与 Android 侧 `QwenBpeTokenizer` 一一对应。
///
/// 实现 GPT-2 系流水线三步：先按 Qwen2 预分词正则切段，再把 UTF-8 字节映射到
/// byte-level unicode 字母表，最后按 merge 排名贪心合并。只编码**纯文本**：
/// TTS 提示词里的特殊 token（`<|im_start|>` / `<|im_end|>` 等）id 固定，
/// 由 `QwenSynthesizer` 直接填入。
///
/// 构造与词表解析是分开的：核心只吃「词表映射 + merges 行」两个纯数据入参，
/// 因此分词逻辑可脱离 2.7 MB 的 `vocab.json` 做单测（对拍官方参考向量）。
class QwenBpeTokenizer {
  /// [vocabulary] 是 piece 到 id 的映射；[merges] 是 `merges.txt` 的行序列
  QwenBpeTokenizer(this._vocabulary, List<String> merges) {
    // 官方同样跳过空行与 `#version` 头，且 rank 只在有效行上自增
    int rank = 0;
    for (final String line in merges) {
      if (line.trim().isEmpty || line.startsWith('#version')) {
        continue;
      }
      final int separator = line.indexOf(' ');
      if (separator <= 0) {
        throw FormatException('merges 行格式异常：$line');
      }
      final int left = _pieceId(line.substring(0, separator));
      final int right = _pieceId(line.substring(separator + 1));
      _ranks[(left << 32) | right] = rank;
      rank++;
    }
  }

  /// 从 `vocab.json` + `merges.txt` 文本构建
  factory QwenBpeTokenizer.fromTexts({
    required String vocabJson,
    required String mergesText,
  }) {
    final Map<String, dynamic> raw = jsonDecode(vocabJson) as Map<String, dynamic>;
    final Map<String, int> vocabulary = <String, int>{};
    raw.forEach((String piece, Object? id) {
      vocabulary[piece] = id! as int;
    });
    return QwenBpeTokenizer(vocabulary, const LineSplitter().convert(mergesText));
  }

  /// 从磁盘文件构建（两者都在模型下载清单里）
  static QwenBpeTokenizer fromFiles(File vocabFile, File mergesFile) =>
      QwenBpeTokenizer.fromTexts(
        vocabJson: vocabFile.readAsStringSync(),
        mergesText: mergesFile.readAsStringSync(),
      );

  final Map<String, int> _vocabulary;

  /// `(左 piece id, 右 piece id) -> merge 排名`，排名越小越先合并
  final Map<int, int> _ranks = <int, int>{};

  /// piece 字符串到 id 的缓存，避免每轮合并都查一次哈希表
  final Map<String, int> _pieceIds = <String, int>{};

  /// 词表条目数；仅用于界面展示，不参与推理
  int get vocabularySize => _vocabulary.length;

  /// 编码纯文本（不含特殊 token）
  ///
  /// 参考实现（Rust tokenizers）先做 NFC 归一化，这里保持一致：少了这一步，
  /// 「组合字符」写法（如 `e` + U+0301）会与预组合写法（`é`）编出不同的 id。
  Int32List encode(String text) {
    final String normalized = unorm.nfc(text);
    final List<int> out = <int>[];
    for (final RegExpMatch match in pretokenize.allMatches(normalized)) {
      final List<int> bytes = utf8.encode(match.group(0)!);
      final List<int> mapped = List<int>.generate(bytes.length, (int i) => byteToChar[bytes[i]]);
      _mergePieces(String.fromCharCodes(mapped), out);
    }
    return Int32List.fromList(out);
  }

  /// 与官方启动自检等价：逐条比对参考 token id
  ///
  /// @return 不一致的描述；**空列表表示全部通过**
  List<String> findMismatches(List<({String text, Int32List ids})> vectors) {
    final List<String> mismatches = <String>[];
    for (final ({String text, Int32List ids}) vector in vectors) {
      final Int32List actual = encode(vector.text);
      if (!_sameIds(actual, vector.ids)) {
        mismatches.add('「${vector.text}」期望 ${vector.ids.toList()}，'
            '实际 ${actual.toList()}');
      }
    }
    return mismatches;
  }

  static bool _sameIds(Int32List left, Int32List right) {
    if (left.length != right.length) {
      return false;
    }
    for (int i = 0; i < left.length; i++) {
      if (left[i] != right[i]) {
        return false;
      }
    }
    return true;
  }

  void _mergePieces(String token, List<int> out) {
    List<String> pieces = token.split('');
    if (pieces.length == 1) {
      final int? id = _vocabulary[token];
      if (id == null) {
        throw StateError('词表缺少单字节 piece：$token');
      }
      out.add(id);
      return;
    }
    while (pieces.length > 1) {
      int bestRank = 0x7FFFFFFF;
      int bestIndex = -1;
      for (int i = 0; i < pieces.length - 1; i++) {
        // 两侧 piece 任一不在词表里，这一对就不可能被合并过
        final int? left = _vocabulary[pieces[i]];
        final int? right = _vocabulary[pieces[i + 1]];
        if (left == null || right == null) {
          continue;
        }
        final int? rank = _ranks[(left << 32) | right];
        if (rank != null && rank < bestRank) {
          bestRank = rank;
          bestIndex = i;
        }
      }
      if (bestIndex < 0) {
        break;
      }
      final String merged = pieces[bestIndex] + pieces[bestIndex + 1];
      pieces = <String>[
        ...pieces.sublist(0, bestIndex),
        merged,
        ...pieces.sublist(bestIndex + 2),
      ];
    }
    for (final String piece in pieces) {
      final int? id = _vocabulary[piece];
      if (id == null) {
        throw StateError('词表缺少 piece：$piece');
      }
      out.add(id);
    }
  }

  int _pieceId(String piece) => _pieceIds.putIfAbsent(piece, () {
        final int? id = _vocabulary[piece];
        if (id == null) {
          throw StateError('merges 里的 piece 不在词表中：$piece');
        }
        return id;
      });

  /// Qwen2 预分词正则（取自 `tokenizer.json`）
  ///
  /// 参考实现是 Rust 且 `\s` 是 Unicode 感知的；Dart 的 RegExp（Irregexp）在
  /// `unicode: true` 下同样是 Unicode 语义，能正确切 U+3000 全角空格、emoji 与换行块。
  static final RegExp pretokenize = RegExp(
    "(?i:'s|'t|'re|'ve|'m|'ll|'d)|[^\\r\\n\\p{L}\\p{N}]?\\p{L}+|\\p{N}|"
    " ?[^\\s\\p{L}\\p{N}]+[\\r\\n]*|\\s*[\\r\\n]+|\\s+(?!\\S)|\\s+",
    unicode: true,
  );

  /// GPT-2 的 byte-to-unicode 表：可打印字节映射为自身，其余按顺序映射到 U+0100 起
  ///
  /// 目的是让「任意字节序列」都能用合法 unicode 字符串表示，从而能进 BPE 的词表。
  /// 表项是 UTF-16 码元（全部 < 0x10000），因此用 `String.fromCharCodes` 重建即可。
  static final List<int> byteToChar = () {
    final Set<int> printable = <int>{
      for (int b = 0x21; b <= 0x7E; b++) b,
      for (int b = 0xA1; b <= 0xAC; b++) b,
      for (int b = 0xAE; b <= 0xFF; b++) b,
    };
    final List<int> table = List<int>.filled(256, 0);
    int next = 256;
    for (int b = 0; b < 256; b++) {
      table[b] = printable.contains(b) ? b : next++;
    }
    return table;
  }();
}
