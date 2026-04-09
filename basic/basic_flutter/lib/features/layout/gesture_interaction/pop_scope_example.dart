import 'package:flutter/material.dart';

/// PopScope Example
/// Demonstrates back button interception
class PopScopeExample extends StatelessWidget {
  const PopScopeExample({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return PopScopeRoute(title: title);
  }
}

class PopScopeRoute extends StatefulWidget {
  const PopScopeRoute({super.key, required this.title});

  final String title;

  @override
  State<PopScopeRoute> createState() => _PopScopeRouteState();
}

class _PopScopeRouteState extends State<PopScopeRoute> {
  bool _canPop = false;

  @override
  Widget build(BuildContext context) {
    return PopScope(
      canPop: _canPop,
      onPopInvoked: (didPop) async {
        if (didPop) return;

        final result = await showDialog<bool>(
          context: context,
          builder: (context) => AlertDialog(
            title: const Text('Confirm Exit'),
            content: const Text('Do you want to leave this page?'),
            actions: [
              TextButton(
                onPressed: () => Navigator.pop(context, false),
                child: const Text('Stay'),
              ),
              ElevatedButton(
                onPressed: () => Navigator.pop(context, true),
                child: const Text('Leave'),
              ),
            ],
          ),
        );

        if (result == true && mounted) {
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
