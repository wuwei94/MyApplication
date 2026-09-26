import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';
import 'package:flutter_slidable/flutter_slidable.dart';

/// Slidable — 列表项滑动操作
///
/// 核心机制与避坑点：
/// 1. 动作区：`startActionPane` / `endActionPane` 声明左右滑出动作。
/// 2. 控制器：`SlidableController` 可程序化展开 / 关闭动作区。
///
/// 官方参考：
/// https://pub.dev/packages/flutter_slidable
class SlidableDemoPage extends BasicLayoutPage {
  const SlidableDemoPage({super.key, required super.title});

  @override
  BasicLayoutPageState<SlidableDemoPage> createState() =>
      _SlidableDemoPageState();
}

class _SlidableDemoPageState extends BasicLayoutPageState<SlidableDemoPage>
    with SingleTickerProviderStateMixin {
  late final SlidableController _slidableController;
  final List<String> _items = <String>['邮件 A', '邮件 B', '邮件 C', '邮件 D'];
  int _nextId = 5;

  @override
  void initState() {
    super.initState();
    _slidableController = SlidableController(this);
  }

  @override
  List<String> buildList() => const <String>[
        '1. 展开第一项右侧动作',
        '2. 追加一条列表项',
        '3. 重置列表',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        _openFirstEndActions();
      case 1:
        setState(() {
          _items.add('邮件 ${_nextId++}');
        });
      case 2:
        setState(() {
          _items
            ..clear()
            ..addAll(<String>['邮件 A', '邮件 B', '邮件 C', '邮件 D']);
          _nextId = 5;
        });
    }
  }

  Future<void> _openFirstEndActions() async {
    if (_items.isEmpty) {
      return;
    }
    await _slidableController.openEndActionPane();
  }

  @override
  Widget buildPreview() {
    return ListView.builder(
      padding: const EdgeInsets.symmetric(vertical: 8),
      itemCount: _items.length,
      itemBuilder: (BuildContext context, int index) {
        final String title = _items[index];
        return Padding(
          padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
          child: Slidable(
            controller: index == 0 ? _slidableController : null,
            startActionPane: ActionPane(
              motion: const ScrollMotion(),
              children: <Widget>[
                SlidableAction(
                  onPressed: (_) {
                    setState(() {
                      _items.removeAt(index);
                    });
                  },
                  backgroundColor: Colors.green,
                  foregroundColor: Colors.white,
                  icon: Icons.archive,
                  label: '归档',
                ),
              ],
            ),
            endActionPane: ActionPane(
              motion: const ScrollMotion(),
              children: <Widget>[
                SlidableAction(
                  onPressed: (_) {
                    setState(() {
                      _items.removeAt(index);
                    });
                  },
                  backgroundColor: Colors.red,
                  foregroundColor: Colors.white,
                  icon: Icons.delete,
                  label: '删除',
                ),
              ],
            ),
            child: Card(
              margin: EdgeInsets.zero,
              child: ListTile(title: Text(title)),
            ),
          ),
        );
      },
    );
  }
}
