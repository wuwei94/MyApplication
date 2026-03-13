import 'package:basic_flutter/l10n/app_strings.dart';
import 'package:flutter/material.dart';
import 'package:get/get.dart';

class LocalePage extends StatelessWidget {
  const LocalePage({super.key});

  void _toggleLocale() {
    if (Get.locale?.languageCode == 'zh') {
      Get.updateLocale(const Locale('en', 'US'));
    } else {
      Get.updateLocale(const Locale('zh', 'CN'));
    }
  }

  @override
  Widget build(context) {
    return Scaffold(
      appBar: AppBar(title: const Text("Locale Page")),
      body: Center(
        child: ElevatedButton(
          onPressed: _toggleLocale,
          child: Text(AppStrings.hello.tr),
        ),
      ),
    );
  }
}
