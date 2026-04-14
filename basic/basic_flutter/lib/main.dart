import 'package:basic_flutter/app/router/app_router.dart';
import 'package:flutter/material.dart';

void main() {
  runApp(const MyApp());
}

/// 在 Flutter 3.0 中，
/// 同时使用 MaterialApp 的 title 和 Scaffold 的 appBar 时，
/// Scaffold 的 appBar 会覆盖 MaterialApp 的 title
class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp.router(
      title: 'Flutter Example',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(primarySwatch: Colors.blue),
      routerConfig: appRouter,
    );
  }
}
