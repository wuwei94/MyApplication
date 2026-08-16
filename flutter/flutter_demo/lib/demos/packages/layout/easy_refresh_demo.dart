import 'package:easy_refresh/easy_refresh.dart';
import 'package:flutter/material.dart';

/// EasyRefresh
/// https://pub.dev/packages/flutter_easyrefresh
///
/// pub.dev 当前实际依赖名为 easy_refresh。
class EasyRefreshDemoPage extends StatelessWidget {
  const EasyRefreshDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return EasyRefreshDemoView(title: title);
  }
}

class EasyRefreshDemoView extends StatefulWidget {
  const EasyRefreshDemoView({super.key, required this.title});

  final String title;

  @override
  State<EasyRefreshDemoView> createState() => _EasyRefreshDemoViewState();
}

class _EasyRefreshDemoViewState extends State<EasyRefreshDemoView> {
  static const Color _accentColor = Color(0xFF0F766E);
  static const int _pageSize = 8;
  static const int _maxPage = 3;
  static const List<Color> _cardColors = <Color>[
    Color(0xFFE0F2FE),
    Color(0xFFDCFCE7),
    Color(0xFFFEF3C7),
    Color(0xFFFCE7F3),
  ];
  static const List<IconData> _cardIcons = <IconData>[
    Icons.refresh_rounded,
    Icons.waterfall_chart_rounded,
    Icons.view_agenda_rounded,
    Icons.swipe_down_alt_rounded,
  ];

  late final EasyRefreshController _controller;
  late DateTime _lastUpdatedAt;
  late List<_RefreshItem> _items;

  int _page = 1;
  String _statusMessage = '下拉刷新会重置列表，上拉加载会追加新数据。';

