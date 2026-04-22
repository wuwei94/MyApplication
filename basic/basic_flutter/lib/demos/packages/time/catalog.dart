import 'package:basic_flutter/catalog/models/catalog_entry.dart';
import 'package:basic_flutter/demos/packages/time/intl_example.dart';
import 'package:basic_flutter/demos/packages/time/time_machine_example.dart';
import 'package:basic_flutter/demos/packages/time/timeago_example.dart';
import 'package:flutter/widgets.dart';

final CatalogEntry packagesTimeCatalog = CatalogEntry.catalog(
  path: 'time',
  title: 'Time',
  subtitle: '格式化、时区与相对时间文案',
  children: <CatalogEntry>[
    CatalogEntry.page(
      path: 'intl',
      title: 'Intl',
      subtitle: '日期时间与多语言格式化',
      pageBuilder: (BuildContext context) => const IntlDemoPage(title: 'Intl'),
    ),
    CatalogEntry.page(
      path: 'time-machine',
      title: 'TimeMachine',
      subtitle: '时区、历法与文化格式化',
      pageBuilder: (BuildContext context) =>
          const TimeMachineDemoPage(title: 'TimeMachine'),
    ),
    CatalogEntry.page(
      path: 'timeago',
      title: 'Timeago',
      subtitle: '相对时间文案生成',
      pageBuilder: (BuildContext context) =>
          const TimeagoDemoPage(title: 'Timeago'),
    ),
  ],
);
