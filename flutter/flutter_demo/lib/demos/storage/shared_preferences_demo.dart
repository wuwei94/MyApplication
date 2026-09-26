import 'package:flutter_demo/core/basic/basic.dart';
import 'package:shared_preferences/shared_preferences.dart';

/// SharedPreferences — 键值读写与重置
///
/// 核心机制与避坑点：
/// 1. 异步 API：`SharedPreferencesAsync` 全链路 Future，读写后必须等结果再刷新 UI。
/// 2. 进程内缓存：同 key 连续写入以最后一次为准；示例用计数键演示读-改-写。
///
/// 官方参考：
/// https://pub.dev/packages/shared_preferences
class SharedPreferencesDemoPage extends BasicResponsePage {
  const SharedPreferencesDemoPage({super.key, required super.title});

  @override
  BasicResponsePageState<SharedPreferencesDemoPage> createState() =>
      _SharedPreferencesDemoPageState();
}

class _SharedPreferencesDemoPageState
    extends BasicResponsePageState<SharedPreferencesDemoPage> {
  static const String _counterKey = 'counter';
  static final SharedPreferencesAsync _prefs = SharedPreferencesAsync();

  @override
  void initState() {
    super.initState();
    showDescription('SharedPreferences 示例：读取 / 写入 / 重置计数键');
    _loadCounter();
  }

  @override
  List<String> buildList() => const <String>[
        '1. 读取计数',
        '2. 写入计数 +1',
        '3. 重置计数',
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
    }
  }

  Future<void> _loadCounter() async {
    appendLog('→ [Read] getInt($_counterKey)');
    final int counter = await _prefs.getInt(_counterKey) ?? 0;
    appendLog('✓ counter = $counter');
  }

  Future<void> _incrementCounter() async {
    final int counter = await _prefs.getInt(_counterKey) ?? 0;
    final int next = counter + 1;
    appendLog('→ [Write] setInt($_counterKey, $next)');
    await _prefs.setInt(_counterKey, next);
    appendLog('✓ counter = $next');
  }

  Future<void> _resetCounter() async {
    appendLog('→ [Remove] remove($_counterKey)');
    await _prefs.remove(_counterKey);
    appendLog('✓ counter 已重置');
  }
}
