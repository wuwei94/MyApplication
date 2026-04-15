import 'package:basic_flutter/app/catalog/catalog_item.dart';
import 'package:basic_flutter/demos/layout/layout_containers/align_example.dart';
import 'package:basic_flutter/demos/layout/layout_containers/center_example.dart';
import 'package:basic_flutter/demos/layout/layout_containers/constrainedbox_example.dart';
import 'package:basic_flutter/demos/layout/layout_containers/container_example.dart';
import 'package:basic_flutter/demos/layout/layout_containers/padding_example.dart';
import 'package:basic_flutter/demos/layout/layout_containers/sizedbox_example.dart';
import 'package:flutter/widgets.dart';

final CatalogItem layoutContainersCatalog = CatalogItem.catalog(
  path: '/layout/containers',
  title: '容器布局',
  subtitle: 'Container、Padding、Center、Align、SizedBox、ConstrainedBox',
  children: <CatalogItem>[
    CatalogItem.page(
      path: '/layout/containers/container',
      title: 'Container',
      subtitle: '容器组件',
      pageBuilder: (BuildContext context) =>
          const ContainerExample(title: 'Container'),
    ),
    CatalogItem.page(
      path: '/layout/containers/padding',
      title: 'Padding',
      subtitle: '内边距组件',
      pageBuilder: (BuildContext context) =>
          const PaddingExample(title: 'Padding'),
    ),
    CatalogItem.page(
      path: '/layout/containers/center',
      title: 'Center',
      subtitle: '居中组件',
      pageBuilder: (BuildContext context) =>
          const CenterExample(title: 'Center'),
    ),
    CatalogItem.page(
      path: '/layout/containers/align',
      title: 'Align',
      subtitle: '对齐组件',
      pageBuilder: (BuildContext context) => const AlignExample(title: 'Align'),
    ),
    CatalogItem.page(
      path: '/layout/containers/sized-box',
      title: 'SizedBox',
      subtitle: '尺寸组件',
      pageBuilder: (BuildContext context) =>
          const SizedBoxExample(title: 'SizedBox'),
    ),
    CatalogItem.page(
      path: '/layout/containers/constrained-box',
      title: 'ConstrainedBox',
      subtitle: '约束组件',
      pageBuilder: (BuildContext context) =>
          const ConstrainedBoxExample(title: 'ConstrainedBox'),
    ),
  ],
);
