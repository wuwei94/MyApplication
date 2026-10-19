/// ASR 的解码层 —— 与 Android `:modules:module_ml` 的 `LiteRtRunner` /
/// `CtcDecoder` / `TdtDecoder` 一一对应。
///
/// 结构上是一条**两段式**流水：先用 `encode` 签名把音频特征编码成隐状态，
/// 再由各自的 [AsrDecoder] 逐 token 解码。除 Parakeet-CTC 之外，
/// 官方支持的所有模型都是这种「encoder + decoder 多子图」形态。
///
/// ### 与 Android 的唯一结构性差异：不能零拷贝绑缓冲
///
/// Android 用的是 LiteRT 2.x 的 `CompiledModel.run(inputs, outputs, key)`，
/// 它能把**任意缓冲**按名字绑给某个签名 —— 于是「把 encoder 的输出缓冲直接拼到
/// decoder 的输入列表前面」是零拷贝的。而 Flutter 侧只能走经典 C API
/// （`TfLiteSignatureRunner*`，见 [MlSignatureGraph] 的类注释），
/// 签名的输入/输出**永远是图内固定张量**，因此每一步「跨子图传数据」都必须**拷贝**。
/// 三处拷贝点：
/// 1. encoder 输出 → decoder 输入（每个窗口一次，在逐步解码的循环之外）；
/// 2. TDT 的 LSTM 状态 ping-pong（每步一次，状态本身很小，几十到几千个 float）；
/// 3. TDT 的 encoder 输出 → `decode_1` 输入（切签名时一次）。
///
/// ### 另一处差异：日志读取用就地视图而不是整块拷出
///
/// Android 每步 `outputBuffers[0].readFloat()` 会把**整张** logits 输出拷到堆上
/// （TDT 的输出形状是 `[maxTimeIndex × tokenCount × logitsPerToken]`，几十 MB），
/// 再在自己的切片上取 argmax。这里改成 `floatView` 就地读同一段切片 —— argmax 的
/// 结果逐位相同，但省掉每步一次 O(整个输出) 的拷贝。用完即弃，下一次 invoke 才会
/// 覆盖它，所以没有读到脏数据的风险。
library;

import 'dart:math' as math;
import 'dart:typed_data';

import 'package:flutter_demo/demos/ml/engines/asr/asr_model_catalog.dart';
import 'package:flutter_demo/demos/ml/engines/asr/asr_tokenizer.dart';
import 'package:flutter_demo/demos/ml/engines/asr/asr_types.dart';
import 'package:flutter_demo/demos/ml/engines/ml_signature.dart';

export 'package:flutter_demo/demos/ml/engines/asr/asr_types.dart'
    show AsrSpeech, AsrStep;

/// ASR 主图的签名封装：`encode` / `decode` / `decode_1` 三个签名按需打开
///
/// 「按需」是必要的：Parakeet-CTC **没有** `decode` 签名（帧级 logits 直接由主图给出），
/// 若在这里无条件打开 `decode`，CTC 模型一进页面就报「模型里没有签名」。
class AsrSignatureGraph {
  AsrSignatureGraph(this.graph, this.config);

  /// 底层多签名图；由引擎持有并统一释放
  final MlSignatureGraph graph;
  final AsrModelConfig config;

  static const String encodeSignature = 'encode';
  static const String decodeSignature = 'decode';
  static const String decode1Signature = 'decode_1';

  List<String>? _keys;
  MlSignatureRunner? _encode;
  MlSignatureRunner? _decode;
  MlSignatureRunner? _decode1;

  List<String> get signatureKeys => _keys ??= graph.signatureKeys;

  /// `encode` 签名：音频特征 → 隐状态
  MlSignatureRunner get encode => _encode ??= _open(encodeSignature);

  /// `decode` 签名（无状态自回归 / TDT 的起步段）
  MlSignatureRunner get decode => _decode ??= _open(decodeSignature);

  /// 模型是否带 `decode_1`（TDT 的有状态续跑签名）
  bool get hasDecode1 => signatureKeys.contains(decode1Signature);

