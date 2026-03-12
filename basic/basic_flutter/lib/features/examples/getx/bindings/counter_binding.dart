import 'package:basic_flutter/features/examples/getx/controllers/worker_controller.dart';
import 'package:get/get.dart';

// 用于懒加载对应的Controller
class MyGetXBinding extends Bindings {
  @override
  void dependencies() {
    //
    Get.lazyPut(() => MyGetXLogic());
  }
}
