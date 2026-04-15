import 'package:basic_flutter/app/catalog/catalog_module.dart';
import 'package:basic_flutter/demos/examples/examples_module.dart';
import 'package:basic_flutter/demos/network/network_module.dart';
import 'package:basic_flutter/demos/storage/storage_module.dart';
import 'package:basic_flutter/demos/anim/anim_module.dart';
import 'package:basic_flutter/demos/packages/packages_module.dart';
import 'package:basic_flutter/demos/video/video_module.dart';
import 'package:basic_flutter/demos/state_manager/state_manager_module.dart';
import 'package:basic_flutter/demos/layout/layout_module.dart';
import 'package:basic_flutter/demos/demo/demo_module.dart';
import 'package:basic_flutter/app/catalog/catalog_item.dart';

/// App 首页目录聚合
/// 
/// 每个 Demo Module 只负责描述自己的内容，
/// 首页目录由统一的模块列表自动生成。
final List<CatalogModule> demoModules = <CatalogModule>[
  examplesModule,
  networkModule,
  storageModule,
  animModule,
  packagesModule,
  videoModule,
  stateManagerModule,
  layoutModule,
  demoModule,
];

/// 新增模块只需将对应模块加入 `demoModules`。
final List<CatalogItem> demoCatalog = List<CatalogItem>.unmodifiable(
  demoModules.map((CatalogModule module) => module.catalog),
);