  MlSignatureRunner get decode1 => _decode1 ??= _open(decode1Signature);

  /// 打开签名运行器并**立即分配张量**
  ///
  /// 必须显式分配：本文件的解码器在构造时就缓存了输入/输出的**就地视图**
  /// （`floatView` / `intView`），而视图依赖张量已经分配好数据区。
  /// 分配之后再调 `resizeInput` 会让视图失效，因此本引擎全程不 resize。
  MlSignatureRunner _open(String key) {
    final MlSignatureRunner runner = graph.runner(key);
    runner.allocateTensors();
    return runner;
  }

  /// 跑一次 `encode`：把特征按模型要求的定长喂进去
  ///
  /// 与 Android 一致：张量名按**位置**取（`args_0` / `output_0` 是 LiteRT 2.x Java API
  /// 给未命名张量起的名字，经典 C API 拿到的则是模型里的真实名字），因此索引顺序
  /// 才是有意义的东西，不要按名字匹配。
  void runEncode(Float32List features) {
    final MlSignatureRunner runner = encode;
    final String name = runner.inputNames.first;
    final Float32List target = _floatInput(runner, name, 'encode 输入');
    target.setAll(0, padOrTruncateFeatures(features, target.length));
    runner.invoke();
  }

  /// 把 encoder 的输出按位置拷进 [target] 标名的签名的前若干个输入
  void copyEncodeOutputsTo(MlSignatureRunner target) {
    final MlSignatureRunner source = encode;
    final List<String> from = source.outputNames;
    final List<String> to = target.inputNames;
    if (to.length < from.length) {
      throw StateError(
        '${target.key} 签名只有 ${to.length} 个输入，装不下 encoder 的 ${from.length} 个输出',
      );
    }
    for (int i = 0; i < from.length; i++) {
      final Float32List src = _floatOutput(source, from[i], 'encode 输出');
      final Float32List dst = _floatInput(target, to[i], '${target.key} 输入');
      if (src.length != dst.length) {
        throw StateError(
          'encoder 输出 ${from[i]}(${src.length}) 与 ${target.key} 输入 '
          '${to[i]}(${dst.length}) 元素个数不符',
        );
      }
      dst.setAll(0, src);
    }
  }
}

/// 把特征对齐到模型要求的定长
///
/// 短了补零（当静音），长了**留尾部** —— 官方注释说明尾部对应最近的音频，
/// 丢掉开头的老数据比丢掉新的更合理。
Float32List padOrTruncateFeatures(Float32List features, int expectedNumOfFloats) {
  if (features.length == expectedNumOfFloats) {
    return features;
  }
  if (features.length < expectedNumOfFloats) {
    final Float32List padded = Float32List(expectedNumOfFloats);
    padded.setAll(0, features);
    return padded;
  }
  return Float32List.sublistView(
    features,
    features.length - expectedNumOfFloats,
    features.length,
  );
}

/// 把 encoder 的输出解成 `(tokenId, 相对时间戳)` 序列
///
/// 三种策略在 [asrDecoderFor] 里按模型名分派。
///
/// **没有 `close()`**：Flutter 侧解码器不持有任何独立原生资源 —— 输入/输出张量
/// 都属于 [MlSignatureGraph]，随引擎一起释放。Android 侧需要 `close()` 是因为
/// `CompiledModel.createInputBuffers()` 每次新建一组独立的 `TensorBuffer`。
abstract class AsrDecoder {
  /// 解码一个窗口；`(tokenId, 时间戳)` 序列末尾以
  /// [AsrSpeech.endOfSequence] 收尾
  List<AsrStep> decode();
}

