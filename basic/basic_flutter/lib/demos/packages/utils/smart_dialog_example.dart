import 'dart:async';

import 'package:basic_flutter/core/utils/ui/smart_dialog.dart';
import 'package:flutter/material.dart';

/// FlutterSmartDialog
/// https://pub.dev/packages/flutter_smart_dialog
class SmartDialogDemoPage extends StatelessWidget {
  const SmartDialogDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return SmartDialogDemoView(title: title);
  }
}

class SmartDialogDemoView extends StatefulWidget {
  const SmartDialogDemoView({super.key, required this.title});

  final String title;

  @override
  State<SmartDialogDemoView> createState() => _SmartDialogDemoViewState();
}

class _SmartDialogDemoViewState extends State<SmartDialogDemoView> {
  final List<_SmartDialogActionRecord> _records = <_SmartDialogActionRecord>[];

  Future<void> _showToastDemo() async {
    unawaited(AppSmartDialog.showToast('这是一条全局 toast，无需传入 BuildContext。'));
    _addRecord(
      title: 'Toast',
      detail: '已触发一条全局轻提示，可直接在业务逻辑或请求回调里调用。',
      accentColor: const Color(0xFF0F766E),
      icon: Icons.mark_chat_unread_outlined,
    );
  }

  Future<void> _showLoadingDemo() async {
    unawaited(AppSmartDialog.showLoading(message: '正在模拟提交网络请求...'));
    _addRecord(
      title: 'Loading',
      detail: 'loading 已展示，1.6 秒后自动关闭，并补一条结果提示。',
      accentColor: const Color(0xFF1D4ED8),
      icon: Icons.hourglass_top_rounded,
    );

    await Future<void>.delayed(const Duration(milliseconds: 1600));
    await AppSmartDialog.dismissLoading();
    unawaited(AppSmartDialog.showToast('请求完成，loading 已关闭。'));

    if (!mounted) {
      return;
    }

    _addRecord(
      title: 'Loading Closed',
      detail: '通过统一 helper 关闭 loading，常用于网络请求 finally 阶段。',
      accentColor: const Color(0xFF4338CA),
      icon: Icons.cloud_done_outlined,
    );
  }

  Future<void> _showConfirmDemo() async {
    final bool? confirmed = await AppSmartDialog.showConfirm(
      title: '确认执行批量操作？',
      message: '这个弹窗来自 flutter_smart_dialog.show(...)，点击按钮后会通过返回值回传结果。',
      confirmText: '继续',
      cancelText: '先看看',
    );

    if (!mounted) {
      return;
    }

    final bool accepted = confirmed ?? false;
    unawaited(AppSmartDialog.showToast(accepted ? '已确认执行。' : '本次操作已取消。'));
    _addRecord(
      title: accepted ? 'Dialog Confirmed' : 'Dialog Cancelled',
      detail: accepted ? '用户点击了继续。' : '用户点击了取消或关闭弹窗。',
      accentColor: accepted ? const Color(0xFF166534) : const Color(0xFF9A3412),
      icon: accepted ? Icons.task_alt_rounded : Icons.close_rounded,
    );
  }

  Future<void> _showCustomDialogDemo() async {
    final String? result = await AppSmartDialog.showCustomDialog<String>(
      builder: (BuildContext context) {
        return const _PackageInfoDialog();
      },
    );

    if (!mounted) {
      return;
    }

    _addRecord(
      title: 'Custom Dialog',
      detail: '自定义弹窗已关闭，结果：${result ?? '点击遮罩关闭'}。',
      accentColor: const Color(0xFF7C3AED),
      icon: Icons.layers_outlined,
    );
  }

  void _addRecord({
    required String title,
    required String detail,
    required Color accentColor,
    required IconData icon,
  }) {
    setState(() {
      _records.insert(
        0,
        _SmartDialogActionRecord(
          title: title,
          detail: detail,
          accentColor: accentColor,
          icon: icon,
          happenedAt: DateTime.now(),
        ),
      );
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(widget.title)),
      body: getBody(),
    );
  }

  Widget getBody() {
    final ThemeData theme = Theme.of(context);

    return ListView(
      padding: const EdgeInsets.fromLTRB(16, 16, 16, 24),
      children: <Widget>[
        _OverviewCard(theme: theme),
        const SizedBox(height: 16),
        _ActionGrid(
          onShowToast: _showToastDemo,
          onShowLoading: _showLoadingDemo,
          onShowConfirmDialog: _showConfirmDemo,
          onShowCustomDialog: _showCustomDialogDemo,
        ),
        const SizedBox(height: 20),
        Text(
          '最近操作',
          style: theme.textTheme.titleMedium?.copyWith(
            fontWeight: FontWeight.w700,
          ),
        ),
        const SizedBox(height: 12),
        if (_records.isEmpty)
          const _EmptyState()
        else
          for (final _SmartDialogActionRecord record in _records)
            Padding(
              padding: const EdgeInsets.only(bottom: 12),
              child: _ActionRecordCard(record: record),
            ),
      ],
    );
  }
}

