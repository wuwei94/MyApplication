import 'package:basic_flutter/features/examples/examples_module.dart';
import 'package:basic_flutter/features/network/network_module.dart';
import 'package:basic_flutter/features/storage/storage_module.dart';
import 'package:basic_flutter/features/anim/anim_module.dart';
import 'package:basic_flutter/features/packages/packages_module.dart';
import 'package:basic_flutter/features/video/video_module.dart';
import 'package:basic_flutter/features/state_manager/state_manager_module.dart';
import 'package:basic_flutter/features/layout/layout_module.dart';
import 'package:basic_flutter/features/demo/demo_module.dart';
import 'package:basic_flutter/navigation/models/route_item.dart';

/// App 首页目录聚合
/// 
/// 每个 Feature Module 通过 catalog 暴露自己的首页入口
/// 新增模块只需在此列表中添加对应模块的 catalog
final List<RouteItem> appCatalog = <RouteItem>[
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
