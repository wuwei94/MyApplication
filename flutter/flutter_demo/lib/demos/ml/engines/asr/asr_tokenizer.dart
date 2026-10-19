/// 纯 Dart 的 HuggingFace `tokenizer.json` 解码器 —— 与 Android 侧 `HfTokenizer`
/// 一一对应。
///
/// **为什么不用官方那套**：官方走 JNI 调 [mlc-ai/tokenizers-cpp]，需要预编译好的
/// `libtokenizers_c.a`（按 ABI 各一份）与 NDK 工具链，而公开仓里根本没有这两样。
/// Android 侧因此改写成纯 Kotlin，本侧同理改写成纯 Dart。
///
/// **覆盖范围**：只实现**解码**方向（id → 文本），这正是 ASR 全流程需要的全部
/// —— 6 个模型的 `tokenizer.json` 实测只有这一处被用到。词表形态、解码链类型都按
/// 实测的真实文件实现：
///
/// | 模型 | 词表 | 解码链 |
/// |---|---|---|
/// | `whisper-tiny` | BPE map（50258 项，id 0–51864） | `ByteLevel` |
/// | `qwen3-asr-0.6b` | BPE map（151643 项） | `ByteLevel` |
/// | `moonshine-tiny` | BPE map（32000 项） | `Sequence[Replace, ByteFallback, Fuse, Strip]` |
/// | `parakeet-ctc-0.6b` | BPE map（1024 项） | `Metaspace` |
/// | `parakeet-tdt-0.6b-v3` | BPE map（8192 项） | `Metaspace` |
/// | `parakeet-tdt_ctc-0.6b-ja` | **Unigram list**（3072 项） | `Metaspace` |
///
/// 遇到未实现的解码器类型会**直接抛异常并报出类型名** —— 免得静默产出错字。
library;

import 'dart:convert';
import 'dart:io';
import 'dart:typed_data';

class AsrHfTokenizer {
  AsrHfTokenizer._(this._idToToken, this._specialIds, this._steps, this.vocabSize);

  final Map<int, String> _idToToken;
  final Set<int> _specialIds;
  final List<AsrDecoderStep> _steps;

  /// 词表大小（最大 id + 1）；[AsrCtcDecoder] 需要它来切分 logits
  final int vocabSize;

  /// 从 `tokenizer.json` 文本构建
  factory AsrHfTokenizer.fromJson(String json) {
    final Object? decoded = jsonDecode(json);
    final Map<String, Object?> root = decoded is Map<String, Object?>
        ? decoded
        : <String, Object?>{};
    final Object? model = root['model'];
    final Map<String, Object?> modelMap =
        model is Map<String, Object?> ? model : <String, Object?>{};

    final Map<int, String> idToToken = <int, String>{};
    _readVocab(modelMap, idToToken);

    final Set<int> specialIds = <int>{};
    final Object? addedTokens = root['added_tokens'];
    if (addedTokens is List<Object?>) {
      for (final Object? item in addedTokens) {
        if (item is! Map<String, Object?>) {
          continue;
        }
        final int id = (item['id'] as num?)?.toInt() ?? -1;
        final String content = item['content'] as String? ?? '';
        if (id < 0 || content.isEmpty) {
          continue;
        }
        idToToken[id] = content;
        if (item['special'] as bool? ?? false) {
          specialIds.add(id);
        }
      }
    }

    final List<AsrDecoderStep> steps = <AsrDecoderStep>[];
    _readDecoder(root['decoder'], steps);

    final int maxId = idToToken.keys.isEmpty
        ? -1
        : idToToken.keys.reduce((int a, int b) => a > b ? a : b);
    return AsrHfTokenizer._(idToToken, specialIds, steps, maxId + 1);
  }

  /// 从文件构建
  factory AsrHfTokenizer.fromFile(File file) =>
      AsrHfTokenizer.fromJson(file.readAsStringSync());