class _OverviewCard extends StatelessWidget {
  const _OverviewCard({required this.theme});

  final ThemeData theme;

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        gradient: const LinearGradient(
          colors: <Color>[Color(0xFF0F172A), Color(0xFF1D4ED8)],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
        borderRadius: BorderRadius.circular(28),
        boxShadow: const <BoxShadow>[
          BoxShadow(
            color: Color(0x220F172A),
            blurRadius: 24,
            offset: Offset(0, 14),
          ),
        ],
      ),
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Text(
              'flutter_smart_dialog 已接入到应用入口',
              style: theme.textTheme.titleLarge?.copyWith(
                color: Colors.white,
                fontWeight: FontWeight.w700,
              ),
            ),
            const SizedBox(height: 8),
            Text(
              '这个示例页用统一 helper 演示 toast、loading 和 custom dialog。'
              '项目已经在 MaterialApp.router builder 与双路由 observer 上完成全局接入。',
              style: theme.textTheme.bodyMedium?.copyWith(
                color: Colors.white.withValues(alpha: 0.86),
                height: 1.45,
              ),
            ),
            const SizedBox(height: 18),
            const Wrap(
              spacing: 10,
              runSpacing: 10,
              children: <Widget>[
                _FeatureChip(label: 'Global Builder'),
                _FeatureChip(label: 'GoRouter Observer'),
                _FeatureChip(label: 'AutoRoute Observer'),
                _FeatureChip(label: 'No Context Toast'),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

class _ActionGrid extends StatelessWidget {
  const _ActionGrid({
    required this.onShowToast,
    required this.onShowLoading,
    required this.onShowConfirmDialog,
    required this.onShowCustomDialog,
  });

  final Future<void> Function() onShowToast;
  final Future<void> Function() onShowLoading;
  final Future<void> Function() onShowConfirmDialog;
  final Future<void> Function() onShowCustomDialog;

  @override
  Widget build(BuildContext context) {
    return Column(
      children: <Widget>[
        Row(
          children: <Widget>[
            Expanded(
              child: _ActionCard(
                title: 'Show Toast',
                description: '统一轻提示样式',
                icon: Icons.campaign_outlined,
                accentColor: const Color(0xFF0F766E),
                onTap: onShowToast,
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: _ActionCard(
                title: 'Show Loading',
                description: '模拟一次请求 loading',
                icon: Icons.cloud_upload_outlined,
                accentColor: const Color(0xFF1D4ED8),
                onTap: onShowLoading,
              ),
            ),
          ],
        ),
        const SizedBox(height: 12),
        Row(
          children: <Widget>[
            Expanded(
              child: _ActionCard(
                title: 'Confirm Dialog',
                description: '带返回值的确认弹窗',
                icon: Icons.forum_outlined,
                accentColor: const Color(0xFF166534),
                onTap: onShowConfirmDialog,
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: _ActionCard(
                title: 'Custom Dialog',
                description: '自定义内容面板',
                icon: Icons.layers_clear_outlined,
                accentColor: const Color(0xFF7C3AED),
                onTap: onShowCustomDialog,
              ),
            ),
          ],
        ),
      ],
    );
  }
}

class _ActionCard extends StatelessWidget {
  const _ActionCard({
    required this.title,
    required this.description,
    required this.icon,
    required this.accentColor,
    required this.onTap,
  });

  final String title;
  final String description;
  final IconData icon;
  final Color accentColor;
  final Future<void> Function() onTap;

  @override
  Widget build(BuildContext context) {
    return Material(
      color: Colors.transparent,
      child: InkWell(
        borderRadius: BorderRadius.circular(22),
        onTap: onTap,
        child: Ink(
          decoration: BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.circular(22),
            border: Border.all(color: accentColor.withValues(alpha: 0.18)),
            boxShadow: const <BoxShadow>[
              BoxShadow(
                color: Color(0x140F172A),
                blurRadius: 20,
                offset: Offset(0, 10),
              ),
            ],
          ),
          child: Padding(
            padding: const EdgeInsets.all(18),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: <Widget>[
                Container(
                  width: 44,
                  height: 44,
                  decoration: BoxDecoration(
                    color: accentColor.withValues(alpha: 0.12),
                    borderRadius: BorderRadius.circular(14),
                  ),
                  alignment: Alignment.center,
                  child: Icon(icon, color: accentColor),
                ),
                const SizedBox(height: 16),
                Text(
                  title,
                  style: Theme.of(context).textTheme.titleMedium?.copyWith(
                    fontWeight: FontWeight.w700,
                  ),
                ),
                const SizedBox(height: 6),
                Text(
                  description,
                  style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                    color: const Color(0xFF64748B),
                    height: 1.4,
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}

class _FeatureChip extends StatelessWidget {
  const _FeatureChip({required this.label});

  final String label;

  @override
  Widget build(BuildContext context) {
    return DecoratedBox(
      decoration: BoxDecoration(
        color: Colors.white.withValues(alpha: 0.14),
        borderRadius: BorderRadius.circular(999),
      ),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
        child: Text(
          label,
          style: const TextStyle(
            color: Colors.white,
            fontSize: 12,
            fontWeight: FontWeight.w600,
          ),
        ),
      ),
    );
  }
}

class _EmptyState extends StatelessWidget {
  const _EmptyState();

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: const Color(0xFFF8FAFC),
        borderRadius: BorderRadius.circular(22),
        border: Border.all(color: const Color(0xFFE2E8F0)),
      ),
      child: const Text(
        '点击上面的任一动作，看看 toast、loading 和 dialog 的实际效果。',
        style: TextStyle(color: Color(0xFF475569), height: 1.5),
      ),
    );
  }
}

class _ActionRecordCard extends StatelessWidget {
  const _ActionRecordCard({required this.record});

  final _SmartDialogActionRecord record;

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: const Color(0xFFE2E8F0)),
      ),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Container(
              width: 42,
              height: 42,
              decoration: BoxDecoration(
                color: record.accentColor.withValues(alpha: 0.12),
                borderRadius: BorderRadius.circular(14),
              ),
              alignment: Alignment.center,
              child: Icon(record.icon, color: record.accentColor),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: <Widget>[
                  Text(
                    record.title,
                    style: Theme.of(context).textTheme.titleSmall?.copyWith(
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                  const SizedBox(height: 6),
                  Text(
                    record.detail,
                    style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                      color: const Color(0xFF475569),
                      height: 1.45,
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(width: 12),
            Text(
              record.formattedTime,
              style: Theme.of(
                context,
              ).textTheme.bodySmall?.copyWith(color: const Color(0xFF94A3B8)),
            ),
          ],
        ),
      ),
    );
  }
}

class _PackageInfoDialog extends StatelessWidget {
  const _PackageInfoDialog();

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return Center(
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: ConstrainedBox(
          constraints: const BoxConstraints(maxWidth: 360),
          child: Material(
            color: Colors.white,
            borderRadius: BorderRadius.circular(28),
            clipBehavior: Clip.antiAlias,
            child: Padding(
              padding: const EdgeInsets.all(24),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                crossAxisAlignment: CrossAxisAlignment.start,
                children: <Widget>[
                  Text(
                    '这个示例演示了什么？',
                    style: theme.textTheme.titleLarge?.copyWith(
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                  const SizedBox(height: 12),
                  Text(
                    '1. toast 与 loading 通过全局 builder 统一样式。\n'
                    '2. dialog 通过 helper 包了一层，业务侧不直接散落 SmartDialog 调用。\n'
                    '3. GoRouter 与 AutoRoute 都挂了 observer，路由切换时弹层行为更稳定。',
                    style: theme.textTheme.bodyMedium?.copyWith(
                      color: const Color(0xFF475569),
                      height: 1.55,
                    ),
                  ),
                  const SizedBox(height: 24),
                  Align(
                    alignment: Alignment.centerRight,
                    child: FilledButton(
                      onPressed: () {
                        AppSmartDialog.dismissCustomDialog<String>(
                          result: '点击了我知道了',
                        );
                      },
                      child: const Text('我知道了'),
                    ),
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }
}

class _SmartDialogActionRecord {
  const _SmartDialogActionRecord({
    required this.title,
    required this.detail,
    required this.accentColor,
    required this.icon,
    required this.happenedAt,
  });

  final String title;
  final String detail;
  final Color accentColor;
  final IconData icon;
  final DateTime happenedAt;

  String get formattedTime {
    final String hour = happenedAt.hour.toString().padLeft(2, '0');
    final String minute = happenedAt.minute.toString().padLeft(2, '0');
    final String second = happenedAt.second.toString().padLeft(2, '0');
    return '$hour:$minute:$second';
  }
}
