import 'package:flutter/material.dart';

/// CustomDialog Example
/// Demonstrates custom dialog creation
class CustomDialogDemoPage extends StatelessWidget {
  const CustomDialogDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return CustomDialogDemoView(title: title);
  }
}

class CustomDialogDemoView extends StatelessWidget {
  const CustomDialogDemoView({super.key, required this.title});

  final String title;

  void _showCustomDialog(BuildContext context) {
    showDialog<void>(
      context: context,
      barrierDismissible: false,
      builder: (context) => Dialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
        child: Container(
          padding: const EdgeInsets.all(20),
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(20),
            gradient: LinearGradient(
              colors: [Colors.blue.shade100, Colors.purple.shade100],
            ),
          ),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
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
                onPressed: () => Navigator.pop(context),
                child: const Text('OK'),
              ),
            ],
          ),
        ),
      ),
    );
  }

  void _showLoadingDialog(BuildContext context) {
    showDialog<void>(
      context: context,
      barrierDismissible: false,
      builder: (context) => const Dialog(
        child: Padding(
          padding: EdgeInsets.all(20),
          child: Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              CircularProgressIndicator(),
              SizedBox(width: 20),
              Text('Loading...'),
            ],
          ),
        ),
      ),
    );

    Future<void>.delayed(const Duration(seconds: 2), () {
      if (context.mounted) {
        Navigator.pop(context);
      }
    });
  }

  void _showImageDialog(BuildContext context) {
    showDialog<void>(
      context: context,
      builder: (context) => Dialog(
        insetPadding: EdgeInsets.zero,
        child: Stack(
          children: [
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
                onPressed: () => Navigator.pop(context),
              ),
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
              onPressed: () => _showCustomDialog(context),
              child: const Text('Show Custom Dialog'),
            ),
            const SizedBox(height: 16),
            ElevatedButton(
              onPressed: () => _showLoadingDialog(context),
              child: const Text('Show Loading Dialog'),
            ),
            const SizedBox(height: 16),
            ElevatedButton(
              onPressed: () => _showImageDialog(context),
              child: const Text('Show Image Dialog'),
            ),
          ],
        ),
      ),
    );
  }
}
