import 'package:basic_flutter/app/router/app_router_config.dart';
import 'package:basic_flutter/core/utils/ui/smart_dialog.dart';
import 'package:flutter/material.dart';

/// 在 Flutter 3.0 中，
/// 同时使用 MaterialApp 的 title 和 Scaffold 的 appBar 时，
/// Scaffold 的 appBar 会覆盖 MaterialApp 的 title
class DemoCatalogApp extends StatelessWidget {
  const DemoCatalogApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp.router(
      title: 'Flutter Example',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(primarySwatch: Colors.blue),
      builder: AppSmartDialog.initBuilder,
      routerConfig: appRouterConfig,
    );
  }
}
