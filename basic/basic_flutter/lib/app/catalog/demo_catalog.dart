import 'package:basic_flutter/app/catalog/catalog_section.dart';
import 'package:basic_flutter/demos/examples/catalog.dart';
import 'package:basic_flutter/demos/network/catalog.dart';
import 'package:basic_flutter/demos/storage/catalog.dart';
import 'package:basic_flutter/demos/anim/catalog.dart';
import 'package:basic_flutter/demos/packages/catalog.dart';
import 'package:basic_flutter/demos/video/catalog.dart';
import 'package:basic_flutter/demos/state_manager/catalog.dart';
import 'package:basic_flutter/demos/layout/catalog.dart';
import 'package:basic_flutter/demos/demo/catalog.dart';
import 'package:basic_flutter/app/catalog/catalog_item.dart';

/// App 首页目录聚合
/// 
/// 每个顶层目录分组只负责描述自己的内容，
/// 首页目录由统一的分组列表自动生成。
final List<CatalogSection> catalogSections = <CatalogSection>[
  examplesCatalog,
  networkCatalog,
  storageCatalog,
  animCatalog,
  packagesCatalog,
  videoCatalog,
  stateManagerCatalog,
  layoutCatalog,
  showcaseCatalog,
];

/// 新增模块只需将对应模块加入 `catalogSections`。
final List<CatalogItem> demoCatalog = List<CatalogItem>.unmodifiable(
  catalogSections.map((CatalogSection section) => section.catalog),
);
