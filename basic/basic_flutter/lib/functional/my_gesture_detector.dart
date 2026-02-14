import 'package:basic_flutter/core/utils/log.dart';
import 'package:flutter/material.dart';

/// GestureDetector 手势检测
class MyGestureDetector extends StatelessWidget {
  const MyGestureDetector({super.key});

  @override
  Widget build(BuildContext context) {
    return const GestureDetectorRoute(title: 'Flutter GestureDetector demo');
  }
}

class GestureDetectorRoute extends StatelessWidget {
  const GestureDetectorRoute({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(title)),
      body: Center(child: getBody()),
    );
  }

  Widget getBody() {
    return GestureDetector(
      onTap: () {
        logDebug('onTap');
      },
      onDoubleTap: () {
        logDebug('onDoubleTap');
      },
      onLongPress: () {
        logDebug('onLongPress');
      },
      child: const FlutterLogo(size: 100),
    );
  }
}
