import 'package:flutter_demo/app/router/app_router_config.dart';
import 'package:flutter_demo/core/utils/ui/smart_dialog.dart';
import 'package:material_ui/material_ui.dart';

/// 在 Flutter 3.0 中，
/// 同时使用 MaterialApp 的 title 和 Scaffold 的 appBar 时，
/// Scaffold 的 appBar 会覆盖 MaterialApp 的 title
class DemoCatalogApp extends StatelessWidget {
  const DemoCatalogApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp.router(
      title: 'Flutter Demo',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(primarySwatch: Colors.blue),
      builder: _appBuilder,
      routerConfig: appRouterConfig,
    );
  }

  /// 全树注入 legacy Theme/Localizations：flutter_markdown_plus 等三方包
  /// 仍依赖 package:flutter/material.dart 的运行时继承，缺少桥接会在渲染期抛错
  static Widget _appBuilder(BuildContext context, Widget? child) {
    // 临时迁移桥，官方标注将在依赖完成迁移后移除
    // ignore: deprecated_member_use
    return MaterialUiCompatibilityBridge(
      child: AppSmartDialog.initBuilder(context, child),
    );
  }
}
