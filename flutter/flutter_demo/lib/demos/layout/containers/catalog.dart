import 'package:flutter/widgets.dart';
import 'package:flutter_demo/catalog/models/catalog_entry.dart';
import 'package:flutter_demo/demos/layout/containers/align_demo.dart';
import 'package:flutter_demo/demos/layout/containers/center_demo.dart';
import 'package:flutter_demo/demos/layout/containers/constrainedbox_demo.dart';
import 'package:flutter_demo/demos/layout/containers/container_demo.dart';
import 'package:flutter_demo/demos/layout/containers/padding_demo.dart';
import 'package:flutter_demo/demos/layout/containers/sizedbox_demo.dart';

final CatalogEntry containersCatalog = CatalogEntry.catalog(
  path: 'containers',
  title: '基础容器',
  subtitle: 'Container、Padding、Center、Align、SizedBox、ConstrainedBox',
  children: <CatalogEntry>[
    CatalogEntry.page(
      path: 'container',
      title: 'Container',
      subtitle: '容器组件',
      pageBuilder: (BuildContext context) =>
          const ContainerDemoPage(title: 'Container'),
    ),
    CatalogEntry.page(
      path: 'padding',
      title: 'Padding',
      subtitle: '内边距组件',
      pageBuilder: (BuildContext context) =>
          const PaddingDemoPage(title: 'Padding'),
    ),
    CatalogEntry.page(
      path: 'center',
      title: 'Center',
      subtitle: '居中组件',
      pageBuilder: (BuildContext context) =>
          const CenterDemoPage(title: 'Center'),
    ),
    CatalogEntry.page(
      path: 'align',
      title: 'Align',
      subtitle: '对齐组件',
      pageBuilder: (BuildContext context) =>
          const AlignDemoPage(title: 'Align'),
    ),
    CatalogEntry.page(
      path: 'sized-box',
      title: 'SizedBox',
      subtitle: '尺寸组件',
      pageBuilder: (BuildContext context) =>
          const SizedBoxDemoPage(title: 'SizedBox'),
    ),
    CatalogEntry.page(
      path: 'constrained-box',
      title: 'ConstrainedBox',
      subtitle: '约束组件',
      pageBuilder: (BuildContext context) =>
          const ConstrainedBoxDemoPage(title: 'ConstrainedBox'),
    ),
  ],
);
