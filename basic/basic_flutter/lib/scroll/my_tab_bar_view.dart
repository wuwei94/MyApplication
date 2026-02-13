import 'package:basic_flutter/widget/keep_alive.dart';
import 'package:flutter/material.dart';

/// TabBarView
class MyTabBarView extends StatelessWidget {
  const MyTabBarView({super.key});

  @override
  Widget build(BuildContext context) {
    return const TabBarViewRoute(title: 'Flutter TabView demo');
  }
}

class TabBarViewRoute extends StatelessWidget {
  const TabBarViewRoute({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    final List<String> tabs = ["新闻", "历史", "图片"];
    return DefaultTabController(
      length: tabs.length,
      child: Scaffold(appBar: getAppBar(tabs), body: getBody(tabs)),
    );
  }

  AppBar getAppBar(List<String> tabs) {
    return AppBar(
      title: Text(title),
      bottom: TabBar(tabs: tabs.map((String tab) => Tab(text: tab)).toList()),
    );
  }

  Widget getBody(List<String> tabs) {
    return TabBarView(
      children: tabs.map((String e) {
        return KeepAliveWrapper(child: Center(child: Text(e)));
      }).toList(),
    );
  }
}
