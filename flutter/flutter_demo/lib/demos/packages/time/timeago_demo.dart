import 'package:flutter_demo/core/basic/basic.dart';
import 'package:intl/intl.dart';
import 'package:timeago/timeago.dart' as timeago;

/// Timeago — 相对时间文案
///
/// 核心机制与避坑点：
/// 1. 消息注册：中文文案需先 `setLocaleMessages`，英文开箱即用。
/// 2. 基准时间：`clock` + `allowFromNow` 可同时展示过去与未来相对时间。
///
/// 官方参考：
/// https://pub.dev/packages/timeago
class TimeagoDemoPage extends BasicResponsePage {
  const TimeagoDemoPage({super.key, required super.title});

  @override
  BasicResponsePageState<TimeagoDemoPage> createState() =>
      _TimeagoDemoPageState();
}

class _TimeagoDemoPageState extends BasicResponsePageState<TimeagoDemoPage> {
  static bool _localeMessagesRegistered = false;
  static final DateFormat _exactTimeFormatter = DateFormat(
    'yyyy-MM-dd HH:mm:ss',
  );

  late DateTime _referenceTime;

  @override
  void initState() {
    super.initState();
    showDescription('Timeago 示例：DateTime 转相对时间文案');
    _registerLocaleMessages();
    _referenceTime = DateTime.now();
  }

  void _registerLocaleMessages() {
    if (_localeMessagesRegistered) {
      return;
    }
    timeago.setLocaleMessages('zh', timeago.ZhMessages());
    timeago.setLocaleMessages('zh_CN', timeago.ZhCnMessages());
    _localeMessagesRegistered = true;
  }

  @override
  List<String> buildList() => const <String>[
        '1. 按 zh_CN 格式化样例',
        '2. 按 en 格式化样例',
        '3. 刷新基准时间',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        _formatSamples('zh_CN');
      case 1:
        _formatSamples('en');
      case 2:
        _refreshReferenceTime();
    }
  }

  void _formatSamples(String localeCode) {
    appendLog('→ [Format] locale=$localeCode clock=${_exact(_referenceTime)}');
    final List<({String label, DateTime time})> samples =
        <({String label, DateTime time})>[
      (
        label: '20 秒前',
        time: _referenceTime.subtract(const Duration(seconds: 20)),
      ),
      (
        label: '5 分钟前',
        time: _referenceTime.subtract(const Duration(minutes: 5)),
      ),
      (
        label: '2 小时前',
        time: _referenceTime.subtract(const Duration(hours: 2)),
      ),
      (
        label: '1 天前',
        time: _referenceTime.subtract(const Duration(days: 1, hours: 3)),
      ),
      (
        label: '3 小时后',
        time: _referenceTime.add(const Duration(hours: 3)),
      ),
    ];

    for (final ({String label, DateTime time}) sample in samples) {
      final String relative = timeago.format(
        sample.time,
        locale: localeCode,
        clock: _referenceTime,
        allowFromNow: true,
      );
      appendLog('✓ ${sample.label} → $relative');
    }
  }

  void _refreshReferenceTime() {
    _referenceTime = DateTime.now();
    appendLog('✓ 基准时间已刷新: ${_exact(_referenceTime)}');
  }

  String _exact(DateTime dateTime) {
    return _exactTimeFormatter.format(dateTime);
  }
}
