import 'package:basic_flutter/core/utils/logger/log.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';

class CupertinoAlertDialogPage extends StatelessWidget {
  const CupertinoAlertDialogPage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('CupertinoAlertDialog')),
      body: Center(
        child: ElevatedButton(
          onPressed: () => _showCupertinoAlertDialog(context),
          child: const Text('显示 CupertinoAlertDialog'),
        ),
      ),
    );
  }

  Future<void> _showCupertinoAlertDialog(BuildContext context) async {
    final String? result = await showCupertinoDialog<String>(
      context: context,
      builder: (BuildContext context) {
        return CupertinoAlertDialog(
          title: const Text('提示'),
          content: const Text('您确定要删除当前文件吗？'),
          actions: <Widget>[
            CupertinoDialogAction(
              child: const Text('取消'),
              onPressed: () => Navigator.pop(context, 'cancel'),
            ),
            CupertinoDialogAction(
              isDestructiveAction: true,
              child: const Text('删除'),
              onPressed: () => Navigator.pop(context, 'delete'),
            ),
          ],
        );
      },
    );
    if (result != null) {
      logDebug('选择了: $result');
    }
  }
}
