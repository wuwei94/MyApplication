import 'package:chewie/chewie.dart';
import 'package:flutter/material.dart';
import 'package:video_player/video_player.dart';

enum _VideoSourceType { asset, network }

/// Chewie + Video Player
/// https://pub.dev/packages/chewie
/// https://pub.dev/packages/video_player
class ChewieVideoPlayerExample extends StatelessWidget {
  const ChewieVideoPlayerExample({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return ChewieVideoPlayerRoute(title: title);
  }
}

class ChewieVideoPlayerRoute extends StatefulWidget {
  const ChewieVideoPlayerRoute({super.key, required this.title});

  final String title;

  @override
  State<ChewieVideoPlayerRoute> createState() => _ChewieVideoPlayerRouteState();
}

class _ChewieVideoPlayerRouteState extends State<ChewieVideoPlayerRoute> {
  static const String _assetVideoPath = 'assets/video/sample.mp4';
  static const String _networkVideoUrl =
      'https://samplelib.com/lib/preview/mp4/sample-5s.mp4';

  _VideoSourceType _selectedSource = _VideoSourceType.asset;
  VideoPlayerController? _videoController;
  ChewieController? _chewieController;
  bool _isLoading = true;
  String _statusMessage = '正在准备 chewie + video_player 示例...';

  @override
  void initState() {
    super.initState();
    _loadVideo(_selectedSource);
  }

  @override
  void dispose() {
    _disposeControllers();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);
    final bool isReady = _chewieController != null && !_isLoading;

    return Scaffold(
      appBar: AppBar(title: Text(widget.title)),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: <Widget>[
          _VideoSummaryCard(
            title: 'chewie + video_player',
            description: _statusMessage,
            accentColor: const Color(0xFF4B57D1),
            sourceLabel: _sourceLabel(_selectedSource),
          ),
          const SizedBox(height: 16),
          _VideoSourceSelector(
            selectedSource: _selectedSource,
            isLoading: _isLoading,
            onSourceSelected: _handleSourceChanged,
          ),
          const SizedBox(height: 20),
          Text(
            '播放区域',
            style: theme.textTheme.titleMedium?.copyWith(
              fontWeight: FontWeight.w700,
            ),
          ),
          const SizedBox(height: 8),
          Text(
            '这个页面使用 chewie 包装 video_player，直接提供默认控制条、全屏和倍速。',
            style: theme.textTheme.bodySmall?.copyWith(
              color: theme.colorScheme.onSurfaceVariant,
            ),
          ),
          const SizedBox(height: 16),
          _VideoPreviewCard(
            isLoading: _isLoading,
            isReady: isReady,
            chewieController: _chewieController,
            statusMessage: _statusMessage,
          ),
          const SizedBox(height: 16),
          _VideoInfoCard(
            currentSource: _selectedSource,
            assetPath: _assetVideoPath,
            networkUrl: _networkVideoUrl,
          ),
        ],
      ),
    );
  }

  Future<void> _handleSourceChanged(_VideoSourceType source) async {
    if (_selectedSource == source || _isLoading) {
      return;
    }

    setState(() {
      _selectedSource = source;
    });

    await _loadVideo(source);
  }

  Future<void> _loadVideo(_VideoSourceType source) async {
    setState(() {
      _isLoading = true;
      _statusMessage = source == _VideoSourceType.asset
          ? '正在加载本地示例视频...'
          : '正在加载网络示例视频...';
    });

    await _disposeControllers();

    try {
      final VideoPlayerController controller = source == _VideoSourceType.asset
          ? VideoPlayerController.asset(_assetVideoPath)
          : VideoPlayerController.networkUrl(Uri.parse(_networkVideoUrl));

      await controller.initialize();

      final ChewieController chewieController = ChewieController(
        videoPlayerController: controller,
        autoPlay: true,
        looping: true,
        allowMuting: true,
        allowPlaybackSpeedChanging: true,
        materialProgressColors: ChewieProgressColors(
          playedColor: const Color(0xFF4B57D1),
          handleColor: const Color(0xFF4B57D1),
          backgroundColor: Colors.white24,
          bufferedColor: const Color(0xFF9BA3FF),
        ),
      );

      if (!mounted) {
        chewieController.dispose();
        await controller.dispose();
        return;
      }

      setState(() {
        _videoController = controller;
        _chewieController = chewieController;
        _isLoading = false;
        _statusMessage = source == _VideoSourceType.asset
            ? '本地示例视频已就绪，可以直接体验 chewie 控制条。'
            : '网络示例视频已就绪，可以体验全屏和倍速。';
      });
    } catch (error) {
      if (!mounted) {
        return;
      }

      setState(() {
        _videoController = null;
        _chewieController = null;
        _isLoading = false;
        _statusMessage = source == _VideoSourceType.asset
            ? '本地视频加载失败，请确认 assets/video/sample.mp4 已正确打包。\n$error'
            : '网络视频加载失败，请检查当前网络状态。\n$error';
      });
    }
  }

  Future<void> _disposeControllers() async {
    final ChewieController? chewieController = _chewieController;
    final VideoPlayerController? videoController = _videoController;

    _chewieController = null;
    _videoController = null;

    chewieController?.dispose();
    await videoController?.dispose();
  }

  String _sourceLabel(_VideoSourceType source) {
    switch (source) {
      case _VideoSourceType.asset:
        return 'Asset 本地';
      case _VideoSourceType.network:
        return 'Network 网络';
    }
  }
}

