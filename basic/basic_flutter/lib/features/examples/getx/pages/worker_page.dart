import 'package:basic_flutter/features/examples/getx/controllers/worker_controller.dart';
import 'package:flutter/material.dart';
import 'package:get/get.dart';

class WorkerPage extends StatelessWidget {
  const WorkerPage({super.key});

  @override
  Widget build(context) {
    final WorkerController controller = Get.put(WorkerController());

    return Scaffold(
      appBar: AppBar(title: const Text("Workers Page")),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Obx(() => Text("Count1: ${controller.count1}")),
            const SizedBox(height: 10),
            Obx(() => Text("Count2: ${controller.count2}")),
            const SizedBox(height: 10),
            Obx(() => Text("Sum: ${controller.sum}")),
            const SizedBox(height: 20),
            ElevatedButton(
              onPressed: () => controller.increment1(),
              child: const Text("Increment Count1"),
            ),
            const SizedBox(height: 10),
            ElevatedButton(
              onPressed: () => controller.increment2(),
              child: const Text("Increment Count2"),
            ),
            const SizedBox(height: 20),
            ElevatedButton(
              onPressed: () => Get.to<void>(() => WorkersValuePage()),
              child: const Text("Go to Other"),
            ),
          ],
        ),
      ),
    );
  }
}

class WorkersValuePage extends StatelessWidget {
  WorkersValuePage({super.key});

  final WorkerController controller = Get.find();

  @override
  Widget build(context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Other Page')),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Obx(() => Text("Count1: ${controller.count1}")),
            const SizedBox(height: 10),
            Obx(() => Text("Count2: ${controller.count2}")),
            const SizedBox(height: 10),
            Obx(() => Text("Sum: ${controller.sum}")),
            const SizedBox(height: 20),
            ElevatedButton(
              onPressed: () => controller.increment1(),
              child: const Text("Increment Count1"),
            ),
            const SizedBox(height: 10),
            ElevatedButton(
              onPressed: () => controller.increment2(),
              child: const Text("Increment Count2"),
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
