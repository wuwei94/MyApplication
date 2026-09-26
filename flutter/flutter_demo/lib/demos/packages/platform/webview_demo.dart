import 'package:flutter_demo/core/basic/basic.dart';
import 'package:webview_flutter/webview_flutter.dart';

/// WebView Flutter — 页面加载与导航状态
///
/// 核心机制与避坑点：
/// 1. 控制器契约：`WebViewController.loadRequest` / `reload` / `currentUrl` 驱动真实导航。
/// 2. 生命周期：`NavigationDelegate` 回调更新 UI 前必须检查 `mounted`。
///
/// 官方参考：
/// https://pub.dev/packages/webview_flutter
class WebViewDemoPage extends BasicResponsePage {
  const WebViewDemoPage({super.key, required super.title});

  @override
  BasicResponsePageState<WebViewDemoPage> createState() =>
      _WebViewDemoPageState();
}

class _WebViewDemoPageState extends BasicResponsePageState<WebViewDemoPage> {
  static const String _initialUrl = 'https://www.baidu.com';

  late final WebViewController _controller;

  @override
  void initState() {
    super.initState();
    showDescription('webview_flutter 示例：loadRequest / reload / 导航状态');
    _controller = WebViewController()
      ..setJavaScriptMode(JavaScriptMode.unrestricted)
      ..setNavigationDelegate(
        NavigationDelegate(
          onPageStarted: (String url) {
            appendLog('→ [onPageStarted] $url');
          },
          onProgress: (int progress) {
            updateLog('progress', '加载进度 $progress%');
          },
          onPageFinished: (String url) {
            removeUpdatingLog('progress');
            appendLog('✓ [onPageFinished] $url');
          },
          onWebResourceError: (WebResourceError error) {
            removeUpdatingLog('progress');
            appendLog('✗ [onWebResourceError] ${error.errorCode} ${error.description}');
          },
        ),
      );
  }

  @override
  List<String> buildList() => const <String>[
        '1. 加载初始页面',
        '2. 刷新当前页面',
        '3. 读取当前 URL',
        '4. 加载 pub.dev 页面',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        _loadInitial();
      case 1:
        _reload();
      case 2:
        _readCurrentUrl();
      case 3:
        _loadPubDev();
    }
  }

  void _loadInitial() {
    appendLog('→ [loadRequest] $_initialUrl');
    _controller.loadRequest(Uri.parse(_initialUrl));
    appendLog('✓ loadRequest 已提交');
  }

  Future<void> _reload() async {
    appendLog('→ [reload] WebViewController.reload()');
    await _controller.reload();
    appendLog('✓ reload 已提交');
  }

  Future<void> _readCurrentUrl() async {
    appendLog('→ [currentUrl] WebViewController.currentUrl()');
    final String? url = await _controller.currentUrl();
    appendLog('✓ currentUrl=${url ?? 'null'}');
  }

  void _loadPubDev() {
    const String url = 'https://pub.dev/packages/webview_flutter';
    appendLog('→ [loadRequest] $url');
    _controller.loadRequest(Uri.parse(url));
    appendLog('✓ loadRequest 已提交');
  }
}
