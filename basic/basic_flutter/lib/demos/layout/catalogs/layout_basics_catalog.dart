import 'package:basic_flutter/app/catalog/catalog_item.dart';
import 'package:basic_flutter/demos/layout/flow_layout/flow_example.dart';
import 'package:basic_flutter/demos/layout/flow_layout/wrap_example.dart';
import 'package:basic_flutter/demos/layout/layout_containers/align_example.dart';
import 'package:basic_flutter/demos/layout/layout_containers/center_example.dart';
import 'package:basic_flutter/demos/layout/layout_containers/constrainedbox_example.dart';
import 'package:basic_flutter/demos/layout/layout_containers/container_example.dart';
import 'package:basic_flutter/demos/layout/layout_containers/padding_example.dart';
import 'package:basic_flutter/demos/layout/layout_containers/sizedbox_example.dart';
import 'package:basic_flutter/demos/layout/linear_layout/column_example.dart';
import 'package:basic_flutter/demos/layout/linear_layout/flexible_expanded_example.dart';
import 'package:basic_flutter/demos/layout/linear_layout/row_example.dart';
import 'package:basic_flutter/demos/layout/stack_positioning/positioned_example.dart';
import 'package:basic_flutter/demos/layout/stack_positioning/stack_example.dart';
import 'package:flutter/widgets.dart';

final List<CatalogItem> layoutBasicsCatalogItems = <CatalogItem>[
  CatalogItem.catalog(
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
        pageBuilder: (BuildContext context) =>
            const AlignExample(title: 'Align'),
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
  ),
  CatalogItem.catalog(
    path: '/layout/linear',
    title: '线性布局',
    subtitle: 'Row、Column、Flexible、Expanded',
    children: <CatalogItem>[
      CatalogItem.page(
        path: '/layout/linear/row',
        title: 'Row',
        subtitle: '水平布局',
        pageBuilder: (BuildContext context) => const RowExample(title: 'Row'),
      ),
      CatalogItem.page(
        path: '/layout/linear/column',
        title: 'Column',
        subtitle: '垂直布局',
        pageBuilder: (BuildContext context) =>
            const ColumnExample(title: 'Column'),
      ),
      CatalogItem.page(
        path: '/layout/linear/flexible-expanded',
        title: 'Flexible & Expanded',
        subtitle: '弹性布局',
        pageBuilder: (BuildContext context) =>
            const FlexibleExpandedExample(title: 'Flexible & Expanded'),
      ),
    ],
  ),
  CatalogItem.catalog(
    path: '/layout/stacking',
    title: '堆叠定位',
    subtitle: 'Stack、Positioned',
    children: <CatalogItem>[
      CatalogItem.page(
        path: '/layout/stacking/stack',
        title: 'Stack',
        subtitle: '堆叠布局',
        pageBuilder: (BuildContext context) => const StackExample(title: 'Stack'),
      ),
      CatalogItem.page(
        path: '/layout/stacking/positioned',
        title: 'Positioned',
        subtitle: '定位组件',
        pageBuilder: (BuildContext context) =>
            const PositionedExample(title: 'Positioned'),
      ),
    ],
  ),
  CatalogItem.catalog(
    path: '/layout/flow',
    title: '流式布局',
    subtitle: 'Wrap、Flow',
    children: <CatalogItem>[
      CatalogItem.page(
        path: '/layout/flow/wrap',
        title: 'Wrap',
        subtitle: '自动换行',
        pageBuilder: (BuildContext context) => const WrapExample(title: 'Wrap'),
      ),
      CatalogItem.page(
        path: '/layout/flow/flow-widget',
        title: 'Flow',
        subtitle: '流式布局',
        pageBuilder: (BuildContext context) => const FlowExample(title: 'Flow'),
      ),
    ],
  ),
];
