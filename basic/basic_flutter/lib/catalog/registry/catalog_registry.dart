import 'package:basic_flutter/catalog/models/catalog_section.dart';
import 'package:basic_flutter/demos/basics/catalog.dart';
import 'package:basic_flutter/demos/image/catalog.dart';
import 'package:basic_flutter/demos/network/catalog.dart';
import 'package:basic_flutter/demos/storage/catalog.dart';
import 'package:basic_flutter/demos/animation/catalog.dart';
import 'package:basic_flutter/demos/packages/catalog.dart';
import 'package:basic_flutter/demos/video/catalog.dart';
import 'package:basic_flutter/demos/state_management/catalog.dart';
import 'package:basic_flutter/demos/layout/catalog.dart';
import 'package:basic_flutter/demos/showcase/catalog.dart';
import 'package:basic_flutter/catalog/models/catalog_entry.dart';

/// App catalog 注册表。
///
/// 每个顶层目录分组只负责描述自己的内容，
/// 首页目录由统一的分组列表自动生成。
final List<CatalogSection> catalogSections = <CatalogSection>[
  basicsCatalog,
  layoutCatalog,
  stateManagementCatalog,
  networkCatalog,
  storageCatalog,
  imageCatalog,
  animationCatalog,
  videoCatalog,
  packagesCatalog,
  showcaseCatalog,
];

/// 新增模块只需将对应模块加入 `catalogSections`。
final List<CatalogEntry> catalogRegistry = List<CatalogEntry>.unmodifiable(
  catalogSections.map((CatalogSection section) => section.catalog),
);
