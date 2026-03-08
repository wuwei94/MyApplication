import 'package:basic_flutter/features/state_management/provider/notifier/my_provider_notifier.dart';
import 'package:basic_flutter/features/state_management/provider/page/my_provider_page.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

/// Provider
/// https://pub.dev/packages/provider
class MyProvider extends StatelessWidget {
  const MyProvider({super.key});

  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider(
      // 在生成器中初始化模型。
      // 这样，提供者就可以拥有 Counter 的生命周期，
      // 确保在不再需要时调用 `dispose`。
      // Initialize the model in the builder.
      // That way, Provider can own Counter's lifecycle,
      // making sure to call `dispose` when not needed anymore.
      create: (_) => MyProviderNotifier(),
      child: const MyProviderPage(title: 'Provider Example'),
    );
  }
}
