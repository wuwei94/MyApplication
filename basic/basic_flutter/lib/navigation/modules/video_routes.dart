import 'package:basic_flutter/features/video/chewie_video_player_example.dart';
import 'package:basic_flutter/features/video/video_player_example.dart';
import 'package:basic_flutter/navigation/models/route_item_model.dart';
import 'package:flutter/widgets.dart';

final List<RouteItem> videoRoutes = [
  RouteItem(
    path: '/video/video-player',
    title: 'VideoPlayer',
    subtitle: 'video_player 示例',
    routeBuilder: (BuildContext context, _) =>
        const VideoPlayerExample(title: 'VideoPlayer'),
  ),
  RouteItem(
    path: '/video/chewie-video-player',
    title: 'ChewieVideoPlayer',
    subtitle: 'chewie + video_player 示例',
    routeBuilder: (BuildContext context, _) =>
        const ChewieVideoPlayerExample(title: 'ChewieVideoPlayer'),
  ),
];
