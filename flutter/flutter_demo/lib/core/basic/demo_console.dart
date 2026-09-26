import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic_demo_colors.dart';
import 'package:flutter_demo/core/basic/basic_demo_dimens.dart';

/// 单条历史日志。
@immutable
class DemoLogLine {
  const DemoLogLine({
    required this.time,
    required this.message,
    this.color,
  });

  /// `HH:mm:ss` 时间戳前缀。
  final String time;

  /// 日志正文（不含时间戳）。
  final String message;

  /// 正文颜色；空则使用 [BasicDemoColors.consoleText]。
  final Color? color;
}

/// 终端风格控制台 — 对应 Android `BasicResponseActivity` 展示区。
///
/// 约定：
/// 1. 仅有说明时居中弱化展示；一旦写入日志，说明被运行日志替换。
/// 2. 历史日志按行追加，高频状态走 [updatingLogs] 原位替换。
/// 3. Header 右上角「清空」就近清空全部内容。
class DemoConsole extends StatelessWidget {
  const DemoConsole({
    super.key,
    required this.description,
    required this.logs,
    required this.updatingLogs,
    required this.onClear,
  });

  /// 页面初始说明；与 [logs] 互斥展示。
  final String? description;

  /// 历史日志（按时间顺序）。
  final List<DemoLogLine> logs;

  /// 高频状态：key → 展示文案，渲染时追加在历史日志之后。
  final Map<String, String> updatingLogs;

  /// 点击 Header「清空」。
  final VoidCallback onClear;

  bool get _hasContent => logs.isNotEmpty || updatingLogs.isNotEmpty;

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.all(BasicDemoDimens.itemPadding),
      decoration: BoxDecoration(
        color: BasicDemoColors.consoleBg,
        borderRadius: BorderRadius.circular(BasicDemoDimens.cornerCard),
        border: Border.all(color: BasicDemoColors.consoleBorder),
      ),
      clipBehavior: Clip.antiAlias,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: <Widget>[
          _buildHeader(context),
          const Divider(height: 1, thickness: 1, color: BasicDemoColors.consoleBorder),
          Expanded(child: _buildContent()),
        ],
      ),
    );
  }

  Widget _buildHeader(BuildContext context) {
    return Container(
      height: BasicDemoDimens.consoleHeaderHeight,
      padding: const EdgeInsets.symmetric(horizontal: BasicDemoDimens.cardPadding),
      color: BasicDemoColors.consoleHeaderBg,
      child: Row(
        children: <Widget>[
          const Text(
            '控制台日志',
            style: TextStyle(
              color: BasicDemoColors.consoleDesc,
              fontSize: BasicDemoDimens.fontBody,
              fontWeight: FontWeight.bold,
            ),
          ),
          const Spacer(),
          TextButton(
            onPressed: onClear,
            style: TextButton.styleFrom(
              padding: const EdgeInsets.all(BasicDemoDimens.compact),
              minimumSize: Size.zero,
              tapTargetSize: MaterialTapTargetSize.shrinkWrap,
            ),
            child: const Text(
              '清空',
              style: TextStyle(
                color: BasicDemoColors.consoleAccent,
                fontSize: BasicDemoDimens.fontBody,
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildContent() {
    if (!_hasContent) {
      final String text = description ?? '';
      return Center(
        child: Padding(
          padding: const EdgeInsets.all(BasicDemoDimens.pagePadding),
          child: Text(
            text,
            textAlign: TextAlign.center,
            style: const TextStyle(
              color: BasicDemoColors.consoleDesc,
              fontSize: BasicDemoDimens.fontTitle,
              height: 1.5,
            ),
          ),
        ),
      );
    }

    final List<InlineSpan> spans = <InlineSpan>[];
    for (final DemoLogLine line in logs) {
      spans.add(TextSpan(
        text: '[${line.time}] ',
        style: const TextStyle(color: BasicDemoColors.consoleTime),
      ));
      spans.add(TextSpan(
        text: '${line.message}\n',
        style: TextStyle(color: line.color ?? BasicDemoColors.consoleText),
      ));
    }
    for (final String message in updatingLogs.values) {
      spans.add(TextSpan(
        text: '$message\n',
        style: const TextStyle(color: BasicDemoColors.consoleAccent),
      ));
    }

    // 日志按行不折行：横向滚动查看超长行，与 Android HorizontalScrollView 行为一致。
    return SingleChildScrollView(
      padding: const EdgeInsets.all(BasicDemoDimens.itemPadding),
      child: SingleChildScrollView(
        scrollDirection: Axis.horizontal,
        child: RichText(
          text: TextSpan(
            style: const TextStyle(
              color: BasicDemoColors.consoleText,
              fontSize: BasicDemoDimens.fontBody,
              fontFamily: 'monospace',
              height: 1.4,
            ),
            children: spans,
          ),
        ),
      ),
    );
  }
}
