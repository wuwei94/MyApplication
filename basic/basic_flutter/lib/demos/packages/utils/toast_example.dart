import 'package:basic_flutter/core/utils/ui/toast.dart';
import 'package:flutter/material.dart';

/// toast
/// https://pub.dev/packages/fluttertoast
class ToastDemoPage extends StatelessWidget {
  const ToastDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return ToastDemoView(title: title);
  }
}

class ToastDemoView extends StatelessWidget {
  const ToastDemoView({super.key, required this.title});

  final String title;

  void _showToast() {
    showToast("show Toast");
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(title)),
      body: getBody(),
      floatingActionButton: getFAB(),
    );
  }

  Widget getBody() {
    return const Center();
  }

  Widget getFAB() {
    return FloatingActionButton(
      onPressed: () => _showToast(),
      tooltip: 'toast',
      child: const Icon(Icons.add),
    );
  }
}
