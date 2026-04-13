import 'package:basic_flutter/features/video/chewie_video_player_example.dart';
import 'package:basic_flutter/features/video/video_player_example.dart';
import 'package:basic_flutter/navigation/models/route_item.dart';
import 'package:flutter/widgets.dart';

final List<RouteItem> videoRoutes = [
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
