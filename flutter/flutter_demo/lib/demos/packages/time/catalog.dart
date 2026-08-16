import 'package:flutter/widgets.dart';
import 'package:flutter_demo/catalog/models/catalog_entry.dart';
import 'package:flutter_demo/demos/packages/time/intl_demo.dart';
import 'package:flutter_demo/demos/packages/time/timeago_demo.dart';

final CatalogEntry packagesTimeCatalog = CatalogEntry.catalog(
  path: 'time',
  title: 'Time',
  subtitle: '日期格式化与相对时间文案',
  children: <CatalogEntry>[
    CatalogEntry.page(
      path: 'intl',
      title: 'Intl',
      subtitle: '中英文日期时间格式化与 Locale 切换',
      pageBuilder: (BuildContext context) => const IntlDemoPage(title: 'Intl'),
    ),
    CatalogEntry.page(
      path: 'timeago',
      title: 'Timeago',
      subtitle: '相对时间文案与未来时间展示',
      pageBuilder: (BuildContext context) =>
          const TimeagoDemoPage(title: 'Timeago'),
    ),
  ],
);
