import 'package:flutter/material.dart';
import 'package:pag_flutter/pag_flutter.dart';

/// PAG animation
/// https://pub.dev/packages/pag_flutter
class PagDemoPage extends StatelessWidget {
  const PagDemoPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return PagDemoView(title: title);
  }
}

class PagDemoView extends StatefulWidget {
  const PagDemoView({super.key, required this.title});

  final String title;

  @override
  State<PagDemoView> createState() => _PagDemoViewState();
}

class _PagDemoViewState extends State<PagDemoView> {
  static const String _sampleAsset = 'assets/anim/pag/diamond.pag';

  late final PAGController _controller;

  @override
  void initState() {
    super.initState();
    _controller = PAGController()..play();
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
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
              'Local PAG sample',
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
    return DecoratedBox(
      decoration: BoxDecoration(
        color: Theme.of(context).colorScheme.surfaceContainerHighest,
        borderRadius: BorderRadius.circular(16),
      ),
      child: Center(
        child: SizedBox(
          width: 280,
          height: 280,
          child: PAGView.asset(
            _sampleAsset,
            controller: _controller,
            repeatCount: 0,
            scaleMode: PAGScaleMode.letterBox,
          ),
        ),
      ),
    );
  }
}
