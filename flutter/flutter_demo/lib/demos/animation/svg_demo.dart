import 'package:flutter/material.dart';
import 'package:flutter_demo/core/basic/basic.dart';
import 'package:flutter_svg/flutter_svg.dart';

/// SVG image
/// https://pub.dev/packages/flutter_svg
class SvgDemoPage extends BasicLayoutPage {
  const SvgDemoPage({super.key, required super.title});

  @override
  BasicLayoutPageState<SvgDemoPage> createState() => _SvgDemoPageState();
}

class _SvgDemoPageState extends BasicLayoutPageState<SvgDemoPage> {
  static const String _sampleAsset = 'assets/anim/svg/playing.svg';

  @override
  Widget buildPreview() {
    return Padding(
      padding: const EdgeInsets.all(BasicDemoDimens.pagePadding),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: <Widget>[
          Text(
            'Local SVG sample',
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
          child: SvgPicture.asset(_sampleAsset, fit: BoxFit.contain),
        ),
      ),
    );
  }
}
