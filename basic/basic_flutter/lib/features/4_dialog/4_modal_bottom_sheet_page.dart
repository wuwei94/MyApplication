import 'package:basic_flutter/core/utils/logger/logger.dart';
import 'package:flutter/material.dart';

class ModalBottomSheetPage extends StatelessWidget {
  const ModalBottomSheetPage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('ModalBottomSheet')),
      body: Center(
        child: ElevatedButton(
          onPressed: () => _showModalBottomSheet(context),
          child: const Text('显示 ModalBottomSheet'),
        ),
      ),
    );
  }

  Future<void> _showModalBottomSheet(BuildContext context) async {
    final int? index = await showModalBottomSheet<int>(
      context: context,
      builder: (BuildContext context) {
        return ListView.builder(
          itemCount: 20,
          itemBuilder: (BuildContext context, int index) {
            return ListTile(
              title: Text("$index"),
              onTap: () => Navigator.of(context).pop(index),
            );
          },
        );
      },
    );
    if (index != null) {
      logDebug("点击了：$index");
    }
  }
}
