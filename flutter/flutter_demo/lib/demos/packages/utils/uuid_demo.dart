import 'package:flutter_demo/core/basic/basic.dart';
import 'package:uuid/uuid.dart';

/// Uuid — 生成 / 校验 / 解析
///
/// 核心机制与避坑点：
/// 1. 版本语义：v1 时间戳、v4 随机、v5 命名空间+哈希、v7 时间序；相同 v5 输入结果稳定。
/// 2. 校验契约：`Uuid.isValidUUID` 判定字符串合法性，`parseAsByteList` / `unparse` 完成往返。
///
/// 官方参考：
/// https://pub.dev/packages/uuid
class UuidDemoPage extends BasicResponsePage {
  const UuidDemoPage({super.key, required super.title});

  @override
  BasicResponsePageState<UuidDemoPage> createState() => _UuidDemoPageState();
}

class _UuidDemoPageState extends BasicResponsePageState<UuidDemoPage> {
  static const Uuid _uuid = Uuid();
  static const String _defaultV5Name = 'flutter_demo';

  String? _lastValue;

  @override
  void initState() {
    super.initState();
    showDescription('Uuid 示例：生成 v1/v4/v5/v7，校验并解析字节');
  }

  @override
  List<String> buildList() => const <String>[
        '1. 生成 v1 UUID',
        '2. 生成 v4 UUID',
        '3. 生成 v5 UUID',
        '4. 生成 v7 UUID',
        '5. 校验并解析最新 UUID',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        _generateV1();
      case 1:
        _generateV4();
      case 2:
        _generateV5();
      case 3:
        _generateV7();
      case 4:
        _validateLast();
    }
  }

  void _generateV1() {
    final String value = _uuid.v1();
    _lastValue = value;
    appendLog('→ [v1] Uuid.v1()');
    appendLog('✓ $value');
  }

  void _generateV4() {
    final String value = _uuid.v4();
    _lastValue = value;
    appendLog('→ [v4] Uuid.v4()');
    appendLog('✓ $value');
  }

  void _generateV5() {
    final String value = _uuid.v5(Namespace.url.value, _defaultV5Name);
    _lastValue = value;
    appendLog('→ [v5] Uuid.v5(Namespace.url, $_defaultV5Name)');
    appendLog('✓ $value');
  }

  void _generateV7() {
    final String value = _uuid.v7();
    _lastValue = value;
    appendLog('→ [v7] Uuid.v7()');
    appendLog('✓ $value');
  }

  void _validateLast() {
    final String? value = _lastValue;
    if (value == null) {
      appendLog('✗ 尚未生成 UUID，请先执行生成操作');
      return;
    }

    appendLog('→ [validate] Uuid.isValidUUID(fromString: $value)');
    final bool isValid = Uuid.isValidUUID(fromString: value);
    if (!isValid) {
      appendLog('✗ 不是合法 UUID');
      return;
    }

    final List<int> bytes = Uuid.parseAsByteList(value);
    final String normalized = Uuid.unparse(bytes);
    final String bytePreview = bytes
        .map((int b) => b.toRadixString(16).padLeft(2, '0'))
        .join(' ');
    appendLog('✓ valid=true bytes=$bytePreview');
    appendLog('✓ unparse=$normalized');
  }
}
