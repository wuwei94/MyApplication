/// 用编辑距离对齐相邻重叠窗口、把文本合并成「已确认 + 未确认」两段。
///
/// 官方 `LevenshteinTokenMerger` 的等价实现。这是整个 ASR 示例里最绕的一块，核心矛盾是：
/// 相邻两个 5 秒窗口有 2 秒重叠，重叠区必然被识别两遍，而**两次结果不会完全一样**。
/// 于是需要拿后一次的窗口去和上一次的文本对齐，判断「上一次的哪些词已经稳定下来」。
///
/// 对齐结果切成三点：
/// - `[0, prevWordIndexOfUnconfirmed)` —— 已确认，可以被下一次输出替换掉
///   （[lastConfirmedWords]）；
/// - `[prevWordIndexOfUnconfirmed, prevWordIndexOfPivot)` —— 未确认区里来自**上一次**的部分；
/// - 未确认区剩下的 —— 来自**当前**窗口，通常更准（新窗口对同一段音频看得更全）。
///
/// 有两条对齐路径，按模型是否给时间戳二选一：
/// - 给时间戳（Parakeet-TDT / CTC）：直接按时间戳找重叠起点，点得很准；
/// - 不给（Moonshine / Whisper / Qwen3-ASR）：退化成「按比例猜一个中点 ± [searchWindow]，
///   在这几个候选起点里挑编辑距离最小的」。
///
/// 与官方的差异：
/// - **去掉了逐 token 的 `Log.d`**（本模块其余页面也不打这类日志，免得一次转写把
///   logcat 刷满）；
/// - `canonicalize` 用**码位区间**判标点，而不是 Java 正则的 `\p{Punct}` ——
///   Dart 的 `RegExp` 对 Unicode 属性转义支持有限，而 Java 的 `\p{Punct}` 恰好就是
///   ASCII 标点的四个区间（`0x21-0x2F` / `0x3A-0x40` / `0x5B-0x60` / `0x7B-0x7E`），
///   按区间判等价且没有转义风险。
/// 逻辑本身逐行对齐。
library;

import 'dart:typed_data';

import 'package:flutter_demo/demos/ml/engines/asr/asr_tokenizer.dart';
import 'package:flutter_demo/demos/ml/engines/asr/asr_types.dart';


/// 后处理输出：已确认 / 未确认两段文本
typedef AsrDecodedText = ({String confirmedText, String unconfirmedText});

/// 把逐 token 的输出整理成「已确认 / 未确认」两段文本
///
/// 界面把未确认部分标灰 —— 这样用户能直接看出哪些字还可能被下一次重叠窗口改写。
class AsrLevenshteinMerger {
  AsrLevenshteinMerger(
    this.tokenizer,
    this.overlapRatio, {
    this.searchWindow = defaultSearchWindow,
    this.maxLevenshteinDistance = defaultMaxDistance,
    this.pivotFactor = defaultPivotFactor,
  });

  final AsrHfTokenizer tokenizer;

  /// 相邻窗口的重叠比例（`overlap / interval`）
  final double overlapRatio;
  final int searchWindow;
  final int maxLevenshteinDistance;
  final double pivotFactor;

  List<String> _prevWords = const <String>[];

  /// 与 [_prevWords] 同下标的时间戳（已按当前窗口的时间轴对齐）
  List<int> _prevWordTimestamps = const <int>[];

  /// [_prevWords] 中第一个属于「未确认」的下标
  int _prevWordIndexOfUnconfirmed = -1;

  /// 未确认区里「上一次」与「当前」的分界点
  int _prevWordIndexOfPivot = -1;

  final List<int> _currentTokens = <int>[];
  final List<int> _currentWordTimestamps = <int>[];

  List<String> _lastConfirmedWords = const <String>[];

  /// 文件转写允许的编辑距离容差（官方常量）
  static const int fileMaxLevenshteinDistance = 5;

  /// 实时录音允许更大的容差（官方常量）
  static const int liveMaxLevenshteinDistance = 20;

  static const int defaultSearchWindow = 2;
  static const int defaultMaxDistance = 5;
  static const double defaultPivotFactor = 0.6;

