import 'package:basic_flutter/core/utils/logger/log.dart';
import 'package:flutter/material.dart';

class CustomDialogPage extends StatelessWidget {
  const CustomDialogPage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Dialog')),
      body: Center(
        child: ElevatedButton(
          onPressed: () => _showDialog(context),
          child: const Text('显示 Dialog'),
        ),
      ),
    );
  }

  Future<void> _showDialog(BuildContext context) async {
    final int? index = await showDialog<int>(
      context: context,
      builder: (BuildContext context) {
        final Widget child = Column(
          children: <Widget>[
            const ListTile(title: Text("显示菜单列表")),
            Expanded(
              child: ListView.builder(
                itemCount: 20,
                itemBuilder: (BuildContext context, int index) {
                  return ListTile(
                    title: Text("$index"),
                    onTap: () => Navigator.of(context).pop(index),
                  );
                },
              ),
            ),
          ],
        );
        return Dialog(child: child);
      },
    );
    if (index != null) {
      logDebug("点击了：$index");
    }
  }
}
