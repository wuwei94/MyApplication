import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';
import 'package:pag_flutter/pag_flutter.dart';

/// PAG animation
/// https://pub.dev/packages/pag_flutter
class PagDemoPage extends BasicImagePage {
  const PagDemoPage({super.key, required super.title});

  @override
  BasicImagePageState<PagDemoPage> createState() => _PagDemoPageState();
}

class _PagDemoPageState extends BasicImagePageState<PagDemoPage> {
  static const String _sampleAsset = 'assets/anim/pag/diamond.pag';

  late final PAGController _controller;

  @override
  List<String> buildList() => const <String>[
        '1. 播放动画',
        '2. 暂停动画',
      ];

  @override
  void onRecyclerClick(int position, String label) {
    switch (position) {
      case 0:
        _controller.play();
      case 1:
        _controller.pause();
    }
  }

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
  Widget buildPreview() {
    return Padding(
      padding: const EdgeInsets.all(BasicDemoDimens.pagePadding),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: <Widget>[
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
          Expanded(child: _buildCanvas(context)),
        ],
      ),
    );
  }

  Widget _buildCanvas(BuildContext context) {
    return DecoratedBox(
      decoration: BoxDecoration(
        color: Theme.of(context).colorScheme.surfaceContainerHighest,
        borderRadius: BorderRadius.circular(BasicDemoDimens.cornerCard),
      ),
      child: Center(
        child: SizedBox(
          width: 280,
          height: 280,
          child: PAGView.asset(
            _sampleAsset,
            controller: _controller,
          ),
        ),
      ),
    );
  }
}
