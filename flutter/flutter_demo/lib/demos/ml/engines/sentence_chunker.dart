/// 把输入文本切成「逐句」的合成块 —— 与 Android `SentenceChunker` 对应。
///
/// 这是上游 pip 包 `chunk_text` 的忠实移植，同时也是**流式粒度**：每句一次完整
/// 合成，合成完就丢给播放器，播放第 N 句的同时合成第 N+1 句，因此首音延迟只有
/// 一句。
///
/// 两处上游怪癖是**故意保留**的（模型的韵律就是照着这个前端调的）：
/// 1. 句末的 `.!?` 会被切分吃掉；
/// 2. 切完后再给每块补一个逗号，于是模型看到的块末 token 永远是 `,`。
library;

/// 句末标点（切分符）
final RegExp _sentenceEnd = RegExp('[.!?]+');

/// 单块最大字符数；超过则按词边界再切
const int _maxChunkChars = 400;

/// 块末若已是这些标点则不再补逗号
const String _punctuation = '.!?,;:';

/// 把 [text] 切成逐句合成块
List<String> sentenceChunk(String text) {
  final List<String> chunks = <String>[];
  for (final String sentence in text.split(_sentenceEnd)) {
    final String trimmed = sentence.trim();
    if (trimmed.isEmpty) continue;
    if (trimmed.length <= _maxChunkChars) {
      chunks.add(_ensurePunctuation(trimmed));
    } else {
      // 过长句：按词边界再切
      final StringBuffer builder = StringBuffer();
      for (final String word in trimmed.split(RegExp(r'\s+'))) {
        if (builder.length + word.length + 1 > _maxChunkChars && builder.isNotEmpty) {
          chunks.add(_ensurePunctuation(builder.toString()));
          builder.clear();
        }
        if (builder.isNotEmpty) {
          builder.write(' ');
        }
        builder.write(word);
      }
      if (builder.isNotEmpty) {
        chunks.add(_ensurePunctuation(builder.toString()));
      }
    }
  }
  return chunks;
}

String _ensurePunctuation(String sentence) =>
    _punctuation.contains(sentence[sentence.length - 1]) ? sentence : '$sentence,';
