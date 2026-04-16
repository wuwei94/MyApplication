import 'package:basic_flutter/core/utils/logger/logger.dart';
import 'package:get/get.dart';

// 编写界面业务逻辑代码，包含生命周期回调函数
class GetXWorkerController extends GetxController {
  final count1 = 0.obs;
  final count2 = 0.obs;

  /// Once the controller has entered memory, onInit will be called.
  /// It is preferable to use onInit instead of class constructors or initState method.
  /// Use onInit to trigger initial events like API searches, listeners registration
  /// or Workers registration.
  /// Workers are event handlers, they do not modify the final result,
  /// but it allows you to listen to an event and trigger customized actions.
  /// Here is an outline of how you can use them:

  /// made this if you need cancel you worker
  late Worker _ever;
  late Worker _everAll;

  @override
  void onInit() {
    super.onInit();

    // 监听一次变化
    once(count1, (value) => {logDebug("$value was changed once (once)")});

    // 监听每次变化
    _ever = ever(
      count1,
      (value) => {logDebug("$value has been changed (ever)")},
    );

    // 监听多个变量
    _everAll = everAll([
      count1,
      count2,
    ], (value) => {logDebug("$value has been changed (everAll)")});

    // 防抖
    debounce(
      count1,
      (value) => {logDebug("debouce$value (debounce)")},
      time: const Duration(seconds: 3),
    );

    // 节流
    interval(
      count2,
      (value) => {logDebug("interval $value (interval)")},
      time: const Duration(seconds: 3),
    );
  }

  int get sum => count1.value + count2.value;

  int increment1() => count1.value++;

  int increment2() => count2.value++;

  void disposeWorker() {
    _ever();
    _everAll();
    _ever.dispose();
    _everAll.dispose();
  }
}
