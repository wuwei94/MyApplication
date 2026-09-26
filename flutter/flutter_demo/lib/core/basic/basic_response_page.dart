import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic_control_page.dart';
import 'package:flutter_demo/core/basic/basic_demo_colors.dart';
import 'package:flutter_demo/core/basic/demo_action_list.dart';
import 'package:flutter_demo/core/basic/demo_console.dart';

/// 通信/调度类示例页面基类 — 对应 Android `BasicResponseActivity`。
///
/// 布局结构：
/// - 上方展示：暗色终端控制台（说明 + 日志 + 高频状态）
/// - 下方列表：操作列表（[buildList] / [onRecyclerClick]）
///
/// 约定与规范：
/// 1. 禁止点击控制台触发操作，所有演示行为必须由下方列表项触发。
/// 2. 页面初始说明使用 [showDescription] 居中展示；首次追加日志后说明被替换。
/// 3. 离散事件使用 [appendLog]（动作 `→ `、成功 `✓ `、失败 `✗ `）。
/// 4. 高频进度使用 [updateLog] 原位更新，禁止高频循环 [appendLog]。
/// 5. 严禁 `【成功】/【失败】` 结果装饰。
abstract class BasicResponsePage extends BasicControlPage {
  const BasicResponsePage({super.key, required super.title});
}

/// [BasicResponsePage] 的状态基类。
abstract class BasicResponsePageState<T extends BasicResponsePage>
    extends BasicControlPageState<T> {
  String? _description;
  final List<DemoLogLine> _logs = <DemoLogLine>[];
  final Map<String, String> _updatingLogs = <String, String>{};

  /// 居中显示页面初始说明。
  void showDescription(String description) {
    setState(() {
      _description = description;
    });
  }

  /// 追加单行日志（自动携带时间戳）。
  void appendLog(String message, {Color? color}) {
    setState(() {
      _logs.add(DemoLogLine(
        time: _timestamp(),
        message: message,
        color: color,
      ));
    });
  }

  /// 追加强调色单行日志。
  void appendLogAccent(String message) {
    appendLog(message, color: BasicDemoColors.consoleAccent);
  }

  /// 追加 JSON 格式化单行日志。
  void appendFormatLog(String prefix, String message) {
    appendLog('$prefix${formatDemoJson(message)}');
  }

  /// 原位更新指定 key 的运行状态；相同 key 会被替换。
  void updateLog(String key, String message) {
    setState(() {
      _updatingLogs[key] = '[${_timestamp()}] $message';
    });
  }

  /// 移除指定 key 的运行状态。
  void removeUpdatingLog(String key) {
    setState(() {
      _updatingLogs.remove(key);
    });
  }

  /// 清空全部运行状态，保留历史日志。
  void clearUpdatingLogs() {
    setState(() {
      _updatingLogs.clear();
    });
  }

  /// 清空全部历史日志与运行状态。
  void clearLog() {
    setState(() {
      _logs.clear();
      _updatingLogs.clear();
    });
  }

  @override
  Widget buildBody() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        Expanded(
          child: DemoConsole(
            description: _description,
            logs: _logs,
            updatingLogs: _updatingLogs,
            onClear: clearLog,
          ),
        ),
        DemoActionList(
          items: buildList(),
          onTap: onRecyclerClick,
          height: operationHeight,
        ),
      ],
    );
  }

  String _timestamp() {
    final DateTime now = DateTime.now();
    final String hh = now.hour.toString().padLeft(2, '0');
    final String mm = now.minute.toString().padLeft(2, '0');
    final String ss = now.second.toString().padLeft(2, '0');
    return '$hh:$mm:$ss';
  }
}
