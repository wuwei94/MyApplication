import 'package:basic_flutter/core/utils/logger/logger.dart';
import 'package:flutter/material.dart';

class SimpleDialogPage extends StatelessWidget {
  const SimpleDialogPage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('SimpleDialog')),
      body: Center(
        child: ElevatedButton(
          onPressed: () => _showSimpleDialog(context),
          child: const Text('显示 SimpleDialog'),
        ),
      ),
    );
  }

  Future<void> _showSimpleDialog(BuildContext context) async {
    final int? i = await showDialog<int>(
      context: context,
      builder: (BuildContext context) {
        return SimpleDialog(
          title: const Text('请选择语言'),
          children: <Widget>[
            SimpleDialogOption(
              onPressed: () => Navigator.pop(context, 1),
              child: const Text('中文简体'),
            ),
            SimpleDialogOption(
              onPressed: () => Navigator.pop(context, 2),
              child: const Text('美国英语'),
            ),
          ],
        );
      },
    );
    if (i != null) {
      logDebug("选择了：${i == 1 ? "中文简体" : "美国英语"}");
    }
  }
}
