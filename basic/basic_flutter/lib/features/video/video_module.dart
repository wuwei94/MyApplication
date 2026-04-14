import 'package:basic_flutter/features/video/chewie_video_player_example.dart';
import 'package:basic_flutter/features/video/video_player_example.dart';
import 'package:basic_flutter/navigation/models/route_item.dart';
import 'package:flutter/widgets.dart';

/// Video 模块
/// 
/// 包含：VideoPlayer、Chewie 等视频播放示例
class VideoModule {
  const VideoModule._();

  /// 首页目录入口
  RouteItem get catalog => RouteItem.section(
        path: '/video',
        title: 'Video',
        subtitle: '视频组件',
        routeItems: routes,
      );

  /// 所有路由列表
  List<RouteItem> get routes => _routes;

  static final List<RouteItem> _routes = [
    RouteItem.page(
      path: '/video/video-player',
      title: 'VideoPlayer',
      subtitle: 'video_player 示例',
      pageBuilder: (BuildContext context) =>
          const VideoPlayerExample(title: 'VideoPlayer'),
    ),
    RouteItem.page(
      path: '/video/chewie-video-player',
      title: 'ChewieVideoPlayer',
      subtitle: 'chewie + video_player 示例',
      pageBuilder: (BuildContext context) =>
          const ChewieVideoPlayerExample(title: 'ChewieVideoPlayer'),
    ),
  ];
}

/// 单例实例
const VideoModule videoModule = VideoModule._();
