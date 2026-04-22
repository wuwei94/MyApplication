import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:time_machine/time_machine.dart';
import 'package:time_machine/time_machine_text_patterns.dart';

/// Time Machine
/// https://pub.dev/packages/time_machine
class TimeMachineDemoPage extends StatelessWidget {
  const TimeMachineDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return TimeMachineDemoView(title: title);
  }
}

class TimeMachineDemoView extends StatefulWidget {
  const TimeMachineDemoView({super.key, required this.title});

  final String title;

  @override
  State<TimeMachineDemoView> createState() => _TimeMachineDemoViewState();
}

class _TimeMachineDemoViewState extends State<TimeMachineDemoView> {
  static Future<void>? _initializationFuture;

  late Future<_TimeMachineSnapshot> _snapshotFuture;

  @override
  void initState() {
    super.initState();
    _snapshotFuture = _loadSnapshot();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
        actions: <Widget>[
          IconButton(
            onPressed: _reloadSnapshot,
            tooltip: '重新加载',
            icon: const Icon(Icons.refresh),
          ),
        ],
      ),
      body: getBody(),
    );
  }

  Widget getBody() {
    return FutureBuilder<_TimeMachineSnapshot>(
      future: _snapshotFuture,
      builder:
          (BuildContext context, AsyncSnapshot<_TimeMachineSnapshot> snapshot) {
            if (snapshot.connectionState != ConnectionState.done) {
              return const _LoadingState();
            }

            if (snapshot.hasError) {
              return _ErrorState(
                error: snapshot.error,
                onRetry: _retryInitialization,
              );
            }

            final _TimeMachineSnapshot data = snapshot.requireData;

            return ListView(
              padding: const EdgeInsets.all(16),
              children: <Widget>[
                _IntroCard(greeting: data.greeting),
                const SizedBox(height: 12),
                _OutputSectionCard(title: '基础输出', lines: data.basicLines),
                const SizedBox(height: 12),
                _OutputSectionCard(title: '格式化输出', lines: data.formattedLines),
                const SizedBox(height: 12),
                _OutputSectionCard(
                  title: '法语格式化',
                  subtitle: 'fr-FR 区域设置',
                  lines: data.frenchLines,
                ),
                const SizedBox(height: 12),
                _OutputSectionCard(
                  title: '解析法语时区时间文本',
                  subtitle: "格式模板：'dddd yyyy-MM-dd HH:mm z'",
                  lines: data.parseLines,
                ),
              ],
            );
          },
    );
  }

  void _reloadSnapshot() {
    setState(() {
      _snapshotFuture = _loadSnapshot();
    });
  }

  void _retryInitialization() {
    _initializationFuture = null;
    _reloadSnapshot();
  }

  Future<void> _ensureInitialized() {
    _initializationFuture ??= TimeMachine.initialize(<String, Object>{
      'rootBundle': rootBundle,
    });

    return _initializationFuture!;
  }

  Future<_TimeMachineSnapshot> _loadSnapshot() async {
    try {
      await _ensureInitialized();
    } catch (error) {
      _initializationFuture = null;
      rethrow;
    }

    final DateTimeZoneProvider tzdb = await DateTimeZoneProviders.tzdb;
    final DateTimeZone paris = await tzdb['Europe/Paris'];
    final Culture french = await _loadCulture('fr-FR');
    final Instant now = Instant.now();
    final ZonedDateTime utcNow = now.inZone(DateTimeZone.utc);
    final ZonedDateTime localNow = now.inLocalZone();
    final ZonedDateTime parisNow = now.inZone(paris);

    final ZonedDateTimePattern invariantPattern =
        ZonedDateTimePattern.createWithInvariantCulture(
          'dddd yyyy-MM-dd HH:mm',
        );
    final ZonedDateTimePattern frenchPattern =
        ZonedDateTimePattern.createWithCulture('dddd yyyy-MM-dd HH:mm', french);
    final ZonedDateTimePattern frenchPatternWithZone =
        ZonedDateTimePattern.createWithCulture(
          'dddd yyyy-MM-dd HH:mm z',
          french,
        );

    final String localText = frenchPatternWithZone.format(localNow);
    final ParseResult<ZonedDateTime> localClone = frenchPatternWithZone.parse(
      localText,
    );

    return _TimeMachineSnapshot(
      greeting: 'Hello, ${DateTimeZone.local} from the Dart Time Machine!',
      basicLines: <String>[
        'UTC Time: $now',
        'Local Time: $localNow',
        'Paris Time: $parisNow',
      ],
      formattedLines: <String>[
        'UTC Time: ${invariantPattern.format(utcNow)}',
        'Local Time: ${invariantPattern.format(localNow)}',
      ],
      frenchLines: <String>[
        'Culture: $french',
        'UTC Time: ${frenchPattern.format(utcNow)}',
        'Local Time: ${frenchPattern.format(localNow)}',
      ],
      parseLines: <String>[
        'Source Text: $localText',
        'Parsed Value: ${localClone.value}',
      ],
    );
  }

  Future<Culture> _loadCulture(String id) async {
    final Culture? culture = await Cultures.getCulture(id);
    return culture ?? Cultures.invariantCulture;
  }
}

class _TimeMachineSnapshot {
  const _TimeMachineSnapshot({
    required this.greeting,
    required this.basicLines,
    required this.formattedLines,
    required this.frenchLines,
    required this.parseLines,
  });

  final String greeting;
  final List<String> basicLines;
  final List<String> formattedLines;
  final List<String> frenchLines;
  final List<String> parseLines;
}

class _IntroCard extends StatelessWidget {
  const _IntroCard({required this.greeting});

  final String greeting;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Text('TimeMachine 示例', style: theme.textTheme.titleMedium),
            const SizedBox(height: 8),
            Text(
              '演示初始化、基础时间输出、法语格式化，以及带时区文本的解析结果。',
              style: theme.textTheme.bodyMedium,
            ),
            const SizedBox(height: 12),
            SelectableText(
              greeting,
              style: theme.textTheme.bodyLarge?.copyWith(
                fontFeatures: const <FontFeature>[FontFeature.tabularFigures()],
              ),
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
            for (int index = 0; index < lines.length; index++) ...<Widget>[
              SelectableText(
                lines[index],
                style: theme.textTheme.bodyLarge?.copyWith(
                  fontFeatures: const <FontFeature>[
                    FontFeature.tabularFigures(),
                  ],
                ),
              ),
              if (index != lines.length - 1) const SizedBox(height: 8),
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
    return const Center(
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: <Widget>[
          CircularProgressIndicator(),
          SizedBox(height: 12),
          Text('正在初始化 TimeMachine 数据...'),
        ],
      ),
    );
  }
}

class _ErrorState extends StatelessWidget {
  const _ErrorState({required this.error, required this.onRetry});

  final Object? error;
  final VoidCallback onRetry;

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: <Widget>[
            const Icon(Icons.error_outline, size: 40),
            const SizedBox(height: 12),
            const Text('TimeMachine 初始化失败'),
            const SizedBox(height: 8),
            Text('$error', textAlign: TextAlign.center),
            const SizedBox(height: 12),
            FilledButton.icon(
              onPressed: onRetry,
              icon: const Icon(Icons.refresh),
              label: const Text('重试'),
            ),
          ],
        ),
      ),
    );
  }
}
