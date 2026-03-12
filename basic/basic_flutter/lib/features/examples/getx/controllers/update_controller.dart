import 'package:basic_flutter/features/examples/getx/models/user_model.dart';
import 'package:get/get.dart';

class UpdateController extends GetxController {
  final user = User().obs;

  void updateUser() {
    user.update((value) {
      value!.name = 'Jose';
      value.age = 30;
    });
  }
}
