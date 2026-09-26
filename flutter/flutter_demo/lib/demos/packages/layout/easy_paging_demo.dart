import 'package:easy_paging/easy_paging.dart';
import 'package:easy_refresh/easy_refresh.dart';
import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';

/// EasyPaging — 分页加载状态机
///
/// 核心机制与避坑点：
/// 1. 状态封装：`EasyPaging` 自动维护 refresh / loadMore / noMore，用 `total` 与 `count` 判定结束。
/// 2. 刷新语义：`refreshOnStart` + 手动刷新都回到第一页。
///
/// 官方参考：
/// https://pub.dev/packages/easy_paging
class EasyPagingDemoPage extends BasicLayoutPage {
  const EasyPagingDemoPage({super.key, required super.title});

  @override
  BasicLayoutPageState<EasyPagingDemoPage> createState() =>
      _EasyPagingDemoPageState();
}

class _EasyPagingDemoPageState extends BasicLayoutPageState<EasyPagingDemoPage> {
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
  List<String> buildList() => const <String>[
        '1. 触发下拉刷新',
        '2. 触发上拉加载',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        _controller.callRefresh();
      case 1:
        _controller.callLoad();
    }
  }

  @override
  Widget buildPreview() {
    return _EasyPagingList(controller: _controller);
  }
}

class _EasyPagingList
    extends EasyPaging<List<_PagingItem>, _PagingItem> {
  const _EasyPagingList({required EasyRefreshController controller})
      : super(controller: controller, refreshOnStart: true);

  @override
  EasyPagingState<List<_PagingItem>, _PagingItem, _EasyPagingList> createState() =>
      _EasyPagingListState();
}

class _EasyPagingListState
    extends EasyPagingState<List<_PagingItem>, _PagingItem, _EasyPagingList> {
  static const int _pageSize = 6;
  static const int _totalCount = 24;

  List<_PagingItem> _buildPage(int pageNumber) {
    final int start = (pageNumber - 1) * _pageSize;
    final int end = (start + _pageSize).clamp(0, _totalCount);
    return List<_PagingItem>.generate(end - start, (int index) {
      final int id = start + index;
      return _PagingItem(id: id, title: 'Item #$id');
    }, growable: false);
  }

  @override
  int get count => data?.length ?? 0;

  @override
  bool get enableLoad => data != null && !isNoMore;

  @override
  int? get page => data == null ? null : ((data!.length / _pageSize).ceil());

  @override
  int? get total => _totalCount;

  @override
  int? get totalPage => (_totalCount / _pageSize).ceil();

  @override
  _PagingItem getItem(int index) => data![index];

  @override
  Future<IndicatorResult?> onRefresh() async {
    await Future<void>.delayed(const Duration(milliseconds: 400));
    data = _buildPage(1);
    setState(() {});
    return IndicatorResult.success;
  }

  @override
  Future<IndicatorResult?> onLoad() async {
    await Future<void>.delayed(const Duration(milliseconds: 400));
    final int current = data?.length ?? 0;
    if (current >= _totalCount) {
      return IndicatorResult.noMore;
    }
    final int nextPage = (current ~/ _pageSize) + 1;
    data = <_PagingItem>[...data ?? <_PagingItem>[], ..._buildPage(nextPage)];
    setState(() {});
    return IndicatorResult.success;
  }

  @override
  Widget buildItem(BuildContext context, int index, _PagingItem item) {
    return Card(
      margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 6),
      child: ListTile(
        title: Text(item.title),
        subtitle: Text('id=${item.id}'),
      ),
    );
  }
}

class _PagingItem {
  const _PagingItem({required this.id, required this.title});

  final int id;
  final String title;
}
