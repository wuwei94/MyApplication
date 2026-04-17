import 'package:basic_flutter/core/utils/ui/toast.dart';
import 'package:flutter/material.dart';
import 'package:flutter_linkify/flutter_linkify.dart';
import 'package:url_launcher/url_launcher.dart';

const List<Linkifier> _demoLinkifiers = <Linkifier>[_DemoUrlLinkifier()];

final RegExp _demoUrlRegex = RegExp(
  r'^(.*?)((?:https?:\/\/|www\.)[^\s]+)',
  caseSensitive: false,
  dotAll: true,
);

final RegExp _demoLooseUrlRegex = RegExp(
  r'''^(.*?)((https?:\/\/)?(www\.)?[-a-zA-Z0-9@:%._\+~#=]{2,256}\.[a-z]{2,4}\b([^\s]+)?)''',
  caseSensitive: false,
  dotAll: true,
);

final RegExp _demoUrlBoundaryRegex = RegExp(r'[，。！？；：（）【】「」『』《》〈〉、“”‘’]');

/// FlutterLinkify
/// https://pub.dev/packages/flutter_linkify
class FlutterLinkifyDemoPage extends StatelessWidget {
  const FlutterLinkifyDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return FlutterLinkifyDemoView(title: title);
  }
}

class FlutterLinkifyDemoView extends StatefulWidget {
  const FlutterLinkifyDemoView({super.key, required this.title});

  final String title;

  @override
  State<FlutterLinkifyDemoView> createState() => _FlutterLinkifyDemoViewState();
}

class _FlutterLinkifyDemoViewState extends State<FlutterLinkifyDemoView> {
  static const List<_DemoSample> _samples = <_DemoSample>[
    _DemoSample(
      label: 'Package',
      text:
          'flutter_linkify 会把正文里的链接自动识别成可点击文本。\n\n'
          '文档：https://pub.dev/packages/flutter_linkify\n'
          '仓库：https://github.com/Cretezy/flutter_linkify\n'
          '镜像：https://flutter.linkify.demo/packages',
    ),
    _DemoSample(
      label: 'Support',
      text:
          '如果用户反馈登录页异常，可以先查看 https://status.example.com，'
          '再查看 https://docs.example.com/troubleshooting 获取排查说明。',
    ),
    _DemoSample(
      label: 'Release',
      text:
          '本次版本说明：\n'
          '- changelog: https://example.com/releases/2026-spring\n'
          '- design notes: www.example.com/design-system\n'
          '- roadmap: https://example.com/roadmap',
    ),
  ];

  late final TextEditingController _controller;

  bool _humanize = true;
  int _selectedSampleIndex = 0;
  String _statusTitle = '等待点击';
  String _statusMessage = '点击下方 Linkify/SelectableLinkify 中的任意链接，体验 onOpen 回调。';
  bool? _lastSucceeded;

  @override
  void initState() {
    super.initState();
    _controller = TextEditingController(text: _samples.first.text);
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);
    final LinkifyOptions options = LinkifyOptions(humanize: _humanize);

