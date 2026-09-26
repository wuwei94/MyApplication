import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';
import 'package:flutter_demo/core/utils/ui/toast.dart';
import 'package:flutter_linkify/flutter_linkify.dart';
import 'package:url_launcher/url_launcher.dart';

/// FlutterLinkify — 正文 URL 自动识别与点击
///
/// 核心机制与避坑点：
/// 1. 识别链路：`Linkify` / `SelectableLinkify` + `Linkifier` 把文本解析成可点链接。
/// 2. 回调契约：`onOpen` 内调用 `launchUrl`，失败需给出 Toast 反馈。
///
/// 官方参考：
/// https://pub.dev/packages/flutter_linkify
class FlutterLinkifyDemoPage extends BasicLayoutPage {
  const FlutterLinkifyDemoPage({super.key, required super.title});

  @override
  BasicLayoutPageState<FlutterLinkifyDemoPage> createState() =>
      _FlutterLinkifyDemoPageState();
}

class _FlutterLinkifyDemoPageState
    extends BasicLayoutPageState<FlutterLinkifyDemoPage> {
  static const String _sampleText =
      'flutter_linkify 会把正文里的链接自动识别成可点击文本。\n\n'
      '文档：https://pub.dev/packages/flutter_linkify\n'
      '仓库：https://github.com/Cretezy/flutter_linkify\n'
      '镜像：www.example.com/design-system';

  bool _humanize = true;

  @override
  List<String> buildList() => const <String>[
        '1. 开启 humanize',
        '2. 关闭 humanize',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        setState(() {
          _humanize = true;
        });
      case 1:
        setState(() {
          _humanize = false;
        });
    }
  }

  @override
  Widget buildPreview() {
    final ThemeData theme = Theme.of(context);
    final LinkifyOptions options = LinkifyOptions(humanize: _humanize);

    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          Text(
            'Linkify · humanize=$_humanize',
            style: theme.textTheme.titleMedium?.copyWith(
              fontWeight: FontWeight.w700,
            ),
          ),
          const SizedBox(height: 12),
          Linkify(
            text: _sampleText,
            options: options,
            onOpen: _handleOpen,
            style: theme.textTheme.bodyLarge?.copyWith(height: 1.6),
            linkStyle: theme.textTheme.bodyLarge?.copyWith(
              color: Colors.blue,
              decoration: TextDecoration.underline,
              fontWeight: FontWeight.w700,
            ),
          ),
          const SizedBox(height: 24),
          Text(
            'SelectableLinkify',
            style: theme.textTheme.titleMedium?.copyWith(
              fontWeight: FontWeight.w700,
            ),
          ),
          const SizedBox(height: 12),
          SelectableLinkify(
            text: _sampleText,
            options: options,
            onOpen: _handleOpen,
            style: theme.textTheme.bodyLarge?.copyWith(height: 1.6),
            linkStyle: theme.textTheme.bodyLarge?.copyWith(
              color: Colors.teal,
              decoration: TextDecoration.underline,
              fontWeight: FontWeight.w700,
            ),
          ),
        ],
      ),
    );
  }

  Future<void> _handleOpen(LinkableElement link) async {
    final Uri uri = _resolveUri(link.url);
    final bool launched = await launchUrl(
      uri,
      mode: LaunchMode.externalApplication,
    );
    showToast(launched ? '已打开 ${link.text}' : '无法打开 ${link.text}');
  }

  Uri _resolveUri(String rawUrl) {
    final String normalizedUrl = rawUrl.trim();
    final Uri? parsed = Uri.tryParse(normalizedUrl);
    if (parsed != null && parsed.scheme.isNotEmpty) {
      return parsed;
    }
    return Uri.parse('https://$normalizedUrl');
  }
}
