import 'dart:async';

import 'package:basic_flutter/core/utils/logger/logger.dart';
import 'package:basic_flutter/core/utils/network/connectivity_service.dart';
import 'package:connectivity_plus/connectivity_plus.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

/// connectivity_plus
/// https://pub.dev/packages/connectivity_plus
class ConnectivityPlusDemoPage extends StatelessWidget {
  const ConnectivityPlusDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return ConnectivityPlusDemoView(title: title);
  }
}

class ConnectivityPlusDemoView extends StatefulWidget {
  const ConnectivityPlusDemoView({super.key, required this.title});

  final String title;

  @override
  State<ConnectivityPlusDemoView> createState() =>
      _ConnectivityPlusDemoViewState();
}

class _ConnectivityPlusDemoViewState extends State<ConnectivityPlusDemoView> {
  static const int _maxHistoryCount = 6;

  final ConnectivityService _connectivityService = ConnectivityService.instance;
  final List<_ConnectivityHistoryRecord> _historyRecords =
      <_ConnectivityHistoryRecord>[];

  StreamSubscription<List<ConnectivityResult>>? _subscription;

  List<ConnectivityResult> _currentResults = const <ConnectivityResult>[
    ConnectivityResult.none,
  ];
  DateTime? _lastUpdatedAt;
  String? _errorMessage;
  bool _isRefreshing = false;

  @override
  void initState() {
    super.initState();
    _subscription = _connectivityService.onConnectivityChanged.listen(
      _handleConnectivityChanged,
    );
    unawaited(_refreshConnectionStatus(source: '页面初始化'));
  }

  @override
  void dispose() {
    _subscription?.cancel();
    super.dispose();
  }

  void _handleConnectivityChanged(List<ConnectivityResult> results) {
    if (!mounted) {
      return;
    }

    _applyConnectionResults(results, source: '连接变化监听');
  }

  Future<void> _refreshConnectionStatus({required String source}) async {
    if (_isRefreshing) {
      return;
    }

    setState(() {
      _isRefreshing = true;
    });

    try {
      final List<ConnectivityResult> results = await _connectivityService
          .checkConnectivity();
      if (!mounted) {
        return;
      }

      _applyConnectionResults(results, source: source);
    } on PlatformException catch (error, stackTrace) {
      logError('Failed to check connectivity status.', error, stackTrace);
      if (!mounted) {
        return;
      }

      setState(() {
        _isRefreshing = false;
        _errorMessage = '读取网络状态失败：${error.message ?? error.code}';
      });
    }
  }

  void _applyConnectionResults(
    List<ConnectivityResult> results, {
    required String source,
  }) {
    final List<ConnectivityResult> normalizedResults = _connectivityService
        .normalizeResults(results);
    final DateTime happenedAt = DateTime.now();
    final String summary = _connectivityService.summaryOf(normalizedResults);

    logInfo('Connectivity update [$source]: $summary');

    setState(() {
      _currentResults = normalizedResults;
      _lastUpdatedAt = happenedAt;
      _errorMessage = null;
      _isRefreshing = false;
      _historyRecords.insert(
        0,
        _ConnectivityHistoryRecord(
          source: source,
          summary: summary,
          happenedAt: happenedAt,
        ),
      );
      if (_historyRecords.length > _maxHistoryCount) {
        _historyRecords.removeRange(_maxHistoryCount, _historyRecords.length);
      }
    });
  }

  bool get _hasConnection {
    return _connectivityService.hasConnection(_currentResults);
  }

  Future<void> _handleManualRefresh() async {
    await _refreshConnectionStatus(source: '手动刷新');
  }

  String _buildStatusTitle() {
    return _hasConnection ? '当前设备已连接网络' : '当前设备暂无网络';
  }

