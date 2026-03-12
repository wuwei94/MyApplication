import 'package:basic_flutter/l10n/app_strings.dart';
import 'package:flutter/material.dart';
import 'package:get/get.dart';

class LocalePage extends StatelessWidget {
  const LocalePage({super.key});

  @override
  Widget build(context) {
    return Scaffold(
      appBar: AppBar(title: const Text("Locale Page")),
      body: Center(
        child: ElevatedButton(
          child: Text(AppStrings.hello.tr),
          onPressed: () {
            Get.updateLocale(const Locale('en', 'US'));
          },
        ),
      ),
    );
  }
}
