import 'dart:convert';
import 'dart:io';
import 'dart:typed_data';

import 'package:flutter/services.dart' show rootBundle;
import 'package:flutter_demo/demos/ml/engines/ml_model_download.dart';
import 'package:flutter_demo/demos/ml/engines/ml_tflite_helper.dart';
import 'package:tflite_flutter/tflite_flutter.dart';

/// 英文 G2P（字素转音素），运行期不依赖 espeak —— 与 Android `EnglishG2P` 对应。
///
/// 混合式，与 kokoro 同构：
/// 1. 一张约 27.5 万条的 espeak-IPA 词典（主力，覆盖绝大多数常用词的正确读音），
///    以 `g2p_dict.txt.gz` 随资产分发， Dart 侧用 [GZipCodec] 解压（Flutter 不像
///    Android aapt 那样自动解压 `.gz` 资产）；
/// 2. 一个 DeepPhonemizer ForwardTransformer（OpenPhonemizer 的 espeak-IPA checkpoint，
///    Clear BSD / MIT），负责词典外的词。它被转成**固定形状**的 LiteRT 图，跑在 CPU 上
///    ——该图用到 EQUAL / SELECT_V2 / 大于 4 维的注意力张量，GPU delegate 会拒绝。
///
/// ```
/// 词 -> 词典[词] 或 (字符 id × charRepeats, <en_us>…<end>, 0 补齐到 maxTokens)
///       -> g2p.tflite (CPU) -> argmax -> 合并重复 -> espeak 风格 IPA
/// 整句 IPA -> 符号 id（178 符号 IPA 集）
/// ```
///
/// 两个 TTS 示例共用本类；唯一差别在标点：Matcha 把标点直接贴在前一个词后面
/// （`"wˈɜːld,"`），而 KittenTTS 的时长预测器是对着上游 pip 包的
/// `basic_english_tokenize` 训练的，标点必须**单独成词**（`"wˈɜːld ,"`）。
/// 因此用 [punctuationSpaced] 区分，而不是把这份前端复制两遍。
class EnglishG2P {
  EnglishG2P({
    this.punctuationSpaced = false,
    String g2pModelFileName = 'dp_g2p_matcha_fp16.tflite',
  }) : _modelFileName = g2pModelFileName;

  /// 标点是否作为独立 token 输出（KittenTTS 页为 `true`，Matcha 页为 `false`）
  final bool punctuationSpaced;

  final String _modelFileName;

  late final Map<String, String> _dictionary;
  late final Map<String, int> _charToIndex;
  late final Map<int, String> _indexToPhoneme;
  late final Map<String, int> _symbolToId;
  late final Set<String> _specialTokens;
  late final int _charRepeats;
  late final int _startId;
  late final int _endId;
  late final int _maxTokens;
  late final int _numPhonemes;

  Interpreter? _interpreter;

  /// 词典条目数；仅用于状态栏展示，不参与推理
  late final int dictionarySize;

  /// 分词器与词典加载耗时（毫秒）
  late final double loadMs;

  /// 解析模型元数据与 espeak-IPA 词典，并构建 G2P 计算图（CPU）。
  ///
  /// 对应 Android `EnglishG2P.init` + `loadMetadata`。
  Future<void> load() async {
    final Stopwatch watch = Stopwatch()..start();

    // g2p 图固定跑 CPU：该图用到 EQUAL / SELECT_V2 / 大于 4 维的注意力张量，
    // GPU delegate 会直接拒绝
    final String g2pPath = await MlModelStore.pathIn(_modelFileName);
    if (!File(g2pPath).existsSync()) {
      throw StateError('模型尚未下载: $_modelFileName');
    }
    _interpreter = (await MlTfliteHelper.createInterpreterFromFile(
      File(g2pPath),
      useGpu: false,
    ))
        .interpreter;

    final Map<String, Object?> meta = _json(await _loadText('assets/ml/g2p_meta.json'));
    final Map<String, Object?> char2idx = _object(meta['char2idx']);
    _charToIndex = <String, int>{};
    char2idx.forEach((String key, Object? value) {
      if (key.length == 1) _charToIndex[key] = (value as num).toInt();
    });
    final Map<String, Object?> idx2ph = _object(meta['idx2ph']);
    _indexToPhoneme = <int, String>{};
    idx2ph.forEach((String key, Object? value) {
      _indexToPhoneme[int.parse(key)] = value as String;
    });
    _charRepeats = (meta['char_repeats'] as num).toInt();
    _startId = (meta['start'] as num).toInt();
    _endId = (meta['end'] as num).toInt();
    _maxTokens = (meta['MAXT'] as num).toInt();
    _numPhonemes = (meta['n_phonemes'] as num).toInt();
    _specialTokens = _array(meta['special']).cast<String>().toSet();

    // matcha 符号 -> id（keithito 符号表来自 config.json）
    final Map<String, Object?> config = _json(await _loadText('assets/ml/config.json'));
    _symbolToId = <String, int>{};
    final List<Object?> symbols = _array(config['symbols']);
    for (int i = 0; i < symbols.length; i++) {
      final Object? symbol = symbols[i];
      if (symbol is String && symbol.length == 1) {
        _symbolToId[symbol] = i;
      }
    }

    // espeak-IPA 词典（主力 G2P）：每行 `词<TAB>ipa`
    final ByteData raw = await rootBundle.load('assets/ml/g2p_dict.txt.gz');
    final List<int> decompressed = GZipCodec().decode(
      raw.buffer.asUint8List(raw.offsetInBytes, raw.lengthInBytes),
    );
    final String text = utf8.decode(decompressed);
    _dictionary = <String, String>{};
    for (final String line in const LineSplitter().convert(text)) {
      final int tab = line.indexOf('\t');
      if (tab > 0) {
        _dictionary[line.substring(0, tab)] = line.substring(tab + 1);
      }
    }
    dictionarySize = _dictionary.length;

    watch.stop();
    loadMs = watch.elapsedMicroseconds / 1000.0;
  }

