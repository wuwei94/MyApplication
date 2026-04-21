import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:timeago/timeago.dart' as timeago;

/// Timeago
/// https://pub.dev/packages/timeago
class TimeagoDemoPage extends StatelessWidget {
  const TimeagoDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return TimeagoDemoView(title: title);
  }
}

class TimeagoDemoView extends StatefulWidget {
  const TimeagoDemoView({super.key, required this.title});

  final String title;

  @override
  State<TimeagoDemoView> createState() => _TimeagoDemoViewState();
}

class _TimeagoDemoViewState extends State<TimeagoDemoView> {
  static bool _localeMessagesRegistered = false;
  static final DateFormat _exactTimeFormatter = DateFormat(
    'yyyy-MM-dd HH:mm:ss',
  );

  _TimeagoLocaleOption _selectedLocale = _TimeagoLocaleOption.zhCn;
  late DateTime _referenceTime;

  @override
  void initState() {
    super.initState();
    _registerLocaleMessages();
    _referenceTime = DateTime.now();
  }

  void _registerLocaleMessages() {
    if (_localeMessagesRegistered) {
      return;
    }

    timeago.setLocaleMessages('zh', timeago.ZhMessages());
    timeago.setLocaleMessages('zh_CN', timeago.ZhCnMessages());
    _localeMessagesRegistered = true;
  }

  void _refreshReferenceTime() {
    setState(() {
      _referenceTime = DateTime.now();
    });
  }

  List<_TimeagoSample> _buildSamples() {
    return <_TimeagoSample>[
      _TimeagoSample(
        title: '刚刚发送',
        description: '模拟一条 20 秒前发出的消息。',
        dateTime: _referenceTime.subtract(const Duration(seconds: 20)),
      ),
      _TimeagoSample(
        title: '5 分钟前更新',
        description: '最常见的列表更新时间展示。',
        dateTime: _referenceTime.subtract(const Duration(minutes: 5)),
      ),
      _TimeagoSample(
        title: '2 小时前登录',
        description: '适合账号安全、日志等场景。',
        dateTime: _referenceTime.subtract(const Duration(hours: 2)),
      ),
      _TimeagoSample(
        title: '昨天创建',
        description: '跨天后会自然切换为天级别文本。',
        dateTime: _referenceTime.subtract(const Duration(days: 1, hours: 3)),
      ),
      _TimeagoSample(
        title: '3 小时后提醒',
        description: '通过 allowFromNow 展示未来时间。',
        dateTime: _referenceTime.add(const Duration(hours: 3)),
      ),
    ];
  }

  String _formatRelativeTime(DateTime dateTime) {
    return timeago.format(
      dateTime,
      locale: _selectedLocale.localeCode,
      clock: _referenceTime,
      allowFromNow: true,
    );
  }

  String _formatExactTime(DateTime dateTime) {
    return _exactTimeFormatter.format(dateTime);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
        actions: <Widget>[
          IconButton(
            onPressed: _refreshReferenceTime,
            tooltip: '刷新基准时间',
            icon: const Icon(Icons.refresh),
          ),
        ],
      ),
      body: getBody(),
    );
  }

  Widget getBody() {
    final List<_TimeagoSample> samples = _buildSamples();

    return ListView(
      padding: const EdgeInsets.all(16),
      children: <Widget>[
        _IntroCard(
          localeLabel: _selectedLocale.label,
          referenceTime: _formatExactTime(_referenceTime),
        ),
        const SizedBox(height: 12),
        _LocaleSelectorCard(
          selectedLocale: _selectedLocale,
          onLocaleChanged: (_TimeagoLocaleOption locale) {
            setState(() {
              _selectedLocale = locale;
            });
          },
        ),
        const SizedBox(height: 12),
        _UsageCard(
          localeCode: _selectedLocale.localeCode,
          referenceTime: _formatExactTime(_referenceTime),
        ),
        const SizedBox(height: 12),
        _SamplesCard(
          samples: samples,
          relativeTimeBuilder: _formatRelativeTime,
          exactTimeBuilder: _formatExactTime,
        ),
      ],
    );
  }
}

enum _TimeagoLocaleOption {
  zhCn('zh_CN', '简体中文'),
  en('en', 'English (full)');

  const _TimeagoLocaleOption(this.localeCode, this.label);

