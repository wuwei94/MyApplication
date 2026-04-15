import 'package:flutter/material.dart';
import 'package:lottie/lottie.dart';

/// Lottie animation
/// https://pub.dev/packages/lottie
class LottieExample extends StatelessWidget {
  const LottieExample({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return LottieRoute(title: title);
  }
}

class LottieRoute extends StatelessWidget {
  const LottieRoute({super.key, required this.title});

  static const String _sampleAsset = 'assets/anim/lottie/playing.json';

  final String title;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(title)),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
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
          child: Lottie.asset(_sampleAsset, fit: BoxFit.contain),
        ),
      ),
    );
  }
}