class _VideoSummaryCard extends StatelessWidget {
  const _VideoSummaryCard({
    required this.title,
    required this.description,
    required this.accentColor,
    required this.sourceLabel,
  });

  final String title;
  final String description;
  final Color accentColor;
  final String sourceLabel;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return Container(
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(24),
        gradient: const LinearGradient(
          colors: <Color>[Color(0xFF222B6A), Color(0xFF4B57D1)],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
        boxShadow: const <BoxShadow>[
          BoxShadow(
            color: Color(0x2A1E266B),
            blurRadius: 24,
            offset: Offset(0, 12),
          ),
        ],
      ),
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Container(
              width: 44,
              height: 44,
              decoration: BoxDecoration(
                color: Colors.white.withValues(alpha: 0.14),
                borderRadius: BorderRadius.circular(14),
              ),
              child: const Icon(
                Icons.ondemand_video_rounded,
                color: Colors.white,
              ),
            ),
            const SizedBox(height: 16),
            Text(
              title,
              style: theme.textTheme.titleLarge?.copyWith(
                color: Colors.white,
                fontWeight: FontWeight.w700,
              ),
            ),
            const SizedBox(height: 8),
            Text(
              description,
              style: theme.textTheme.bodyMedium?.copyWith(
                color: Colors.white.withValues(alpha: 0.88),
              ),
            ),
            const SizedBox(height: 14),
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
              decoration: BoxDecoration(
                color: accentColor.withValues(alpha: 0.22),
                borderRadius: BorderRadius.circular(999),
              ),
              child: Text(
                '当前来源：$sourceLabel',
                style: theme.textTheme.labelLarge?.copyWith(
                  color: Colors.white,
                  fontWeight: FontWeight.w600,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _VideoSourceSelector extends StatelessWidget {
  const _VideoSourceSelector({
    required this.selectedSource,
    required this.isLoading,
    required this.onSourceSelected,
  });

  final _VideoSourceType selectedSource;
  final bool isLoading;
  final ValueChanged<_VideoSourceType> onSourceSelected;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: <Widget>[
        Text(
          '视频来源',
          style: Theme.of(
            context,
          ).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w700),
        ),
        const SizedBox(height: 12),
        SegmentedButton<_VideoSourceType>(
          segments: const <ButtonSegment<_VideoSourceType>>[
            ButtonSegment<_VideoSourceType>(
              value: _VideoSourceType.asset,
              label: Text('Asset'),
              icon: Icon(Icons.folder_outlined),
            ),
            ButtonSegment<_VideoSourceType>(
              value: _VideoSourceType.network,
              label: Text('Network'),
              icon: Icon(Icons.language_outlined),
            ),
          ],
          selected: <_VideoSourceType>{selectedSource},
          onSelectionChanged: isLoading
              ? null
              : (Set<_VideoSourceType> selection) {
                  onSourceSelected(selection.first);
                },
        ),
      ],
    );
  }
}

class _VideoPreviewCard extends StatelessWidget {
  const _VideoPreviewCard({
    required this.isLoading,
    required this.isReady,
    required this.chewieController,
    required this.statusMessage,
  });