/// 默认解码器：自回归地喂 token、逐位置取 argmax。
///
/// 覆盖 Moonshine / Whisper / Qwen3-ASR —— 它们的解码都是**无状态**的
/// （每一步把「迄今所有 token」整条喂回去），靠因果掩码保证不看到未来。
class AsrDefaultDecoder implements AsrDecoder {
  AsrDefaultDecoder(this.graph) {
    final MlSignatureRunner decode = graph.decode;
    final List<String> inputs = decode.inputNames;
    if (inputs.length < 2) {
      throw StateError('decode 签名只有 ${inputs.length} 个输入，无法区分 token 与掩码');
    }
    _tokenName = inputs[inputs.length - 2];
    _maskName = inputs[inputs.length - 1];
    _logitsName = decode.outputNames.first;

    _tokenCount = decode.elementCountOf(_tokenName, input: true);
    if (_tokenCount <= 0) {
      throw StateError('decode 的 token 输入 $_tokenName 元素个数异常：$_tokenCount');
    }
    final int logitsCount = decode.elementCountOf(_logitsName, input: false);
    _logitsPerToken = logitsCount ~/ _tokenCount;
    if (_logitsPerToken <= 0) {
      throw StateError(
        'decode 的 logits 输出 $_logitsName 有 $logitsCount 个元素，'
        '装不下 $_tokenCount 个 token 的 logits',
      );
    }

    _tokens = _intInput(decode, _tokenName, 'decode 的 token 输入');
    _logits = _floatOutput(decode, _logitsName, 'decode 的 logits');
    // 因果掩码只与形状有关，一个窗口内不会变，因此只写一次
    // （Android 每窗口重写一遍，内容完全相同）
    _buildCausalMask(_floatInput(decode, _maskName, 'decode 的掩码输入'));
  }

  final AsrSignatureGraph graph;
  late final String _tokenName;
  late final String _maskName;
  late final String _logitsName;
  late final int _tokenCount;
  late final int _logitsPerToken;
  late final Int32List _tokens;
  late final Float32List _logits;

  @override
  List<AsrStep> decode() {
    final List<AsrStep> steps = <AsrStep>[];
    final AsrSignatureGraph g = graph;
    final MlSignatureRunner runner = g.decode;
    g.copyEncodeOutputsTo(runner);

    _tokens.fillRange(0, _tokenCount, 0);
    _tokens[0] = g.config.decodeStartTokenId;

    // Qwen3-ASR 会在正式文本之前吐一段引导标记，官方用「先跳过直到某个 token」处理
    bool seenSkipUntilToken = g.config.decodeSkipUntilTokenId < 0;
    final Float32List logits = _logits;

    for (int i = 0; i < _tokenCount - 1; i++) {
      runner.invoke();

      final int startIndex = i * _logitsPerToken;
      final int endIndex = startIndex + _logitsPerToken;
      final int tokenId = _argMax(logits, startIndex, endIndex) - startIndex;
      if (tokenId == g.config.decodeStopTokenId) {
        break;
      }

      if (seenSkipUntilToken) {
        steps.add((tokenId: tokenId, timestamp: AsrSpeech.noTimestamp));
      } else if (tokenId == g.config.decodeSkipUntilTokenId) {
        seenSkipUntilToken = true;
      }

      _tokens[i + 1] = tokenId;
    }

    steps.add((tokenId: AsrSpeech.endOfSequence, timestamp: AsrSpeech.noTimestamp));
    return steps;
  }

  /// 下三角因果掩码；张量是方阵、边长由元素个数开方推得
  /// （官方同样如此，因为模型侧不暴露 shape）
  void _buildCausalMask(Float32List mask) {
    mask.fillRange(0, mask.length, _maskedOutValue);
    final int n = math.sqrt(mask.length).toInt();
    for (int r = 0; r < n; r++) {
      for (int c = 0; c <= r && r * n + c < mask.length; c++) {
        mask[r * n + c] = _maskedInValue;
      }
    }
  }

  static const double _maskedInValue = 0.0;
  static const double _maskedOutValue = -0.7 * 3.4028234663852886e38; // -0.7 * Float.MAX_VALUE
}