  String _buildStatusMessage() {
    if (_errorMessage != null) {
      return _errorMessage!;
    }

    if (_hasConnection) {
      return '当前检测到 ${_connectivityService.summaryOf(_currentResults)}。'
          '切换 Wi-Fi、移动网络或 VPN 时，这个页面会自动更新。';
    }

    return '当前返回结果为 none。连接网络后，页面会通过监听流自动刷新状态。';
  }

  String _buildLastUpdatedLabel() {
    if (_lastUpdatedAt == null) {
      return '等待首次读取';
    }

    return '最近更新：${_formatTime(_lastUpdatedAt!)}';
  }

  String _formatTime(DateTime dateTime) {
    final String hour = dateTime.hour.toString().padLeft(2, '0');
    final String minute = dateTime.minute.toString().padLeft(2, '0');
    final String second = dateTime.second.toString().padLeft(2, '0');
    return '$hour:$minute:$second';
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
    final Color accentColor = _hasConnection
        ? const Color(0xFF1C8A63)
        : theme.colorScheme.error;

    return ListView(
      padding: const EdgeInsets.all(16),
      children: <Widget>[
        const _SectionCard(
          title: 'connectivity_plus 示例',
          subtitle: '这个页面只演示两件事：读取当前网络类型、监听网络状态变化。',
          child: Wrap(
            spacing: 8,
            runSpacing: 8,
            children: <Widget>[
              _FeatureChip(label: 'checkConnectivity()'),
              _FeatureChip(label: 'onConnectivityChanged'),
            ],
          ),
        ),
        const SizedBox(height: 16),
        _SectionCard(
          title: _buildStatusTitle(),
          subtitle: _buildStatusMessage(),
          accentColor: accentColor,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: <Widget>[
              Row(
                children: <Widget>[
                  Icon(
                    _hasConnection
                        ? Icons.wifi_tethering_rounded
                        : Icons.portable_wifi_off_rounded,
                    color: accentColor,
                    size: 28,
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Text(
                      _connectivityService.summaryOf(_currentResults),
                      style: theme.textTheme.titleMedium?.copyWith(
                        fontWeight: FontWeight.w700,
                      ),
                    ),
                  ),
                  const SizedBox(width: 12),
                  _StatusBadge(
                    label: _hasConnection ? 'ONLINE' : 'OFFLINE',
                    color: accentColor,
                  ),
                ],
              ),
              const SizedBox(height: 12),
              Text(
                _buildLastUpdatedLabel(),
                style: theme.textTheme.bodySmall?.copyWith(
                  color: theme.colorScheme.onSurfaceVariant,
                ),
              ),
              const SizedBox(height: 12),
              FilledButton.icon(
                onPressed: _isRefreshing ? null : _handleManualRefresh,
                icon: _isRefreshing
                    ? const SizedBox(
                        width: 16,
                        height: 16,
                        child: CircularProgressIndicator(strokeWidth: 2),
                      )
                    : const Icon(Icons.refresh_rounded),
                label: Text(_isRefreshing ? '刷新中...' : '刷新当前状态'),
              ),
            ],
          ),
        ),
        const SizedBox(height: 16),
        _SectionCard(
          title: '当前连接类型',
          subtitle: _hasConnection
              ? 'connectivity_plus 7.x 可能同时返回多个网络类型，例如 Wi-Fi + VPN。'
              : '没有检测到可用网络时，会返回单个 none。',
          child: Wrap(
            spacing: 8,
            runSpacing: 8,
            children: _currentResults
                .map(
                  (ConnectivityResult result) => _ConnectivityChip(
                    label: _connectivityService.labelOf(result),
                    active: result != ConnectivityResult.none,
                  ),
                )
                .toList(),
          ),
        ),
        const SizedBox(height: 16),
        _SectionCard(
          title: '最近变化记录',
          subtitle: '保留最近 6 次状态更新，方便观察监听流是否生效。',
          child: _historyRecords.isEmpty
              ? Text(
                  '暂无变化记录。首次状态读取完成后，这里会出现一条初始化记录。',
                  style: theme.textTheme.bodyMedium,
                )
              : Column(
                  children: _historyRecords
                      .map(
                        (_ConnectivityHistoryRecord record) => Padding(
                          padding: const EdgeInsets.only(bottom: 12),
                          child: _HistoryTile(record: record),
                        ),
                      )
                      .toList(),
                ),
        ),
        const SizedBox(height: 16),
        const _SectionCard(
          title: '使用提示',
          subtitle:
              '这个包反映的是设备当前网络接口状态，不代表一定能成功访问外网。'
              '如果你的业务需要真正判断服务可用性，仍然要结合实际请求结果来确认。',
          child: SizedBox.shrink(),
        ),
      ],
    );
  }
}

