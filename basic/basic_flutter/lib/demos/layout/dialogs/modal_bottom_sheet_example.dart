import 'package:flutter/material.dart';

/// ModalBottomSheet Example
/// Demonstrates various bottom sheet types
class ModalBottomSheetDemoPage extends StatelessWidget {
  const ModalBottomSheetDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return ModalBottomSheetDemoView(title: title);
  }
}

class ModalBottomSheetDemoView extends StatelessWidget {
  const ModalBottomSheetDemoView({super.key, required this.title});

  final String title;

  void _showModalBottomSheet(BuildContext context) {
    showModalBottomSheet<void>(
      context: context,
      builder: (context) => Container(
        padding: const EdgeInsets.all(16),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const Text(
              'Modal Bottom Sheet',
              style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
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

  void _showDraggableSheet(BuildContext context) {
    showModalBottomSheet<void>(
      context: context,
      isScrollControlled: true,
      builder: (context) => DraggableScrollableSheet(
        expand: false,
        builder: (context, scrollController) {
          return Container(
            decoration: const BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.vertical(top: Radius.circular(16)),
            ),
            child: ListView.builder(
              controller: scrollController,
              itemCount: 30,
              itemBuilder: (context, index) =>
                  ListTile(title: Text('Item $index')),
            ),
          );
        },
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
              onPressed: () => _showModalBottomSheet(context),
              child: const Text('Show Modal Bottom Sheet'),
            ),
            const SizedBox(height: 16),
            ElevatedButton(
              onPressed: () => _showDraggableSheet(context),
              child: const Text('Show Draggable Sheet'),
            ),
          ],
        ),
      ),
    );
  }
}