    return Scaffold(
      appBar: AppBar(title: Text(widget.title)),
      body: ListView(
        padding: const EdgeInsets.fromLTRB(16, 16, 16, 24),
        children: <Widget>[
          const _SectionCard(
            title: 'flutter_linkify 6.0.0',
            subtitle: '适合正文说明、帮助文档这类“文本为主，但希望 URL 自动变成可点链接”的场景。',
            child: Wrap(
              spacing: 8,
              runSpacing: 8,
              children: <Widget>[
                _TagChip(label: 'URL detection'),
                _TagChip(label: 'Linkify'),
                _TagChip(label: 'SelectableLinkify'),
                _TagChip(label: 'onOpen'),
              ],
            ),
          ),
          const SizedBox(height: 16),
          _SectionCard(
            title: '输入文本',
            subtitle:
                '切换预设文案或直接编辑输入框，下面两个预览会实时更新；`humanize` 开启后，会把 `https://` 这类协议头隐藏掉。',
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: <Widget>[
                Wrap(
                  spacing: 8,
                  runSpacing: 8,
                  children: List<Widget>.generate(_samples.length, (int index) {
                    final _DemoSample sample = _samples[index];

                    return ChoiceChip(
                      label: Text(sample.label),
                      selected: _selectedSampleIndex == index,
                      onSelected: (bool selected) {
                        if (!selected) {
                          return;
                        }

                        _applySample(index);
                      },
                    );
                  }),
                ),
                const SizedBox(height: 12),
                SwitchListTile.adaptive(
                  contentPadding: EdgeInsets.zero,
                  title: const Text('humanize 链接文本'),
                  subtitle: const Text('开启后，展示文本会尽量隐藏协议头，让链接展示更接近自然语言。'),
                  value: _humanize,
                  onChanged: (bool value) {
                    setState(() {
                      _humanize = value;
                    });
                  },
                ),
                const SizedBox(height: 8),
                TextField(
                  controller: _controller,
                  minLines: 6,
                  maxLines: 10,
                  decoration: InputDecoration(
                    hintText:
                        '输入包含 URL 的内容，例如 https://pub.dev 或 www.example.com/docs',
                    filled: true,
                    fillColor: const Color(0xFFF8FAFC),
                    border: OutlineInputBorder(
                      borderRadius: BorderRadius.circular(16),
                      borderSide: const BorderSide(color: Color(0xFFDCE3F0)),
                    ),
                    enabledBorder: OutlineInputBorder(
                      borderRadius: BorderRadius.circular(16),
                      borderSide: const BorderSide(color: Color(0xFFDCE3F0)),
                    ),
                    focusedBorder: OutlineInputBorder(
                      borderRadius: BorderRadius.circular(16),
                      borderSide: const BorderSide(color: Color(0xFF2563EB)),
                    ),
                  ),
                  onChanged: (String value) {
                    setState(() {});
                  },
                ),
                const SizedBox(height: 12),
                Text(
                  '当前共 ${_controller.text.length} 个字符。',
                  style: theme.textTheme.bodySmall?.copyWith(
                    color: theme.colorScheme.onSurfaceVariant,
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: 16),
          _SectionCard(
            title: 'Linkify 预览',
            subtitle:
                '适合普通富文本展示场景，点击链接后会调用 `onOpen`，这里复用了项目里已有的 `url_launcher`。',
            child: _PreviewShell(
              child: Linkify(
                text: _controller.text,
                linkifiers: _demoLinkifiers,
                options: options,
                onOpen: _handleOpen,
                style: theme.textTheme.bodyLarge?.copyWith(height: 1.6),
                linkStyle: theme.textTheme.bodyLarge?.copyWith(
                  color: const Color(0xFF2563EB),
                  decoration: TextDecoration.underline,
                  fontWeight: FontWeight.w700,
                  height: 1.6,
                ),
              ),
            ),
          ),
          const SizedBox(height: 16),
          _SectionCard(
            title: 'SelectableLinkify 预览',
            subtitle: '如果文本需要复制、长按选择或作为帮助中心内容展示，通常更适合用 SelectableLinkify。',
            child: _PreviewShell(
              child: SelectableLinkify(
                text: _controller.text,
                linkifiers: _demoLinkifiers,
                options: options,
                onOpen: _handleOpen,
                style: theme.textTheme.bodyLarge?.copyWith(height: 1.6),
                linkStyle: theme.textTheme.bodyLarge?.copyWith(
                  color: const Color(0xFF0F766E),
                  decoration: TextDecoration.underline,
                  fontWeight: FontWeight.w700,
                  height: 1.6,
                ),
              ),
            ),
          ),
          const SizedBox(height: 16),
          _StatusCard(
            title: _statusTitle,
            message: _statusMessage,
            succeeded: _lastSucceeded,
          ),
        ],
      ),
    );
  }

  void _applySample(int index) {
    setState(() {
      _selectedSampleIndex = index;
      _controller.text = _samples[index].text;
      _controller.selection = TextSelection.collapsed(
        offset: _controller.text.length,
      );
    });
  }

  Future<void> _handleOpen(LinkableElement link) async {
    final Uri uri = _resolveUri(link.url);
    final bool launched = await launchUrl(
      uri,
      mode: LaunchMode.externalApplication,
    );

    if (!mounted) {
      return;
    }

    setState(() {
      _statusTitle = launched ? '打开成功' : '打开失败';
      _statusMessage = launched
          ? '已尝试打开 ${link.text} -> $uri'
          : '当前设备未能处理 ${link.text} -> $uri';
      _lastSucceeded = launched;
    });

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

class _SectionCard extends StatelessWidget {
  const _SectionCard({
    required this.title,
    required this.subtitle,
    required this.child,
  });

  final String title;
  final String subtitle;
  final Widget child;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return DecoratedBox(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: const Color(0xFFE2E8F0)),
        boxShadow: const <BoxShadow>[
          BoxShadow(
            color: Color(0x120A2533),
            blurRadius: 18,
            offset: Offset(0, 8),
          ),
        ],
      ),
      child: Padding(
        padding: const EdgeInsets.all(18),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Text(
              title,
              style: theme.textTheme.titleMedium?.copyWith(
                fontWeight: FontWeight.w700,
              ),
            ),
            const SizedBox(height: 8),
            Text(
              subtitle,
              style: theme.textTheme.bodyMedium?.copyWith(height: 1.5),
            ),
            const SizedBox(height: 16),
            child,
          ],
        ),
      ),
    );
  }
}

class _PreviewShell extends StatelessWidget {
  const _PreviewShell({required this.child});

  final Widget child;

