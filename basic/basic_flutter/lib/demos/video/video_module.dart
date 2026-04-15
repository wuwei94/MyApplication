import 'package:basic_flutter/demos/video/chewie_video_player_example.dart';
import 'package:basic_flutter/demos/video/video_player_example.dart';
import 'package:basic_flutter/app/catalog/catalog_item.dart';
import 'package:basic_flutter/app/catalog/catalog_module.dart';
import 'package:flutter/widgets.dart';

/// Video 模块
/// 
/// 包含：VideoPlayer、Chewie 等视频播放示例
class VideoModule extends CatalogModule {
  const VideoModule._();

  @override
  String get path => '/video';

  @override
  String get title => 'Video';

  @override
  String get subtitle => '视频组件';

  @override
  List<CatalogItem> get items => _items;

  static final List<CatalogItem> _items = [
    CatalogItem.page(
      path: 'video-player',
      title: 'VideoPlayer',
      subtitle: 'video_player 示例',
      pageBuilder: (BuildContext context) =>
          const VideoPlayerExample(title: 'VideoPlayer'),
    ),
    CatalogItem.page(
      path: 'chewie-video-player',
      title: 'ChewieVideoPlayer',
      subtitle: 'chewie + video_player 示例',
      pageBuilder: (BuildContext context) =>
          const ChewieVideoPlayerExample(title: 'ChewieVideoPlayer'),
    ),
  ];
}

/// 单例实例
const VideoModule videoModule = VideoModule._();
