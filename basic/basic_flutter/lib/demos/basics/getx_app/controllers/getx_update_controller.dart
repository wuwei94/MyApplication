import 'package:basic_flutter/demos/basics/getx_app/models/getx_user_model.dart';
import 'package:get/get.dart';

class GetXUpdateController extends GetxController {
  final user = GetXUserModel().obs;

  void updateUser() {
    user.update((value) {
      value!.name = 'Jose';
      value.age = 30;
    });
  }
}
