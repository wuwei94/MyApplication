import 'package:basic_flutter/demos/video/chewie_video_player_example.dart';
import 'package:basic_flutter/demos/video/video_player_example.dart';
import 'package:basic_flutter/app/catalog/catalog_item.dart';
import 'package:flutter/widgets.dart';

/// Video 模块
/// 
/// 包含：VideoPlayer、Chewie 等视频播放示例
class VideoModule {
  const VideoModule._();

  /// 首页目录入口
  CatalogItem get catalog => CatalogItem.catalog(
        path: '/video',
        title: 'Video',
        subtitle: '视频组件',
        children: routes,
      );

  /// 所有路由列表
  List<CatalogItem> get routes => _routes;

  static final List<CatalogItem> _routes = [
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
