import 'package:flutter/material.dart';

/// PopScope Example
/// Demonstrates back button interception
class PopScopeDemoPage extends StatelessWidget {
  const PopScopeDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return PopScopeDemoView(title: title);
  }
}

class PopScopeDemoView extends StatefulWidget {
  const PopScopeDemoView({super.key, required this.title});

  final String title;

  @override
  State<PopScopeDemoView> createState() => _PopScopeDemoViewState();
}

class _PopScopeDemoViewState extends State<PopScopeDemoView> {
  bool _canPop = false;

  @override
  Widget build(BuildContext context) {
    return PopScope(
      canPop: _canPop,
      onPopInvokedWithResult: (didPop, popResult) async {
        if (didPop) return;

        final result = await showDialog<bool>(
          context: context,
          builder: (dialogContext) => AlertDialog(
            title: const Text('Confirm Exit'),
            content: const Text('Do you want to leave this page?'),
            actions: [
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

        if (result == true && context.mounted) {
          Navigator.pop(context);
        }
      },
      child: Scaffold(
        appBar: AppBar(title: Text(widget.title)),
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Text(
                _canPop ? 'Can pop freely' : 'Pop is intercepted',
                style: const TextStyle(fontSize: 20),
              ),
              const SizedBox(height: 16),
              Switch(
                value: _canPop,
                onChanged: (value) => setState(() => _canPop = value),
              ),
              const SizedBox(height: 32),
              const Text('Press back button to test'),
            ],
          ),
        ),
      ),
    );
  }
}
