import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';

/// PopScope
/// Demonstrates back button interception
class PopScopeDemoPage extends BasicLayoutPage {
  const PopScopeDemoPage({super.key, required super.title});

  @override
  BasicLayoutPageState<PopScopeDemoPage> createState() =>
      _PopScopeDemoPageState();
}

class _PopScopeDemoPageState extends BasicLayoutPageState<PopScopeDemoPage> {
  bool _canPop = false;

  @override
  List<String> buildList() => const <String>[
        '1. 允许直接返回',
        '2. 拦截返回并确认',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        setState(() => _canPop = true);
      case 1:
        setState(() => _canPop = false);
    }
  }

  @override
  Widget buildPreview() {
    return PopScope(
      canPop: _canPop,
      onPopInvokedWithResult: (bool didPop, Object? popResult) async {
        if (didPop) return;

        final bool? result = await showDialog<bool>(
          context: context,
          builder: (BuildContext dialogContext) => AlertDialog(
            title: const Text('Confirm Exit'),
            content: const Text('Do you want to leave this page?'),
            actions: <Widget>[
              TextButton(
                onPressed: () => Navigator.pop(dialogContext, false),
                child: const Text('Stay'),
              ),
              ElevatedButton(
                onPressed: () => Navigator.pop(dialogContext, true),
                child: const Text('Leave'),
              ),
            ],
          ),
        );

        if (result == true && mounted) {
          Navigator.pop(context);
        }
      },
      child: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            Text(
              _canPop ? 'Can pop freely' : 'Pop is intercepted',
              style: const TextStyle(fontSize: 20),
            ),
            const SizedBox(height: 16),
            Switch(
              value: _canPop,
              onChanged: (bool value) => setState(() => _canPop = value),
            ),
            const SizedBox(height: 32),
            const Text('Press back button to test'),
          ],
        ),
      ),
    );
  }
}
