import 'package:basic_flutter/catalog/models/catalog_entry.dart';
import 'package:basic_flutter/demos/layout/containers/align_example.dart';
import 'package:basic_flutter/demos/layout/containers/center_example.dart';
import 'package:basic_flutter/demos/layout/containers/constrainedbox_example.dart';
import 'package:basic_flutter/demos/layout/containers/container_example.dart';
import 'package:basic_flutter/demos/layout/containers/padding_example.dart';
import 'package:basic_flutter/demos/layout/containers/sizedbox_example.dart';
import 'package:flutter/widgets.dart';

final CatalogEntry containersCatalog = CatalogEntry.catalog(
  path: 'containers',
  title: '容器布局',
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
      pageBuilder: (BuildContext context) => const AlignDemoPage(title: 'Align'),
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
