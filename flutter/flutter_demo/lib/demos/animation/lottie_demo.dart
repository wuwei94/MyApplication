import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';
import 'package:lottie/lottie.dart';

/// Lottie animation
/// https://pub.dev/packages/lottie
class LottieDemoPage extends BasicLayoutPage {
  const LottieDemoPage({super.key, required super.title});

  @override
  BasicLayoutPageState<LottieDemoPage> createState() => _LottieDemoPageState();
}

class _LottieDemoPageState extends BasicLayoutPageState<LottieDemoPage> {
  static const String _sampleAsset = 'assets/anim/lottie/playing.json';

  @override
  Widget buildPreview() {
    return Padding(
      padding: const EdgeInsets.all(BasicDemoDimens.pagePadding),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: <Widget>[
          Text(
            'Local Lottie sample',
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
          child: Lottie.asset(_sampleAsset, fit: BoxFit.contain),
        ),
      ),
    );
  }
}
