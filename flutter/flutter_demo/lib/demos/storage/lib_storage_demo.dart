import 'package:flutter_demo/core/basic/basic.dart';
import 'package:lib_storage/lib_storage.dart';

/// lib_storage — IStorage 统一接口与 Storage 门面内核切换
///
/// 核心机制与避坑点：
/// 1. 门面模式：业务只依赖 `Storage`，替换 `Storage.kernel` 即可切换 SharedPreferences / Hive 后端。
/// 2. 敏感数据不走本门面（统一 SecureStorage）；示例退出时恢复默认 Hive 内核，避免污染其它页面。
///
/// 本地 package：
/// ../flutter_libs/lib_storage
class LibStorageDemoPage extends BasicResponsePage {
  const LibStorageDemoPage({super.key, required super.title});

  @override
  BasicResponsePageState<LibStorageDemoPage> createState() =>
      _LibStorageDemoPageState();
}

class _LibStorageDemoPageState
    extends BasicResponsePageState<LibStorageDemoPage> {
  static const String _counterKey = 'lib_storage_counter';

  @override
  void initState() {
    super.initState();
    showDescription('lib_storage 示例：读取 / 写入 / 切换内核 / 清空');
    _loadCounter();
  }

  @override
  void dispose() {
    // 示例页退出后恢复默认内核，避免影响其它示例页面。
    Storage.kernel = const HiveStorage();
    super.dispose();
  }

  @override
  List<String> buildList() => const <String>[
        '1. 读取计数',
        '2. 写入计数 +1',
        '3. 重置计数',
        '4. 切换内核',
        '5. 清空全部',
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
        _switchKernel();
      case 4:
        _clearAll();
    }
  }

  String get _kernelName {
    return Storage.kernel is SharedPreferencesStorage
        ? 'SharedPreferencesStorage'
        : 'HiveStorage';
  }

  Future<void> _loadCounter() async {
    appendLog('→ [Read] Storage.getValue($_counterKey) kernel=$_kernelName');
    try {
      final int counter = await Storage.getValue<int>(_counterKey, 0);
      appendLog('✓ counter = $counter');
    } catch (error) {
      appendLog('✗ 读取失败: $error');
    }
  }

  Future<void> _incrementCounter() async {
    try {
      final int counter = await Storage.getValue<int>(_counterKey, 0);
      final int next = counter + 1;
      appendLog('→ [Write] Storage.setValue($_counterKey, $next)');
      await Storage.setValue(_counterKey, next);
      appendLog('✓ counter = $next');
    } catch (error) {
      appendLog('✗ 写入失败: $error');
    }
  }

  Future<void> _resetCounter() async {
    appendLog('→ [Remove] Storage.remove($_counterKey)');
    try {
      await Storage.remove(_counterKey);
      appendLog('✓ counter 已重置');
    } catch (error) {
      appendLog('✗ 删除失败: $error');
    }
  }

  void _switchKernel() {
    final IStorage next = Storage.kernel is SharedPreferencesStorage
        ? const HiveStorage()
        : const SharedPreferencesStorage();
    Storage.kernel = next;
    appendLog('→ [Switch] Storage.kernel = $_kernelName');
    appendLog('✓ 内核已切换，后续读写走新后端');
  }

  Future<void> _clearAll() async {
    appendLog('→ [Clear] Storage.clearAll()');
    try {
      await Storage.clearAll();
      appendLog('✓ 当前内核数据已清空');
    } catch (error) {
      appendLog('✗ 清空失败: $error');
    }
  }
}
