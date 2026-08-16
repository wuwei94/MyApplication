import 'package:flutter/widgets.dart';

class CatalogEntry {
  final String path;
  final String title;
  final String subtitle;
  final List<CatalogEntry> children;
  final WidgetBuilder pageBuilder;

  CatalogEntry.page({
    required this.path,
    required this.title,
    this.subtitle = "",
    required this.pageBuilder,
  }) : children = const <CatalogEntry>[];

  CatalogEntry.catalog({
    required this.path,
    required this.title,
    this.subtitle = "",
    required this.children,
  }) : pageBuilder = ((_) => const SizedBox.shrink());

  @override
  String toString() {
    return 'CatalogEntry('
        'path: $path, '
        'title: $title, '
        'subtitle: $subtitle, '
        'children: $children'
        ')';
  }
}
