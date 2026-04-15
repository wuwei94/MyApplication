import 'package:basic_flutter/app/catalog/catalog_item.dart';

/// 顶层 Demo 分组统一接口。
abstract class CatalogSection {
  const CatalogSection();

  String get path;
  String get title;
  String get subtitle;
  List<CatalogItem> get items;

  CatalogItem get catalog => CatalogItem.catalog(
        path: path,
        title: title,
        subtitle: subtitle,
        children: items,
      );
}
