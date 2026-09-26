import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';
import 'package:infinite_scroll_pagination/infinite_scroll_pagination.dart';

/// Infinite Scroll Pagination — 分页列表状态机
///
/// 核心机制与避坑点：
/// 1. 分页键：`getNextPageKey` + `fetchPage` 驱动 `PagingController`，无更多页时返回 `null`。
/// 2. 刷新契约：`refresh()` 重置到第一页；错误态通过 `value.error` 暴露。
///
/// 官方参考：
/// https://pub.dev/packages/infinite_scroll_pagination
class InfiniteScrollPaginationDemoPage extends BasicLayoutPage {
  const InfiniteScrollPaginationDemoPage({super.key, required super.title});

  @override
  BasicLayoutPageState<InfiniteScrollPaginationDemoPage> createState() =>
      _InfiniteScrollPaginationDemoPageState();
}

class _InfiniteScrollPaginationDemoPageState
    extends BasicLayoutPageState<InfiniteScrollPaginationDemoPage> {
  static const int _pageSize = 10;
  static const int _maxPage = 3;

  late final PagingController<int, _Item> _pagingController;

  @override
  void initState() {
    super.initState();
    _pagingController = PagingController<int, _Item>(
      getNextPageKey: (PagingState<int, _Item> state) {
        final int loadedPages = state.keys?.length ?? 0;
        if (loadedPages >= _maxPage) {
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

  @override
  List<String> buildList() => const <String>[
        '1. 刷新到第一页',
        '2. 抛出分页错误',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        _pagingController.refresh();
      case 1:
        _pagingController.value = _pagingController.value.copyWith(
          error: '示例分页错误',
        );
    }
  }

  @override
  Widget buildPreview() {
    return PagingListener<int, _Item>(
      controller: _pagingController,
      builder: (
        BuildContext context,
        PagingState<int, _Item> state,
        NextPageCallback fetchNextPage,
      ) {
        return PagedListView<int, _Item>(
          state: state,
          fetchNextPage: fetchNextPage,
          builderDelegate: PagedChildBuilderDelegate<_Item>(
            itemBuilder: (BuildContext context, _Item item, int index) {
              return Card(
                margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 6),
                child: ListTile(
                  title: Text(item.title),
                  subtitle: Text('id=${item.id}'),
                ),
              );
            },
            firstPageProgressIndicatorBuilder: (_) =>
                const Center(child: CircularProgressIndicator()),
            newPageProgressIndicatorBuilder: (_) =>
                const Center(child: CircularProgressIndicator()),
            firstPageErrorIndicatorBuilder: (_) => Center(
              child: Text('加载失败：${state.error}'),
            ),
            noItemsFoundIndicatorBuilder: (_) => const Center(child: Text('暂无数据')),
            noMoreItemsIndicatorBuilder: (_) => const Center(child: Text('已经到底了')),
          ),
        );
      },
    );
  }

  Future<List<_Item>> _fetchPage(int pageKey) async {
    await Future<void>.delayed(const Duration(milliseconds: 400));
    return List<_Item>.generate(
      _pageSize,
      (int index) {
        final int id = pageKey + index;
        return _Item(id: id, title: 'Item #$id');
      },
      growable: false,
    );
  }
}

class _Item {
  const _Item({required this.id, required this.title});

  final int id;
  final String title;
}
