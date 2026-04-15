import 'package:flutter/widgets.dart';

class CatalogItem {
  final String path;
  final String title;
  final String subtitle;
  final List<CatalogItem> children;
  final WidgetBuilder pageBuilder;

  CatalogItem.page({
    required this.path,
    required this.title,
    this.subtitle = "",
    required this.pageBuilder,
  }) : children = const <CatalogItem>[];

  CatalogItem.catalog({
    required this.path,
    required this.title,
    this.subtitle = "",
    required this.children,
  }) : pageBuilder = ((_) => const SizedBox.shrink());

  @override
  String toString() {
    return 'CatalogItem('
        'path: $path, '
        'title: $title, '
        'subtitle: $subtitle, '
        'children: $children'
        ')';
  }
}
