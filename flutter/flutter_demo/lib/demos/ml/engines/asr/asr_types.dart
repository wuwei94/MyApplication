/// ASR 的公共类型与哨兵常量。
///
/// 单独成一个文件是为了**切断 import 传染链**：解码器 `AsrDecoder` 需要
/// `MlSignatureGraph`（即 `tflite_flutter` 的原生绑定），而
/// `AsrLevenshteinMerger` 只需要 [AsrSpeech] 这两个整数常量。
/// 若把这些常量留在 `asr_decoder.dart` 里，merger 就会顺着
/// `asr_decoder.dart -> ml_signature.dart -> tflite_flutter` 把原生绑定拖进来，
/// 使纯 Dart 单测在 `flutter test`（VM，无 native 库）下直接加载失败。
/// 与 Android 侧同值。
library;

/// 序列里的哨兵常量 —— 与 Android `SpeechRecognizer` 同值
abstract final class AsrSpeech {
  /// 序列结束哨兵
  static const int endOfSequence = -1;

  /// 该模型不提供时间戳
  static const int noTimestamp = -1;
}

/// 一次解码结果：token id + 相对时间戳
typedef AsrStep = ({int tokenId, int timestamp});
