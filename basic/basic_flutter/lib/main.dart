import 'package:basic_flutter/routes/app_router.dart';
import 'package:basic_flutter/state_management/bloc/observer/my_bloc_observer.dart';
import 'package:basic_flutter/state_management/provider/notifier/my_provider_notifier.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';

void main() {
  Bloc.observer = const AppBlocObserver();

  //runApp(const MyApp());
  runApp(
    ChangeNotifierProvider(
      //在生成器中初始化模型。
      //这样，提供者就可以拥有Counter的生命周期，
      //确保在不再需要时调用“dispose”。
      // Initialize the model in the builder.
      // That way, Provider can own Counter's lifecycle,
      // making sure to call `dispose` when not needed anymore.
      create: (context) => MyProviderNotifier(),
      child: const MyApp(),
    ),
  );
}

/// 在 Flutter 3.0 中，
/// 同时使用 MaterialApp 的 title 和 Scaffold 的 appBar 时，
/// Scaffold 的 appBar 会覆盖 MaterialApp 的 title
class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp.router(
      title: 'Flutter Demo',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(primarySwatch: Colors.blue),
      routerConfig: appRouter,
    );
  }
}

/// 首页 - 路由列表
class HomePage extends StatelessWidget {
  const HomePage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Flutter Demo')),
      body: ListView.builder(
        itemCount: AppRouteList.getRouteList().length,
        itemBuilder: (context, index) {
          final item = AppRouteList.getRouteList()[index];
          if (item.routePath.isEmpty) {
            return ListTile(
              title: Text(item.routeName),
            );
          }
          return ListTile(
            title: Text(item.routeName),
            subtitle: item.routeDescribe.isNotEmpty
                ? Text(item.routeDescribe)
                : null,
            onTap: () => context.push(item.routePath),
          );
        },
      ),
    );
  }
}
