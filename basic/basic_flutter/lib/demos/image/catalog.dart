import 'package:basic_flutter/catalog/models/catalog_entry.dart';
import 'package:basic_flutter/catalog/models/catalog_section.dart';
import 'package:basic_flutter/demos/image/cached_network_image_example.dart';
import 'package:basic_flutter/demos/image/extended_image_example.dart';
import 'package:basic_flutter/demos/image/flutter_image_compress_example.dart';
import 'package:basic_flutter/demos/image/flutter_luban_example.dart';
import 'package:basic_flutter/demos/image/image_cropper_example.dart';
import 'package:basic_flutter/demos/image/image_picker_example.dart';
import 'package:basic_flutter/demos/image/lib_image_loader_example.dart';
import 'package:basic_flutter/demos/image/photo_view_example.dart';
import 'package:basic_flutter/demos/image/wechat_picker_example.dart';
import 'package:flutter/widgets.dart';

/// Image 模块
///
/// 包含：图片加载、缓存、预览与图片选择示例，以及统一封装 lib_image_loader 的用法示例
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
      path: 'lib-image-loader',
      title: 'lib_image_loader',
      subtitle: '统一图片加载封装（内核可切换 + 缓存清理）',
      pageBuilder: (BuildContext context) =>
          const LibImageLoaderDemoPage(title: 'lib_image_loader'),
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
      path: 'flutter-luban',
      title: 'FlutterLuban',
      subtitle: '基于 Luban 算法的图片体积压缩',
      pageBuilder: (BuildContext context) =>
          const FlutterLubanDemoPage(title: 'FlutterLuban'),
    ),
    CatalogEntry.page(
      path: 'image-cropper',
      title: 'ImageCropper',
      subtitle: '原生图片裁剪、比例锁定与输出质量控制',
      pageBuilder: (BuildContext context) =>
          const ImageCropperDemoPage(title: 'ImageCropper'),
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
