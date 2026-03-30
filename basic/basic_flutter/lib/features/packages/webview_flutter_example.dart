import 'dart:async';

import 'package:flutter/material.dart';
import 'package:webview_flutter/webview_flutter.dart';

/// WebView Flutter
/// https://pub.dev/packages/webview_flutter
class WebViewFlutterExample extends StatelessWidget {
  const WebViewFlutterExample({super.key});

  @override
  Widget build(BuildContext context) {
    return const WebViewFlutterRoute(title: 'WebViewFlutter Example');
  }
}

class WebViewFlutterRoute extends StatefulWidget {
  const WebViewFlutterRoute({super.key, required this.title});

  final String title;

  @override
  State<WebViewFlutterRoute> createState() => _WebViewFlutterRouteState();
}

class _WebViewFlutterRouteState extends State<WebViewFlutterRoute> {
  static const String _initialUrl = 'https://www.baidu.com';

  late final WebViewController _controller;
  String _pageTitle = 'Loading...';
  String _currentUrl = _initialUrl;
  String? _errorMessage;
  int _loadingProgress = 0;

  @override
  void initState() {
    super.initState();
    _controller = WebViewController()
      ..setJavaScriptMode(JavaScriptMode.unrestricted)
      ..setNavigationDelegate(
        NavigationDelegate(
          onPageStarted: (String url) {
            if (!mounted) {
              return;
            }

            setState(() {
              _currentUrl = url;
              _pageTitle = 'Loading...';
              _errorMessage = null;
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
          onPageFinished: (String url) {
            unawaited(_syncPageInfo(url));
          },
          onWebResourceError: (WebResourceError error) {
            if (!mounted) {
              return;
            }

            setState(() {
              _errorMessage = error.description;
              _loadingProgress = 100;
            });
          },
        ),
      )
      ..loadRequest(Uri.parse(_initialUrl));
  }

  Future<void> _syncPageInfo(String url) async {
    final String? title = await _controller.getTitle();

    if (!mounted) {
      return;
    }

    setState(() {
      _currentUrl = url;
      _pageTitle = title ?? 'Untitled Page';
      _loadingProgress = 100;
    });
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