class _ConnectivityHistoryRecord {
  const _ConnectivityHistoryRecord({
    required this.source,
    required this.summary,
    required this.happenedAt,
  });

  final String source;
  final String summary;
  final DateTime happenedAt;

  String get formattedTime {
    final String hour = happenedAt.hour.toString().padLeft(2, '0');
    final String minute = happenedAt.minute.toString().padLeft(2, '0');
    final String second = happenedAt.second.toString().padLeft(2, '0');
    return '$hour:$minute:$second';
  }
}

class _SectionCard extends StatelessWidget {
  const _SectionCard({
    required this.title,
    required this.subtitle,
    required this.child,
    this.accentColor = const Color(0xFF0F5DAA),
  });

  final String title;
  final String subtitle;
  final Widget child;
  final Color accentColor;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return DecoratedBox(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: accentColor.withValues(alpha: 0.16)),
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
              style: theme.textTheme.bodyMedium?.copyWith(
                color: theme.colorScheme.onSurfaceVariant,
              ),
            ),
            const SizedBox(height: 16),
            child,
          ],
        ),
      ),
    );
  }
}

class _FeatureChip extends StatelessWidget {
  const _FeatureChip({required this.label});

  final String label;

  @override
  Widget build(BuildContext context) {
    return DecoratedBox(
      decoration: BoxDecoration(
        color: const Color(0xFFE8F1FB),
        borderRadius: BorderRadius.circular(999),
      ),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
        child: Text(label),
      ),
    );
  }
}

class _StatusBadge extends StatelessWidget {
  const _StatusBadge({required this.label, required this.color});

  final String label;
  final Color color;

  @override
  Widget build(BuildContext context) {
    return DecoratedBox(
      decoration: BoxDecoration(
        color: color.withValues(alpha: 0.12),
        borderRadius: BorderRadius.circular(999),
      ),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
        child: Text(
          label,
          style: TextStyle(color: color, fontWeight: FontWeight.w700),
        ),
      ),
    );
  }
}

class _ConnectivityChip extends StatelessWidget {
  const _ConnectivityChip({required this.label, required this.active});

  final String label;
  final bool active;

  @override
  Widget build(BuildContext context) {
    final Color color = active
        ? const Color(0xFF1C8A63)
        : const Color(0xFF5F6B7A);

    return DecoratedBox(
      decoration: BoxDecoration(
        color: color.withValues(alpha: 0.1),
        borderRadius: BorderRadius.circular(999),
        border: Border.all(color: color.withValues(alpha: 0.2)),
      ),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
        child: Text(
          label,
          style: TextStyle(color: color, fontWeight: FontWeight.w600),
        ),
      ),
    );
  }
}

class _HistoryTile extends StatelessWidget {
  const _HistoryTile({required this.record});

  final _ConnectivityHistoryRecord record;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return DecoratedBox(
      decoration: BoxDecoration(
        color: const Color(0xFFF6F8FB),
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: const Color(0xFFDCE4EF)),
      ),
      child: Padding(
        padding: const EdgeInsets.all(12),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            const Icon(Icons.network_check_rounded, color: Color(0xFF0F5DAA)),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: <Widget>[
                  Text(
                    record.summary,
                    style: theme.textTheme.bodyLarge?.copyWith(
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    '${record.source} · ${record.formattedTime}',
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
