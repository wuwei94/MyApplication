import 'package:flutter/material.dart';

/// AnimatedList Example
/// Demonstrates animated list items
class AnimatedListDemoPage extends StatelessWidget {
  const AnimatedListDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return AnimatedListDemoView(title: title);
  }
}

class AnimatedListDemoView extends StatefulWidget {
  const AnimatedListDemoView({super.key, required this.title});

  final String title;

  @override
  State<AnimatedListDemoView> createState() => _AnimatedListDemoViewState();
}

class _AnimatedListDemoViewState extends State<AnimatedListDemoView> {
  final GlobalKey<AnimatedListState> _listKey = GlobalKey<AnimatedListState>();
  final List<String> _items = ['Item 1', 'Item 2', 'Item 3'];
  int _counter = 4;

  void _addItem() {
    final index = _items.length;
    _items.add('Item $_counter');
    _listKey.currentState?.insertItem(index);
    _counter++;
  }

  void _removeItem(int index) {
    final removedItem = _items[index];
    _items.removeAt(index);
    _listKey.currentState?.removeItem(
      index,
      (context, animation) => _buildRemovedItem(removedItem, animation),
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
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
        actions: [IconButton(icon: const Icon(Icons.add), onPressed: _addItem)],
      ),
      body: AnimatedList(
        key: _listKey,
        initialItemCount: _items.length,
        itemBuilder: (context, index, animation) {
          return _buildItem(_items[index], index, animation);
        },
      ),
    );
  }
}
