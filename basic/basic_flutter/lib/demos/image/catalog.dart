import 'package:basic_flutter/catalog/models/catalog_entry.dart';
import 'package:basic_flutter/catalog/models/catalog_section.dart';
import 'package:basic_flutter/demos/image/cached_network_image_example.dart';
import 'package:basic_flutter/demos/image/extended_image_example.dart';
import 'package:basic_flutter/demos/image/flutter_image_compress_example.dart';
import 'package:basic_flutter/demos/image/image_picker_example.dart';
import 'package:basic_flutter/demos/image/photo_view_example.dart';
import 'package:basic_flutter/demos/image/wechat_picker_example.dart';
import 'package:flutter/widgets.dart';

/// Image 模块
///
/// 包含：图片加载、缓存、预览与图片选择示例
class ImageCatalog extends CatalogSection {
  const ImageCatalog._();

  @override
  String get path => 'image';

  @override
  String get title => 'Image Example';

  @override
  String get subtitle => '加载、预览、压缩与媒体选择';

  @override
  List<CatalogEntry> get items => _items;

  static final List<CatalogEntry> _items = <CatalogEntry>[
    CatalogEntry.page(
      path: 'cached-network-image',
      title: 'CachedNetworkImage',
      subtitle: '网络缓存、占位图与错误态封装',
      pageBuilder: (BuildContext context) =>
          const CachedNetworkImageDemoPage(title: 'CachedNetworkImage'),
    ),
    CatalogEntry.page(
      path: 'extended-image',
      title: 'ExtendedImage',
      subtitle: '网络加载结合手势缩放与缓存控制',
      pageBuilder: (BuildContext context) =>
          const ExtendedImageDemoPage(title: 'ExtendedImage'),
    ),
    CatalogEntry.page(
      path: 'photo-view',
      title: 'PhotoView',
      subtitle: '大图缩放、拖拽平移与图库浏览',
      pageBuilder: (BuildContext context) =>
          const PhotoViewDemoPage(title: 'PhotoView'),
    ),
    CatalogEntry.page(
      path: 'flutter-image-compress',
      title: 'FlutterImageCompress',
      subtitle: '质量压缩、尺寸控制与临时文件输出',
      pageBuilder: (BuildContext context) =>
          const FlutterImageCompressDemoPage(title: 'FlutterImageCompress'),
    ),
    CatalogEntry.page(
      path: 'image-picker',
      title: 'ImagePicker',
      subtitle: '系统相册单选、多选与拍照流程',
      pageBuilder: (BuildContext context) =>
          const ImagePickerDemoPage(title: 'ImagePicker'),
    ),
    CatalogEntry.page(
      path: 'wechat-picker',
      title: 'WechatPicker',
      subtitle: '微信风格相册选择、拍照与结果预览',
      pageBuilder: (BuildContext context) =>
          const WechatPickerDemoPage(title: 'WechatPicker'),
    ),
  ];
}

const ImageCatalog imageCatalog = ImageCatalog._();