  /// 把 token id 序列还原成可读文本
  String decode(Int32List tokenIds) {
    List<String> tokens = <String>[];
    for (final int id in tokenIds) {
      if (_specialIds.contains(id)) {
        continue;
      }
      final String? token = _idToToken[id];
      if (token == null) {
        continue;
      }
      tokens.add(token);
    }
    for (final AsrDecoderStep step in _steps) {
      tokens = step.apply(tokens);
    }
    // 官方 `HuggingfaceTokenizer.decode` 结尾也调了 trim()，这里保持一致：
    // 词首空格（whisper 的 ByteLevel 会带出来）不该出现在结果里
    return tokens.join().trim();
  }

  /// 解析 `model.vocab`，两种实测形态都支持：
  /// - BPE：`{"token": id}`
  /// - Unigram：`[["token", score], ...]` —— **id 就是数组下标**，不是那个 score
  static void _readVocab(Map<String, Object?> model, Map<int, String> idToToken) {
    final Object? vocab = model['vocab'];
    if (vocab is Map<String, Object?>) {
      vocab.forEach((String token, Object? id) {
        if (id is num) {
          idToToken[id.toInt()] = token;
        }
      });
      return;
    }
    if (vocab is List<Object?>) {
      for (int id = 0; id < vocab.length; id++) {
        final Object? entry = vocab[id];
        if (entry is! List<Object?> || entry.isEmpty) {
          continue;
        }
        final Object? token = entry[0];
        if (token is String && token.isNotEmpty) {
          idToToken[id] = token;
        }
      }
    }
  }

  static void _readDecoder(Object? decoder, List<AsrDecoderStep> steps) {
    if (decoder is! Map<String, Object?>) {
      return;
    }
    final String type = decoder['type'] as String? ?? '';
    switch (type) {
      case '':
        return;
      case 'Sequence':
        final Object? children = decoder['decoders'];
        if (children is List<Object?>) {
          for (final Object? child in children) {
            _readDecoder(child, steps);
          }
        }
      case 'ByteLevel':
        steps.add(const _ByteLevelStep());
      case 'ByteFallback':
        steps.add(const _ByteFallbackStep());
      case 'Fuse':
        steps.add(const _FuseStep());
      case 'Metaspace':
        steps.add(
          _MetaspaceStep(
            replacement: decoder['replacement'] as String? ?? '\u2581',
            prependScheme: decoder['prepend_scheme'] as String? ?? 'always',
          ),
        );
      case 'Strip':
        steps.add(
          _StripStep(
            content: decoder['content'] as String? ?? '',
            start: (decoder['start'] as num?)?.toInt() == 1,
            stop: (decoder['stop'] as num?)?.toInt() == 1,
          ),
        );
      case 'Replace':
        final Object? pattern = decoder['pattern'];
        if (pattern is Map<String, Object?> && pattern.containsKey('Regex')) {
          steps.add(
            _ReplaceStep(
              pattern: pattern['Regex'] as String? ?? '',
              replacement: decoder['content'] as String? ?? '',
              isRegex: true,
            ),
          );
        } else {
          final String literal = pattern is Map<String, Object?>
              ? pattern['String'] as String? ?? ''
              : '';
          steps.add(
            _ReplaceStep(
              pattern: literal,
              replacement: decoder['content'] as String? ?? '',
              isRegex: false,
            ),
          );
        }
      default:
        throw UnsupportedError('尚未实现的 tokenizer 解码器类型：$type');
    }
  }
}

/// 解码链上的一个环节，语义对齐 `tokenizers` 库的 `Decoder`
sealed class AsrDecoderStep {
  const AsrDecoderStep();

  List<String> apply(List<String> tokens);
}

/// `Replace`：把 pattern 替换成 content（String 或 Regex 两种 pattern）
class _ReplaceStep extends AsrDecoderStep {
  const _ReplaceStep({
    required this.pattern,
    required this.replacement,
    required this.isRegex,
  });

  final String pattern;
  final String replacement;
  final bool isRegex;

  @override
  List<String> apply(List<String> tokens) {
    if (isRegex) {
      final RegExp regex = RegExp(pattern);
      return <String>[for (final String token in tokens) token.replaceAll(regex, replacement)];
    }
    return <String>[for (final String token in tokens) token.replaceAll(pattern, replacement)];
  }
}