  @override
  void initState() {
    super.initState();
    _controller = EasyRefreshController();
    _lastUpdatedAt = DateTime.now();
    _items = _buildItems(page: _page, updatedAt: _lastUpdatedAt);
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(widget.title)),
      body: getBody(),
    );
  }

  Widget getBody() {
    return EasyRefresh.builder(
      controller: _controller,
      header: const ClassicHeader(
        dragText: '继续下拉即可刷新',
        armedText: '松开后开始刷新',
        readyText: '正在刷新内容...',
        processingText: '正在刷新内容...',
        processedText: '刷新完成',
        failedText: '刷新失败',
        noMoreText: '当前没有更多刷新动作',
        messageText: '最后更新时间 %T',
      ),
      footer: const ClassicFooter(
        dragText: '继续上拉即可加载更多',
        armedText: '松开后开始加载',
        readyText: '正在加载更多...',
        processingText: '正在加载更多...',
        processedText: '加载完成',
        failedText: '加载失败',
        noMoreText: '已经到底了',
        messageText: '最后更新时间 %T',
      ),
      onRefresh: _handleRefresh,
      onLoad: _handleLoad,
      childBuilder: (BuildContext context, ScrollPhysics physics) {
        return ListView.separated(
          physics: physics,
          padding: const EdgeInsets.fromLTRB(16, 16, 16, 24),
          itemCount: _items.length + 1,
          separatorBuilder: (BuildContext context, int index) {
            return const SizedBox(height: 12);
          },
          itemBuilder: (BuildContext context, int index) {
            if (index == 0) {
              return _buildOverviewCard(context);
            }

            final _RefreshItem item = _items[index - 1];
            return _RefreshItemCard(item: item, accentColor: _accentColor);
          },
        );
      },
    );
  }

  Widget _buildOverviewCard(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return DecoratedBox(
      decoration: BoxDecoration(
        color: const Color(0xFFF0FDFA),
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: const Color(0xFF99F6E4)),
      ),
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: <Widget>[
                Container(
                  width: 48,
                  height: 48,
                  decoration: BoxDecoration(
                    color: _accentColor,
                    borderRadius: BorderRadius.circular(16),
                  ),
                  alignment: Alignment.center,
                  child: const Icon(
                    Icons.swipe_down_alt_rounded,
                    color: Colors.white,
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: <Widget>[
                      Text(
                        'easy_refresh / flutter_easyrefresh',
                        style: theme.textTheme.titleMedium?.copyWith(
                          fontWeight: FontWeight.w700,
                        ),
                      ),
                      const SizedBox(height: 6),
                      Text(
                        '这个示例展示了下拉刷新、上拉加载，以及通过 controller 主动触发刷新。',
                        style: theme.textTheme.bodyMedium?.copyWith(
                          color: Colors.black54,
                          height: 1.45,
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
            const SizedBox(height: 16),
            Wrap(
              spacing: 10,
              runSpacing: 10,
              children: <Widget>[
                _MetricChip(label: '当前页', value: '$_page / $_maxPage'),
                _MetricChip(label: '列表项', value: '${_items.length}'),
                _MetricChip(
                  label: '最近更新',
                  value: _formatDateTime(_lastUpdatedAt),
                ),
              ],
            ),
            const SizedBox(height: 14),
            Text(
              _statusMessage,
              style: theme.textTheme.bodyMedium?.copyWith(
                color: const Color(0xFF115E59),
                height: 1.4,
              ),
            ),
            const SizedBox(height: 16),
            FilledButton.icon(
              onPressed: _triggerRefresh,
              icon: const Icon(Icons.play_circle_outline_rounded),
              label: const Text('代码触发一次刷新'),
            ),
          ],
        ),
      ),
    );
  }

  Future<void> _triggerRefresh() async {
    setState(() {
      _statusMessage = '正在通过 EasyRefreshController 主动触发刷新...';
    });
    await _controller.callRefresh();
  }

  Future<IndicatorResult> _handleRefresh() async {
    await Future<void>.delayed(const Duration(milliseconds: 900));

    if (!mounted) {
      return IndicatorResult.success;
    }

    final DateTime updatedAt = DateTime.now();
    setState(() {
      _page = 1;
      _lastUpdatedAt = updatedAt;
      _items = _buildItems(page: _page, updatedAt: updatedAt);
      _statusMessage = '刷新完成，列表已回到第一页。';
    });
    return IndicatorResult.success;
  }

  Future<IndicatorResult> _handleLoad() async {
    await Future<void>.delayed(const Duration(milliseconds: 800));

    if (_page >= _maxPage) {
      if (!mounted) {
        return IndicatorResult.noMore;
      }

      setState(() {
        _statusMessage = '没有更多数据了，可以继续下拉刷新重新开始。';
      });
      return IndicatorResult.noMore;
    }

    final int nextPage = _page + 1;
    final DateTime updatedAt = DateTime.now();
    final List<_RefreshItem> nextItems = _buildItems(
      page: nextPage,
      updatedAt: updatedAt,
    );

    if (!mounted) {
      return IndicatorResult.success;
    }

    final bool isLastPage = nextPage >= _maxPage;
    setState(() {
      _page = nextPage;
      _lastUpdatedAt = updatedAt;
      _items = <_RefreshItem>[..._items, ...nextItems];
      _statusMessage = isLastPage
          ? '第 $nextPage 页已加载完成，这也是最后一页。'
          : '已成功加载第 $nextPage 页数据。';
    });
    return isLastPage ? IndicatorResult.noMore : IndicatorResult.success;
  }

  List<_RefreshItem> _buildItems({
    required int page,
    required DateTime updatedAt,
  }) {
    return List<_RefreshItem>.generate(_pageSize, (int index) {
      final int number = (page - 1) * _pageSize + index + 1;
      return _RefreshItem(
        title: 'EasyRefresh Item $number',
        subtitle: '第 $page 页 · 更新时间 ${_formatTime(updatedAt)}',
        badge: page == 1 ? 'Refreshed' : 'Loaded',
        color: _cardColors[number % _cardColors.length],
        icon: _cardIcons[number % _cardIcons.length],
      );
    });
  }

  String _formatDateTime(DateTime value) {
    return '${_twoDigits(value.month)}-${_twoDigits(value.day)} '
        '${_twoDigits(value.hour)}:${_twoDigits(value.minute)}';
  }

  String _formatTime(DateTime value) {
    return '${_twoDigits(value.hour)}:${_twoDigits(value.minute)}:'
        '${_twoDigits(value.second)}';
  }

  String _twoDigits(int value) {
    return value.toString().padLeft(2, '0');
  }
}

class _MetricChip extends StatelessWidget {
  const _MetricChip({required this.label, required this.value});

  final String label;
  final String value;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return DecoratedBox(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: const Color(0xFFCCFBF1)),
      ),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          mainAxisSize: MainAxisSize.min,
          children: <Widget>[
            Text(
              label,
              style: theme.textTheme.labelMedium?.copyWith(
                color: Colors.black45,
              ),
            ),
            const SizedBox(height: 4),
            Text(
              value,
              style: theme.textTheme.titleSmall?.copyWith(
                fontWeight: FontWeight.w700,
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _RefreshItemCard extends StatelessWidget {
  const _RefreshItemCard({required this.item, required this.accentColor});

  final _RefreshItem item;
  final Color accentColor;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return DecoratedBox(
      decoration: BoxDecoration(
        color: item.color,
        borderRadius: BorderRadius.circular(18),
      ),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Row(
          children: <Widget>[
            Container(
              width: 44,
              height: 44,
              decoration: BoxDecoration(
                color: Colors.white.withValues(alpha: 0.92),
                borderRadius: BorderRadius.circular(14),
              ),
              alignment: Alignment.center,
              child: Icon(item.icon, color: accentColor),
            ),
            const SizedBox(width: 14),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: <Widget>[
                  Text(
                    item.title,
                    style: theme.textTheme.titleMedium?.copyWith(
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                  const SizedBox(height: 6),
                  Text(
                    item.subtitle,
                    style: theme.textTheme.bodySmall?.copyWith(
                      color: Colors.black54,
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(width: 12),
            DecoratedBox(
              decoration: BoxDecoration(
                color: Colors.white.withValues(alpha: 0.88),
                borderRadius: BorderRadius.circular(999),
              ),
              child: Padding(
                padding: const EdgeInsets.symmetric(
                  horizontal: 10,
                  vertical: 6,
                ),
                child: Text(
                  item.badge,
                  style: theme.textTheme.labelMedium?.copyWith(
                    color: accentColor,
                    fontWeight: FontWeight.w700,
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _RefreshItem {
  const _RefreshItem({
    required this.title,
    required this.subtitle,
    required this.badge,
    required this.color,
    required this.icon,
  });

  final String title;
  final String subtitle;
  final String badge;
  final Color color;
  final IconData icon;
}
