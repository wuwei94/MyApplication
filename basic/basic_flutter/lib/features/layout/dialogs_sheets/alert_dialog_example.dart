import 'package:flutter/material.dart';

/// AlertDialog Example
/// Demonstrates various dialog types
class AlertDialogExample extends StatelessWidget {
  const AlertDialogExample({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return AlertDialogRoute(title: title);
  }
}

class AlertDialogRoute extends StatelessWidget {
  const AlertDialogRoute({super.key, required this.title});

  final String title;

  void _showAlertDialog(BuildContext context) {
    showDialog<void>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Alert'),
        content: const Text('This is an alert dialog.'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Cancel'),
          ),
          ElevatedButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('OK'),
          ),
        ],
      ),
    );
  }

  void _showSimpleDialog(BuildContext context) {
    showDialog<void>(
      context: context,
      builder: (context) => SimpleDialog(
        title: const Text('Select Option'),
        children: [
          SimpleDialogOption(
            onPressed: () => Navigator.pop(context, 'Option 1'),
            child: const Text('Option 1'),
          ),
          SimpleDialogOption(
            onPressed: () => Navigator.pop(context, 'Option 2'),
            child: const Text('Option 2'),
          ),
          SimpleDialogOption(
            onPressed: () => Navigator.pop(context, 'Option 3'),
            child: const Text('Option 3'),
          ),
        ],
      ),
    );
  }

  void _showBottomSheet(BuildContext context) {
    showModalBottomSheet<void>(
      context: context,
      builder: (context) => Container(
        padding: const EdgeInsets.all(16),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const Text(
              'Bottom Sheet',
              style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 16),
            ListTile(
              leading: const Icon(Icons.share),
              title: const Text('Share'),
              onTap: () => Navigator.pop(context),
            ),
            ListTile(
              leading: const Icon(Icons.link),
              title: const Text('Copy Link'),
              onTap: () => Navigator.pop(context),
            ),
            ListTile(
              leading: const Icon(Icons.delete),
              title: const Text('Delete'),
              onTap: () => Navigator.pop(context),
            ),
          ],
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(title)),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            ElevatedButton(
              onPressed: () => _showAlertDialog(context),
              child: const Text('Show AlertDialog'),
            ),
            const SizedBox(height: 16),
            ElevatedButton(
              onPressed: () => _showSimpleDialog(context),
              child: const Text('Show SimpleDialog'),
            ),
            const SizedBox(height: 16),
            ElevatedButton(
              onPressed: () => _showBottomSheet(context),
              child: const Text('Show BottomSheet'),
            ),
          ],
        ),
      ),
    );
  }
}
