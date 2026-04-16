import 'package:basic_flutter/catalog/models/catalog_entry.dart';
import 'package:basic_flutter/catalog/models/resolved_catalog_entry.dart';

/// 将源码中的相对路径目录树解析为运行时绝对路径目录树。
class CatalogTreeResolver {
  CatalogTreeResolver._();

  static List<ResolvedCatalogEntry> resolve(List<CatalogEntry> items) {
    return _resolveItems(items, '');
  }

  static List<ResolvedCatalogEntry> _resolveItems(
    List<CatalogEntry> items,
    String parentPath,
  ) {
    return items.map((CatalogEntry item) {
      final String fullPath = _joinPaths(parentPath, item.path);
      final List<ResolvedCatalogEntry> resolvedChildren = item.children.isEmpty
          ? const <ResolvedCatalogEntry>[]
          : _resolveItems(item.children, fullPath);

      if (resolvedChildren.isEmpty) {
        return ResolvedCatalogEntry.page(
          path: fullPath,
          title: item.title,
          subtitle: item.subtitle,
          pageBuilder: item.pageBuilder,
        );
      }

      return ResolvedCatalogEntry.catalog(
        path: fullPath,
        title: item.title,
        subtitle: item.subtitle,
        children: resolvedChildren,
      );
    }).toList(growable: false);
  }

  static String _joinPaths(String parentPath, String childPath) {
    if (childPath.startsWith('/')) {
      return childPath;
    }

    final String normalizedParent = parentPath.endsWith('/')
        ? parentPath.substring(0, parentPath.length - 1)
        : parentPath;

    if (normalizedParent.isEmpty) {
      return '/$childPath';
    }

    return '$normalizedParent/$childPath';
  }
}