  /// 整句文本 -> 符号 id（BOS/EOS 由各合成器自行补）
  ///
  /// host 侧文本规范化：全大写缩写逐字母拼读（GPU -> "gee pee you"），
  /// 数字按词读出（4090 -> "four thousand ninety"）。
  List<int> phonemize(String text) {
    final StringBuffer ipa = StringBuffer();
    bool first = true;
    void append(String phonemes) {
      if (phonemes.isEmpty) return;
      if (!first) ipa.write(' ');
      ipa.write(phonemes);
      first = false;
    }

    for (final RegExpMatch match in _token.allMatches(text)) {
      final String token = match.group(0)!;
      if (_acronym.hasMatch(token)) {
        // 缩写逐字母拼读（"GPU" -> "gee pee you"）
        append(token.toLowerCase().split('').map((String ch) {
          return _letterIpa[ch] ?? '';
        }).join());
      } else if (_digit.firstMatch(token) != null) {
        for (final String word in numberToWords(token)) {
          append(_dictionary[word] ?? phonemizeWord(word));
        }
      } else if (_word.hasMatch(token)) {
        // 词典优先；词典外才交给神经 G2P
        final String lower = token.toLowerCase();
        append(_dictionary[lower] ?? phonemizeWord(lower));
      } else {
        // 标点：KittenTTS 要求独立成词（走 append 会插空格），Matcha 要求紧贴前词
        if (punctuationSpaced) {
          append(token);
        } else {
          ipa.write(token);
        }
      }
    }

    final String ipaText = ipa.toString();
    final List<int> ids = <int>[];
    for (int i = 0; i < ipaText.length; i++) {
      final int? id = _symbolToId[ipaText[i]];
      if (id != null) ids.add(id);
    }
    return ids;
  }

  /// "1,234.5" -> ["one","thousand","two","hundred","thirty","four","point","five"]
  List<String> numberToWords(String raw) {
    final String token = raw.replaceAll(',', '');
    if (token.contains('.')) {
      final List<String> parts = token.split('.');
      final List<String> words = <String>[
        ...(parts[0].isNotEmpty
            ? integerToWords(int.tryParse(parts[0]) ?? 0)
            : const <String>['zero']),
        'point',
      ];
      for (int i = 0; i < parts[1].length; i++) {
        final int digit = parts[1].codeUnitAt(i) - 48;
        if (digit >= 0 && digit <= 9) words.add(_ones[digit]);
      }
      return words;
    }
    return integerToWords(int.tryParse(token) ?? 0);
  }

  List<String> _wordsUnderThousand(int value) {
    int n = value;
    final List<String> words = <String>[];
    if (n >= 100) {
      words.add(_ones[n ~/ 100]);
      words.add('hundred');
      n %= 100;
    }
    if (n >= 20) {
      words.add(_tens[n ~/ 10]);
      n %= 10;
    }
    if (n > 0) words.add(_ones[n]);
    return words;
  }

  List<String> integerToWords(int value) {
    if (value == 0) return const <String>['zero'];
    if (value < 0) return <String>['minus', ...integerToWords(-value)];
    final List<int> groups = <int>[];
    int n = value;
    while (n > 0) {
      groups.add(n % 1000);
      n ~/= 1000;
    }
    if (groups.length > _scales.length) {
      // 太大读不出名字；逐位数字读
      return value.toString().split('').map((String d) => _ones[int.parse(d)]).toList();
    }
    final List<String> words = <String>[];
    for (int i = groups.length - 1; i >= 0; i--) {
      if (groups[i] == 0) continue;
      words.addAll(_wordsUnderThousand(groups[i]));
      if (_scales[i].isNotEmpty) words.add(_scales[i]);
    }
    return words;
  }

