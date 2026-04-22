import 'package:basic_flutter/catalog/models/catalog_entry.dart';
import 'package:basic_flutter/demos/packages/content/custom_google_font_example.dart';
import 'package:basic_flutter/demos/packages/content/extended_text_field_example.dart';
import 'package:basic_flutter/demos/packages/content/flutter_linkify_example.dart';
import 'package:flutter/widgets.dart';

final CatalogEntry packagesContentCatalog = CatalogEntry.catalog(
  path: 'content',
  title: 'Content',
  subtitle: '文本编辑、链接识别与字体展示',
  children: <CatalogEntry>[
    CatalogEntry.page(
      path: 'extended-text-field',
      title: 'ExtendedTextField',
      subtitle: '特殊 token、高亮文本与内嵌图片输入',
      pageBuilder: (BuildContext context) =>
          const ExtendedTextFieldDemoPage(title: 'ExtendedTextField'),
    ),
    CatalogEntry.page(
      path: 'flutter-linkify',
      title: 'FlutterLinkify',
      subtitle: '正文链接识别、点击跳转与可选择预览',
      pageBuilder: (BuildContext context) =>
          const FlutterLinkifyDemoPage(title: 'FlutterLinkify'),
    ),
    CatalogEntry.page(
      path: 'custom-google-font',
      title: 'Custom Google Font',
      subtitle: '动态加载 Google Fonts 并对比展示',
      pageBuilder: (BuildContext context) =>
          const CustomGoogleFontDemoPage(title: 'Custom Google Font'),
    ),
  ],
);
