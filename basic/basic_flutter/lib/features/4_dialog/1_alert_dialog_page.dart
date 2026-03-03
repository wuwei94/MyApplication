import 'package:basic_flutter/core/utils/logger/log.dart';
import 'package:flutter/material.dart';

class AlertDialogPage extends StatelessWidget {
  const AlertDialogPage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('AlertDialog')),
      body: Center(
        child: ElevatedButton(
          onPressed: () => _showAlertDialog(context),
          child: const Text('显示 AlertDialog'),
        ),
      ),
    );
  }

  Future<void> _showAlertDialog(BuildContext context) async {
    final bool? delete = await showDialog<bool>(
      context: context,
      builder: (context) {
        return AlertDialog(
          title: const Text("提示"),
          content: const Text("您确定要删除当前文件吗?"),
          actions: <Widget>[
            TextButton(
              child: const Text("取消"),
              onPressed: () => Navigator.of(context).pop(false),
            ),
            TextButton(
              child: const Text("删除"),
              onPressed: () => Navigator.of(context).pop(true),
            ),
          ],
        );
      },
    );
    if (delete != null) {
      if (delete) {
        logDebug("确定删除");
      } else {
        logDebug("取消删除");
      }
    }
  }
}
