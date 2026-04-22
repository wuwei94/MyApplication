import 'package:basic_flutter/catalog/models/catalog_entry.dart';
import 'package:basic_flutter/demos/packages/content/custom_google_font_example.dart';
import 'package:basic_flutter/demos/packages/content/extended_text_field_example.dart';
import 'package:basic_flutter/demos/packages/content/flutter_linkify_example.dart';
import 'package:flutter/widgets.dart';

final CatalogEntry packagesContentCatalog = CatalogEntry.catalog(
  path: 'content',
  title: 'Content',
  subtitle: '文本编辑、链接识别与字体渲染',
  children: <CatalogEntry>[
    CatalogEntry.page(
      path: 'extended-text-field',
      title: 'ExtendedTextField',
      subtitle: '富文本输入与特殊标记渲染',
      pageBuilder: (BuildContext context) =>
          const ExtendedTextFieldDemoPage(title: 'ExtendedTextField'),
    ),
    CatalogEntry.page(
      path: 'flutter-linkify',
      title: 'FlutterLinkify',
      subtitle: '正文中的链接自动识别',
      pageBuilder: (BuildContext context) =>
          const FlutterLinkifyDemoPage(title: 'FlutterLinkify'),
    ),
    CatalogEntry.page(
      path: 'custom-google-font',
      title: 'Custom Google Font',
      subtitle: '动态加载并展示 Google Fonts',
      pageBuilder: (BuildContext context) =>
          const CustomGoogleFontDemoPage(title: 'Custom Google Font'),
    ),
  ],
);