  /// 用神经 G2P 把单个词转成 espeak 风格 IPA 串
  String phonemizeWord(String word) {
    final List<int> ids = <int>[_startId];
    for (int i = 0; i < word.length; i++) {
      final int? id = _charToIndex[word[i]];
      if (id != null) {
        for (int r = 0; r < _charRepeats; r++) {
          ids.add(id);
        }
      }
    }
    ids.add(_endId);
    final int length = ids.length < _maxTokens ? ids.length : _maxTokens;

    // 图输入是 FLOAT32（[1, 96]），字符 id 以浮点写入
    final Float32List input = Float32List(_maxTokens);
    for (int i = 0; i < _maxTokens; i++) {
      input[i] = i < length ? ids[i].toDouble() : 0.0;
    }
    final Interpreter interpreter = _interpreter!;
    MlTfliteHelper.writeFloatInput(interpreter, 0, input);
    interpreter.invoke();
    final Float32List logits = MlTfliteHelper.readFloatOutput(interpreter);

    final StringBuffer result = StringBuffer();
    int previous = -1;
    for (int t = 0; t < length; t++) {
      int best = 0;
      double bestScore = logits[t * _numPhonemes];
      for (int k = 1; k < _numPhonemes; k++) {
        final double score = logits[t * _numPhonemes + k];
        if (score > bestScore) {
          bestScore = score;
          best = k;
        }
      }
      if (best == previous) continue;
      previous = best;
      final String? phoneme = _indexToPhoneme[best];
      if (phoneme == null) continue;
      if (_specialTokens.contains(phoneme) || best == 0) continue;
      for (int i = 0; i < phoneme.length; i++) {
        if (phoneme[i] != '-') {
          // 去掉缩写的连字符
          result.write(phoneme[i]);
        }
      }
    }
    return result.toString();
  }

  void close() {
    _interpreter?.close();
    _interpreter = null;
  }

  // ─────────────────── 内部工具 ───────────────────

  static Future<String> _loadText(String assetPath) async {
    final ByteData data = await rootBundle.load(assetPath);
    return utf8.decode(data.buffer.asUint8List(data.offsetInBytes, data.lengthInBytes));
  }

  static Map<String, Object?> _json(String text) {
    final Object? decoded = jsonDecode(text);
    return _object(decoded);
  }

  static Map<String, Object?> _object(Object? value) {
    if (value is Map) {
      return value.map((Object? key, Object? item) => MapEntry<String, Object?>('$key', item));
    }
    throw ArgumentError('Expected JSON object');
  }

  static List<Object?> _array(Object? value) {
    if (value is List) return value;
    throw ArgumentError('Expected JSON array');
  }

  // 大小写敏感的分词：ACRONYM（2 个以上大写）| NUMBER | word | 标点。
  // 字符类里同时含单引号与双引号，用普通字符串 + 转义书写（raw string 无法混含两种引号）
  static final RegExp _token = RegExp(
      '[A-Z]{2,}|\\d[\\d,]*(?:\\.\\d+)?|[A-Za-z\']+|[.,!?;:—…"]');
  static final RegExp _acronym = RegExp('[A-Z]{2,}');
  static final RegExp _word = RegExp("[A-Za-z']+");
  static final RegExp _digit = RegExp('\\d');

  // espeak 字母名 IPA —— 缩写逐字母读出（如 "GPU" -> dʒˈiːpˈiːjˈuː）
  static const Map<String, String> _letterIpa = <String, String>{
    'a': 'ˈeɪ', 'b': 'bˈiː', 'c': 'sˈiː', 'd': 'dˈiː', 'e': 'ˈiː',
    'f': 'ˈɛf', 'g': 'dʒˈiː', 'h': 'ˈeɪtʃ', 'i': 'ˈaɪ', 'j': 'dʒˈeɪ',
    'k': 'kˈeɪ', 'l': 'ˈɛl', 'm': 'ˈɛm', 'n': 'ˈɛn', 'o': 'ˈoʊ',
    'p': 'pˈiː', 'q': 'kjˈuː', 'r': 'ˈɑːɹ', 's': 'ˈɛs', 't': 'tˈiː',
    'u': 'jˈuː', 'v': 'vˈiː', 'w': 'dˈʌbəljˌuː', 'x': 'ˈɛks',
    'y': 'wˈaɪ', 'z': 'zˈiː',
  };

  static const List<String> _ones = <String>[
    'zero', 'one', 'two', 'three', 'four', 'five', 'six', 'seven', 'eight', 'nine', 'ten',
    'eleven', 'twelve', 'thirteen', 'fourteen', 'fifteen', 'sixteen', 'seventeen',
    'eighteen', 'nineteen',
  ];

  static const List<String> _tens = <String>[
    '', '', 'twenty', 'thirty', 'forty', 'fifty', 'sixty', 'seventy', 'eighty', 'ninety',
  ];

  static const List<String> _scales = <String>['', 'thousand', 'million', 'billion', 'trillion'];
}
