import 'package:basic_flutter/catalog/models/catalog_entry.dart';
import 'package:basic_flutter/catalog/models/catalog_section.dart';
import 'package:basic_flutter/demos/packages/content/catalog.dart';
import 'package:basic_flutter/demos/packages/layout/catalog.dart';
import 'package:basic_flutter/demos/packages/platform/catalog.dart';
import 'package:basic_flutter/demos/packages/time/catalog.dart';
import 'package:basic_flutter/demos/packages/utils/catalog.dart';

/// Packages 模块
///
/// 按布局、内容、时间、平台、工具分组组织第三方包示例。
class PackagesCatalog extends CatalogSection {
  const PackagesCatalog._();

  @override
  String get path => 'packages';

  @override
  String get title => 'Packages Example';

  @override
  String get subtitle => '按能力整理的第三方包示例';

  @override
  List<CatalogEntry> get items => _items;

  static final List<CatalogEntry> _items = [
    packagesLayoutCatalog,
    packagesContentCatalog,
    packagesPlatformCatalog,
    packagesTimeCatalog,
    packagesUtilsCatalog,
  ];
}

/// 单例实例
const PackagesCatalog packagesCatalog = PackagesCatalog._();
