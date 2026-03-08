import 'package:flutter/material.dart';

/// Simplest possible model, with just one field.
///
/// [ChangeNotifier] is a class in `flutter:foundation`. [MyProviderChangeNotifier] does
/// _not_ depend on Provider.
class MyProviderChangeNotifier with ChangeNotifier {
  int value = 0;

  void increment() {
    value += 1;
    notifyListeners();
  }

  void decrement() {
    value -= 1;
    notifyListeners();
  }

  void reset() {
    value = 0;
    notifyListeners();
  }
}
