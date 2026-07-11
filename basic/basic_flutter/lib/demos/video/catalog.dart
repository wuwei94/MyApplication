import 'package:basic_flutter/catalog/models/catalog_entry.dart';
import 'package:basic_flutter/catalog/models/catalog_section.dart';
import 'package:basic_flutter/demos/video/chewie_video_player_example.dart';
import 'package:basic_flutter/demos/video/video_player_example.dart';
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
  String get subtitle => '本地/网络播放与控制层对比';

  @override
  List<CatalogEntry> get items => _items;

  static final List<CatalogEntry> _items = [
    CatalogEntry.page(
      path: 'video-player',
      title: 'VideoPlayer',
      subtitle: '纯播放器实现的本地与网络视频播放',
      pageBuilder: (BuildContext context) =>
          const VideoPlayerDemoPage(title: 'VideoPlayer'),
    ),
    CatalogEntry.page(
      path: 'chewie-video-player',
      title: 'ChewieVideoPlayer',
      subtitle: '带控制条、全屏与倍速的视频播放体验',
      pageBuilder: (BuildContext context) =>
          const ChewieVideoPlayerDemoPage(title: 'ChewieVideoPlayer'),
    ),
  ];
}

/// 单例实例
const VideoCatalog videoCatalog = VideoCatalog._();