  /// 推入一个 token（对应官方 `LevenshteinTokenMerger.decode`）
  ///
  /// 方法名从 `decode` 改成 `push`：本文件里「decode」已经被「token id → 文本」的
  /// [AsrHfTokenizer.decode] 占用，同名会让调用点难以分辨。
  ///
  /// @param tokenId token id，或 [AsrSpeech.endOfSequence]
  /// @param timestamp 该 token 的相对时间戳，或 [AsrSpeech.noTimestamp]
  /// @return `null` 表示「本次不更新界面」
  AsrDecodedText? push(int tokenId, int timestamp) {
    final bool isEndOfSequence = tokenId == AsrSpeech.endOfSequence;
    if (!isEndOfSequence) {
      _currentTokens.add(tokenId);
    }

    final List<String> currentWords = _detokenize(_currentTokens);
    if (timestamp != AsrSpeech.noTimestamp) {
      // 新出现的词沿用当前 token 的时间戳（粗粒度近似，官方亦如此）
      while (currentWords.length > _currentWordTimestamps.length) {
        _currentWordTimestamps.add(timestamp);
      }
    }

    if (isEndOfSequence) {
      final AsrDecodedText? result =
          _textAtEndOfSequence(currentWords);
      if (timestamp != AsrSpeech.noTimestamp) {
        // 把上一次的时间戳平移到下一个窗口的时间轴上
        final int timestampStep = (timestamp * (1 - overlapRatio)).toInt();
        _prevWordTimestamps = _prevWordTimestamps
            .map((int value) => value - timestampStep)
            .toList();
      }
      _currentTokens.clear();
      _currentWordTimestamps.clear();
      _prevWordIndexOfUnconfirmed = -1;
      _prevWordIndexOfPivot = -1;
      return result;
    }

    // 第一个窗口：整段都算未确认
    if (_prevWords.isEmpty) {
      return (confirmedText: '', unconfirmedText: currentWords.join(' '));
    }

    // 当前窗口还不够长，先不改动上一次的输出
    final int lastPrevTimestamp =
        _prevWordTimestamps.isEmpty ? 0 : _prevWordTimestamps.last;
    if ((timestamp != AsrSpeech.noTimestamp && timestamp < lastPrevTimestamp) ||
        (timestamp == AsrSpeech.noTimestamp &&
            currentWords.length < _prevWords.length * overlapRatio)) {
      return null;
    }

    // 分界点上一次已经算过，只需把新 token 接进未确认文本
    if (_prevWordIndexOfUnconfirmed != -1) {
      return (
        confirmedText: '',
        unconfirmedText: _mergeIntoUnconfirmedText(currentWords).join(' '),
      );
    }

    return _textFromPrevAndCurrent(currentWords);
  }

  /// 收尾：把上一次的文本和当前窗口合并，必要时把整段升级为「已确认」
  AsrDecodedText? _textAtEndOfSequence(List<String> currentWords) {
    final List<String> mergedWords = _mergeIntoUnconfirmedText(currentWords);
    final List<int> mergedTimestamps = _mergeTimestamps(mergedWords.length);
    if (mergedWords.isEmpty && _prevWords.isNotEmpty) {
      // 当前窗口是静音 → 上一次的词全部确认
      _lastConfirmedWords = _prevWords;
      _prevWords = mergedWords;
      _prevWordTimestamps = mergedTimestamps;
      return (
        confirmedText: _lastConfirmedWords.join(' '),
        unconfirmedText: '',
      );
    }

    // 当前窗口里静音比平时长时，会和「已确认」的尾部重复，这里去一次重
    _prevWords = dedupWords(_lastConfirmedWords, mergedWords, mergedWords.length);
    final int drop = mergedTimestamps.length - _prevWords.length;
    _prevWordTimestamps = drop > 0
        ? mergedTimestamps.sublist(drop)
        : List<int>.from(mergedTimestamps);
    if (_prevWords.isNotEmpty && _prevWordIndexOfUnconfirmed == -1) {
      return (confirmedText: '', unconfirmedText: _prevWords.join(' '));
    }

    return null;
  }

  /// 在上一次的文本里找两个点：① 与当前窗口对齐的起点（之前的算已确认）；
  /// ② 未确认区内部「上一次 / 当前」的分界（[pivotFactor] 决定切多少给当前窗口）。
  AsrDecodedText _textFromPrevAndCurrent(List<String> currentWords) {
    final int middleIndexToSearch;
    if (_prevWordTimestamps.isNotEmpty && _currentWordTimestamps.isNotEmpty) {
      final int targetTimestamp = _currentWordTimestamps.first;
      final int index =
          _prevWordTimestamps.indexWhere((int value) => value >= targetTimestamp);
      middleIndexToSearch =
          index != -1 ? index : (_prevWords.length * (1 - overlapRatio)).toInt();
    } else {
      middleIndexToSearch = (_prevWords.length * (1 - overlapRatio)).toInt();
    }

    int minDistance = maxLevenshteinDistance;
    // 一处重叠都没找到时，退化成「上一次全部算未确认」
    _prevWordIndexOfUnconfirmed = (middleIndexToSearch + searchWindow + 1)
        .clamp(0, _prevWords.length);
    for (int i = -searchWindow; i <= searchWindow; i++) {
      final int startIndex =
          (middleIndexToSearch + i).clamp(0, _prevWords.length - 1);
      final int endIndex =
          (startIndex + currentWords.length).clamp(0, _prevWords.length);
      final int distance = levenshtein(
        canonicalize(_prevWords.sublist(startIndex, endIndex)),
        canonicalize(
          currentWords.sublist(0, (endIndex - startIndex).clamp(0, currentWords.length)),
        ),
      );
      if (distance < minDistance) {
        minDistance = distance;
        _prevWordIndexOfUnconfirmed = startIndex;
      }
    }

    final double unconfirmedWordsUntilPivot =
        (_prevWords.length - _prevWordIndexOfUnconfirmed) * pivotFactor;
    final int pivotOffset = unconfirmedWordsUntilPivot.ceil();
    _prevWordIndexOfPivot =
        (_prevWordIndexOfUnconfirmed + pivotOffset).clamp(0, _prevWords.length);
    _lastConfirmedWords = _prevWords.sublist(0, _prevWordIndexOfUnconfirmed);
    return (
      confirmedText: _lastConfirmedWords.join(' '),
      unconfirmedText: _mergeIntoUnconfirmedText(currentWords).join(' '),
    );
  }

