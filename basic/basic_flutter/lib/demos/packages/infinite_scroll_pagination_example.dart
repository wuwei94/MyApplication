import 'dart:async';

import 'package:flutter/material.dart';
import 'package:infinite_scroll_pagination/infinite_scroll_pagination.dart';

/// infinite_scroll_pagination
/// https://pub.dev/packages/infinite_scroll_pagination
class InfiniteScrollPaginationDemoPage extends StatelessWidget {
  const InfiniteScrollPaginationDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return InfiniteScrollPaginationDemoView(title: title);
  }
}

class InfiniteScrollPaginationDemoView extends StatefulWidget {
  const InfiniteScrollPaginationDemoView({super.key, required this.title});

  final String title;

  @override
  State<InfiniteScrollPaginationDemoView> createState() =>
      _InfiniteScrollPaginationDemoViewState();
}

class _InfiniteScrollPaginationDemoViewState
    extends State<InfiniteScrollPaginationDemoView> {
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

  late final PagingController<int, _PaginationItem> _pagingController;
  late DateTime _lastUpdatedAt;

  int _page = 1;
  String _statusMessage = '下拉刷新会重置列表，继续向下滚动会自动追加新数据。';

  @override
  void initState() {
    super.initState();
    _lastUpdatedAt = DateTime.now();
    _pagingController = PagingController<int, _PaginationItem>(
      getNextPageKey: (PagingState<int, _PaginationItem> state) {
        final int currentPageCount = state.keys?.length ?? 0;
        if (currentPageCount >= _maxPage) {
          return null;
        }
        return state.nextIntPageKey;
      },
      fetchPage: _fetchPage,
    );
  }

  @override
  void dispose() {
    _pagingController.dispose();
    super.dispose();
  }

  Future<List<_PaginationItem>> _fetchPage(int pageKey) async {
    await Future<void>.value();

    final DateTime updatedAt = DateTime.now();
    final List<_PaginationItem> items = _buildItems(
      page: pageKey,
      updatedAt: updatedAt,
    );

    if (mounted) {
      final bool isLastPage = pageKey >= _maxPage;
      setState(() {
        _page = pageKey;
        _lastUpdatedAt = updatedAt;
        _statusMessage = isLastPage
            ? '第 $pageKey 页已加载完成，这也是最后一页。'
            : pageKey == 1
            ? '刷新完成，列表已回到第一页。'
            : '已成功加载第 $pageKey 页数据。';
      });
    }

    return items;
  }

  Future<void> _triggerRefresh() async {
    setState(() {
      _statusMessage = '正在通过 PagingController 主动触发刷新...';
    });
    await _handleRefresh();
  }

  Future<void> _handleRefresh() async {
    final Completer<void> completer = Completer<void>();

    void listener() {
      final PagingState<int, _PaginationItem> state = _pagingController.value;
      final bool refreshFinished =
          !state.isLoading && state.status != PagingStatus.loadingFirstPage;

      if (refreshFinished && !completer.isCompleted) {
        _pagingController.removeListener(listener);
        completer.complete();
      }
    }

    setState(() {
      _page = 1;
      _statusMessage = '正在重新请求第一页数据...';
    });

    _pagingController.addListener(listener);
    _pagingController.refresh();
    listener();

    await completer.future;
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(widget.title)),
      body: getBody(),
    );
  }

  Widget getBody() {
    return PagingListener<int, _PaginationItem>(
      controller: _pagingController,
      builder:
          (
            BuildContext context,
            PagingState<int, _PaginationItem> state,
            NextPageCallback fetchNextPage,
          ) {
            return RefreshIndicator(
              onRefresh: _handleRefresh,
              child: CustomScrollView(
                physics: const AlwaysScrollableScrollPhysics(
                  parent: BouncingScrollPhysics(),
                ),
                slivers: <Widget>[
                  SliverToBoxAdapter(
                    child: Padding(
                      padding: const EdgeInsets.fromLTRB(16, 16, 16, 12),
                      child: _buildOverviewCard(context, state),
                    ),
                  ),
                  SliverPadding(
                    padding: const EdgeInsets.fromLTRB(16, 0, 16, 24),
                    sliver: PagedSliverList<int, _PaginationItem>.separated(
                      state: state,
                      fetchNextPage: fetchNextPage,
                      separatorBuilder: (BuildContext context, int index) {
                        return const SizedBox(height: 12);
                      },
                      builderDelegate:
                          PagedChildBuilderDelegate<_PaginationItem>(
                            itemBuilder:
                                (
                                  BuildContext context,
                                  _PaginationItem item,
                                  int index,
                                ) {
                                  return _PaginationItemCard(
                                    item: item,
                                    accentColor: _accentColor,
                                  );
                                },
                            firstPageProgressIndicatorBuilder:
                                _buildFirstPageLoadingIndicator,
                            newPageProgressIndicatorBuilder:
                                _buildNewPageLoadingIndicator,
                            firstPageErrorIndicatorBuilder:
                                _buildFirstPageErrorIndicator,
                            newPageErrorIndicatorBuilder:
                                _buildNewPageErrorIndicator,
                            noItemsFoundIndicatorBuilder:
                                _buildNoItemsIndicator,
                            noMoreItemsIndicatorBuilder:
                                _buildNoMoreItemsIndicator,
                            invisibleItemsThreshold: 0,
                          ),
                    ),
                  ),
                ],
              ),
            );
          },
    );
  }

  Widget _buildOverviewCard(
    BuildContext context,
    PagingState<int, _PaginationItem> state,
  ) {
    final ThemeData theme = Theme.of(context);
    final int loadedCount = state.items?.length ?? 0;

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
                        'infinite_scroll_pagination',
                        style: theme.textTheme.titleMedium?.copyWith(
                          fontWeight: FontWeight.w700,
                        ),
                      ),
                      const SizedBox(height: 6),
                      Text(
                        '这个示例展示了下拉刷新、自动分页加载，以及通过 controller 主动触发刷新。',
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
                _MetricChip(label: '列表项', value: '$loadedCount'),
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

  Widget _buildFirstPageLoadingIndicator(BuildContext context) {
    return const _StatusCard(
      title: '正在加载第一页',
      subtitle: '第一页会自动触发请求，稍等一下就能看到列表内容。',
      badge: 'Loading',
      color: Color(0xFFE0F2FE),
      icon: Icons.hourglass_top_rounded,
    );
  }

  Widget _buildNewPageLoadingIndicator(BuildContext context) {
    return const Padding(
      padding: EdgeInsets.only(top: 4, bottom: 16),
      child: _StatusCard(
        title: '正在加载更多',
        subtitle: '继续向下滚动时，分页组件会自动请求下一页数据。',
        badge: 'Loading',
        color: Color(0xFFDCFCE7),
        icon: Icons.more_horiz_rounded,
      ),
    );
  }

  Widget _buildFirstPageErrorIndicator(BuildContext context) {
    return _ActionStatusCard(
      title: '第一页加载失败',
      subtitle: '可以下拉刷新，也可以点击下面的按钮重新请求第一页。',
      badge: 'Retry',
      color: const Color(0xFFFCE7F3),
      icon: Icons.error_outline_rounded,
      buttonLabel: '重试第一页',
      onPressed: _pagingController.fetchNextPage,
    );
  }

  Widget _buildNewPageErrorIndicator(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(top: 4, bottom: 16),
      child: _ActionStatusCard(
        title: '后续分页加载失败',
        subtitle: '当前列表内容还在，点击按钮可以继续请求下一页。',
        badge: 'Retry',
        color: const Color(0xFFFCE7F3),
        icon: Icons.error_outline_rounded,
        buttonLabel: '继续重试',
        onPressed: _pagingController.fetchNextPage,
      ),
    );
  }

  Widget _buildNoItemsIndicator(BuildContext context) {
    return _ActionStatusCard(
      title: '当前没有内容',
      subtitle: '这个状态通常用于搜索无结果或筛选后没有命中的场景。',
      badge: 'Empty',
      color: const Color(0xFFFEF3C7),
      icon: Icons.search_off_rounded,
      buttonLabel: '重新请求第一页',
      onPressed: _pagingController.refresh,
    );
  }

  Widget _buildNoMoreItemsIndicator(BuildContext context) {
    return const Padding(
      padding: EdgeInsets.only(top: 4, bottom: 16),
      child: _StatusCard(
        title: '已经到底了',
        subtitle: '所有分页数据都已加载完成，可以下拉刷新重新开始。',
        badge: 'Done',
        color: Color(0xFFDCFCE7),
        icon: Icons.check_circle_outline_rounded,
      ),
    );
  }

  List<_PaginationItem> _buildItems({
    required int page,
    required DateTime updatedAt,
  }) {
    return List<_PaginationItem>.generate(_pageSize, (int index) {
      final int number = (page - 1) * _pageSize + index + 1;
      return _PaginationItem(
        title: 'InfiniteScrollPagination Item $number',
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

class _PaginationItemCard extends StatelessWidget {
  const _PaginationItemCard({required this.item, required this.accentColor});

  final _PaginationItem item;
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

class _StatusCard extends StatelessWidget {
  const _StatusCard({
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

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return DecoratedBox(
      decoration: BoxDecoration(
        color: color,
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
              child: Icon(icon, color: const Color(0xFF0F766E)),
            ),
            const SizedBox(width: 14),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: <Widget>[
                  Text(
                    title,
                    style: theme.textTheme.titleMedium?.copyWith(
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                  const SizedBox(height: 6),
                  Text(
                    subtitle,
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
                  badge,
                  style: theme.textTheme.labelMedium?.copyWith(
                    color: const Color(0xFF0F766E),
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

class _ActionStatusCard extends StatelessWidget {
  const _ActionStatusCard({
    required this.title,
    required this.subtitle,
    required this.badge,
    required this.color,
    required this.icon,
    required this.buttonLabel,
    required this.onPressed,
  });

  final String title;
  final String subtitle;
  final String badge;
  final Color color;
  final IconData icon;
  final String buttonLabel;
  final VoidCallback onPressed;

  @override
  Widget build(BuildContext context) {
    return Column(
      children: <Widget>[
        _StatusCard(
          title: title,
          subtitle: subtitle,
          badge: badge,
          color: color,
          icon: icon,
        ),
        const SizedBox(height: 12),
        FilledButton.icon(
          onPressed: onPressed,
          icon: const Icon(Icons.refresh_rounded),
          label: Text(buttonLabel),
        ),
      ],
    );
  }
}

class _PaginationItem {
  const _PaginationItem({
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
