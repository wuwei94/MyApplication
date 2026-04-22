import 'package:basic_flutter/demos/showcase/custom_local_font_example.dart';
import 'package:basic_flutter/catalog/models/catalog_entry.dart';
import 'package:basic_flutter/catalog/models/catalog_section.dart';
import 'package:flutter/widgets.dart';

/// Showcase 模块
///
/// 包含：自定义字体等演示示例
class ShowcaseCatalog extends CatalogSection {
  const ShowcaseCatalog._();

  @override
  String get path => 'showcase';

  @override
  String get title => 'Showcase Example';

  @override
  String get subtitle => '视觉展示与本地资源效果';

  @override
  List<CatalogEntry> get items => _items;

  static final List<CatalogEntry> _items = [
    CatalogEntry.page(
      path: 'custom-local-font',
      title: 'Custom Local Font',
      subtitle: '本地字体接入、字重展示与系统字体对比',
      pageBuilder: (BuildContext context) =>
          const CustomLocalFontDemoPage(title: 'Custom Local Font'),
    ),
  ];
}

/// 单例实例
const ShowcaseCatalog showcaseCatalog = ShowcaseCatalog._();
