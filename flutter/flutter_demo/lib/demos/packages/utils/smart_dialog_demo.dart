import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';
import 'package:flutter_demo/core/utils/ui/smart_dialog.dart';

/// FlutterSmartDialog — 全局 Toast / Loading / Dialog 纯操作触发
///
/// 核心机制与避坑点：
/// 1. 全局弹层：`AppSmartDialog` 统一入口，toast / loading / dialog 均无需传入 BuildContext。
/// 2. 触发边界：所有演示行为由下方操作列表触发，禁止页面内隐藏手势。
///
/// 官方参考：
/// https://pub.dev/packages/flutter_smart_dialog
class SmartDialogDemoPage extends BasicControlPage {
  const SmartDialogDemoPage({super.key, required super.title});

  @override
  BasicControlPageState<SmartDialogDemoPage> createState() =>
      _SmartDialogDemoPageState();
}

class _SmartDialogDemoPageState
    extends BasicControlPageState<SmartDialogDemoPage> {
  @override
  List<String> buildList() => const <String>[
        '1. 显示全局 Toast',
        '2. 显示并关闭 Loading',
        '3. 弹出确认对话框',
        '4. 弹出自定义对话框',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        _showToastDemo();
      case 1:
        _showLoadingDemo();
      case 2:
        _showConfirmDemo();
      case 3:
        _showCustomDialogDemo();
    }
  }

  void _showToastDemo() {
    unawaited(AppSmartDialog.showToast('这是一条全局 toast，无需传入 BuildContext。'));
  }

  Future<void> _showLoadingDemo() async {
    unawaited(AppSmartDialog.showLoading(message: '正在模拟提交网络请求...'));
    await Future<void>.delayed(const Duration(milliseconds: 1600));
    await AppSmartDialog.dismissLoading();
    unawaited(AppSmartDialog.showToast('请求完成，loading 已关闭。'));
  }

  Future<void> _showConfirmDemo() async {
    final bool? confirmed = await AppSmartDialog.showConfirm(
      title: '确认执行批量操作？',
      message: '这个弹窗来自 flutter_smart_dialog.show(...)，点击按钮后会通过返回值回传结果。',
      confirmText: '继续',
      cancelText: '先看看',
    );
    final bool accepted = confirmed ?? false;
    unawaited(AppSmartDialog.showToast(accepted ? '已确认执行。' : '本次操作已取消。'));
  }

  Future<void> _showCustomDialogDemo() async {
    final String? result = await AppSmartDialog.showCustomDialog<String>(
      builder: (BuildContext context) => const _DemoInfoDialog(),
    );
    unawaited(
      AppSmartDialog.showToast('自定义弹窗已关闭，结果：${result ?? '点击遮罩关闭'}。'),
    );
  }
}

class _DemoInfoDialog extends StatelessWidget {
  const _DemoInfoDialog();

  @override
  Widget build(BuildContext context) {
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
                  const Text(
                    '自定义弹窗',
                    style: TextStyle(fontSize: 18, fontWeight: FontWeight.w700),
                  ),
                  const SizedBox(height: 12),
                  const Text(
                    '通过 AppSmartDialog.showCustomDialog 展示任意 Widget，'
                    '并通过 dismissCustomDialog 回传结果。',
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
