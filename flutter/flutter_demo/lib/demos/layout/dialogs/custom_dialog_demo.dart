import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';
import 'package:flutter_demo/core/utils/ui/toast.dart';

/// CustomDialog
/// Demonstrates custom dialog creation
class CustomDialogDemoPage extends BasicLayoutPage {
  const CustomDialogDemoPage({super.key, required super.title});

  @override
  BasicLayoutPageState<CustomDialogDemoPage> createState() =>
      _CustomDialogDemoPageState();
}

class _CustomDialogDemoPageState
    extends BasicLayoutPageState<CustomDialogDemoPage> {
  @override
  List<String> buildList() => const <String>[
        '1. 显示成功自定义对话框',
        '2. 显示加载对话框',
        '3. 显示图片对话框',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        _showCustomDialog();
      case 1:
        _showLoadingDialog();
      case 2:
        _showImageDialog();
    }
  }

  void _showCustomDialog() {
    showDialog<void>(
      context: context,
      barrierDismissible: false,
      builder: (BuildContext dialogContext) => Dialog(
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(20),
        ),
        child: Container(
          padding: const EdgeInsets.all(20),
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(20),
            gradient: LinearGradient(
              colors: <Color>[Colors.blue.shade100, Colors.purple.shade100],
            ),
          ),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: <Widget>[
              const Icon(Icons.check_circle, size: 60, color: Colors.green),
              const SizedBox(height: 16),
              const Text(
                'Success!',
                style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
              ),
              const SizedBox(height: 8),
              const Text('Your operation was completed successfully.'),
              const SizedBox(height: 20),
              ElevatedButton(
                onPressed: () {
                  Navigator.pop(dialogContext);
                  showToast('Custom dialog closed');
                },
                child: const Text('OK'),
              ),
            ],
          ),
        ),
      ),
    );
  }

  void _showLoadingDialog() {
    showDialog<void>(
      context: context,
      barrierDismissible: false,
      builder: (BuildContext dialogContext) => const Dialog(
        child: Padding(
          padding: EdgeInsets.all(20),
          child: Row(
            mainAxisSize: MainAxisSize.min,
            children: <Widget>[
              CircularProgressIndicator(),
              SizedBox(width: 20),
              Text('Loading...'),
            ],
          ),
        ),
      ),
    );

    Future<void>.delayed(const Duration(seconds: 2), () {
      if (mounted) {
        Navigator.pop(context);
        showToast('Loading dialog dismissed');
      }
    });
  }

  void _showImageDialog() {
    showDialog<void>(
      context: context,
      builder: (BuildContext dialogContext) => Dialog(
        insetPadding: EdgeInsets.zero,
        child: Stack(
          children: <Widget>[
            Image.network(
              'https://picsum.photos/400/600',
              fit: BoxFit.cover,
              height: 400,
              width: 300,
            ),
            Positioned(
              top: 8,
              right: 8,
              child: IconButton(
                icon: const Icon(Icons.close, color: Colors.white),
                onPressed: () => Navigator.pop(dialogContext),
              ),
            ),
          ],
        ),
      ),
    );
  }

  @override
  Widget buildPreview() {
    return const Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: <Widget>[
          Icon(Icons.picture_in_picture_alt, size: 48),
          SizedBox(height: 16),
          Text('Custom Dialog / Loading Dialog / Image Dialog'),
        ],
      ),
    );
  }
}
