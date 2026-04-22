import 'dart:async';

import 'package:basic_flutter/core/utils/event_bus/flutter_event_bus.dart';
import 'package:flutter/material.dart';

/// EventBus
/// https://pub.dev/packages/event_bus
class EventBusDemoPage extends StatelessWidget {
  const EventBusDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return EventBusDemoView(title: title);
  }
}

class EventBusDemoView extends StatefulWidget {
  const EventBusDemoView({super.key, required this.title});

  final String title;

  @override
  State<EventBusDemoView> createState() => _EventBusDemoViewState();
}

class _EventBusDemoViewState extends State<EventBusDemoView> {
  final List<_ReceivedEventRecord> _eventRecords = <_ReceivedEventRecord>[];
  StreamSubscription<_EventBusDemoEvent>? _subscription;
  int _counter = 0;

  @override
  void initState() {
    super.initState();
    _subscription = FlutterEventBus.instance
        .onEvent<_EventBusDemoEvent>()
        .listen(_handleEvent);
  }

  @override
  void dispose() {
    _subscription?.cancel();
    super.dispose();
  }

  void _handleEvent(_EventBusDemoEvent event) {
    if (!mounted) {
      return;
    }

    setState(() {
      _eventRecords.insert(
        0,
        _ReceivedEventRecord(
          title: event.title,
          description: event.description,
          icon: event.icon,
          accentColor: event.accentColor,
          happenedAt: DateTime.now(),
        ),
      );
    });
  }

  void _postCounterEvent() {
    final int nextCounter = _counter + 1;

    setState(() {
      _counter = nextCounter;
    });

    FlutterEventBus.instance.post<_CounterChangedEvent>(
      _CounterChangedEvent(value: nextCounter),
    );
  }

  void _postMessageEvent() {
    final int messageIndex = _eventRecords.length + 1;

    FlutterEventBus.instance.post<_MessagePublishedEvent>(
      _MessagePublishedEvent(message: '手动广播消息 #$messageIndex'),
    );
  }

  void _clearRecords() {
    setState(() {
      _eventRecords.clear();
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(widget.title)),
      body: getBody(),
    );
  }

  Widget getBody() {
    final ThemeData theme = Theme.of(context);

    return ListView(
      padding: const EdgeInsets.all(16),
      children: <Widget>[
        _SummaryCard(
          counter: _counter,
          eventCount: _eventRecords.length,
          theme: theme,
        ),
        const SizedBox(height: 16),
        _ActionPanel(
          onPostCounterEvent: _postCounterEvent,
          onPostMessageEvent: _postMessageEvent,
          onClearRecords: _clearRecords,
        ),
        const SizedBox(height: 16),
        Text(
          '监听结果',
          style: theme.textTheme.titleMedium?.copyWith(
            fontWeight: FontWeight.w700,
          ),
        ),
        const SizedBox(height: 12),
        if (_eventRecords.isEmpty)
          _EmptyState(theme: theme)
        else
          for (final _ReceivedEventRecord record in _eventRecords)
            Padding(
              padding: const EdgeInsets.only(bottom: 12),
              child: _EventRecordCard(record: record),
            ),
      ],
    );
  }
}

abstract class _EventBusDemoEvent {
  const _EventBusDemoEvent();

  String get title;
  String get description;
  IconData get icon;
  Color get accentColor;
}

class _CounterChangedEvent extends _EventBusDemoEvent {
  const _CounterChangedEvent({required this.value});

  final int value;

  @override
  String get title => 'CounterChangedEvent';

  @override
  String get description => '计数器已更新为 $value';

  @override
  IconData get icon => Icons.exposure_plus_1_rounded;

  @override
  Color get accentColor => const Color(0xFF0F5DAA);
}

class _MessagePublishedEvent extends _EventBusDemoEvent {
  const _MessagePublishedEvent({required this.message});

  final String message;

  @override
  String get title => 'MessagePublishedEvent';

  @override
  String get description => message;

  @override
  IconData get icon => Icons.campaign_rounded;

  @override
  Color get accentColor => const Color(0xFF1C8A63);
}

class _ReceivedEventRecord {
  const _ReceivedEventRecord({
    required this.title,
    required this.description,
    required this.icon,
    required this.accentColor,
    required this.happenedAt,
  });

  final String title;
  final String description;
  final IconData icon;
  final Color accentColor;
  final DateTime happenedAt;

  String get formattedTime {
    final String hour = happenedAt.hour.toString().padLeft(2, '0');
    final String minute = happenedAt.minute.toString().padLeft(2, '0');
    final String second = happenedAt.second.toString().padLeft(2, '0');
    return '$hour:$minute:$second';
  }
}

class _SummaryCard extends StatelessWidget {
  const _SummaryCard({
    required this.counter,
    required this.eventCount,
    required this.theme,
  });

