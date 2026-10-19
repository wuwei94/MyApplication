/// Laya float32 宿主解码器 —— NumPy float32 主机解码器的移植版，
/// 与 Android `LayaDecoder` 逐行对应。
library;

import 'dart:math' as math;
import 'dart:typed_data';

import 'package:flutter_demo/demos/ml/engines/laya/laya_calibration.dart';
import 'package:flutter_demo/demos/ml/engines/laya/laya_json.dart';
import 'package:flutter_demo/demos/ml/engines/laya/laya_prompt_builder.dart';
import 'package:flutter_demo/demos/ml/engines/laya/laya_tokenizer.dart';

/// float32 主机解码，复现 NumPy 的归约顺序与 Python 的舍入语义。
abstract final class LayaDecoder {
  /// 按构建器标记（marker）顺序读取各选项的 logit。
  static Float32List gather(Float32List tokenLogits, List<int> markers) {
    final Float32List out = Float32List(markers.length);
    for (int i = 0; i < markers.length; i++) {
      out[i] = tokenLogits[markers[i]];
    }
    return out;
  }

  /// 数值稳定的 softmax，使用参考实现的 float32 指数与归约。
  static Float32List softmax(Float32List values) {
    if (values.isEmpty) {
      throw ArgumentError('At least one logit is required');
    }
    double max = values[0];
    for (int i = 1; i < values.length; i++) {
      if (values[i] > max) max = values[i];
    }
    final Float32List exponents =
        Float32List(values.length);
    for (int i = 0; i < values.length; i++) {
      exponents[i] = math.exp(values[i] - max);
    }
    final double sum = layaNumpySum(exponents);
    for (int i = 0; i < exponents.length; i++) {
      exponents[i] = exponents[i] / sum;
    }
    return exponents;
  }

  /// 未经标定的原始概率为动作图（act graph）提供四个特征。
  static Float32List actFeatures(Float32List rawLogits) {
    final Float32List p = softmax(rawLogits);
    final int k = math.max(p.length, 2);
    final List<double> sorted = p.toList()..sort((double a, double b) => b.compareTo(a));
    final double top1 = sorted[0];
    final double top2 = sorted.length > 1 ? sorted[1] : 0.0;
    final Float32List terms = Float32List(p.length);
    for (int i = 0; i < p.length; i++) {
      terms[i] = p[i] * math.log(math.max(p[i], 1e-9));
    }
    final double entropy = -layaNumpySum(terms) / math.log(k.toDouble());
    return Float32List.fromList(
        <double>[top1, top1 - top2, entropy, k.toDouble() / 255.0]);
  }

  /// 在原始 softmax 之前应用选项分桶标定；温度下限为 1e-3。
  static Float32List probabilities(
    Float32List rawLogits,
    LayaQuestion question,
    LayaCalibration calibration,
  ) {
    final double temperature = math.max(
        1e-3, calibration.temperature(question.type, rawLogits.length));
    final Float32List scaled = Float32List(rawLogits.length);
    for (int i = 0; i < rawLogits.length; i++) {
      scaled[i] = rawLogits[i] / temperature;
    }
    return softmax(scaled);
  }

  /// 返回归一化熵置信度，独立于动作头的概率。
  static double confidence(Float32List p) {
    if (p.length < 2) return 1.0;
    final Float32List terms = Float32List(p.length);
    for (int i = 0; i < p.length; i++) {
      terms[i] = p[i] * math.log(p[i].clamp(1e-12, 1.0));
    }
    final double entropy = -layaNumpySum(terms);
    return (1.0 - entropy / math.log(p.length.toDouble())).clamp(0.0, 1.0);
  }

  /// Python `round(x, 4)` 口径：银行家舍入（HALF_EVEN）。
  static double round4(double value) {
    if (!value.isFinite) return value;
    final double factor = 10000.0;
    final double scaled = value * factor;
    final double floorScaled = scaled.floorToDouble();
    final double diff = scaled - floorScaled;
    final double rounded;
    if (diff < 0.5) {
      rounded = floorScaled;
    } else if (diff > 0.5) {
      rounded = floorScaled + 1.0;
    } else {
      // 恰好一半：向偶数舍入
      rounded = (floorScaled / 2.0).round() * 2.0 == floorScaled
          ? floorScaled
          : floorScaled + 1.0;
    }
    double result = rounded / factor;
    // 保留 -0.0 的符号语义（Python round(-0.00001, 4) == -0.0）
    if (result == 0.0 && value.isNegative) result = -0.0;
    return result;
  }

  /// 产出官方四位小数答案结构，且不改变题目顺序。
  static Map<String, Object?> decode(
    Float32List rawLogits,
    Float32List actLogits,
    Map<String, Object?> question,
    LayaCalibration calibration,
  ) =>
      decodeQuestion(rawLogits, actLogits,
          LayaPromptBuilder.normalizeStatic(question), calibration);

  /// 产出官方四位小数答案结构，且不改变题目顺序。
  static Map<String, Object?> decodeQuestion(
    Float32List rawLogits,
    Float32List actLogits,
    LayaQuestion question,
    LayaCalibration calibration,
  ) {
    if (rawLogits.length != question.optionCount) {
      throw ArgumentError('Logits/criteria option count mismatch');
    }
    if (actLogits.length != 2) {
      throw ArgumentError('Act head must return two logits');
    }
    final Float32List p = probabilities(rawLogits, question, calibration);
    final Map<String, Object?> action = <String, Object?>{
      'act_probability': round4(softmax(actLogits)[0].toDouble()),
    };
    switch (question.type) {
      case 'choice':
        final List<String> labels = layaAsObject(question.criteria).keys.toList();
        int argmax = 0;
        for (int index = 1; index < p.length; index++) {
          if (p[index] > p[argmax]) argmax = index;
        }
        return <String, Object?>{
          'type': 'choice',
          'choice': labels[argmax],
          'probabilities': <String, Object?>{
            for (int i = 0; i < labels.length; i++) labels[i]: round4(p[i].toDouble()),
          },
          'confidence': round4(confidence(p)),
          'action': action,
        };
      case 'score':
        final List<Object?> criteria = layaAsArray(question.criteria);
        // NumPy arange(int64)*float32 会把每次乘法与归约提升为 float64。
        double expectation = 0.0;
        for (int i = 0; i < p.length; i++) {
          expectation += i.toDouble() * p[i].toDouble();
        }
        return <String, Object?>{
          'type': 'score',
          'score': round4(expectation),
          'legend': <String, Object?>{
            for (int i = 0; i < criteria.length; i++) '$i': criteria[i],
          },
          'probabilities': <String, Object?>{
            for (int i = 0; i < p.length; i++) '$i': round4(p[i].toDouble()),
          },
          'confidence': round4(confidence(p)),
          'action': action,
        };
      case 'noul':
        final double trueProbability = p[1].toDouble();
        return <String, Object?>{
          'type': 'noul',
          'noul': round4(trueProbability),
          'confidence': round4(math.max(trueProbability, 1.0 - trueProbability)),
          'action': action,
        };
      default:
        throw ArgumentError('Unknown question type: ${question.type}');
    }
  }
}