  final String localeCode;
  final String label;
}

class _TimeagoSample {
  const _TimeagoSample({
    required this.title,
    required this.description,
    required this.dateTime,
  });

  final String title;
  final String description;
  final DateTime dateTime;
}

class _IntroCard extends StatelessWidget {
  const _IntroCard({required this.localeLabel, required this.referenceTime});

  final String localeLabel;
  final String referenceTime;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Text('Timeago 示例', style: theme.textTheme.titleMedium),
            const SizedBox(height: 8),
            Text(
              '演示 timeago 如何把 DateTime 转成“刚刚、5 分钟前、in 3 hours”'
              ' 这类相对时间文案。当前页面只演示 zh_CN 和完整英文 en。',
              style: theme.textTheme.bodyMedium,
            ),
            const SizedBox(height: 12),
            SelectableText(
              '当前语言: $localeLabel\n基准时间: $referenceTime',
              style: theme.textTheme.bodyMedium,
            ),
          ],
        ),
      ),
    );
  }
}

class _LocaleSelectorCard extends StatelessWidget {
  const _LocaleSelectorCard({
    required this.selectedLocale,
    required this.onLocaleChanged,
  });

  final _TimeagoLocaleOption selectedLocale;
  final ValueChanged<_TimeagoLocaleOption> onLocaleChanged;

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
              'timeago 英文开箱即用；中文需要先注册消息文案。'
              '这个 demo 不展示 en_short。',
              style: theme.textTheme.bodyMedium,
            ),
            const SizedBox(height: 16),
            Wrap(
              spacing: 8,
              runSpacing: 8,
              children: <Widget>[
                for (final _TimeagoLocaleOption locale
                    in _TimeagoLocaleOption.values)
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

class _UsageCard extends StatelessWidget {
  const _UsageCard({required this.localeCode, required this.referenceTime});

  final String localeCode;
  final String referenceTime;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Text('核心调用', style: theme.textTheme.titleMedium),
            const SizedBox(height: 8),
            Text(
              '这个页面固定使用同一份“基准时间”来生成文案，方便观察切换效果。',
              style: theme.textTheme.bodyMedium,
            ),
            const SizedBox(height: 12),
            SelectableText(
              'timeago.format(\n'
              '  dateTime,\n'
              "  locale: '$localeCode',\n"
              '  clock: referenceTime,\n'
              '  allowFromNow: true,\n'
              ')',
              style: theme.textTheme.bodyMedium,
            ),
            const SizedBox(height: 8),
            Text(
              '当前 referenceTime: $referenceTime',
              style: theme.textTheme.bodySmall,
            ),
          ],
        ),
      ),
    );
  }
}

class _SamplesCard extends StatelessWidget {
  const _SamplesCard({
    required this.samples,
    required this.relativeTimeBuilder,
    required this.exactTimeBuilder,
  });

  final List<_TimeagoSample> samples;
  final String Function(DateTime dateTime) relativeTimeBuilder;
  final String Function(DateTime dateTime) exactTimeBuilder;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Text('输出结果', style: theme.textTheme.titleMedium),
            const SizedBox(height: 8),
            Text(
              '下面几组固定样例最适合拿来验证列表、消息流和提醒文案。',
              style: theme.textTheme.bodyMedium,
            ),
            const SizedBox(height: 12),
            for (int index = 0; index < samples.length; index++) ...<Widget>[
              _SampleTile(
                sample: samples[index],
                relativeTime: relativeTimeBuilder(samples[index].dateTime),
                exactTime: exactTimeBuilder(samples[index].dateTime),
              ),
              if (index != samples.length - 1) const Divider(height: 24),
            ],
          ],
        ),
      ),
    );
  }
}

class _SampleTile extends StatelessWidget {
  const _SampleTile({
    required this.sample,
    required this.relativeTime,
    required this.exactTime,
  });

  final _TimeagoSample sample;
  final String relativeTime;
  final String exactTime;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: <Widget>[
        Text(sample.title, style: theme.textTheme.titleSmall),
        const SizedBox(height: 4),
        Text(sample.description, style: theme.textTheme.bodyMedium),
        const SizedBox(height: 8),
        SelectableText(
          'timeago: $relativeTime\nDateTime: $exactTime',
          style: theme.textTheme.bodyMedium,
        ),
      ],
    );
  }
}
