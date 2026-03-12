import 'package:basic_flutter/features/examples/getx/pages/home_page.dart';
import 'package:basic_flutter/l10n/app_translations.dart';
import 'package:basic_flutter/features/examples/getx/navigation/app_routes.dart';
import 'package:flutter/material.dart';
import 'package:get/get.dart';

/// GetX
/// https://pub.dev/packages/get
class GetXApp extends StatelessWidget {
  const GetXApp({super.key});

  @override
  Widget build(BuildContext context) {
    // 使用 GetMaterialApp 包裹，使 GetX 功能正常工作
    return GetMaterialApp(
      debugShowCheckedModeBanner: false,
      translations: AppTranslations(),
      locale: const Locale('zh', 'CN'),
      fallbackLocale: const Locale('en', 'US'),
      home: const HomePage(),
      getPages: AppRoutes.getPages(),
      unknownRoute: AppRoutes.getUnknown(),
    );
  }
}
