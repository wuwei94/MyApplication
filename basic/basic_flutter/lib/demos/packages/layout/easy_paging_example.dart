import 'package:easy_paging/easy_paging.dart';
import 'package:easy_refresh/easy_refresh.dart';
import 'package:flutter/material.dart';

/// EasyPaging
/// https://pub.dev/packages/easy_paging
class EasyPagingDemoPage extends StatelessWidget {
  const EasyPagingDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return EasyPagingDemoView(title: title);
  }
}

class EasyPagingDemoView extends StatefulWidget {
  const EasyPagingDemoView({super.key, required this.title});

  final String title;

  @override
  State<EasyPagingDemoView> createState() => _EasyPagingDemoViewState();
}

class _EasyPagingDemoViewState extends State<EasyPagingDemoView> {
  late final EasyRefreshController _controller;

  @override
  void initState() {
    super.initState();
    _controller = EasyRefreshController();
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
      body: _EasyPagingPackageDemo(controller: _controller),
    );
  }
}

class _EasyPagingPackageDemo
    extends EasyPaging<List<_EasyPagingItem>, _EasyPagingItem> {
  const _EasyPagingPackageDemo({required EasyRefreshController controller})
    : super(controller: controller, refreshOnStart: true);

  @override
  EasyPagingState<
    List<_EasyPagingItem>,
    _EasyPagingItem,
    _EasyPagingPackageDemo
  >
  createState() => _EasyPagingPackageDemoState();
}

