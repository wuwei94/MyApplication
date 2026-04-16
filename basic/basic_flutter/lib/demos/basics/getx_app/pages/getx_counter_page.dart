import 'package:basic_flutter/demos/basics/getx_app/controllers/getx_counter_controller.dart';
import 'package:flutter/material.dart';
import 'package:get/get.dart';

class GetXCounterPage extends StatelessWidget {
  const GetXCounterPage({super.key, required this.title});

  final String title;

  @override
  Widget build(context) {
    // 使用Get.put()实例化你的类，使其对当下的所有子路由可用。
    final GetXCountController controller = Get.put(GetXCountController());

    return Scaffold(
      appBar: AppBar(title: Text(title)),
      // 用一个简单的Get.to()即可代替Navigator.push那8行，无需上下文！
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Obx(() => Text("Count: ${controller.count}")),
            const SizedBox(height: 20),
            ElevatedButton(
              onPressed: () => Get.to<void>(() => CounterValuePage()),
              child: const Text("Go to Other"),
            ),
          ],
        ),
      ),

      floatingActionButton: FloatingActionButton(
        onPressed: () {
          controller.increment();
        },
        child: const Icon(Icons.add),
      ),
    );
  }
}

class CounterValuePage extends StatelessWidget {
  CounterValuePage({super.key});

  // 你可以让Get找到一个正在被其他页面使用的Controller，并将它返回给你。
  final GetXCountController controller = Get.find();

  @override
  Widget build(context) {
    // 访问更新后的计数变量
    return Scaffold(
      appBar: AppBar(title: const Text('Other Page')),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Obx(() => Text("Count: ${controller.count}")),
            const SizedBox(height: 20),
            ElevatedButton(
              onPressed: () => Get.back<void>(),
              child: const Text('Back to Home'),
            ),
          ],
        ),
      ),
    );
  }
}
