import 'package:flutter_demo/catalog/models/catalog_entry.dart';
import 'package:flutter_demo/catalog/models/catalog_section.dart';
import 'package:flutter_demo/demos/packages/content/catalog.dart';
import 'package:flutter_demo/demos/packages/layout/catalog.dart';
import 'package:flutter_demo/demos/packages/platform/catalog.dart';
import 'package:flutter_demo/demos/packages/time/catalog.dart';
import 'package:flutter_demo/demos/packages/utils/catalog.dart';

/// Packages 模块
///
/// 按布局、内容、时间、平台、工具分组组织第三方包示例。
class PackagesCatalog extends CatalogSection {
  const PackagesCatalog._();

  @override
  String get path => 'packages';

  @override
  String get title => 'Packages';

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
