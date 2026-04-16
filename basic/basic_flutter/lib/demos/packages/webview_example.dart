import 'package:flutter/material.dart';
import 'package:webview_flutter/webview_flutter.dart';

/// WebView Flutter
/// https://pub.dev/packages/webview_flutter
class WebViewDemoPage extends StatelessWidget {
  const WebViewDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return WebViewDemoView(title: title);
  }
}

class WebViewDemoView extends StatefulWidget {
  const WebViewDemoView({super.key, required this.title});

  final String title;

  @override
  State<WebViewDemoView> createState() => _WebViewDemoViewState();
}

class _WebViewDemoViewState extends State<WebViewDemoView> {
  static const String _initialUrl = 'https://www.baidu.com';

  late final WebViewController _controller;
  int _loadingProgress = 0;

  @override
  void initState() {
    super.initState();
    _controller = WebViewController()
      ..setJavaScriptMode(JavaScriptMode.unrestricted)
      ..setNavigationDelegate(
        NavigationDelegate(
          onPageStarted: (_) {
            if (!mounted) {
              return;
            }

            setState(() {
              _loadingProgress = 0;
            });
          },
          onProgress: (int progress) {
            if (!mounted) {
              return;
            }

            setState(() {
              _loadingProgress = progress;
            });
          },
          onPageFinished: (_) {
            if (!mounted) {
              return;
            }

            setState(() {
              _loadingProgress = 100;
            });
          },
          onWebResourceError: (_) {
            if (!mounted) {
              return;
            }

            setState(() {
              _loadingProgress = 100;
            });
          },
        ),
      )
      ..loadRequest(Uri.parse(_initialUrl));
  }

  Future<void> _reloadPage() async {
    await _controller.reload();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
        actions: [
          IconButton(
            onPressed: _reloadPage,
            icon: const Icon(Icons.refresh),
            tooltip: 'Reload',
          ),
        ],
      ),
      body: Column(
        children: [
          if (_loadingProgress < 100)
            LinearProgressIndicator(value: _loadingProgress / 100),
          Expanded(child: WebViewWidget(controller: _controller)),
        ],
      ),
    );
  }
}
