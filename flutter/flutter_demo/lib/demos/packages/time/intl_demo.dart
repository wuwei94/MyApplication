import 'package:flutter_demo/core/basic/basic.dart';
import 'package:intl/date_symbol_data_local.dart';
import 'package:intl/intl.dart';

/// Intl — 日期时间本地化格式化
///
/// 核心机制与避坑点：
/// 1. 初始化：`initializeDateFormatting` 必须先于非默认 locale 的 DateFormat 调用完成。
/// 2. 格式契约：同一 DateTime 在不同 locale 下输出形态不同，用固定样例对比最直观。
///
/// 官方参考：
/// https://pub.dev/packages/intl
class IntlDemoPage extends BasicResponsePage {
  const IntlDemoPage({super.key, required super.title});

  @override
  BasicResponsePageState<IntlDemoPage> createState() => _IntlDemoPageState();
}

class _IntlDemoPageState extends BasicResponsePageState<IntlDemoPage> {
  static Future<void>? _initializationFuture;
  late DateTime _sampleDate;

  @override
  void initState() {
    super.initState();
    showDescription('Intl 示例：DateFormat 中英文格式化对比');
    _sampleDate = DateTime.now();
    _ensureInitialized();
  }

  Future<void> _ensureInitialized() {
    _initializationFuture ??= _initializeFormattingData();
    return _initializationFuture!;
  }

  Future<void> _initializeFormattingData() async {
    await initializeDateFormatting('zh_CN');
    await initializeDateFormatting('en_US');
  }

  @override
  List<String> buildList() => const <String>[
        '1. 按 zh_CN 格式化',
        '2. 按 en_US 格式化',
        '3. 刷新样例时间',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        _formatWithLocale('zh_CN');
      case 1:
        _formatWithLocale('en_US');
      case 2:
        _refreshSampleDate();
    }
  }

  Future<void> _formatWithLocale(String localeTag) async {
    appendLog('→ [Format] locale=$localeTag');
    try {
      await _ensureInitialized();
      final DateFormat full = DateFormat.yMMMMEEEEd(localeTag).add_Hms();
      final DateFormat short = DateFormat.yMd(localeTag);
      final DateFormat time = DateFormat.jm(localeTag);
      appendLog('✓ canonicalized=${Intl.canonicalizedLocale(localeTag)}');
      appendLog('✓ yMMMMEEEEd+Hms=${full.format(_sampleDate)}');
      appendLog('✓ yMd=${short.format(_sampleDate)}');
      appendLog('✓ jm=${time.format(_sampleDate)}');
    } catch (error) {
      appendLog('✗ 格式化失败: $error');
    }
  }

  void _refreshSampleDate() {
    _sampleDate = DateTime.now();
    appendLog('✓ 样例时间已刷新: ${_sampleDate.toIso8601String()}');
  }
}