  List<String> _detokenize(List<int> tokens) => tokenizer
      .decode(Int32List.fromList(tokens))
      .split(' ')
      .map((String word) => word.trim())
      .where((String word) => word.isNotEmpty)
      .toList();

  List<String> _mergeIntoUnconfirmedText(List<String> currentWords) {
    if (_prevWordIndexOfUnconfirmed == -1) {
      return currentWords;
    }
    return mergeIntoUnconfirmedText(
      prevWords: _prevWords,
      prevWordTimestamps: _prevWordTimestamps,
      currentWords: currentWords,
      currentWordTimestamps: _currentWordTimestamps,
      overlapRatio: overlapRatio,
      pivotFactor: pivotFactor,
      prevWordIndexOfUnconfirmed: _prevWordIndexOfUnconfirmed,
      prevWordIndexOfPivot: _prevWordIndexOfPivot,
    );
  }

  List<int> _mergeTimestamps(int numMergedWords) {
    if (numMergedWords == 0) {
      return const <int>[];
    }
    if (_prevWordTimestamps.isEmpty ||
        _prevWordIndexOfUnconfirmed == -1 ||
        _prevWordIndexOfUnconfirmed == _prevWordIndexOfPivot) {
      return List<int>.from(_currentWordTimestamps);
    }
    final int numWordsFromPrevWords =
        _prevWordIndexOfPivot - _prevWordIndexOfUnconfirmed;
    final List<int> timestampsFromPrevWords = _prevWordTimestamps.sublist(
      _prevWordIndexOfUnconfirmed,
      _prevWordIndexOfPivot,
    );
    final int numWordsFromCurrentWords = numMergedWords - numWordsFromPrevWords;
    final int from = _currentWordTimestamps.length - numWordsFromCurrentWords;
    final List<int> timestampsFromCurrentWords =
        from > 0 ? _currentWordTimestamps.sublist(from) : <int>[];
    return <int>[...timestampsFromPrevWords, ...timestampsFromCurrentWords];
  }

  // ─────────────────── 纯函数部分（可直接单测） ───────────────────

  /// 标准 Levenshtein 编辑距离（插入/删除/替换代价均为 1）
  static int levenshtein(String s1, String s2) {
    final List<int> previous = List<int>.generate(s2.length + 1, (int j) => j);
    final List<int> current = List<int>.filled(s2.length + 1, 0);
    for (int i = 1; i <= s1.length; i++) {
      current[0] = i;
      for (int j = 1; j <= s2.length; j++) {
        final int deletionCost = previous[j] + 1;
        final int insertionCost = current[j - 1] + 1;
        final int matchOrSubstitutionCost =
            previous[j - 1] + (s1.codeUnitAt(i - 1) == s2.codeUnitAt(j - 1) ? 0 : 1);
        current[j] = deletionCost < insertionCost
            ? (deletionCost < matchOrSubstitutionCost
                ? deletionCost
                : matchOrSubstitutionCost)
            : (insertionCost < matchOrSubstitutionCost
                ? insertionCost
                : matchOrSubstitutionCost);
      }
      for (int j = 0; j <= s2.length; j++) {
        previous[j] = current[j];
      }
    }
    return previous[s2.length];
  }

