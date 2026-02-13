import 'package:basic_flutter/state_management/get/controller/my_get_controller.dart';
import 'package:flutter/material.dart';
import 'package:get/get.dart';

class MyGetXPage extends StatelessWidget {
  MyGetXPage({super.key, required this.title});

  final String title;

  final MyGetXController controller = Get.put(MyGetXController());

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(title)),
      body: getBody(),
      floatingActionButton: getFAB(),
    );
  }

  Widget getBody() {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          const Text('You have pushed the button this many times:'),
          GetX<MyGetXController>(
            builder: (_) {
              return Text('clicks: ${controller.count}');
            },
          ),
        ],
      ),
    );
  }

  Widget getFAB() {
    return FloatingActionButton(
      tooltip: 'increment',
      onPressed: () => controller.increment,
      child: const Icon(Icons.add),
    );
  }
}
