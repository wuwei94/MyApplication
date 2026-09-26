import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';
import 'package:flutter_svga/flutter_svga.dart';

/// SVGA animation
/// https://pub.dev/packages/flutter_svga
class SvgaDemoPage extends BasicImagePage {
  const SvgaDemoPage({super.key, required super.title});

  @override
  BasicImagePageState<SvgaDemoPage> createState() => _SvgaDemoPageState();
}

class _SvgaDemoPageState extends BasicImagePageState<SvgaDemoPage>
    with SingleTickerProviderStateMixin {
  static const String _sampleAsset = 'assets/anim/svga/diamond.svga';

  late final SVGAAnimationController _controller;
  Object? _loadError;
  bool _isLoading = true;

  @override
  List<String> buildList() => const <String>[
        '1. 循环播放',
        '2. 暂停动画',
        '3. 重新加载资源',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        unawaited(_controller.repeat());
      case 1:
        _controller.stop();
      case 2:
        _loadAnimation();
    }
  }

  @override
  void initState() {
    super.initState();
    _controller = SVGAAnimationController(vsync: this);
    _loadAnimation();
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  Future<void> _loadAnimation() async {
    setState(() {
      _isLoading = true;
      _loadError = null;
    });

    try {
      final MovieEntity videoItem = await SVGAParser.shared.decodeFromAssets(
        _sampleAsset,
      );

      if (!mounted) {
        return;
      }

      _controller.videoItem = videoItem;
      unawaited(_controller.repeat());

      setState(() {
        _isLoading = false;
      });
    } catch (error) {
      if (!mounted) {
        return;
      }

      setState(() {
        _loadError = error;
        _isLoading = false;
      });
    }
  }

  @override
  Widget buildPreview() {
    return Padding(
      padding: const EdgeInsets.all(BasicDemoDimens.pagePadding),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: <Widget>[
          Text(
            'Local SVGA sample',
            style: Theme.of(context).textTheme.titleMedium,
          ),
          const SizedBox(height: 8),
          SelectableText(
            _sampleAsset,
            style: Theme.of(context).textTheme.bodySmall,
          ),
          const SizedBox(height: 16),
          Expanded(child: _buildCanvas(context)),
        ],
      ),
    );
  }

  Widget _buildCanvas(BuildContext context) {
    if (_isLoading) {
      return const Center(child: CircularProgressIndicator());
    }

    if (_loadError != null) {
      return Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: <Widget>[
            const Icon(Icons.error_outline, size: 40),
            const SizedBox(height: 12),
            const Text('Failed to load SVGA animation'),
            const SizedBox(height: 8),
            Text(
              '$_loadError',
              textAlign: TextAlign.center,
              style: Theme.of(context).textTheme.bodySmall,
            ),
          ],
        ),
      );
    }

    return DecoratedBox(
      decoration: BoxDecoration(
        color: Theme.of(context).colorScheme.surfaceContainerHighest,
        borderRadius: BorderRadius.circular(BasicDemoDimens.cornerCard),
      ),
      child: Center(
        child: SVGAImage(
          _controller,
          fit: BoxFit.contain,
          clearsAfterStop: false,
          preferredSize: const Size(280, 280),
        ),
      ),
    );
  }
}
