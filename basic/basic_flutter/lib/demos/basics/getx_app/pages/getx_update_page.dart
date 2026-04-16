import 'package:basic_flutter/demos/basics/getx_app/controllers/getx_update_controller.dart';
import 'package:flutter/material.dart';
import 'package:get/get.dart';

class GetXUpdatePage extends StatelessWidget {
  const GetXUpdatePage({super.key, required this.title});

  final String title;

  @override
  Widget build(context) {
    // 使用Get.put()实例化你的类，使其对当下的所有子路由可用。
    final GetXUpdateController controller = Get.put(GetXUpdateController());

    return Scaffold(
      appBar: AppBar(title: Text(title)),

      // 用一个简单的Get.to()即可代替Navigator.push那8行，无需上下文！
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Obx(
              () => Text(
                "User: ${controller.user.value.name}, Age: ${controller.user.value.age}",
              ),
            ),
            const SizedBox(height: 20),
            ElevatedButton(
              onPressed: () => Get.to<void>(() => UpdateValuePage()),
              child: const Text("Go to Other"),
            ),
          ],
        ),
      ),

      floatingActionButton: FloatingActionButton(
        onPressed: () {
          controller.updateUser();
        },
        child: const Icon(Icons.add),
      ),
    );
  }
}

class UpdateValuePage extends StatelessWidget {
  UpdateValuePage({super.key});

  // 你可以让Get找到一个正在被其他页面使用的Controller，并将它返回给你。
  final GetXUpdateController controller = Get.find();

  @override
  Widget build(context) {
    // 访问更新后的用户信息
    return Scaffold(
      appBar: AppBar(title: const Text('Other Page')),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Obx(
              () => Text(
                "User: ${controller.user.value.name}, Age: ${controller.user.value.age}",
              ),
            ),
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
