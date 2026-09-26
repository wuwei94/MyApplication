import 'package:flutter_demo/core/basic/basic.dart';
import 'package:flutter_demo/core/utils/ui/toast.dart';

/// Toast — 纯操作反馈演示
///
/// 核心机制与避坑点：
/// 1. 反馈通道：纯操作类示例用 Toast / 状态指示反馈，不引入控制台。
/// 2. 触发边界：所有演示行为由下方操作列表触发，禁止页面内隐藏手势。
class ToastDemoPage extends BasicControlPage {
  const ToastDemoPage({super.key, required super.title});

  @override
  BasicControlPageState<ToastDemoPage> createState() => _ToastDemoPageState();
}

class _ToastDemoPageState extends BasicControlPageState<ToastDemoPage> {
  @override
  List<String> buildList() => const <String>[
        '1. 显示短 Toast',
        '2. 显示长 Toast',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        showToast('show Toast');
      case 1:
        showToast('show Toast · longer message for multi-line gravity preview');
    }
  }
}