  final int counter;
  final int eventCount;
  final ThemeData theme;

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(24),
        gradient: const LinearGradient(
          colors: <Color>[Color(0xFF16324F), Color(0xFF0F5DAA)],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
        boxShadow: const <BoxShadow>[
          BoxShadow(
            color: Color(0x220C2C42),
            blurRadius: 24,
            offset: Offset(0, 12),
          ),
        ],
      ),
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Text(
              'FlutterEventBus 单例演示',
              style: theme.textTheme.titleLarge?.copyWith(
                color: Colors.white,
                fontWeight: FontWeight.w700,
              ),
            ),
            const SizedBox(height: 8),
            Text(
              '点击下方按钮发送事件，当前页面通过 '
              'FlutterEventBus.instance.onEvent<_EventBusDemoEvent>() '
              '监听并实时刷新。',
              style: theme.textTheme.bodyMedium?.copyWith(
                color: Colors.white.withValues(alpha: 0.84),
                height: 1.45,
              ),
            ),
            const SizedBox(height: 18),
            Row(
              children: <Widget>[
                Expanded(
                  child: _MetricTile(
                    label: '计数器',
                    value: '$counter',
                    icon: Icons.confirmation_number_rounded,
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: _MetricTile(
                    label: '已接收事件',
                    value: '$eventCount',
                    icon: Icons.stream_rounded,
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

class _MetricTile extends StatelessWidget {
  const _MetricTile({
    required this.label,
    required this.value,
    required this.icon,
  });

  final String label;
  final String value;
  final IconData icon;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return Container(
      decoration: BoxDecoration(
        color: Colors.white.withValues(alpha: 0.12),
        borderRadius: BorderRadius.circular(18),
        border: Border.all(color: Colors.white.withValues(alpha: 0.14)),
      ),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
        child: Row(
          children: <Widget>[
            Icon(icon, color: Colors.white),
            const SizedBox(width: 12),
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: <Widget>[
                Text(
                  label,
                  style: theme.textTheme.bodySmall?.copyWith(
                    color: Colors.white.withValues(alpha: 0.74),
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  value,
                  style: theme.textTheme.titleMedium?.copyWith(
                    color: Colors.white,
                    fontWeight: FontWeight.w700,
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

class _ActionPanel extends StatelessWidget {
  const _ActionPanel({
    required this.onPostCounterEvent,
    required this.onPostMessageEvent,
    required this.onClearRecords,
  });

  final VoidCallback onPostCounterEvent;
  final VoidCallback onPostMessageEvent;
  final VoidCallback onClearRecords;

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        color: const Color(0xFFF8FAFC),
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: const Color(0xFFD8E1EB)),
      ),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Text(
              '发送事件',
              style: Theme.of(
                context,
              ).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w700),
            ),
            const SizedBox(height: 12),
            Wrap(
              spacing: 12,
              runSpacing: 12,
              children: <Widget>[
                FilledButton.icon(
                  onPressed: onPostCounterEvent,
                  icon: const Icon(Icons.exposure_plus_1_rounded),
                  label: const Text('发送计数事件'),
                ),
                FilledButton.tonalIcon(
                  onPressed: onPostMessageEvent,
                  icon: const Icon(Icons.campaign_rounded),
                  label: const Text('发送消息事件'),
                ),
                OutlinedButton.icon(
                  onPressed: onClearRecords,
                  icon: const Icon(Icons.delete_outline_rounded),
                  label: const Text('清空记录'),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

class _EmptyState extends StatelessWidget {
  const _EmptyState({required this.theme});

  final ThemeData theme;

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        color: const Color(0xFFF8FAFC),
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: const Color(0xFFD8E1EB)),
      ),
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          children: <Widget>[
            const Icon(Icons.inbox_rounded, size: 36, color: Color(0xFF7F8EA3)),
            const SizedBox(height: 12),
            Text(
              '还没有收到事件',
              style: theme.textTheme.titleMedium?.copyWith(
                fontWeight: FontWeight.w700,
              ),
            ),
            const SizedBox(height: 6),
            Text(
              '先点击上方按钮发送事件，下面会展示监听到的事件日志。',
              textAlign: TextAlign.center,
              style: theme.textTheme.bodyMedium?.copyWith(
                color: theme.colorScheme.onSurfaceVariant,
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _EventRecordCard extends StatelessWidget {
  const _EventRecordCard({required this.record});

  final _ReceivedEventRecord record;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return Container(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(22),
        border: Border.all(color: record.accentColor.withValues(alpha: 0.18)),
        boxShadow: const <BoxShadow>[
          BoxShadow(
            color: Color(0x120A2533),
            blurRadius: 18,
            offset: Offset(0, 8),
          ),
        ],
      ),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Container(
              width: 44,
              height: 44,
              decoration: BoxDecoration(
                color: record.accentColor.withValues(alpha: 0.12),
                borderRadius: BorderRadius.circular(14),
              ),
              child: Icon(record.icon, color: record.accentColor),
            ),
            const SizedBox(width: 14),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: <Widget>[
                  Text(
                    record.title,
                    style: theme.textTheme.titleMedium?.copyWith(
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                  const SizedBox(height: 6),
                  Text(record.description, style: theme.textTheme.bodyMedium),
                  const SizedBox(height: 10),
                  Text(
                    '接收时间：${record.formattedTime}',
                    style: theme.textTheme.bodySmall?.copyWith(
                      color: theme.colorScheme.onSurfaceVariant,
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
