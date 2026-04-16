import 'package:basic_flutter/demos/video/chewie_video_player_example.dart';
import 'package:basic_flutter/demos/video/video_player_example.dart';
import 'package:basic_flutter/catalog/models/catalog_entry.dart';
import 'package:basic_flutter/catalog/models/catalog_section.dart';
import 'package:flutter/widgets.dart';

/// Video 模块
/// 
/// 包含：VideoPlayer、Chewie 等视频播放示例
class VideoCatalog extends CatalogSection {
  const VideoCatalog._();

  @override
  String get path => 'video';

  @override
  String get title => 'Video Example';

  @override
  String get subtitle => '视频组件';

  @override
  List<CatalogEntry> get items => _items;

  static final List<CatalogEntry> _items = [
    CatalogEntry.page(
      path: 'video-player',
      title: 'VideoPlayer',
      subtitle: 'video_player 示例',
      pageBuilder: (BuildContext context) =>
          const VideoPlayerDemoPage(title: 'VideoPlayer'),
    ),
    CatalogEntry.page(
      path: 'chewie-video-player',
      title: 'ChewieVideoPlayer',
      subtitle: 'chewie + video_player 示例',
      pageBuilder: (BuildContext context) =>
          const ChewieVideoPlayerDemoPage(title: 'ChewieVideoPlayer'),
    ),
  ];
}

/// 单例实例
const VideoCatalog videoCatalog = VideoCatalog._();
