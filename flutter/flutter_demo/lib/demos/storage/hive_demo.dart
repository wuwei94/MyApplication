import 'package:flutter_demo/core/basic/basic.dart';
import 'package:hive_flutter/hive_flutter.dart';

/// Hive — 轻量 Box 键值读写与清空
///
/// 核心机制与避坑点：
/// 1. 先初始化再开 Box：`Hive.initFlutter()` 后 `openBox` 返回 Future，首访必须 await。
/// 2. `Box<dynamic>` 读取需默认值兜底；写入类型要与读取断言一致，否则 cast 失败。
///
/// 官方参考：
/// https://pub.dev/packages/hive
class HiveDemoPage extends BasicResponsePage {
  const HiveDemoPage({super.key, required super.title});

  @override
  BasicResponsePageState<HiveDemoPage> createState() => _HiveDemoPageState();
}

class _HiveDemoPageState extends BasicResponsePageState<HiveDemoPage> {
  static const String _counterKey = 'counter';
  static const String _boxName = 'hive_demo_box';
  static Future<Box<dynamic>>? _boxFuture;

  @override
  void initState() {
    super.initState();
    showDescription('Hive 示例：Box 读取 / 写入 / 重置 / 清空');
    _loadCounter();
  }

  @override
  List<String> buildList() => const <String>[
        '1. 读取计数',
        '2. 写入计数 +1',
        '3. 重置计数',
        '4. 清空 Box',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        _loadCounter();
      case 1:
        _incrementCounter();
      case 2:
        _resetCounter();
      case 3:
        _clearBox();
    }
  }

  Future<Box<dynamic>> _requireBox() {
    return _boxFuture ??= _openBox();
  }

  Future<Box<dynamic>> _openBox() async {
    await Hive.initFlutter();
    return Hive.openBox<dynamic>(_boxName);
  }

  Future<void> _loadCounter() async {
    appendLog('→ [Read] box.get($_counterKey)');
    try {
      final Box<dynamic> box = await _requireBox();
      final dynamic raw = box.get(_counterKey, defaultValue: 0);
      final int counter = raw is int ? raw : 0;
      appendLog('✓ counter = $counter');
    } catch (error) {
      appendLog('✗ 读取失败: $error');
    }
  }

  Future<void> _incrementCounter() async {
    try {
      final Box<dynamic> box = await _requireBox();
      final dynamic raw = box.get(_counterKey, defaultValue: 0);
      final int next = (raw is int ? raw : 0) + 1;
      appendLog('→ [Write] box.put($_counterKey, $next)');
      await box.put(_counterKey, next);
      appendLog('✓ counter = $next');
    } catch (error) {
      appendLog('✗ 写入失败: $error');
    }
  }

  Future<void> _resetCounter() async {
    appendLog('→ [Delete] box.delete($_counterKey)');
    try {
      final Box<dynamic> box = await _requireBox();
      await box.delete(_counterKey);
      appendLog('✓ counter 已重置');
    } catch (error) {
      appendLog('✗ 删除失败: $error');
    }
  }

  Future<void> _clearBox() async {
    appendLog('→ [Clear] box.clear()');
    try {
      final Box<dynamic> box = await _requireBox();
      await box.clear();
      appendLog('✓ Box 已清空');
    } catch (error) {
      appendLog('✗ 清空失败: $error');
    }
  }
}
