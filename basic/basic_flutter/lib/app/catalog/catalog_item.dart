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

  CatalogItem copyWithResolvedPath({
    required String path,
    List<CatalogItem>? children,
  }) {
    final List<CatalogItem> resolvedChildren = children ?? this.children;

    if (resolvedChildren.isEmpty) {
      return CatalogItem.page(
        path: path,
        title: title,
        subtitle: subtitle,
        pageBuilder: pageBuilder,
      );
    }

    return CatalogItem.catalog(
      path: path,
      title: title,
      subtitle: subtitle,
      children: resolvedChildren,
    );
  }

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