/// CTC 解码：逐帧取最大 logit 再把**连续重复**压掉（官方 `CtcDecoder` 的等价实现）。
///
/// 用于 `parakeet-ctc-0.6b` —— 它是官方支持列表里唯一**没有**独立 decoder 子图的模型，
/// 主图直接输出 `[T × vocabSize]` 的帧级 logits，所以解码完全在宿主侧做。
///
/// ⚠️ 两处「与官方逐字一致、但不要去『修』」的地方：
/// 1. 官方 KDoc 写的是「collapse repeats -> remove blanks」，但代码里**只做了压重复**，
///    并没有把 blank 类剔除。这里保持原样 —— 换个模型/换个转换方式，blank 的下标就会变，
///    擅自「修正」反而会引入上游没有的分支。
/// 2. 时间戳就是该 token 在 logits 里的**帧下标**（CTC 每帧都有输出，天然带时间）。
class AsrCtcDecoder implements AsrDecoder {
  AsrCtcDecoder(this.graph, this.vocabSize) {
    if (vocabSize <= 0) {
      throw StateError('CTC 解码需要正的词表大小，实得 $vocabSize');
    }
  }

  final AsrSignatureGraph graph;
  final int vocabSize;

  @override
  List<AsrStep> decode() {
    final Float32List logits = _floatOutput(
      graph.encode,
      graph.encode.outputNames.first,
      'encode 的 logits 输出',
    );
    if (logits.isEmpty) {
      return <AsrStep>[
        (tokenId: AsrSpeech.endOfSequence, timestamp: AsrSpeech.noTimestamp),
      ];
    }
    return collapseDuplicates(sampleMaximum(logits, vocabSize));
  }

  /// 逐帧 argmax；[logits] 是 `[frames × vocabSize]` 的平坦数组
  static Int32List sampleMaximum(Float32List logits, int vocabSize) {
    final int numFrames = logits.length ~/ vocabSize;
    final Int32List tokenIds = Int32List(numFrames);
    for (int t = 0; t < numFrames; t++) {
      final int start = t * vocabSize;
      tokenIds[t] = _argMax(logits, start, start + vocabSize) - start;
    }
    return tokenIds;
  }

  /// 压掉连续重复的 token，时间戳取首次出现的位置
  static List<AsrStep> collapseDuplicates(Int32List tokenIds) {
    final List<AsrStep> steps = <AsrStep>[];
    int previousId = -1;
    int timestamp = 0;
    for (final int id in tokenIds) {
      if (id != previousId) {
        steps.add((tokenId: id, timestamp: timestamp));
        previousId = id;
      }
      timestamp++;
    }
    steps.add((tokenId: AsrSpeech.endOfSequence, timestamp: timestamp));
    return steps;
  }
}

