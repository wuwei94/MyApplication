import 'package:flutter_demo/catalog/models/catalog_entry.dart';

/// 顶层 Demo 分组统一接口。
abstract class CatalogSection {
  const CatalogSection();

  String get path;
  String get title;
  String get subtitle;
  List<CatalogEntry> get items;

  CatalogEntry get catalog => CatalogEntry.catalog(
        path: path,
        title: title,
        subtitle: subtitle,
        children: items,
      );
}
