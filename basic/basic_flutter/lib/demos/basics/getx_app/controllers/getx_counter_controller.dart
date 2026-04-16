import 'package:get/get.dart';

class GetXCountController extends GetxController {
  var count = 0.obs;

  dynamic increment() => count++;
}