  /// 未确认文本 = 上一次 `[已确认, pivot)` 的词 + 当前窗口 pivot 之后的词。
  /// 若当前窗口 pivot 之后还不够长，就整段沿用上一次（避免未确认区忽长忽短地跳）。
  static List<String> mergeIntoUnconfirmedText({
    required List<String> prevWords,
    required List<int> prevWordTimestamps,
    required List<String> currentWords,
    required List<int> currentWordTimestamps,
    required double overlapRatio,
    required double pivotFactor,
    required int prevWordIndexOfUnconfirmed,
    required int prevWordIndexOfPivot,
  }) {
    final bool useTimestamps =
        currentWordTimestamps.isNotEmpty && prevWordTimestamps.isNotEmpty;
    final int numWordsBeforePivotInCurrent;
    if (useTimestamps) {
      final int timestampAtPivot = prevWordIndexOfPivot >= prevWordTimestamps.length
          ? prevWordTimestamps.last + 1
          : prevWordTimestamps[prevWordIndexOfPivot];
      numWordsBeforePivotInCurrent =
          currentWordTimestamps.where((int value) => value < timestampAtPivot).length;
    } else {
      final double overlapCeil = (currentWords.length * overlapRatio).ceilToDouble();
      final int numWordsOverlapInCurrent = overlapCeil.toInt() <
              (prevWords.length - prevWordIndexOfUnconfirmed)
          ? (prevWords.length - prevWordIndexOfUnconfirmed)
          : overlapCeil.toInt();
      numWordsBeforePivotInCurrent =
          (numWordsOverlapInCurrent * pivotFactor).toInt().clamp(0, currentWords.length);
    }
    final int numWordsAfterPivotInCurrent =
        currentWords.length - numWordsBeforePivotInCurrent;
    if (numWordsAfterPivotInCurrent < prevWords.length - prevWordIndexOfPivot) {
      return prevWords.sublist(prevWordIndexOfUnconfirmed);
    }

    final List<String> unconfirmedTextInPrev =
        prevWords.sublist(prevWordIndexOfUnconfirmed, prevWordIndexOfPivot);
    final List<String> unconfirmedTextInCurrent =
        currentWords.sublist(numWordsBeforePivotInCurrent);
    return <String>[
      ...unconfirmedTextInPrev,
      ...dedupWords(unconfirmedTextInPrev, unconfirmedTextInCurrent),
    ];
  }

  /// 去掉 [currentWords] 开头与 [prevWords] 尾部重复的词（最多看 [searchWindow] 个）
  ///
  /// 用「前缀近似相等」而不是严格相等，因为重叠区两次识别的结果常有个别字母差异
  /// （官方 [closeEnough] 的判据：长度 ≤3 要求前缀完全相同，否则编辑距离 ≤1）。
  static List<String> dedupWords(
    List<String> prevWords,
    List<String> currentWords, [
    int searchWindow = defaultSearchWindow,
  ]) {
    final int maxSearchRange = searchWindow <
            (prevWords.length < currentWords.length
                ? prevWords.length
                : currentWords.length)
        ? searchWindow
        : (prevWords.length < currentWords.length
            ? prevWords.length
            : currentWords.length);
    for (int i = maxSearchRange; i >= 1; i--) {
      if (closeEnough(
        canonicalize(prevWords.sublist(prevWords.length - i)),
        canonicalize(currentWords.sublist(0, i)),
      )) {
        return currentWords.sublist(i);
      }
    }
    return currentWords;
  }

  /// 比较用的规范化形式：转小写 + 去标点
  static String canonicalize(List<String> words) {
    final String joined =
        words.map((String word) => word.toLowerCase()).join(' ');
    final StringBuffer buffer = StringBuffer();
    for (final int code in joined.codeUnits) {
      if (!_isAsciiPunctuation(code)) {
        buffer.writeCharCode(code);
      }
    }
    return buffer.toString();
  }

  /// 逐词近似判等：先截到等长，长度 ≤3 时要求完全相同，否则允许 1 个编辑距离
  ///
  /// 注意官方在 [dedupWords] 里传入的是**整段**规范化文本（`canonicalize` 的产物），
  /// 落到这个「字符串版」重载上 —— 也就是说判的都是「整段文本前缀差 ≤1」。
  /// 保留这一语义（Kotlin 里靠重载解析隐式选中，Dart 里没有重载，只能显式只留这一个）。
  static bool closeEnough(String s1, String s2) {
    final int minLength = s1.length < s2.length ? s1.length : s2.length;
    final String prefix1 = s1.substring(0, minLength);
    final String prefix2 = s2.substring(0, minLength);
    return minLength <= _shortWordLength
        ? prefix1 == prefix2
        : levenshtein(prefix1, prefix2) <= 1;
  }

  /// Java 正则 `\p{Punct}` 的等价判定（ASCII 标点的四个码位区间）
  static bool _isAsciiPunctuation(int code) =>
      (code >= 0x21 && code <= 0x2F) ||
      (code >= 0x3A && code <= 0x40) ||
      (code >= 0x5B && code <= 0x60) ||
      (code >= 0x7B && code <= 0x7E);

  /// 短词阈值：3 个字符以内不允许容错，否则 "a"/"i" 这种会互相误判成重复
  static const int _shortWordLength = 3;
}
