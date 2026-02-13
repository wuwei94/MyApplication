import 'package:basic_flutter/state_management/getX/res/strings/str_res_keys_en.dart';
import 'package:basic_flutter/state_management/getX/res/strings/str_res_keys_zh.dart';
import 'package:get/get.dart';

class MyTranslations extends Translations {
  @override
  Map<String, Map<String, String>> get keys => {
    'zh_CN': zhCnRes,
    'en_US': enUsRes,
  };
}
