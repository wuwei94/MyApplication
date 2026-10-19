/// Laya 标定表 —— 只用到温度（temperature）字段，与 Android `LayaCalibration` 对应。
library;

import 'dart:io';

import 'package:flutter_demo/demos/ml/engines/laya/laya_json.dart';

class LayaCalibration {
  const LayaCalibration({
    this.temperatures = const <double>[1.0, 1.0, 1.0],
    this.byOptions = const <String, double>{},
  });

  final List<double> temperatures;
  final Map<String, double> byOptions;

  /// 有选项数分桶时用分桶温度，否则用题型温度。
  double temperature(String qtype, int k) {
    final double? bucketed = byOptions[bucket(qtype, k)];
    if (bucketed != null) return bucketed;
    switch (qtype) {
      case 'choice':
        return temperatures[0];
      case 'score':
        return temperatures[1];
      case 'noul':
        return temperatures[2];
      default:
        throw ArgumentError('Unknown question type: $qtype');
    }
  }

  /// 恒等标定：不校准 logits，用于 T=1 参考口径。
  static const LayaCalibration identity = LayaCalibration();

  /// 从已安装的 JSON 文件加载标定。
  static Future<LayaCalibration> load(File file) async =>
      fromMap(layaAsObject(layaParse(file.readAsStringSync())));

  /// 只读温度字段；无关的来源字段保持未用。
  static LayaCalibration fromMap(Map<String, Object?> config) {
    final Object? rawTemperature = config['temperature'];
    final List<double> defaults = rawTemperature is List
        ? rawTemperature.map((Object? value) => (value as num).toDouble()).toList()
        : const <double>[1.0, 1.0, 1.0];
    if (defaults.length != 3) {
      throw ArgumentError('Expected choice/score/noul temperatures');
    }
    final Object? rawBuckets = config['temperature_by_options'];
    final Map<String, double> buckets = rawBuckets is Map
        ? rawBuckets.map((Object? key, Object? value) =>
            MapEntry<String, double>('$key', (value as num).toDouble()))
        : const <String, double>{};
    return LayaCalibration(temperatures: defaults, byOptions: buckets);
  }

  /// 匹配宿主契约的两/五/十选项分桶边界。
  static String bucket(String qtype, int k) {
    final String suffix;
    if (k <= 2) {
      suffix = '2';
    } else if (k <= 5) {
      suffix = '3-5';
    } else if (k <= 10) {
      suffix = '6-10';
    } else {
      suffix = '11+';
    }
    return '$qtype:$suffix';
  }
}