/// TDT（Token-and-Duration Transducer）解码（官方 `TdtDecoder` 的等价实现）。
///
/// 用于 `parakeet-tdt-0.6b-v3` 与 `parakeet-tdt_ctc-0.6b-ja`。与 [AsrDefaultDecoder]
/// （无状态、喂整条 token 历史）相比，它有三个特点：
/// 1. **每个位置同时预测一个 token 和一个时长**，时长决定时间轴前进多少帧，
///    因此循环变量是「时间下标」而不是「token 下标」。
/// 2. **先无状态、后有状态**：序列开头用 `decode` 签名（无状态）跑满一条定长 token
///    数组以利用上下文，数组填满后切到 `decode_1` 签名（有状态），从 LSTM 隐状态续跑。
/// 3. **状态用缓冲区 ping-pong 传递**：`decode_1` 的输入状态就是上一次的输出状态。
///
/// 与官方的差异（除上面「不能零拷贝」那条之外）：
/// - 官方把 encoder 的特征维 `NUM_FEATURES` 硬编码成 1024，注释理由是
///   「CompileModel doesn't expose shape info」。实测 2.3.0 **是**暴露的
///   （`getInputTensorType(...).layout.dimensions`），所以这里改成从张量形状读
///   **最后一维**。encoder 输出是 `[1, T, F]`，末维即特征维，因此
///   `numElements / F = T` 与官方原式完全一致；只有读不到形状时才回落到常数 1024。
/// - 状态搬运改成显式拷贝（见 [decode] 里的说明）。
class AsrTdtDecoder implements AsrDecoder {
  AsrTdtDecoder(this.graph) {
    final MlSignatureRunner decode = graph.decode;
    final List<String> inputs = decode.inputNames;
    if (inputs.length < 4) {
      throw StateError('TDT 的 decode 签名只有 ${inputs.length} 个输入，装不下 encoder 输出 + token + 两个状态');
    }
    // encoder 输出 + tokenIds + state0 + state1
    _encodeOutputCount = inputs.length - 3;
    _tokenName = inputs[_encodeOutputCount];
    _stateNames = <String>[
      inputs[_encodeOutputCount + 1],
      inputs[_encodeOutputCount + 2],
    ];

    final List<int> shape = decode.inputShape(inputs.first);
    final int lastDim = shape.isEmpty ? _fallbackNumFeatures : shape.last;
    _numFeatures = lastDim > 0 ? lastDim : _fallbackNumFeatures;
    final int encodeElements = decode.elementCountOf(inputs.first, input: true);
    _maxTimeIndex = _numFeatures > 0 ? encodeElements ~/ _numFeatures : 0;
    _tokenCount = decode.elementCountOf(_tokenName, input: true);
    final int logitsElements = decode.elementCountOf(decode.outputNames.first, input: false);
    _logitsPerToken = (_tokenCount > 0 && _maxTimeIndex > 0)
        ? logitsElements ~/ _tokenCount ~/ _maxTimeIndex
        : 0;
    if (_maxTimeIndex <= 0 || _tokenCount <= 0 || _logitsPerToken <= 0) {
      throw StateError(
        'TDT 的张量形状推导失败：时间步 $_maxTimeIndex、token $_tokenCount、'
        '每 token logits $_logitsPerToken（encoder 特征维 $_numFeatures）',
      );
    }

    _blankTokenId = graph.config.decodeStartTokenId;

    _tokens = _intInput(decode, _tokenName, 'TDT 的 token 输入');
    _logits = _floatOutput(decode, decode.outputNames.first, 'TDT 的 logits');
    _decodeStateIn = <Float32List>[
      _floatInput(decode, _stateNames[0], 'TDT 状态输入 0'),
      _floatInput(decode, _stateNames[1], 'TDT 状态输入 1'),
    ];
    _decodeStateOut = <Float32List>[
      _floatOutput(decode, decode.outputNames[1], 'TDT 状态输出 0'),
      _floatOutput(decode, decode.outputNames[2], 'TDT 状态输出 1'),
    ];

    if (graph.hasDecode1) {
      final MlSignatureRunner decode1 = graph.decode1;
      final List<String> inputs1 = decode1.inputNames;
      if (inputs1.length < _encodeOutputCount + 3) {
        throw StateError('TDT 的 decode_1 签名输入个数与 decode 不一致');
      }
      _decode1Tokens =
          _intInput(decode1, inputs1[_encodeOutputCount], 'TDT decode_1 的 token 输入');
      _decode1Logits = _floatOutput(decode1, decode1.outputNames.first, 'TDT decode_1 的 logits');
      _decode1StateIn = <Float32List>[
        _floatInput(decode1, inputs1[_encodeOutputCount + 1], 'TDT decode_1 状态输入 0'),
        _floatInput(decode1, inputs1[_encodeOutputCount + 2], 'TDT decode_1 状态输入 1'),
      ];
      _decode1StateOut = <Float32List>[
        _floatOutput(decode1, decode1.outputNames[1], 'TDT decode_1 状态输出 0'),
        _floatOutput(decode1, decode1.outputNames[2], 'TDT decode_1 状态输出 1'),
      ];
    }

    // LSTM 状态的两块「挂车」缓冲：官方直接在模型的两组缓冲之间 ping-pong，
    // 这里用等长的堆缓冲复刻同一语义（见 decode() 里对 toggling 的说明）。
    _stateSlots = <List<Float32List>>[
      <Float32List>[
        Float32List(_decodeStateIn[0].length),
        Float32List(_decodeStateIn[1].length),
      ],
      <Float32List>[
        Float32List(_decodeStateIn[0].length),
        Float32List(_decodeStateIn[1].length),
      ],
    ];
  }

