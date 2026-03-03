import 'package:basic_flutter/core/utils/logger/log.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';

class CupertinoActionSheetPage extends StatelessWidget {
  const CupertinoActionSheetPage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('CupertinoActionSheet')),
      body: Center(
        child: ElevatedButton(
          onPressed: () => _showCupertinoActionSheet(context),
          child: const Text('显示 CupertinoActionSheet'),
        ),
      ),
    );
  }

  Future<void> _showCupertinoActionSheet(BuildContext context) async {
    final String? result = await showCupertinoModalPopup<String>(
      context: context,
      builder: (BuildContext context) {
        return CupertinoActionSheet(
          title: const Text('选择操作'),
          message: const Text('请选择一个选项'),
          actions: <Widget>[
            CupertinoActionSheetAction(
              child: const Text('拍照'),
              onPressed: () => Navigator.pop(context, 'camera'),
            ),
            CupertinoActionSheetAction(
              child: const Text('从相册选择'),
              onPressed: () => Navigator.pop(context, 'gallery'),
            ),
          ],
          cancelButton: CupertinoActionSheetAction(
            isDefaultAction: true,
            child: const Text('取消'),
            onPressed: () => Navigator.pop(context, 'cancel'),
          ),
        );
      },
    );
    if (result != null) {
      logDebug('选择了: $result');
    }
  }
}
