import 'package:flutter_demo/catalog/models/catalog_entry.dart';
import 'package:flutter_demo/catalog/models/catalog_section.dart';
import 'package:flutter_demo/demos/animation/catalog.dart';
import 'package:flutter_demo/demos/basics/catalog.dart';
import 'package:flutter_demo/demos/bluetooth/catalog.dart';
import 'package:flutter_demo/demos/chart/catalog.dart';
import 'package:flutter_demo/demos/engine/catalog.dart';
import 'package:flutter_demo/demos/image/catalog.dart';
import 'package:flutter_demo/demos/layout/catalog.dart';
import 'package:flutter_demo/demos/ml/catalog.dart';
import 'package:flutter_demo/demos/network/catalog.dart';
import 'package:flutter_demo/demos/packages/catalog.dart';
import 'package:flutter_demo/demos/showcase/catalog.dart';
import 'package:flutter_demo/demos/state_management/catalog.dart';
import 'package:flutter_demo/demos/storage/catalog.dart';
import 'package:flutter_demo/demos/video/catalog.dart';

/// App catalog 注册表。
///
/// 每个顶层目录分组只负责描述自己的内容，
/// 首页目录由统一的分组列表自动生成。
final List<CatalogSection> catalogSections = <CatalogSection>[
  basicsCatalog,
  layoutCatalog,
  stateManagementCatalog,
  networkCatalog,
  bluetoothCatalog,
  storageCatalog,
  imageCatalog,
  animationCatalog,
  engineCatalog,
  videoCatalog,
  mlCatalog,
  chartCatalog,
  packagesCatalog,
  showcaseCatalog,
];

/// 新增模块只需将对应模块加入 `catalogSections`。
final List<CatalogEntry> catalogRegistry = List<CatalogEntry>.unmodifiable(
  catalogSections.map((CatalogSection section) => section.catalog),
);
