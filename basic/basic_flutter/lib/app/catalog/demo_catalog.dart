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
/// 每个 Demo Module 通过 catalog 暴露自己的首页入口
/// 新增模块只需在此列表中添加对应模块的 catalog
final List<CatalogItem> demoCatalog = <CatalogItem>[
  examplesModule.catalog,
  networkModule.catalog,
  storageModule.catalog,
  animModule.catalog,
  packagesModule.catalog,
  videoModule.catalog,
  stateManagerModule.catalog,
  layoutModule.catalog,
  demoModule.catalog,
];
