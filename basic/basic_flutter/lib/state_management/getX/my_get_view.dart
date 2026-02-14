import 'package:basic_flutter/core/utils/log.dart';
import 'package:basic_flutter/state_management/getX/my_get_logic.dart';
import 'package:basic_flutter/state_management/getX/res/strings/str_res_keys.dart';
import 'package:basic_flutter/state_management/getX/routes/my_route.dart';
import 'package:flutter/material.dart';
import 'package:get/get.dart';

class FirstPage extends StatelessWidget {
  FirstPage({super.key});

  final MyGetXLogic controller = Get.put(MyGetXLogic());

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Flutter GetX demo')),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            GetX<MyGetXLogic>(
              builder: (_) => Text('clicks: ${controller.count}'),
            ),
            ElevatedButton(
              child: const Text('Next Route'),
              onPressed: () {
                Get.toNamed<void>(Routes.app.second);
              },
            ),
            ElevatedButton(
              child: const Text('Show SnackBar'),
              onPressed: () {
                Get.snackbar("Hi", "I'm modern snackBar");
              },
            ),
            ElevatedButton(
              child: const Text('Show Dialog'),
              onPressed: () {
                Get.defaultDialog<void>(
                  title: "title",
                  middleText: "this is dialog message",
                );
              },
            ),
            ElevatedButton(
              child: const Text('Show BottomSheet'),
              onPressed: () {
                Get.bottomSheet<void>(
                  Container(
                    height: 200,
                    color: Colors.white,
                    child: const Center(child: Text("bottomSheet")),
                  ),
                );
              },
            ),
            ElevatedButton(
              child: Text(SR.hello.tr),
              onPressed: () {
                Get.updateLocale(const Locale('en', 'US'));
              },
            ),
          ],
        ),
      ),
      floatingActionButton: FloatingActionButton(
        child: const Icon(Icons.add),
        onPressed: () {
          controller.increment();
        },
      ),
    );
  }
}

class SecondPage extends GetView<MyGetXLogic> {
  const SecondPage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Second Route')),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Obx(() {
              logDebug("count1 rebuild");
              return Text('count1 : ${controller.count1}');
            }),
            Obx(() {
              logDebug("count2 rebuild");
              return Text('count2 : ${controller.count2}');
            }),
            Obx(() {
              logDebug("sum rebuild");
              return Text('The sum of count1 and count1 : ${controller.sum}');
            }),
            Obx(
              () => Text(
                'Name: ${controller.user.value.name},Age: ${controller.user.value.age}',
              ),
            ),
            ElevatedButton(
              child: const Text("Increment"),
              onPressed: () {
                controller.increment1();
              },
            ),
            ElevatedButton(
              child: const Text("Increment"),
              onPressed: () {
                controller.increment2();
              },
            ),
            ElevatedButton(
              child: const Text("Update name"),
              onPressed: () {
                controller.updateUser();
              },
            ),
            ElevatedButton(
              child: const Text("Dispose worker"),
              onPressed: () {
                controller.disposeWorker();
              },
            ),
            ElevatedButton(
              child: const Text("Go to third page"),
              onPressed: () {
                Get.toNamed<void>(
                  Routes.app.third,
                  arguments: 'arguments of second',
                );
              },
            ),
            ElevatedButton(
              child: const Text("Back page"),
              onPressed: () {
                Get.back<void>();
              },
            ),
          ],
        ),
      ),
    );
  }
}

class ThirdPage extends GetView<MyGetXLogic> {
  const ThirdPage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text("Third Route ${Get.arguments}")),
      body: Center(
        child: Obx(
          () => ListView.builder(
            itemCount: controller.list.length,
            itemBuilder: (context, index) {
              return Text("${controller.list[index]}");
            },
          ),
        ),
      ),
      floatingActionButton: FloatingActionButton(
        child: const Icon(Icons.add),
        onPressed: () {
          controller.incrementList();
        },
      ),
    );
  }
}
