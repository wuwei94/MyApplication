import 'package:basic_flutter/catalog/models/catalog_entry.dart';
import 'package:basic_flutter/catalog/models/catalog_section.dart';
import 'package:basic_flutter/demos/image/cached_network_image_example.dart';
import 'package:basic_flutter/demos/image/extended_image_example.dart';
import 'package:flutter/widgets.dart';

/// Image 模块
///
/// 包含：图片加载、缓存与预览示例
class ImageCatalog extends CatalogSection {
  const ImageCatalog._();

  @override
  String get path => 'image';

  @override
  String get title => 'Image Example';

  @override
  String get subtitle => '图片加载';

  @override
  List<CatalogEntry> get items => _items;

  static final List<CatalogEntry> _items = <CatalogEntry>[
    CatalogEntry.page(
      path: 'cached-network-image',
      title: 'CachedNetworkImage',
      subtitle: '基于cached_network_image的\nCachedNetworkImageLoader示例',
      pageBuilder: (BuildContext context) =>
          const CachedNetworkImageDemoPage(title: 'CachedNetworkImage'),
    ),
    CatalogEntry.page(
      path: 'extended-image',
      title: 'ExtendedImage',
      subtitle: '基于extended_image的\nExtendedImageLoader示例',
      pageBuilder: (BuildContext context) =>
          const ExtendedImageDemoPage(title: 'ExtendedImage'),
    ),
  ];
}

const ImageCatalog imageCatalog = ImageCatalog._();