/// `Strip`：按需从每个词的头部/尾部剥掉指定字符
class _StripStep extends AsrDecoderStep {
  const _StripStep({
    required this.content,
    required this.start,
    required this.stop,
  });

  final String content;
  final bool start;
  final bool stop;

  @override
  List<String> apply(List<String> tokens) => <String>[
        for (final String token in tokens) _stripOne(token),
      ];

  String _stripOne(String token) {
    String chars = token;
    if (start) {
      int index = 0;
      while (index < chars.length && content.contains(chars[index])) {
        index++;
      }
      chars = chars.substring(index);
    }
    if (stop) {
      int index = chars.length;
      while (index > 0 && content.contains(chars[index - 1])) {
        index--;
      }
      chars = chars.substring(0, index);
    }
    return chars;
  }
}

class _FuseStep extends AsrDecoderStep {
  const _FuseStep();

  @override
  List<String> apply(List<String> tokens) => <String>[tokens.join()];
}

/// `Metaspace`：`▁` 还原成空格；`prepend_scheme` 为 `always` 时吃掉词首那一个空格
class _MetaspaceStep extends AsrDecoderStep {
  const _MetaspaceStep({required this.replacement, required this.prependScheme});

  final String replacement;
  final String prependScheme;

  @override
  List<String> apply(List<String> tokens) {
    final List<String> mapped = <String>[
      for (final String token in tokens) token.replaceAll(replacement, ' '),
    ];
    if (mapped.isEmpty) {
      return mapped;
    }
    if (prependScheme == 'always' || prependScheme == 'first') {
      final String first = mapped.first;
      if (first.startsWith(' ')) {
        mapped[0] = first.substring(1);
      }
    }
    return mapped;
  }
}

/// `ByteFallback`：把 `<0xXX>` 形式的词还原成原始字节，遇到普通词时把攒下的字节
/// 先按 UTF-8 落地。moonshine 的词表就是这样处理中文等多字节字符的。
class _ByteFallbackStep extends AsrDecoderStep {
  const _ByteFallbackStep();

  static final RegExp _byteToken = RegExp(r'^<0x([0-9A-Fa-f]{2})>$');

  @override
  List<String> apply(List<String> tokens) {
    final List<String> result = <String>[];
    final List<int> pending = <int>[];
    void flush() {
      if (pending.isNotEmpty) {
        result.add(utf8.decode(pending, allowMalformed: true));
        pending.clear();
      }
    }

    for (final String token in tokens) {
      final RegExpMatch? match = _byteToken.firstMatch(token);
      if (match != null) {
        pending.add(int.parse(match.group(1) ?? '0', radix: 16));
      } else {
        flush();
        result.add(token);
      }
    }
    flush();
    return result;
  }
}

/// `ByteLevel`：GPT-2 的 byte-to-unicode 反向映射
///
/// 与 `tokenizers` 的一处刻意差异：那边是**逐词**做字节→UTF-8 解码，一个多字节字符
/// 若被拆到两个词里就会各吐出替换符；这里把整段词的字节**攒齐后一次性** UTF-8 解码，
/// 同样的输入能还原出正确文字。
class _ByteLevelStep extends AsrDecoderStep {
  const _ByteLevelStep();

  /// GPT-2 byte-to-unicode 表里「不可打印字节」的排列顺序：
  /// `0..32`、`127..160`、`173` 依次映射到 `U+0100` 起
  static final List<int> _nonPrintableBytes = <int>[
    for (int b = 0; b <= 32; b++) b,
    for (int b = 127; b <= 160; b++) b,
    173,
  ];

  @override
  List<String> apply(List<String> tokens) {
    final List<int> bytes = <int>[];
    for (final String token in tokens) {
      for (final int code in token.codeUnits) {
        bytes.add(_charToByte(code));
      }
    }
    return <String>[utf8.decode(bytes, allowMalformed: true)];
  }

  static int _charToByte(int code) {
    if (code >= 0x100 && code <= 0x143) {
      return _nonPrintableBytes[code - 0x100];
    }
    return code & 0xFF;
  }
}
