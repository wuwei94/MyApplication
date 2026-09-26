import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';

/// AnimatedList
/// Demonstrates animated list items
class AnimatedListDemoPage extends BasicLayoutPage {
  const AnimatedListDemoPage({super.key, required super.title});

  @override
  BasicLayoutPageState<AnimatedListDemoPage> createState() =>
      _AnimatedListDemoPageState();
}

class _AnimatedListDemoPageState
    extends BasicLayoutPageState<AnimatedListDemoPage> {
  final GlobalKey<AnimatedListState> _listKey = GlobalKey<AnimatedListState>();
  final List<String> _items = <String>['Item 1', 'Item 2', 'Item 3'];
  int _counter = 4;

  @override
  List<String> buildList() => const <String>[
        '1. 插入末尾条目',
        '2. 删除首个条目',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        _addItem();
      case 1:
        if (_items.isNotEmpty) {
          _removeItem(0);
        }
    }
  }

  void _addItem() {
    final int index = _items.length;
    _items.add('Item $_counter');
    _listKey.currentState?.insertItem(index);
    _counter++;
  }

  void _removeItem(int index) {
    final String removedItem = _items[index];
    _items.removeAt(index);
    _listKey.currentState?.removeItem(
      index,
      (BuildContext context, Animation<double> animation) =>
          _buildRemovedItem(removedItem, animation),
    );
  }

  Widget _buildRemovedItem(String item, Animation<double> animation) {
    return SizeTransition(
      sizeFactor: animation,
      child: Card(
        margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
        child: ListTile(
          title: Text(item, style: const TextStyle(color: Colors.grey)),
          leading: const Icon(Icons.delete, color: Colors.red),
        ),
      ),
    );
  }

  Widget _buildItem(String item, int index, Animation<double> animation) {
    return SizeTransition(
      sizeFactor: animation,
      child: Card(
        margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
        child: ListTile(
          title: Text(item),
          leading: CircleAvatar(child: Text('$index')),
          trailing: IconButton(
            icon: const Icon(Icons.delete),
            onPressed: () => _removeItem(index),
          ),
        ),
      ),
    );
  }

  @override
  Widget buildPreview() {
    return AnimatedList(
      key: _listKey,
      initialItemCount: _items.length,
      itemBuilder: (
        BuildContext context,
        int index,
        Animation<double> animation,
      ) {
        return _buildItem(_items[index], index, animation);
      },
    );
  }
}