  final AsrSignatureGraph graph;

  late final int _encodeOutputCount;
  late final String _tokenName;
  late final List<String> _stateNames;
  late final int _numFeatures;
  late final int _maxTimeIndex;
  late final int _tokenCount;
  late final int _logitsPerToken;
  late final int _blankTokenId;

  late final Int32List _tokens;
  late final Float32List _logits;
  late final List<Float32List> _decodeStateIn;
  late final List<Float32List> _decodeStateOut;
  Int32List? _decode1Tokens;
  Float32List? _decode1Logits;
  List<Float32List>? _decode1StateIn;
  List<Float32List>? _decode1StateOut;

  /// 两块状态「槽」，交替充当输入与输出（对应官方 `statesBuffers`）
  late final List<List<Float32List>> _stateSlots;

  @override
  List<AsrStep> decode() {
    final List<AsrStep> steps = <AsrStep>[];
    final AsrSignatureGraph g = graph;

    // 初始状态全零
    for (final List<Float32List> slot in _stateSlots) {
      for (final Float32List buffer in slot) {
        buffer.fillRange(0, buffer.length, 0.0);
      }
    }

    // 先用无状态签名起步：官方认为序列开头处无状态解码的上下文更完整，质量更好
    bool stateful = false;
    MlSignatureRunner runner = g.decode;
    g.copyEncodeOutputsTo(runner);
    Int32List tokens = _tokens..fillRange(0, _tokenCount, 0);
    tokens[0] = _blankTokenId;
    int tokenCount = _tokenCount;

    // 状态的「输入/输出槽」交替：与官方 `inputStatesBuffersIndex` 同义。
    // 官方只在「已切到有状态签名**且**本步出了非 blank token」时交换，
    // 因此无状态阶段的状态输入恒为初始零 —— 这个（略怪但如实的）行为一并保留。
    int stateIndex = 0;
    int tokenIndex = 0;
    int timeIndex = 0;

    while (timeIndex < _maxTimeIndex) {
      _copyStates(
        stateful ? _decode1StateIn! : _decodeStateIn,
        _stateSlots[stateIndex],
      );
      runner.invoke();

      final Float32List logits = stateful ? _decode1Logits! : _logits;
      final int startIndexOfTimeIndex = timeIndex * tokenCount * _logitsPerToken;
      final int startIndexOfTokenId = startIndexOfTimeIndex + tokenIndex * _logitsPerToken;
      final int endIndexOfDuration = startIndexOfTimeIndex + (tokenIndex + 1) * _logitsPerToken;
      // 每个位置的 logits = 词表部分 + 时长部分，时长占最后 NUM_DURATIONS 个
      final int endIndexOfTokenId = endIndexOfDuration - _numDurations;

      final int tokenId =
          _argMax(logits, startIndexOfTokenId, endIndexOfTokenId) - startIndexOfTokenId;
      if (tokenId != _blankTokenId) {
        steps.add((tokenId: tokenId, timestamp: timeIndex));
        if (tokenCount > 1) {
          tokenIndex++;
          if (tokenIndex >= tokenCount) {
            if (!g.hasDecode1) {
              break;
            }
            // 无状态 token 数组已满 → 切到有状态解码，从 LSTM 隐状态续跑
            stateful = true;
            runner = g.decode1;
            g.copyEncodeOutputsTo(runner);
            tokens = _decode1Tokens!..fillRange(0, _decode1Tokens!.length, 0);
            tokenCount = 1;
            tokenIndex = 0;
          }
        }
        tokens[tokenIndex] = tokenId;
      }

      final int duration =
          _argMax(logits, endIndexOfTokenId, endIndexOfDuration) - endIndexOfTokenId;
      // blank 且时长为 0 时必须至少前进一帧，否则死循环
      timeIndex += (duration == 0 && tokenId == _blankTokenId) ? 1 : duration;

      if (stateful && tokenCount == 1 && tokenId != _blankTokenId) {
        // 有状态 RNN 解码器：只在**非 blank** 时接续新状态。
        // 官方注释：RNN-T 贪心解码里预测网络只在吐字时前进，blank 步接续状态
        // 等于把上一个 token 重吃一遍，会逐帧劣化转写结果。
        _copyStates(
          stateful ? _decode1StateOut! : _decodeStateOut,
          _stateSlots[1 - stateIndex],
        );
        stateIndex = 1 - stateIndex;
      }
    }

    steps.add((tokenId: AsrSpeech.endOfSequence, timestamp: _maxTimeIndex));
    return steps;
  }

