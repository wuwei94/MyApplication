import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_keyboard_visibility/flutter_keyboard_visibility.dart';

/// flutter_keyboard_visibility
/// https://pub.dev/packages/flutter_keyboard_visibility
class KeyboardVisibilityDemoPage extends StatelessWidget {
  const KeyboardVisibilityDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return KeyboardVisibilityDemoView(title: title);
  }
}

class KeyboardVisibilityDemoView extends StatefulWidget {
  const KeyboardVisibilityDemoView({super.key, required this.title});

  final String title;

  @override
  State<KeyboardVisibilityDemoView> createState() =>
      _KeyboardVisibilityDemoViewState();
}

class _KeyboardVisibilityDemoViewState
    extends State<KeyboardVisibilityDemoView> {
  static const String _initialText = '点击输入框唤起键盘，然后观察下面两个监听区域的变化。';

  late final TextEditingController _textController;
  late final FocusNode _focusNode;
  late final KeyboardVisibilityController _keyboardVisibilityController;

  StreamSubscription<bool>? _keyboardSubscription;

  bool _isKeyboardVisible = false;
  int _eventCount = 0;
  String _lastEventMessage = '初始状态：键盘已隐藏。';

  @override
  void initState() {
    super.initState();
    _textController = TextEditingController(text: _initialText);
    _focusNode = FocusNode();
    _keyboardVisibilityController = KeyboardVisibilityController();
    _isKeyboardVisible = _keyboardVisibilityController.isVisible;
    _lastEventMessage = _buildEventMessage(
      isKeyboardVisible: _isKeyboardVisible,
      eventCount: _eventCount,
    );
    _keyboardSubscription = _keyboardVisibilityController.onChange.listen(
      _handleKeyboardChanged,
    );
  }

  @override
  void dispose() {
    _keyboardSubscription?.cancel();
    _focusNode.dispose();
    _textController.dispose();
    super.dispose();
  }

  void _handleKeyboardChanged(bool isKeyboardVisible) {
    if (!mounted) {
      return;
    }

    setState(() {
      _isKeyboardVisible = isKeyboardVisible;
      _eventCount += 1;
      _lastEventMessage = _buildEventMessage(
        isKeyboardVisible: isKeyboardVisible,
        eventCount: _eventCount,
      );
    });
  }

  String _buildEventMessage({
    required bool isKeyboardVisible,
    required int eventCount,
  }) {
    final String statusText = isKeyboardVisible ? '显示' : '隐藏';
    if (eventCount == 0) {
      return '初始状态：键盘已$statusText。';
    }

    return '第 $eventCount 次事件：键盘已$statusText。';
  }

  void _focusInput() {
    _focusNode.requestFocus();
  }

  void _dismissKeyboard() {
    _focusNode.unfocus();
  }

  @override
  Widget build(BuildContext context) {
    return KeyboardDismissOnTap(
      child: Scaffold(
        appBar: AppBar(title: Text(widget.title)),
        body: ListView(
          padding: const EdgeInsets.fromLTRB(16, 16, 16, 24),
          children: <Widget>[
            const _SectionCard(
              title: 'flutter_keyboard_visibility 6.0.0',
              subtitle: '这个 demo 只演示三件事：监听键盘状态、订阅显隐事件、点击空白收起键盘。',
              child: Wrap(
                spacing: 8,
                runSpacing: 8,
                children: <Widget>[
                  _TagChip(label: 'KeyboardVisibilityBuilder'),
                  _TagChip(label: 'KeyboardVisibilityController'),
                  _TagChip(label: 'KeyboardDismissOnTap'),
                ],
              ),
            ),
            const SizedBox(height: 16),
            _SectionCard(
              title: '1. 先把键盘弹出来',
              subtitle: '点击输入框或“聚焦输入框”按钮即可唤起键盘。',
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: <Widget>[
                  TextField(
                    controller: _textController,
                    focusNode: _focusNode,
                    maxLines: 3,
                    decoration: const InputDecoration(
                      border: OutlineInputBorder(),
                      labelText: '示例输入框',
                      hintText: '输入任意内容来观察键盘状态变化',
                    ),
                  ),
                  const SizedBox(height: 12),
                  Wrap(
                    spacing: 8,
                    runSpacing: 8,
                    children: <Widget>[
                      FilledButton.icon(
                        onPressed: _focusInput,
                        icon: const Icon(Icons.keyboard_rounded),
                        label: const Text('聚焦输入框'),
                      ),
                      OutlinedButton.icon(
                        onPressed: _dismissKeyboard,
                        icon: const Icon(Icons.keyboard_hide_rounded),
                        label: const Text('收起键盘'),
                      ),
                    ],
                  ),
                  const SizedBox(height: 12),
                  Container(
                    width: double.infinity,
                    padding: const EdgeInsets.all(16),
                    decoration: BoxDecoration(
                      color: const Color(0xFFF5F7FB),
                      borderRadius: BorderRadius.circular(16),
                      border: Border.all(color: const Color(0xFFD8E2F0)),
                    ),
                    child: const Text(
                      '键盘弹出后，试着点击这块空白区域。因为页面外层包了 KeyboardDismissOnTap，所以会自动失焦并收起键盘。',
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 16),
            KeyboardVisibilityBuilder(
              builder: (BuildContext context, bool isKeyboardVisible) {
                final Color accentColor = isKeyboardVisible
                    ? const Color(0xFF0F9D58)
                    : const Color(0xFF5F6B7A);

                return _SectionCard(
                  title: '2. Builder 监听结果',
                  subtitle: '适合让局部 UI 直接跟随键盘状态变化。',
                  child: Row(
                    children: <Widget>[
                      Icon(
                        isKeyboardVisible
                            ? Icons.keyboard_rounded
                            : Icons.keyboard_hide_rounded,
                        color: accentColor,
                        size: 28,
                      ),
                      const SizedBox(width: 12),
                      Expanded(
                        child: Text(
                          isKeyboardVisible
                              ? 'KeyboardVisibilityBuilder 告诉我们：键盘当前处于显示状态。'
                              : 'KeyboardVisibilityBuilder 告诉我们：键盘当前处于隐藏状态。',
                          style: Theme.of(context).textTheme.bodyLarge,
                        ),
                      ),
                      const SizedBox(width: 12),
                      _StatusBadge(
                        label: isKeyboardVisible ? 'VISIBLE' : 'HIDDEN',
                        color: accentColor,
                      ),
                    ],
                  ),
                );
              },
            ),
            const SizedBox(height: 16),
            _SectionCard(
              title: '3. Controller 订阅结果',
              subtitle: '适合做日志、埋点或把键盘状态同步给页面中的其他逻辑。',
              child: Column(
                children: <Widget>[
                  Row(
                    children: <Widget>[
                      Expanded(
                        child: _MetricTile(
                          label: '最新状态',
                          value: _isKeyboardVisible ? '显示' : '隐藏',
                        ),
                      ),
                      const SizedBox(width: 12),
                      Expanded(
                        child: _MetricTile(
                          label: '事件次数',
                          value: '$_eventCount',
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 12),
                  Container(
                    width: double.infinity,
                    padding: const EdgeInsets.all(16),
                    decoration: BoxDecoration(
                      color: const Color(0xFFF8FAFC),
                      borderRadius: BorderRadius.circular(16),
                    ),
                    child: Text(_lastEventMessage),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 16),
            const _SectionCard(
              title: '补充说明',
              subtitle: '根据官方文档，移动端最适合使用这个包；Web 和桌面端默认会返回 false。',
              child: Text(
                '如果你的需求只是让某一块布局跟着键盘变化，用 KeyboardVisibilityBuilder 就够了；如果还需要在状态变化时执行额外逻辑，再接 KeyboardVisibilityController.onChange 会更合适。',
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _SectionCard extends StatelessWidget {
  const _SectionCard({
    required this.title,
    required this.subtitle,
    required this.child,
  });

  final String title;
  final String subtitle;
  final Widget child;

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Text(
              title,
              style: Theme.of(
                context,
              ).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w700),
            ),
            const SizedBox(height: 6),
            Text(
              subtitle,
              style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                color: Colors.black87,
                height: 1.45,
              ),
            ),
            const SizedBox(height: 16),
            child,
          ],
        ),
      ),
    );
  }
}

class _MetricTile extends StatelessWidget {
  const _MetricTile({required this.label, required this.value});

  final String label;
  final String value;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: const Color(0xFFF5F7FB),
        borderRadius: BorderRadius.circular(16),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          Text(
            label,
            style: Theme.of(
              context,
            ).textTheme.bodySmall?.copyWith(color: const Color(0xFF5F6B7A)),
          ),
          const SizedBox(height: 6),
          Text(
            value,
            style: Theme.of(
              context,
            ).textTheme.headlineSmall?.copyWith(fontWeight: FontWeight.w700),
          ),
        ],
      ),
    );
  }
}

class _StatusBadge extends StatelessWidget {
  const _StatusBadge({required this.label, required this.color});

  final String label;
  final Color color;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
      decoration: BoxDecoration(
        color: color.withValues(alpha: 0.12),
        borderRadius: BorderRadius.circular(999),
      ),
      child: Text(
        label,
        style: TextStyle(color: color, fontWeight: FontWeight.w700),
      ),
    );
  }
}

class _TagChip extends StatelessWidget {
  const _TagChip({required this.label});

  final String label;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
      decoration: BoxDecoration(
        color: const Color(0xFFEAF2FF),
        borderRadius: BorderRadius.circular(999),
      ),
      child: Text(
        label,
        style: Theme.of(context).textTheme.bodySmall?.copyWith(
          color: const Color(0xFF0F5DAA),
          fontWeight: FontWeight.w600,
        ),
      ),
    );
  }
}