  @override
  Widget build(BuildContext context) {
    return DecoratedBox(
      decoration: BoxDecoration(
        gradient: const LinearGradient(
          colors: <Color>[Color(0xFFF8FAFC), Color(0xFFF1F5F9)],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: const Color(0xFFDCE3F0)),
      ),
      child: Padding(padding: const EdgeInsets.all(16), child: child),
    );
  }
}

class _StatusCard extends StatelessWidget {
  const _StatusCard({
    required this.title,
    required this.message,
    required this.succeeded,
  });

  final String title;
  final String message;
  final bool? succeeded;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);
    final Color accentColor;

    if (succeeded == null) {
      accentColor = const Color(0xFF2563EB);
    } else if (succeeded!) {
      accentColor = const Color(0xFF0F766E);
    } else {
      accentColor = theme.colorScheme.error;
    }

    return DecoratedBox(
      decoration: BoxDecoration(
        color: accentColor.withValues(alpha: 0.08),
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: accentColor.withValues(alpha: 0.18)),
      ),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Text(
              title,
              style: theme.textTheme.titleMedium?.copyWith(
                color: accentColor,
                fontWeight: FontWeight.w700,
              ),
            ),
            const SizedBox(height: 8),
            Text(
              message,
              style: theme.textTheme.bodyMedium?.copyWith(height: 1.5),
            ),
          ],
        ),
      ),
    );
  }
}

class _TagChip extends StatelessWidget {
  const _TagChip({required this.label});

  final String label;

  @override
  Widget build(BuildContext context) {
    return DecoratedBox(
      decoration: BoxDecoration(
        color: const Color(0xFFEFF6FF),
        borderRadius: BorderRadius.circular(999),
        border: Border.all(color: const Color(0xFFBFDBFE)),
      ),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
        child: Text(
          label,
          style: Theme.of(context).textTheme.bodySmall?.copyWith(
            color: const Color(0xFF1D4ED8),
            fontWeight: FontWeight.w600,
          ),
        ),
      ),
    );
  }
}

class _DemoSample {
  const _DemoSample({required this.label, required this.text});

  final String label;
  final String text;
}

class _DemoUrlLinkifier extends Linkifier {
  const _DemoUrlLinkifier();

  @override
  List<LinkifyElement> parse(
    List<LinkifyElement> elements,
    LinkifyOptions options,
  ) {
    final List<LinkifyElement> list = <LinkifyElement>[];

    for (final LinkifyElement element in elements) {
      if (element is! TextElement) {
        list.add(element);
        continue;
      }

      final RegExp regex = options.looseUrl
          ? _demoLooseUrlRegex
          : _demoUrlRegex;
      final Match? match = regex.firstMatch(element.text);

      if (match == null) {
        list.add(element);
        continue;
      }

      final String remainingText = element.text.replaceFirst(
        match.group(0)!,
        '',
      );
      final String leadingText = match.group(1) ?? '';
      final String matchedUrl = match.group(2) ?? '';
      final ({String linkText, String trailingText}) splitResult =
          _splitTrailingBoundary(matchedUrl);

      if (leadingText.isNotEmpty) {
        list.add(TextElement(leadingText));
      }

      if (splitResult.linkText.isNotEmpty) {
        String originalUrl = splitResult.linkText;
        String visibleUrl = splitResult.linkText;
        String? endingText;

        if (options.excludeLastPeriod && originalUrl.endsWith('.')) {
          originalUrl = originalUrl.substring(0, originalUrl.length - 1);
          visibleUrl = visibleUrl.substring(0, visibleUrl.length - 1);
          endingText = '.';
        }

        if (!originalUrl.startsWith(
          RegExp(r'https?:\/\/', caseSensitive: false),
        )) {
          originalUrl =
              '${options.defaultToHttps ? 'https://' : 'http://'}$originalUrl';
        }

        if (options.humanize) {
          visibleUrl = visibleUrl.replaceFirst(RegExp(r'https?://'), '');
        }

        if (options.removeWww) {
          visibleUrl = visibleUrl.replaceFirst(RegExp(r'www\.'), '');
        }

        list.add(
          (options.humanize || options.removeWww)
              ? UrlElement(originalUrl, visibleUrl, splitResult.linkText)
              : UrlElement(originalUrl, null, splitResult.linkText),
        );

        if (endingText != null) {
          list.add(TextElement(endingText));
        }
      }

      final String trailingText = '${splitResult.trailingText}$remainingText';
      if (trailingText.isNotEmpty) {
        list.addAll(
          parse(<LinkifyElement>[TextElement(trailingText)], options),
        );
      }
    }

    return list;
  }

  ({String linkText, String trailingText}) _splitTrailingBoundary(
    String matchedUrl,
  ) {
    final Match? boundaryMatch = _demoUrlBoundaryRegex.firstMatch(matchedUrl);

    if (boundaryMatch == null) {
      return (linkText: matchedUrl, trailingText: '');
    }

    return (
      linkText: matchedUrl.substring(0, boundaryMatch.start),
      trailingText: matchedUrl.substring(boundaryMatch.start),
    );
  }
}
