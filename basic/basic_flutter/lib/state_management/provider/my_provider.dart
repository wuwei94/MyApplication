import 'package:basic_flutter/state_management/provider/page/my_provider_page.dart';
import 'package:flutter/material.dart';

/// Provider
/// https://pub.dev/packages/provider
class MyProvider extends StatelessWidget {
  const MyProvider({super.key});

  @override
  Widget build(BuildContext context) {
    return const MyProviderPage(title: 'Flutter Provider demo');
  }
}
