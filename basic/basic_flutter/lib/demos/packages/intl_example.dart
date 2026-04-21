import 'package:flutter/material.dart';
import 'package:intl/date_symbol_data_local.dart';
import 'package:intl/intl.dart';

/// Intl
/// https://pub.dev/packages/intl
class IntlDemoPage extends StatelessWidget {
  const IntlDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return IntlDemoView(title: title);
  }
}

class IntlDemoView extends StatefulWidget {
  const IntlDemoView({super.key, required this.title});

  final String title;

  @override
  State<IntlDemoView> createState() => _IntlDemoViewState();
}

class _IntlDemoViewState extends State<IntlDemoView> {
  static Future<void>? _initializationFuture;

  _IntlLocaleOption _selectedLocale = _IntlLocaleOption.zhCn;
  late DateTime _sampleDate;
  late Future<void> _intlFuture;

  @override
  void initState() {
    super.initState();
    _sampleDate = DateTime.now();
    _intlFuture = _ensureInitialized();
  }

  Future<void> _ensureInitialized() {
    _initializationFuture ??= _initializeFormattingData();
    return _initializationFuture!;
  }

  Future<void> _initializeFormattingData() async {
    for (final _IntlLocaleOption locale in _IntlLocaleOption.values) {
      await initializeDateFormatting(locale.localeTag);
    }
  }

  void _refreshNow() {
    setState(() {
      _sampleDate = DateTime.now();
    });
  }

  void _retryInitialization() {
    _initializationFuture = null;
    setState(() {
      _intlFuture = _ensureInitialized();
    });
  }

  List<String> _buildDateLines() {
    final String localeTag = _selectedLocale.localeTag;
    final DateFormat fullFormatter = DateFormat.yMMMMEEEEd(localeTag).add_Hms();
    final DateFormat shortFormatter = DateFormat.yMd(localeTag);
    final DateFormat timeFormatter = DateFormat.jm(localeTag);

    return <String>[
      'canonicalizedLocale: ${Intl.canonicalizedLocale(localeTag)}',
      'yMMMMEEEEd + Hms: ${fullFormatter.format(_sampleDate)}',
      'yMd: ${shortFormatter.format(_sampleDate)}',
      'jm: ${timeFormatter.format(_sampleDate)}',
    ];
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
        actions: <Widget>[
          IconButton(
            onPressed: _refreshNow,
            tooltip: '刷新当前时间',
            icon: const Icon(Icons.refresh),
          ),
        ],
      ),
      body: getBody(),
    );
  }

  Widget getBody() {
    return FutureBuilder<void>(
      future: _intlFuture,
      builder: (BuildContext context, AsyncSnapshot<void> snapshot) {
        if (snapshot.connectionState != ConnectionState.done) {
          return const _LoadingState();
        }

        if (snapshot.hasError) {
          return _ErrorState(
            error: snapshot.error,
            onRetry: _retryInitialization,
          );
        }

        return ListView(
          padding: const EdgeInsets.all(16),
          children: <Widget>[
            _IntroCard(
              localeLabel: _selectedLocale.label,
              sampleDate: _sampleDate,
            ),
            const SizedBox(height: 12),
            _IntlControlsCard(
              selectedLocale: _selectedLocale,
              onLocaleChanged: (_IntlLocaleOption locale) {
                setState(() {
                  _selectedLocale = locale;
                });
              },
            ),
            const SizedBox(height: 12),
            _OutputSectionCard(
              title: 'DateFormat',
              subtitle: '中英文日期时间格式化对比',
              lines: _buildDateLines(),
            ),
          ],
        );
      },
    );
  }
}

enum _IntlLocaleOption {
  zhCn('zh_CN', '简体中文'),
  enUs('en_US', 'English');

  const _IntlLocaleOption(this.localeTag, this.label);

  final String localeTag;
  final String label;
}

class _IntroCard extends StatelessWidget {
  const _IntroCard({required this.localeLabel, required this.sampleDate});

  final String localeLabel;
  final DateTime sampleDate;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Text('Intl 示例', style: theme.textTheme.titleMedium),
            const SizedBox(height: 8),
            Text(
              '演示 intl 在 Flutter 中最常见的日期时间格式化能力，'
              '以及中英文切换后的输出差异。',
              style: theme.textTheme.bodyMedium,
            ),
            const SizedBox(height: 12),
            SelectableText(
              '当前演示 Locale: $localeLabel\nSample DateTime: ${sampleDate.toIso8601String()}',
              style: theme.textTheme.bodyMedium,
            ),
          ],
        ),
      ),
    );
  }
}

class _IntlControlsCard extends StatelessWidget {
  const _IntlControlsCard({
    required this.selectedLocale,
    required this.onLocaleChanged,
  });

  final _IntlLocaleOption selectedLocale;
  final ValueChanged<_IntlLocaleOption> onLocaleChanged;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Text('Locale 切换', style: theme.textTheme.titleMedium),
            const SizedBox(height: 8),
            Text(
              '切换中英文后，可以直观看到 DateFormat 输出如何随区域变化。',
              style: theme.textTheme.bodyMedium,
            ),
            const SizedBox(height: 16),
            Wrap(
              spacing: 8,
              runSpacing: 8,
              children: <Widget>[
                for (final _IntlLocaleOption locale in _IntlLocaleOption.values)
                  ChoiceChip(
                    label: Text(locale.label),
                    selected: selectedLocale == locale,
                    onSelected: (bool selected) {
                      if (!selected) {
                        return;
                      }
                      onLocaleChanged(locale);
                    },
                  ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

class _OutputSectionCard extends StatelessWidget {
  const _OutputSectionCard({
    required this.title,
    required this.lines,
    this.subtitle,
  });

  final String title;
  final String? subtitle;
  final List<String> lines;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Text(title, style: theme.textTheme.titleMedium),
            if (subtitle != null) ...<Widget>[
              const SizedBox(height: 4),
              Text(
                subtitle!,
                style: theme.textTheme.bodySmall?.copyWith(
                  color: theme.colorScheme.onSurfaceVariant,
                ),
              ),
            ],
            const SizedBox(height: 12),
            for (final String line in lines) ...<Widget>[
              SelectableText(line, style: theme.textTheme.bodyMedium),
              const SizedBox(height: 8),
            ],
          ],
        ),
      ),
    );
  }
}

class _LoadingState extends StatelessWidget {
  const _LoadingState();

  @override
  Widget build(BuildContext context) {
    return const Center(child: CircularProgressIndicator());
  }
}

class _ErrorState extends StatelessWidget {
  const _ErrorState({required this.error, required this.onRetry});

  final Object? error;
  final VoidCallback onRetry;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return Center(
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Card(
          child: Padding(
            padding: const EdgeInsets.all(16),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: <Widget>[
                Text('Intl 初始化失败', style: theme.textTheme.titleMedium),
                const SizedBox(height: 8),
                SelectableText('$error', style: theme.textTheme.bodyMedium),
                const SizedBox(height: 12),
                ElevatedButton.icon(
                  onPressed: onRetry,
                  icon: const Icon(Icons.refresh),
                  label: const Text('重试'),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
