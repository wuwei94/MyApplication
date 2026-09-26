import 'package:easy_refresh/easy_refresh.dart';
import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';

/// EasyRefresh — 下拉刷新 / 上拉加载
///
/// 核心机制与避坑点：
/// 1. 双触发：`onRefresh` / `onLoad` 对应 header / footer 状态机，回调结束需完成 Future。
/// 2. 控制器：`EasyRefreshController.callRefresh` / `callLoad` 可程序化触发。
///
/// 官方参考：
/// https://pub.dev/packages/easy_refresh
class EasyRefreshDemoPage extends BasicLayoutPage {
  const EasyRefreshDemoPage({super.key, required super.title});

  @override
  BasicLayoutPageState<EasyRefreshDemoPage> createState() =>
      _EasyRefreshDemoPageState();
}

class _EasyRefreshDemoPageState
    extends BasicLayoutPageState<EasyRefreshDemoPage> {
  static const int _pageSize = 8;
  static const int _maxPage = 3;

  late final EasyRefreshController _controller;
  late List<int> _items;
  int _page = 1;

  @override
  void initState() {
    super.initState();
    _controller = EasyRefreshController();
    _items = List<int>.generate(_pageSize, (int index) => index);
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
        return ListView.builder(
          physics: physics,
          padding: const EdgeInsets.symmetric(vertical: 8),
          itemCount: _items.length,
          itemBuilder: (BuildContext context, int index) {
            return Card(
              margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 6),
              child: ListTile(
                title: Text('Item #${_items[index]}'),
                subtitle: Text('page=$_page'),
              ),
            );
          },
        );
      },
    );
  }

  Future<void> _handleRefresh() async {
    await Future<void>.delayed(const Duration(milliseconds: 500));
    if (!mounted) {
      return;
    }
    setState(() {
      _page = 1;
      _items = List<int>.generate(_pageSize, (int index) => index);
    });
  }

  Future<void> _handleLoad() async {
    await Future<void>.delayed(const Duration(milliseconds: 500));
    if (!mounted) {
      return;
    }
    if (_page >= _maxPage) {
      _controller.finishLoad(IndicatorResult.noMore);
      return;
    }
    setState(() {
      _page += 1;
      final int start = (_page - 1) * _pageSize;
      _items.addAll(List<int>.generate(_pageSize, (int i) => start + i));
    });
    _controller.finishLoad(
      _page >= _maxPage ? IndicatorResult.noMore : IndicatorResult.success,
    );
  }
}
