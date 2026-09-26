import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';
import 'package:flutter_keyboard_visibility/flutter_keyboard_visibility.dart';

/// KeyboardVisibility — 键盘显示状态
///
/// 核心机制与避坑点：
/// 1. 双入口：`KeyboardVisibilityBuilder` 跟随布局，`KeyboardVisibilityController.onChange` 执行逻辑。
/// 2. 生命周期：stream 订阅必须在 dispose 中 cancel。
///
/// 官方参考：
/// https://pub.dev/packages/flutter_keyboard_visibility
class KeyboardVisibilityDemoPage extends BasicLayoutPage {
  const KeyboardVisibilityDemoPage({super.key, required super.title});

  @override
  BasicLayoutPageState<KeyboardVisibilityDemoPage> createState() =>
      _KeyboardVisibilityDemoPageState();
}

class _KeyboardVisibilityDemoPageState
    extends BasicLayoutPageState<KeyboardVisibilityDemoPage> {
  late final KeyboardVisibilityController _keyboardVisibilityController;
  late final TextEditingController _textController;
  late final FocusNode _focusNode;
  StreamSubscription<bool>? _subscription;
  bool _isKeyboardVisible = false;

  @override
  void initState() {
    super.initState();
    _keyboardVisibilityController = KeyboardVisibilityController();
    _textController = TextEditingController();
    _focusNode = FocusNode();
    _isKeyboardVisible = _keyboardVisibilityController.isVisible;
    _subscription = _keyboardVisibilityController.onChange.listen(
      _handleKeyboardChanged,
    );
  }

  @override
  void dispose() {
    _subscription?.cancel();
    _textController.dispose();
    _focusNode.dispose();
    super.dispose();
  }

  @override
  List<String> buildList() => const <String>[
        '1. 聚焦输入框',
        '2. 收起键盘',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        _focusNode.requestFocus();
      case 1:
        _focusNode.unfocus();
    }
  }

  void _handleKeyboardChanged(bool isKeyboardVisible) {
    if (!mounted) {
      return;
    }
    setState(() {
      _isKeyboardVisible = isKeyboardVisible;
    });
  }

  @override
  Widget buildPreview() {
    final ThemeData theme = Theme.of(context);

    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          Text(
            'Controller.isVisible=$_isKeyboardVisible',
            style: theme.textTheme.titleSmall,
          ),
          const SizedBox(height: 12),
          KeyboardVisibilityBuilder(
            builder: (BuildContext context, bool isKeyboardVisible) {
              final String label = isKeyboardVisible ? '键盘显示中' : '键盘已隐藏';
              return Text(
                'Builder: $label',
                style: theme.textTheme.bodyLarge,
              );
            },
          ),
          const SizedBox(height: 16),
          TextField(
            controller: _textController,
            focusNode: _focusNode,
            decoration: const InputDecoration(
              border: OutlineInputBorder(),
              labelText: '点击聚焦唤起键盘',
            ),
          ),
        ],
      ),
    );
  }
}
