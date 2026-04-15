import 'package:basic_flutter/features/video/chewie_video_player_example.dart';
import 'package:basic_flutter/features/video/video_player_example.dart';
import 'package:basic_flutter/navigation/models/route_module.dart';
import 'package:basic_flutter/navigation/models/route_item.dart';
import 'package:flutter/widgets.dart';

final RouteModule videoModule = RouteModule(
  entry: RouteItem.section(
    path: '/video',
    title: 'Video',
    subtitle: '视频组件',
    routeItems: videoRoutes,
  ),
  routes: videoRoutes,
);

final List<RouteItem> videoRoutes = [
  RouteItem.page(
    path: 'video-player',
    title: 'VideoPlayer',
    subtitle: 'video_player 示例',
    pageBuilder: (BuildContext context) =>
        const VideoPlayerExample(title: 'VideoPlayer'),
  ),
  RouteItem.page(
    path: 'chewie-video-player',
    title: 'ChewieVideoPlayer',
    subtitle: 'chewie + video_player 示例',
    pageBuilder: (BuildContext context) =>
        const ChewieVideoPlayerExample(title: 'ChewieVideoPlayer'),
  ),
];
