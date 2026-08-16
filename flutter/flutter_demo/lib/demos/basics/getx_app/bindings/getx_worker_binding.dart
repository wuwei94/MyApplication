import 'package:flutter_demo/demos/basics/getx_app/controllers/getx_worker_controller.dart';
import 'package:get/get.dart';

// 用于懒加载对应的Controller
class GetXWorkerBinding extends Bindings {
  @override
  void dependencies() {
    //
    Get.lazyPut(() => GetXWorkerController());
  }
}
