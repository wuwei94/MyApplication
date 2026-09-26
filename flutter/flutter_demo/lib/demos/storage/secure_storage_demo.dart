import 'package:flutter_demo/core/basic/basic.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';

/// Flutter Secure Storage — 加密键值读写与重置
///
/// 核心机制与避坑点：
/// 1. 值仅 `String`：数值需自行序列化与解析，示例用计数键演示读-改-写。
/// 2. 全应用密钥空间：`deleteAll` 清空全部条目，不能按前缀批量删，慎用于共享 Keychain。
///
/// 官方参考：
/// https://pub.dev/packages/flutter_secure_storage
class SecureStorageDemoPage extends BasicResponsePage {
  const SecureStorageDemoPage({super.key, required super.title});

  @override
  BasicResponsePageState<SecureStorageDemoPage> createState() =>
      _SecureStorageDemoPageState();
}

class _SecureStorageDemoPageState
    extends BasicResponsePageState<SecureStorageDemoPage> {
  static const String _counterKey = 'counter';
  static const FlutterSecureStorage _storage = FlutterSecureStorage();

  @override
  void initState() {
    super.initState();
    showDescription('SecureStorage 示例：读取 / 写入 / 重置计数键');
    _loadCounter();
  }

  @override
  List<String> buildList() => const <String>[
        '1. 读取计数',
        '2. 写入计数 +1',
        '3. 重置计数',
        '4. 清空全部密钥',
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
        _deleteAll();
    }
  }

  Future<void> _loadCounter() async {
    appendLog('→ [Read] read(key: $_counterKey)');
    try {
      final String? value = await _storage.read(key: _counterKey);
      final int counter = int.tryParse(value ?? '') ?? 0;
      appendLog('✓ counter = $counter');
    } catch (error) {
      appendLog('✗ 读取失败: $error');
    }
  }

  Future<void> _incrementCounter() async {
    try {
      final String? value = await _storage.read(key: _counterKey);
      final int next = (int.tryParse(value ?? '') ?? 0) + 1;
      appendLog('→ [Write] write(key: $_counterKey, value: $next)');
      await _storage.write(key: _counterKey, value: '$next');
      appendLog('✓ counter = $next');
    } catch (error) {
      appendLog('✗ 写入失败: $error');
    }
  }

  Future<void> _resetCounter() async {
    appendLog('→ [Delete] delete(key: $_counterKey)');
    try {
      await _storage.delete(key: _counterKey);
      appendLog('✓ counter 已重置');
    } catch (error) {
      appendLog('✗ 删除失败: $error');
    }
  }

  Future<void> _deleteAll() async {
    appendLog('→ [Clear] deleteAll()');
    try {
      await _storage.deleteAll();
      appendLog('✓ 全部密钥已清空');
    } catch (error) {
      appendLog('✗ 清空失败: $error');
    }
  }
}
