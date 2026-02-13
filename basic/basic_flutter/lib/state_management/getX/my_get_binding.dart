import 'package:basic_flutter/state_management/getX/my_get_logic.dart';
import 'package:get/get.dart';

// 用于懒加载对应的Controller
class MyGetXBinding extends Bindings {
  @override
  void dependencies() {
    //
    Get.lazyPut(() => MyGetXLogic());
  }
}
