import 'package:flutter/material.dart';
import 'package:get/get.dart';

class UtilsPage extends StatelessWidget {
  const UtilsPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(title)),
      body: ListView(
        children: [
          // 显示 SnackBar
          ListTile(
            leading: const Icon(Icons.notifications),
            title: const Text('显示 SnackBar'),
            subtitle: const Text('Get.snackBar'),
            onTap: () {
              Get.snackbar('Hi', 'Message');
            },
          ),
          const Divider(),

          // 显示对话框
          ListTile(
            leading: const Icon(Icons.chat_bubble_outline),
            title: const Text('显示对话框'),
            subtitle: const Text('Get.defaultDialog'),
            onTap: () {
              Get.defaultDialog<void>(title: 'I am a dialog');
            },
          ),
          const Divider(),
        ],
      ),
    );
  }
}
