import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';
import 'package:flutter_demo/core/utils/ui/toast.dart';

/// ModalBottomSheet
/// Demonstrates various bottom sheet types
class ModalBottomSheetDemoPage extends BasicLayoutPage {
  const ModalBottomSheetDemoPage({super.key, required super.title});

  @override
  BasicLayoutPageState<ModalBottomSheetDemoPage> createState() =>
      _ModalBottomSheetDemoPageState();
}

class _ModalBottomSheetDemoPageState
    extends BasicLayoutPageState<ModalBottomSheetDemoPage> {
  @override
  List<String> buildList() => const <String>[
        '1. 显示 Modal Bottom Sheet',
        '2. 显示 Draggable Sheet',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        _showModalBottomSheet();
      case 1:
        _showDraggableSheet();
    }
  }

  void _showModalBottomSheet() {
    showModalBottomSheet<void>(
      context: context,
      builder: (BuildContext sheetContext) => Container(
        padding: const EdgeInsets.all(16),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: <Widget>[
            const Text(
              'Modal Bottom Sheet',
              style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 16),
            ListTile(
              leading: const Icon(Icons.share),
              title: const Text('Share'),
              onTap: () {
                Navigator.pop(sheetContext);
                showToast('Modal · Share');
              },
            ),
            ListTile(
              leading: const Icon(Icons.link),
              title: const Text('Copy Link'),
              onTap: () {
                Navigator.pop(sheetContext);
                showToast('Modal · Copy Link');
              },
            ),
            ListTile(
              leading: const Icon(Icons.delete),
              title: const Text('Delete'),
              onTap: () {
                Navigator.pop(sheetContext);
                showToast('Modal · Delete');
              },
            ),
          ],
        ),
      ),
    );
  }

  void _showDraggableSheet() {
    showModalBottomSheet<void>(
      context: context,
      isScrollControlled: true,
      builder: (BuildContext sheetContext) => DraggableScrollableSheet(
        expand: false,
        builder: (BuildContext sheetContext, ScrollController scrollController) {
          return Container(
            decoration: const BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.vertical(top: Radius.circular(16)),
            ),
            child: ListView.builder(
              controller: scrollController,
              itemCount: 30,
              itemBuilder: (BuildContext context, int index) =>
                  ListTile(title: Text('Item $index')),
            ),
          );
        },
      ),
    );
  }

  @override
  Widget buildPreview() {
    return const Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: <Widget>[
          Icon(Icons.vertical_align_bottom, size: 48),
          SizedBox(height: 16),
          Text('Modal Bottom Sheet / Draggable Scrollable Sheet'),
        ],
      ),
    );
  }
}
