import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_svga/flutter_svga.dart';

/// SVGA animation
/// https://pub.dev/packages/flutter_svga
class SvgaExample extends StatelessWidget {
  const SvgaExample({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return SvgaRoute(title: title);
  }
}

class SvgaRoute extends StatefulWidget {
  const SvgaRoute({super.key, required this.title});

  final String title;

  @override
  State<SvgaRoute> createState() => _SvgaRouteState();
}

class _SvgaRouteState extends State<SvgaRoute>
    with SingleTickerProviderStateMixin {
  static const String _sampleAsset = 'assets/anim/svga/diamond.svga';

  late final SVGAAnimationController _controller;
  Object? _loadError;
  bool _isLoading = true;

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
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(widget.title)),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
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
            Expanded(child: _buildPreview(context)),
          ],
        ),
      ),
    );
  }

  Widget _buildPreview(BuildContext context) {
    if (_isLoading) {
      return const Center(child: CircularProgressIndicator());
    }

    if (_loadError != null) {
      return Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
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
        borderRadius: BorderRadius.circular(16),
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
