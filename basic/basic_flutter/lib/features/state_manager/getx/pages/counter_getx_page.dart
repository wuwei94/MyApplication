import 'package:basic_flutter/features/state_manager/getx/controllers/counter_getx_controller.dart';
import 'package:flutter/material.dart';
import 'package:get/get.dart';

class CounterGetxPage extends StatelessWidget {
  const CounterGetxPage({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    // 在 build 方法中使用 Get.put，确保控制器与页面生命周期绑定
    // 使用 tag 避免与其他页面冲突
    final CounterGetxController controller = Get.put(
      CounterGetxController(),
      tag: 'counter_$title',
    );

    return Scaffold(
      appBar: AppBar(title: Text(title)),
      body: _buildBody(controller),
      floatingActionButton: _buildFAB(controller),
    );
  }

  Widget _buildBody(CounterGetxController controller) {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          const Text('You have pushed the button this many times:'),
          Obx(() {
            return Text('${controller.count}');
          }),
        ],
      ),
    );
  }

  Widget _buildFAB(CounterGetxController controller) {
    return FloatingActionButton(
      tooltip: 'increment',
      onPressed: () => controller.increment(),
      child: const Icon(Icons.add),
    );
  }
}
