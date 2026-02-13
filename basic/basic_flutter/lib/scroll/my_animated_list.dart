import 'package:basic_flutter/common/log.dart';
import 'package:flutter/material.dart';

/// AnimatedList
class MyAnimatedList extends StatelessWidget {
  const MyAnimatedList({super.key});

  @override
  Widget build(BuildContext context) {
    return const AnimatedListRoute(title: 'Flutter AnimatedList demo');
  }
}

class AnimatedListRoute extends StatefulWidget {
  const AnimatedListRoute({super.key, required this.title});

  final String title;

  @override
  State<AnimatedListRoute> createState() => _AnimatedListRouteState();
}

class _AnimatedListRouteState extends State<AnimatedListRoute> {
  List<String> data = <String>[];
  int counter = 5;

  final GlobalKey<AnimatedListState> globalKey = GlobalKey<AnimatedListState>();

  @override
  void initState() {
    for (var i = 0; i < counter; i++) {
      data.add('${i + 1}');
    }
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(widget.title)),
      body: getBody(),
      floatingActionButton: getFAB(),
    );
  }

  Widget getBody() {
    return AnimatedList(
      key: globalKey,
      initialItemCount: data.length,
      itemBuilder: (context, index, animation) {
        //添加列表项时会执行 渐显动画
        return FadeTransition(
          opacity: animation,
          child: buildItem(context, index),
        );
      },
    );
  }

  Widget getFAB() {
    return FloatingActionButton(
      onPressed: _add,
      tooltip: 'add',
      child: const Icon(Icons.add),
    );
  }

  Widget buildItem(BuildContext context, int index) {
    final char = data[index];
    return ListTile(
      key: ValueKey(char),
      title: Text(char),
      trailing: IconButton(
        icon: const Icon(Icons.delete),
        onPressed: () => _delete(context, index),
      ),
    );
  }

  void _add() {
    setState(() {
      data.add('${++counter}');
      globalKey.currentState!.insertItem(data.length - 1);
      logDebug('添加 $counter');
    });
  }

  void _delete(BuildContext context, int index) {
    setState(() {
      globalKey.currentState!.removeItem(
        index,
        (context, animation) {
          final item = buildItem(context, index);
          logDebug('删除 ${data[index]}');
          data.removeAt(index);
          //删除列表项时会执行 渐隐动画
          return FadeTransition(
            opacity: CurvedAnimation(
              parent: animation,
              curve: const Interval(0.5, 1.0),
            ),
            //删除列表项时会执行 缩小动画
            child: SizeTransition(
              sizeFactor: animation,
              axisAlignment: 0.0,
              child: item,
            ),
          );
        },
        duration: const Duration(milliseconds: 200), // 动画时间为 200 ms
      );
    });
  }
}
