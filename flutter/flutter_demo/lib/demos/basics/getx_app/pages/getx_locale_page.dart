import 'package:flutter/material.dart';
import 'package:flutter_demo/l10n/app_strings.dart';
import 'package:get/get.dart';

class GetXLocalePage extends StatelessWidget {
  const GetXLocalePage({super.key, required this.title});

  final String title;

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
      appBar: AppBar(title: Text(title)),
      body: Center(
        child: ElevatedButton(
          onPressed: _toggleLocale,
          child: Text(AppStrings.hello.tr),
        ),
      ),
    );
  }
}