  final bool isLoading;
  final bool isReady;
  final ChewieController? chewieController;
  final String statusMessage;

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        color: const Color(0xFF0E122B),
        borderRadius: BorderRadius.circular(24),
      ),
      clipBehavior: Clip.antiAlias,
      child: AspectRatio(
        aspectRatio:
            chewieController?.videoPlayerController.value.aspectRatio ??
            (16 / 9),
        child: ColoredBox(
          color: const Color(0xFF0E122B),
          child: Builder(
            builder: (BuildContext context) {
              if (isLoading) {
                return const Center(
                  child: CircularProgressIndicator(color: Colors.white),
                );
              }

              if (!isReady || chewieController == null) {
                return _VideoErrorState(message: statusMessage);
              }

              return Chewie(controller: chewieController!);
            },
          ),
        ),
      ),
    );
  }
}

class _VideoErrorState extends StatelessWidget {
  const _VideoErrorState({required this.message});

  final String message;

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: <Widget>[
            const Icon(
              Icons.error_outline_rounded,
              color: Colors.white70,
              size: 36,
            ),
            const SizedBox(height: 12),
            Text(
              message,
              textAlign: TextAlign.center,
              style: Theme.of(
                context,
              ).textTheme.bodyMedium?.copyWith(color: Colors.white70),
            ),
          ],
        ),
      ),
    );
  }
}

class _VideoInfoCard extends StatelessWidget {
  const _VideoInfoCard({
    required this.currentSource,
    required this.assetPath,
    required this.networkUrl,
  });

  final _VideoSourceType currentSource;
  final String assetPath;
  final String networkUrl;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return Container(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: const Color(0xFFDCE1FF)),
        boxShadow: const <BoxShadow>[
          BoxShadow(
            color: Color(0x120A2533),
            blurRadius: 18,
            offset: Offset(0, 8),
          ),
        ],
      ),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Text(
              '当前配置',
              style: theme.textTheme.titleMedium?.copyWith(
                fontWeight: FontWeight.w700,
              ),
            ),
            const SizedBox(height: 12),
            _InfoRow(label: '已选来源', value: _labelOf(currentSource)),
            _InfoRow(label: 'Asset 路径', value: assetPath),
            _InfoRow(label: 'Network 地址', value: networkUrl),
          ],
        ),
      ),
    );
  }

  String _labelOf(_VideoSourceType source) {
    switch (source) {
      case _VideoSourceType.asset:
        return 'Asset 本地';
      case _VideoSourceType.network:
        return 'Network 网络';
    }
  }
}

class _InfoRow extends StatelessWidget {
  const _InfoRow({required this.label, required this.value});

  final String label;
  final String value;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);

    return Padding(
      padding: const EdgeInsets.only(bottom: 10),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          SizedBox(
            width: 96,
            child: Text(
              label,
              style: theme.textTheme.bodyMedium?.copyWith(
                color: theme.colorScheme.onSurfaceVariant,
              ),
            ),
          ),
          Expanded(
            child: Text(
              value,
              style: theme.textTheme.bodyMedium?.copyWith(
                fontWeight: FontWeight.w600,
              ),
            ),
          ),
        ],
      ),
    );
  }
}
