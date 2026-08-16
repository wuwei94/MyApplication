import 'package:flutter/widgets.dart';

/// 运行时使用的目录树节点，路径已经解析为绝对路径。
class ResolvedCatalogEntry {
  final String path;
  final String title;
  final String subtitle;
  final List<ResolvedCatalogEntry> children;
  final WidgetBuilder pageBuilder;

  ResolvedCatalogEntry.page({
    required this.path,
    required this.title,
    this.subtitle = "",
    required this.pageBuilder,
  }) : children = const <ResolvedCatalogEntry>[];

  ResolvedCatalogEntry.catalog({
    required this.path,
    required this.title,
    this.subtitle = "",
    required this.children,
  }) : pageBuilder = ((_) => const SizedBox.shrink());

  @override
  String toString() {
    return 'ResolvedCatalogEntry('
        'path: $path, '
        'title: $title, '
        'subtitle: $subtitle, '
        'children: $children'
        ')';
  }
}