class _EasyPagingPackageDemoState
    extends
        EasyPagingState<
          List<_EasyPagingItem>,
          _EasyPagingItem,
          _EasyPagingPackageDemo
        > {
  static const Color _accentColor = Color(0xFF0F766E);
  static const int _pageSize = 6;
  static const int _totalCount = 24;
  static const List<Color> _cardColors = <Color>[
    Color(0xFFE0F2FE),
    Color(0xFFDCFCE7),
    Color(0xFFFEF3C7),
    Color(0xFFFCE7F3),
  ];
  static const List<IconData> _cardIcons = <IconData>[
    Icons.filter_list_rounded,
    Icons.auto_awesome_mosaic_rounded,
    Icons.swipe_down_alt_rounded,
    Icons.rule_folder_rounded,
  ];
  static const List<String> _topics = <String>[
    'Catalog 注册',
    '分页状态',
    '滚动体验',
    'UI 结构',
  ];
  static const List<String> _descriptions = <String>[
    '模拟从服务端按页拉取目录卡片，并在列表尾部继续加载更多内容。',
    '用 total 与 count 自动判断 noMore，无需额外手动维护结束态。',
    '刷新后自动回到第一页，适合演示 refreshOnStart 与手动触发刷新。',
    '列表项使用统一卡片风格，方便在 Demo Catalog 中直接观察分页反馈。',
  ];

  DateTime? _lastUpdatedAt;
  int? _pageNumber;
  String _statusMessage = '页面首次展示会自动触发 refreshOnStart，滚动到底部继续加载更多。';

  @override
  int get count => data?.length ?? 0;

  @override
  bool get enableLoad => data != null && !isNoMore;

  @override
  int? get page => _pageNumber;

  @override
  int? get total => _totalCount;

  @override
  int? get totalPage => (_totalCount / _pageSize).ceil();

  @override
  _EasyPagingItem getItem(int index) {
    return data![index];
  }

  @override
  Header buildHeader() {
    return const ClassicHeader(
      dragText: '继续下拉即可刷新',
      armedText: '松开后开始刷新',
      readyText: '正在刷新分页数据...',
      processingText: '正在刷新分页数据...',
      processedText: '刷新完成',
      failedText: '刷新失败',
      noMoreText: '当前没有更多刷新动作',
      messageText: '最后更新时间 %T',
    );
  }

  @override
  Footer buildFooter() {
    return const ClassicFooter(
      dragText: '继续上拉即可加载更多',
      armedText: '松开后开始加载',
      readyText: '正在加载下一页...',
      processingText: '正在加载下一页...',
      processedText: '加载完成',
      failedText: '加载失败',
      noMoreText: '已经到底了',
      messageText: '最后更新时间 %T',
    );
  }

  @override
  Widget? buildEmptyWidget() {
    return Center(
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 24),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: <Widget>[
            Icon(Icons.inbox_outlined, color: Colors.teal.shade300, size: 36),
            const SizedBox(height: 12),
            const Text('当前没有分页数据，试试下拉刷新重新拉取。', textAlign: TextAlign.center),
          ],
        ),
      ),
    );
  }

  @override
  Widget? buildRefreshOnStartWidget() {
    return const ColoredBox(
      color: Colors.white,
      child: Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: <Widget>[
            CircularProgressIndicator.adaptive(),
            SizedBox(height: 12),
            Text('EasyPaging 正在初始化第一页...'),
          ],
        ),
      ),
    );
  }

  @override
  Widget buildItem(BuildContext context, int index, _EasyPagingItem item) {
    return _EasyPagingItemCard(item: item, accentColor: _accentColor);
  }

  @override
  Widget buildSliver() {
    return SliverPadding(
      padding: const EdgeInsets.fromLTRB(16, 16, 16, 24),
      sliver: SliverList(
        delegate: SliverChildBuilderDelegate((BuildContext context, int index) {
          if (index == 0) {
            return _buildOverviewCard(context);
          }

          final _EasyPagingItem item = getItem(index - 1);
          return Padding(
            padding: const EdgeInsets.only(top: 12),
            child: buildItem(context, index - 1, item),
          );
        }, childCount: count + 1),
      ),
    );
  }

  Future<void> _triggerRefresh() async {
    final EasyRefreshController controller = widget.controller!;
    setState(() {
      _statusMessage = '正在通过 EasyRefreshController 主动触发刷新...';
    });
    await controller.callRefresh();
  }

  Future<void> _triggerLoad() async {
    if (isNoMore) {
      setState(() {
        _statusMessage = '当前已经没有更多数据了，可以先刷新后再重新体验分页。';
      });
      return;
    }

    final EasyRefreshController controller = widget.controller!;
    setState(() {
      _statusMessage = '正在通过 EasyRefreshController 主动触发加载更多...';
    });
    await controller.callLoad();
  }

  @override
  Future<IndicatorResult> onRefresh() async {
    await Future<void>.delayed(const Duration(milliseconds: 900));

    final DateTime updatedAt = DateTime.now();
    final List<_EasyPagingItem> firstPageItems = _buildPage(
      pageNumber: 1,
      updatedAt: updatedAt,
    );

    if (!mounted) {
      return IndicatorResult.success;
    }

    setState(() {
      data = firstPageItems;
      _pageNumber = 1;
      _lastUpdatedAt = updatedAt;
      _statusMessage = '刷新完成，列表已经重置到第一页。';
    });
    return IndicatorResult.success;
  }

  @override
  Future<IndicatorResult> onLoad() async {
    await Future<void>.delayed(const Duration(milliseconds: 800));

    if (isNoMore) {
      if (!mounted) {
        return IndicatorResult.noMore;
      }

      setState(() {
        _statusMessage = '所有分页数据已经加载完毕。';
      });
      return IndicatorResult.noMore;
    }

    final int nextPage = (_pageNumber ?? 0) + 1;
    final DateTime updatedAt = DateTime.now();
    final List<_EasyPagingItem> nextItems = _buildPage(
      pageNumber: nextPage,
      updatedAt: updatedAt,
    );

    if (!mounted) {
      return IndicatorResult.success;
    }

    final List<_EasyPagingItem> currentItems = data ?? <_EasyPagingItem>[];
    final List<_EasyPagingItem> mergedItems = <_EasyPagingItem>[
      ...currentItems,
      ...nextItems,
    ];
    final bool willReachLastPage = mergedItems.length >= _totalCount;

    setState(() {
      data = mergedItems;
      _pageNumber = nextPage;
      _lastUpdatedAt = updatedAt;
      _statusMessage = willReachLastPage
          ? '第 $nextPage 页加载完成，这也是最后一页。'
          : '第 $nextPage 页加载完成，继续上拉可以获取更多内容。';
    });
    return willReachLastPage ? IndicatorResult.noMore : IndicatorResult.success;
  }

  Widget _buildOverviewCard(BuildContext context) {
    final ThemeData theme = Theme.of(context);
    final String pageLabel = _pageNumber == null
        ? '-- / $totalPage'
        : '$_pageNumber / $totalPage';
    final String updatedAtLabel = _lastUpdatedAt == null
        ? '等待首次刷新'
        : _formatDateTime(_lastUpdatedAt!);

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
                    Icons.auto_awesome_mosaic_rounded,
                    color: Colors.white,
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: <Widget>[
                      Text(
                        'easy_paging',
                        style: theme.textTheme.titleMedium?.copyWith(
                          fontWeight: FontWeight.w700,
                        ),
                      ),
                      const SizedBox(height: 6),
                      Text(
                        '这个示例展示了 EasyPaging 如何基于 easy_refresh 承接 refresh / load 生命周期，并自动根据 total 判断 noMore。',
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
                _MetricChip(label: '当前页', value: pageLabel),
                _MetricChip(label: '已加载', value: '$count / $_totalCount'),
                _MetricChip(label: '最近更新', value: updatedAtLabel),
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
            Wrap(
              spacing: 12,
              runSpacing: 12,
              children: <Widget>[
                FilledButton.icon(
                  onPressed: _triggerRefresh,
                  icon: const Icon(Icons.play_circle_outline_rounded),
                  label: const Text('代码触发刷新'),
                ),
                OutlinedButton.icon(
                  onPressed: _triggerLoad,
                  icon: const Icon(Icons.download_rounded),
                  label: const Text('代码触发加载'),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  List<_EasyPagingItem> _buildPage({
    required int pageNumber,
    required DateTime updatedAt,
  }) {
    final int start = (pageNumber - 1) * _pageSize;
    final int remaining = _totalCount - start;
    if (remaining <= 0) {
      return <_EasyPagingItem>[];
    }

    final int itemCount = remaining < _pageSize ? remaining : _pageSize;
    return List<_EasyPagingItem>.generate(itemCount, (int offset) {
      final int index = start + offset;
      final int paletteIndex = index % _cardColors.length;
      return _EasyPagingItem(
        index: index + 1,
        title: 'Paging Card ${index + 1}',
        topic: _topics[index % _topics.length],
        description: _descriptions[index % _descriptions.length],
        icon: _cardIcons[paletteIndex],
        color: _cardColors[paletteIndex],
        updatedAtLabel: _formatDateTime(updatedAt),
      );
    });
  }

  String _formatDateTime(DateTime value) {
    final String month = value.month.toString().padLeft(2, '0');
    final String day = value.day.toString().padLeft(2, '0');
    final String hour = value.hour.toString().padLeft(2, '0');
    final String minute = value.minute.toString().padLeft(2, '0');
    return '$month-$day $hour:$minute';
  }
}

class _EasyPagingItem {
  const _EasyPagingItem({
    required this.index,
    required this.title,
    required this.topic,
    required this.description,
    required this.icon,
    required this.color,
    required this.updatedAtLabel,
  });

  final int index;
  final String title;
  final String topic;
  final String description;
  final IconData icon;
  final Color color;
  final String updatedAtLabel;
}

class _EasyPagingItemCard extends StatelessWidget {
  const _EasyPagingItemCard({required this.item, required this.accentColor});

  final _EasyPagingItem item;
  final Color accentColor;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return DecoratedBox(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: const Color(0xFFD1FAE5)),
        boxShadow: const <BoxShadow>[
          BoxShadow(
            color: Color(0x140F172A),
            blurRadius: 20,
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
                color: item.color,
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
                    style: theme.textTheme.titleSmall?.copyWith(
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                  const SizedBox(height: 6),
                  Text(
                    item.description,
                    style: theme.textTheme.bodyMedium?.copyWith(
                      color: Colors.black54,
                      height: 1.4,
                    ),
                  ),
                  const SizedBox(height: 12),
                  Wrap(
                    spacing: 10,
                    runSpacing: 8,
                    children: <Widget>[
                      _MetaBadge(
                        icon: Icons.tag_rounded,
                        label: '第 ${item.index} 项',
                      ),
                      _MetaBadge(
                        icon: Icons.grid_view_rounded,
                        label: item.topic,
                      ),
                      _MetaBadge(
                        icon: Icons.schedule_rounded,
                        label: item.updatedAtLabel,
                      ),
                    ],
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
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: const Color(0xFFCCFBF1)),
      ),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
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

class _MetaBadge extends StatelessWidget {
  const _MetaBadge({required this.icon, required this.label});

  final IconData icon;
  final String label;

  @override
  Widget build(BuildContext context) {
    return DecoratedBox(
      decoration: BoxDecoration(
        color: const Color(0xFFF0FDFA),
        borderRadius: BorderRadius.circular(999),
      ),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          children: <Widget>[
            Icon(icon, size: 16, color: const Color(0xFF0F766E)),
            const SizedBox(width: 6),
            Text(label),
          ],
        ),
      ),
    );
  }
}