  /// 把 [source]（模型内的状态张量）整块拷进 [target]（状态槽）
  void _copyStates(List<Float32List> source, List<Float32List> target) {
    for (int i = 0; i < 2; i++) {
      if (source[i].length != target[i].length) {
        throw StateError(
          'TDT 状态张量长度不符：${target[i].length} vs ${source[i].length}',
        );
      }
      target[i].setAll(0, source[i]);
    }
  }

  /// TDT 的时长分箱数（模型规格）
  static const int _numDurations = 5;

  /// 仅在读不到张量形状时使用的兜底特征维（Parakeet 系列即 1024）
  static const int _fallbackNumFeatures = 1024;
}

/// 按模型名分派解码器（官方 `preparePipeline` 里的 `when`）
///
/// 判据是**模型名里的子串**而不是某个字段，顺序也与官方一致：先 `-ctc-` 再 `-tdt`。
/// 注意 `parakeet-tdt_ctc-0.6b-ja` 虽然名字里同时出现 `tdt` 与 `ctc`，
/// 但它不含 `-ctc-` 这一子串，因此落到 [AsrTdtDecoder] —— 与官方行为一致。
AsrDecoder asrDecoderFor(AsrSignatureGraph graph, AsrHfTokenizer tokenizer) {
  final String key = graph.config.key;
  if (key.contains('-ctc-')) {
    return AsrCtcDecoder(graph, tokenizer.vocabSize);
  }
  if (key.contains('-tdt')) {
    return AsrTdtDecoder(graph);
  }
  return AsrDefaultDecoder(graph);
}

// ─────────────────── 张量读写的私有小工具 ───────────────────

/// 取 float32 **输入**张量的就地视图，并校验元素类型
Float32List _floatInput(MlSignatureRunner runner, String name, String what) {
  if (!runner.isFloat32(name, input: true)) {
    throw StateError('$what（$name）不是 float32 张量');
  }
  return runner.floatView(name, input: true);
}

/// 取 float32 **输出**张量的就地视图，并校验元素类型
Float32List _floatOutput(MlSignatureRunner runner, String name, String what) {
  if (!runner.isFloat32(name, input: false)) {
    throw StateError('$what（$name）不是 float32 张量');
  }
  return runner.floatView(name, input: false);
}

/// 取 int32 **输入**张量的就地视图，并校验元素类型
Int32List _intInput(MlSignatureRunner runner, String name, String what) {
  if (!runner.isInt32(name, input: true)) {
    throw StateError('$what（$name）不是 int32 张量');
  }
  return runner.intView(name, input: true);
}

/// 在 `[start, end)` 上取最大值的下标；区间非法或全为空时返回 `start`
int _argMax(Float32List values, int start, int end) {
  final int from = start < 0 ? 0 : start;
  final int to = end > values.length ? values.length : end;
  if (from >= to) {
    return start;
  }
  int best = from;
  double bestValue = values[from];
  for (int i = from + 1; i < to; i++) {
    if (values[i] > bestValue) {
      bestValue = values[i];
      best = i;
    }
  }
  return best;
}
